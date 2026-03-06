# Workflow: Abstract Frame Loop & Analyzer into JvmCameraCapture

## Problem

`CameraSession.jvm.kt` owns too much low-level logic that should live in `JvmCameraCapture`:
- Frame loop (read → convert → emit `ImageBitmap`)
- `currentFrame` / `isStreaming` / `frameLoopJob` state
- Image analyzer invocation per frame
- `currentMat` storage (lives in `JvmCameraEngine`)
- Camera open on selector change restart logic

This makes `CameraSession` fat and tightly coupled to frame management. In contrast, iOS keeps `CameraSession` thin — `IOSCameraController` owns the camera lifecycle internally.

## Target Architecture

```
CameraSession.jvm (thin)
  └─ delegates to CameraEngine
       └─ JvmCameraCapture (owns frame loop, streaming, mat storage)
            ├─ open / release / switchCamera
            ├─ startStreaming / stopStreaming
            ├─ currentFrame: StateFlow<ImageBitmap?>
            ├─ isStreaming: StateFlow<Boolean>
            ├─ currentMat: Mat?
            └─ onFrameAnalyzed callback
```

## Phases

### Phase 1: Extend `JvmCameraCapture` Interface

**File:** `internal/capture/JvmCameraCapture.kt`

Add to the interface:
- `fun startStreaming()` — starts the internal frame read loop
- `fun stopStreaming()` — cancels the frame loop
- `val currentFrame: StateFlow<ImageBitmap?>` — emits converted frames
- `val isStreaming: StateFlow<Boolean>` — streaming state
- `var currentMat: Mat?` — latest raw frame (used by take picture / record)
- `var onFrameAnalyzed: ((Mat) -> Unit)?` — callback for image analysis

Keep existing `open()`, `release()`, `read()`, `set()`, `get()` unchanged.

**Checkpoint:** Interface compiles, implementations show errors (expected).

---

### Phase 2: Implement in `JvmCameraCaptureImpl`

**File:** `internal/capture/JvmCameraCaptureImpl.kt`

Move frame loop logic from `CameraSession.jvm.kt`:
- Own a `CoroutineScope` (with a single-thread dispatcher for camera I/O)
- `startStreaming()` launches coroutine that:
  1. Reads frames via `capture.read(mat)`
  2. Clones to `currentMat`
  3. Converts to `ImageBitmap` via `mat.toImageBitmap()`
  4. Emits to `_currentFrame`
  5. Invokes `onFrameAnalyzed?.invoke(mat)` if set
- `stopStreaming()` cancels the frame job and awaits completion
- `release()` also calls `stopStreaming()` before releasing the native capture

**Checkpoint:** `JvmCameraCaptureImpl` compiles with new members.

---

### Phase 3: Simplify `CameraSession.jvm.kt`

**File:** `session/CameraSession.jvm.kt`

Remove:
- `_currentFrame` / `frameLoopJob` / `cameraDispatcher` / `sessionScope` fields
- `startFrameLoop()` method
- `observeFrameLoopRestart()` method
- Frame-related imports (`Mat`, `toImageBitmap`, `delay`, `isActive`, etc.)

Change:
- `currentFrame` → delegates to `capture.currentFrame`
- `isStreaming` → derives from `capture.isStreaming`
- `setupCamera()` → after `capture.open()`, call `capture.startStreaming()`
- `dispose()` → call `capture.stopStreaming()` + `capture.release()` (no more manual frame job cancel)
- Wire analyzer: set `capture.onFrameAnalyzed` based on `state.imageAnalyzer` / `state.isImageAnalyzerEnabled`

**Checkpoint:** `CameraSession` is thin — no frame loop, no coroutine scope for I/O.

---

### Phase 4: Update `JvmCameraEngine` / `CameraEngineImpl.jvm.kt`

**File:** `internal/core/JvmCameraEngine.kt`

- Remove `var currentMat: Mat?` from `JvmCameraEngine` interface (now lives in `JvmCameraCapture`)
- Update `CameraEngineImpl.jvm.kt` to read `currentMat` from `capture.currentMat` if needed (for `DefaultTakePictureCommand` / `DefaultRecordController`)
- Either keep `currentMat` as a delegating property (`get() = capture.currentMat`) or update consumers to access `capture` directly

**Checkpoint:** Engine compiles, take picture / record still access current frame.

---

### Phase 5: Update `SessionTopologyApplier.jvm.kt`

**File:** `internal/core/applier/SessionTopologyApplier.jvm.kt`

In `applyCamSelectorInternal()`:
- Call `capture.stopStreaming()` before `capture.release()`
- Call `capture.startStreaming()` after `capture.open()` succeeds
- This removes the need for `CameraSession` to observe `camSelector` state changes for frame loop restart

**Checkpoint:** Camera switching works end-to-end through the applier.

---

### Phase 6: Update `AnalyzerApplier.jvm.kt`

**File:** `internal/core/applier/AnalyzerApplier.jvm.kt`

- Wire `capture.onFrameAnalyzed` when analyzer is set/cleared
- When `isImageAnalyzerEnabled` changes, enable/disable the callback
- This moves analyzer responsibility out of the frame loop in `CameraSession`

**Checkpoint:** Image analysis works through applier → capture callback.

---

### Phase 7: Update Test Fakes

**Files:**
- `jvmTest/.../FakeJvmCameraCapture.kt` — implement new interface members (`startStreaming`, `stopStreaming`, `currentFrame`, `isStreaming`, `currentMat`, `onFrameAnalyzed`)
- `jvmTest/.../FakeCameraEngine.jvm.kt` — remove `currentMat` init (now in fake capture), update `updateImageAnalyzer` override
- `jvmTest/.../FakeCameraSession.jvm.kt` — verify no changes needed

**Checkpoint:** All JVM tests compile and pass.

---

### Phase 8: Verify & Format

```bash
./gradlew :camposer:compileKotlinJvm       # compilation
./gradlew :camposer:jvmTest                 # tests
./gradlew spotlessApply                      # formatting
./gradlew checkLegacyAbi                     # public API unchanged
```

## Dependency Graph

```
Phase 1 (interface)
  └─ Phase 2 (impl) ─┐
                      ├─ Phase 3 (session)
                      ├─ Phase 4 (engine)
                      ├─ Phase 5 (topology applier)
                      └─ Phase 6 (analyzer applier)
                            └─ Phase 7 (test fakes)
                                  └─ Phase 8 (verify)
```

Phases 3–6 are independent of each other (can be done in parallel after Phase 2).

## Risk Notes

- `currentFrame` is currently a `public val` on `CameraSession.jvm.kt` — moving it requires keeping the same public signature (delegates to capture internally). Run `checkLegacyAbi` to confirm.
- The single-thread dispatcher currently lives in `CameraSession`. Moving it into `JvmCameraCaptureImpl` means the capture owns its own threading — cleaner, but `dispose()` must close it.
- `FakeCameraEngine` pre-populates `currentMat` — this logic moves to `FakeJvmCameraCapture`.

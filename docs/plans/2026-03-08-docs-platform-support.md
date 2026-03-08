# Docs Platform Support Banners Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add a compact platform support badge to every doc page and update the overview to include Desktop.

**Architecture:** Two custom MkDocs Material admonition types (`platform` / `platform-limited`) defined via CSS; each doc page gets one badge line immediately after its `# Title`; `camera-format.md` also gets a new Runtime Configuration section.

**Tech Stack:** MkDocs Material, pymdownx admonition extension (already enabled), CSS custom properties + SVG mask-image.

---

## Verify docs locally

Before starting and after each task, run:
```bash
cd /Users/ujizin/StudioProjects/Camposer
pip install mkdocs-material   # if not installed
mkdocs serve
```
Open `http://127.0.0.1:8000` and navigate to the changed page.

---

### Task 1: Add custom admonition CSS

**Files:**
- Modify: `docs/assets/stylesheets/extra.css` (append at end of file)

**Step 1: Append the following block to the end of `extra.css`**

```css
/* ── Platform support admonitions ─────────────────────────────────────────── */

:root {
  --md-admonition-icon--platform: url('data:image/svg+xml;charset=utf-8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M21,7L9,19L3.5,13.5L4.91,12.09L9,16.17L19.59,5.59L21,7Z"/></svg>');
  --md-admonition-icon--platform-limited: url('data:image/svg+xml;charset=utf-8,<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><path d="M13,14H11V10H13M13,18H11V16H13M1,21H23L12,2L1,21Z"/></svg>');
}

/* platform — green checkmark (fully supported) */
.md-typeset .admonition.platform,
.md-typeset details.platform {
  border-color: #2e7d32;
}

.md-typeset .platform > .admonition-title,
.md-typeset .platform > summary {
  background-color: rgba(46, 125, 50, 0.1);
}

.md-typeset .platform > .admonition-title::before,
.md-typeset .platform > summary::before {
  background-color: #2e7d32;
  -webkit-mask-image: var(--md-admonition-icon--platform);
  mask-image: var(--md-admonition-icon--platform);
}

[data-md-color-scheme="slate"] .md-typeset .platform > .admonition-title,
[data-md-color-scheme="slate"] .md-typeset .platform > summary {
  background-color: rgba(46, 125, 50, 0.2);
}

/* platform-limited — amber warning triangle (partial/unsupported on Desktop) */
.md-typeset .admonition.platform-limited,
.md-typeset details.platform-limited {
  border-color: #e65100;
}

.md-typeset .platform-limited > .admonition-title,
.md-typeset .platform-limited > summary {
  background-color: rgba(230, 81, 0, 0.1);
}

.md-typeset .platform-limited > .admonition-title::before,
.md-typeset .platform-limited > summary::before {
  background-color: #e65100;
  -webkit-mask-image: var(--md-admonition-icon--platform-limited);
  mask-image: var(--md-admonition-icon--platform-limited);
}

[data-md-color-scheme="slate"] .md-typeset .platform-limited > .admonition-title,
[data-md-color-scheme="slate"] .md-typeset .platform-limited > summary {
  background-color: rgba(230, 81, 0, 0.2);
}
```

**Step 2: Verify locally**

Run `mkdocs serve`, open any doc page, and in the browser console verify the CSS is loaded. We can't see the badge yet since no markdown uses it — that's fine.

---

### Task 2: Update `docs/index.md` — Add Desktop to overview

**Files:**
- Modify: `docs/index.md`

**Step 1: Update hero description paragraph**

Change:
```html
    <p class="camposer-hero__description">
        Camposer is built with Jetpack Compose and gives you a modern camera stack for Android and iOS:
        photo capture, video recording, flash/torch controls, zoom, focus, stabilization, and analyzers.
    </p>
```
To:
```html
    <p class="camposer-hero__description">
        Camposer is built with Jetpack Compose and gives you a modern camera stack for Android, iOS and Desktop (JVM):
        photo capture, video recording, flash/torch controls, zoom, focus, stabilization, and analyzers.
    </p>
```

**Step 2: Update the Platform Support table**

Change:
```markdown
| Platform | Status |
|----------|--------|
|  **Android** | ✅ Supported
|  **iOS** | ✅ Supported
```
To:
```markdown
| Platform | Status |
|----------|--------|
| **Android** | ✅ Supported |
| **iOS** | ✅ Supported |
| **Desktop (JVM)** | ✅ Supported (partial — see per-feature docs) |
```

**Step 3: Update Features list**

Change the current features list to add missing items and Desktop notes:
```markdown
## Features

- [Camera Session](./camera-session.md)
- [Camera Selector](./camera-selector.md)
- [Capture Mode](./camera-capture-mode.md)
- [Camera Controller](./camera-controller/camera-controller.md)
    - [Take Picture](./camera-controller/take-picture.md)
    - [Record Video](./camera-controller/record-video.md) *(Android and iOS)*
- [Camera Format](./camera-format.md) *(Android and iOS)*
- [Mirror Mode](./mirror-mode.md)
- [Scale Type](./scale-type.md)
- [Flash Mode & Torch](./flash-mode.md) *(Android and iOS)*
- [Zoom](./zoom.md)
- [Exposure Compensation](./exposure-compensation.md)
- [Focus on Tap](./focus-on-tap.md) *(Android and iOS)*
- [Orientation Strategy](./orientation-strategy.md) *(Android and iOS)*
- [Image Capture Strategy](./image-capture-strategy.md) *(Android and iOS)*
- [Image Analyzer](./image-analyzer/image-analyzer.md)
- [Code Analyzer](./image-analyzer/code-analyzer.md) *(Android and iOS)*
- [Implementation Mode](./implementation-mode.md) *(Android only)*
```

**Step 4: Verify locally** — `mkdocs serve`, check Overview page.

---

### Task 3: Add `!!! platform` badges to fully-supported pages

For each file below, insert the badge on the line immediately after the `# Title`, with a blank line above and below it. The exact pattern is:

```markdown
# Page Title

!!! platform "Android, iOS and Desktop"

[rest of content]
```

**Files to modify (11 pages):**

| File | Title line to find |
|---|---|
| `docs/getting-started.md` | `# Getting Started` (or whatever h1 it has) |
| `docs/camera-session.md` | first `#` heading |
| `docs/camera-selector.md` | `# Camera Selector` |
| `docs/camera-capture-mode.md` | `# Capture Mode` |
| `docs/camera-controller/camera-controller.md` | first `#` heading |
| `docs/camera-controller/take-picture.md` | first `#` heading |
| `docs/mirror-mode.md` | `# Mirror Mode` |
| `docs/scale-type.md` | `# Scale Type` |
| `docs/image-analyzer/image-analyzer.md` | `# Image Analyzer (Custom)` |
| `docs/zoom.md` | `# Zoom` |
| `docs/exposure-compensation.md` | `# Exposure Compensation` |

Read each file first, find the exact h1 text, then insert:
```markdown
!!! platform "Android, iOS and Desktop"
```
immediately after the h1 (blank line between h1 and badge, blank line between badge and next content).

**Step 2: Verify locally** — spot-check 2–3 pages in the browser. Green pill with checkmark should appear below the title.

---

### Task 4: Add `!!! platform-limited` badges to limited pages

Same insertion pattern as Task 3 — immediately after the `# Title` heading.

**Files and badge text:**

| File | Badge text |
|---|---|
| `docs/camera-controller/record-video.md` | `Android and iOS` |
| `docs/camera-format.md` | `Android and iOS` |
| `docs/flash-mode.md` | `Android and iOS` |
| `docs/focus-on-tap.md` | `Android and iOS` |
| `docs/orientation-strategy.md` | `Android and iOS` |
| `docs/image-capture-strategy.md` | `Android and iOS` |
| `docs/image-analyzer/code-analyzer.md` | `Android and iOS` |
| `docs/implementation-mode.md` | `Android only` |

Example for `flash-mode.md`:
```markdown
# Flash Mode & Torch

!!! platform-limited "Android and iOS"

## Introduction
```

**Step 2: Verify locally** — amber pill with warning triangle should appear below the title.

---

### Task 5: Add Runtime Configuration section to `camera-format.md`

**Files:**
- Modify: `docs/camera-format.md` (append new section at end)

**Step 1: Append the following section**

```markdown
## Runtime Configuration

In addition to configuring format via `CamFormat` at session startup, you can update frame rate and video stabilization at runtime using `CameraController`:

```kotlin
val cameraController = remember { CameraController() }
val cameraSession = rememberCameraSession(cameraController)

// Change frame rate at runtime (must be within the camera's supported range)
cameraController.setVideoFrameRate(60)

// Change video stabilization mode at runtime
cameraController.setVideoStabilizationEnabled(VideoStabilizationMode.Standard)
```

You can query the camera's supported frame-rate range before calling `setVideoFrameRate`:

```kotlin
val cameraInfoState by cameraSession.info.collectStateWithLifecycle()
val minFPS = cameraInfoState.minFPS
val maxFPS = cameraInfoState.maxFPS
```

!!! warning
    `setVideoFrameRate` returns `Result.failure` if the requested value is outside `minFPS..maxFPS`.
    `setVideoStabilizationEnabled` requires the device to support video stabilization — check `cameraInfoState.isVideoStabilizationSupported` before calling it.
```

**Step 2: Verify locally** — navigate to Camera Format page, confirm new section renders with correct code blocks and warning admonition.

---

## Completion Checklist

- [ ] `extra.css` has both `platform` and `platform-limited` admonition styles
- [ ] `index.md` mentions Desktop in hero, table, and features list
- [ ] All 11 fully-supported pages have a green `!!! platform "Android, iOS and Desktop"` badge
- [ ] All 8 limited pages have an amber `!!! platform-limited "..."` badge
- [ ] `camera-format.md` has the new Runtime Configuration section
- [ ] `mkdocs serve` renders all pages without errors

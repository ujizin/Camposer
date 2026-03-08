# Design: Docs Platform Support Banners + Desktop Platform

**Date:** 2026-03-08
**Scope:** `docs/` — all existing pages + `docs/assets/stylesheets/extra.css`

---

## Goal

1. Add Desktop (JVM) to the `docs/index.md` overview page.
2. Add a compact platform support badge to every doc page, placed immediately after the `# Title`.
3. Add a "Runtime Configuration" section to `camera-format.md` for `setVideoFrameRate` and `setVideoStabilizationEnabled`.

---

## 1. CSS — Custom Admonition Types (`extra.css`)

Add two custom admonition types using MkDocs Material's mask-image mechanism.

### `!!! platform ""`
Green pill with a checkmark icon. Used for features supported on all three platforms (Android, iOS, Desktop) or a meaningful subset.

### `!!! platform-limited ""`
Amber pill with a warning triangle icon. Used for features unavailable or no-op on Desktop.

The empty string title `""` suppresses the admonition header, making it render as a compact single-line badge matching the screenshot reference.

Both types reuse the existing `admonition` markdown extension already enabled in `mkdocs.yml` — no new plugins needed.

---

## 2. `docs/index.md` Changes

### Hero description
Update the description paragraph to mention Desktop alongside Android and iOS.

### Platform Support table
Add a Desktop row:

| Platform | Status |
|---|---|
| **Android** | ✅ Supported |
| **iOS** | ✅ Supported |
| **Desktop (JVM)** | ✅ Supported (partial — see per-feature docs) |

### Features list
Add missing entries: `Scale Type`, `Mirror Mode` (currently unlisted).
Annotate Desktop-only limitations inline where helpful.

---

## 3. Per-Page Platform Badges

Badge placed on the line immediately after `# Title` (before any other content).

### `!!! platform ""` — green ✓ — "Android, iOS and Desktop"

| File |
|---|
| `getting-started.md` |
| `camera-session.md` |
| `camera-selector.md` |
| `camera-capture-mode.md` |
| `camera-controller/camera-controller.md` |
| `camera-controller/take-picture.md` |
| `mirror-mode.md` |
| `scale-type.md` |
| `image-analyzer/image-analyzer.md` |
| `zoom.md` |
| `exposure-compensation.md` |

### `!!! platform-limited ""` — amber ⚠ — listed text

| File | Text |
|---|---|
| `camera-controller/record-video.md` | Android and iOS |
| `camera-format.md` | Android and iOS |
| `flash-mode.md` | Android and iOS |
| `focus-on-tap.md` | Android and iOS |
| `orientation-strategy.md` | Android and iOS |
| `image-capture-strategy.md` | Android and iOS |
| `image-analyzer/code-analyzer.md` | Android and iOS |
| `implementation-mode.md` | Android only |

---

## 4. `camera-format.md` — Runtime Configuration Section

Add a new section at the bottom of `camera-format.md` documenting the two `CameraController` runtime APIs:

- `cameraController.setVideoFrameRate(fps: Int)` — Android and iOS only
- `cameraController.setVideoStabilizationEnabled(mode: VideoStabilizationMode)` — Android and iOS only

Both are covered by the page-level `platform-limited` badge. The section includes a brief description and a usage code example.

---

## Implementation Order

1. `extra.css` — add CSS for both admonition types
2. `docs/index.md` — add Desktop to hero, table, and features list
3. All doc pages — insert platform badge after `# Title`
4. `camera-format.md` — insert runtime configuration section

# Submitting to F-Droid

## What F-Droid needs that a normal build does not

F-Droid compiles the app **on their servers, from source, and signs it with
their own key**. Nothing you build locally is uploaded. That imposes three
requirements:

1. **Every line of source public and freely licensed** — including the
   decompression engine, which lives in a separate repository.
2. **No proprietary dependencies and no pre-built binaries** in the tree. The
   dependencies here are AndroidX and Compose from Maven Central and Google's
   repository, which are acceptable.
3. **Buildable by a machine that has only this repository** — no absolute paths
   pointing into anyone's private disk.

Point 3 is the one this project has to be careful about.

## The engine is a submodule

The app does not contain the decompression engine. `app/src/main/cpp/
CMakeLists.txt` compiles `czplan.c` out of ZPlanKit, which is what guarantees a
dive plans identically on Android, macOS and iOS.

It is the **`engine` submodule at the root of this repository**, and it is the
default — no property needs to be set, and `submodules: true` in the recipe is
all F-Droid requires. A plain `git clone --recurse-submodules` builds.

```sh
git clone --recurse-submodules https://github.com/landerlab-apps/Lplanner-FDroid.git
```

Pinning to a submodule commit has a second benefit worth having: the app
version and the engine commit move together, so a published schedule cannot
change underneath a release.

**Relative paths are resolved against the repository root**, not the app
module. This is not a detail — `project.file("engine")` resolves to
`app/engine`, and the submodule is a level above that, so an earlier version of
this document told F-Droid to set `-Pzplankit.dir=engine` and the build would
have failed at configure time with the engine sitting right there in the tree.
`app/build.gradle.kts` now uses `rootProject.file()`.

To build against a ZPlanKit checkout you are actively editing, uncomment the
override in `gradle.properties` — and remember the submodule is what ships.

## Build recipe

F-Droid metadata lives in their `fdroiddata` repository as
`metadata/com.landerlab.lplanner.fdroid.yml`. Submit it as a merge request.

```yaml
Categories:
  - Sports & Health
License: GPL-3.0-only
AuthorName: Carlos Lander
AuthorEmail: scubalander@gmail.com
SourceCode: https://github.com/landerlab-apps/Lplanner-FDroid
IssueTracker: https://github.com/landerlab-apps/Lplanner-FDroid/issues
Changelog: https://github.com/landerlab-apps/lplanner/blob/main/CHANGELOG.md

AutoName: Lplanner

RepoType: git
Repo: https://github.com/landerlab-apps/Lplanner-FDroid.git

Builds:
  - versionName: 1.6.0
    versionCode: 11
    commit: v1.6.0
    subdir: app
    submodules: true
    gradle:
      - yes
    ndk: r27c

AutoUpdateMode: Version
UpdateCheckMode: Tags
CurrentVersion: 1.6.0
CurrentVersionCode: 11
```

Check before submitting:

- `ndk: r27c` must match `ndkVersion` in `app/build.gradle.kts`
  (27.2.12479018). Bump one, bump the other.
- `commit:` must be a **tag**, not a branch. Tag each release: `git tag v1.6.0`.
- The metadata filename must be the application id, `com.landerlab.lplanner
  .fdroid.yml` — note the suffix.
- `gradle: [yes]` because there are no product flavours in this project. If
  flavours are ever added, name the flavour here instead.

## Anti-features

None apply. No network permission, no advertising, no tracking, no dependency
on a non-free network service. Expect a clean listing.

## Before every release

```sh
tools/drift-check.sh
```

The Play project and this one are deliberately independent, which means a fix
in one does not reach the other on its own. The script compares the source that
is supposed to be identical.

## Checklist

- [x] ZPlanKit public at `github.com/landerlab-apps/ZPlanKit`, GPL-3.0
- [x] `engine` submodule added and committed (pinned at 0bca7d4, engine 1.21.0)
- [ ] This repository public on GitHub
- [ ] `tools/drift-check.sh` clean
- [ ] Release tagged `v1.6.0`
- [ ] `./gradlew assembleRelease` succeeds from a clean
      `git clone --recurse-submodules`, with no property set
- [ ] Screenshots in `fastlane/metadata/android/en-US/images/phoneScreenshots/`
- [ ] Merge request opened against `fdroiddata`

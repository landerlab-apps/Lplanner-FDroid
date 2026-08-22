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

## The engine must become a submodule

The app does not contain the decompression engine. `app/src/main/cpp/
CMakeLists.txt` compiles `czplan.c` out of ZPlanKit, which is what guarantees a
dive plans identically on Android, macOS and iOS.

For local work the path comes from `gradle.properties`:

```
zplankit.dir=../../../DeveloperApple/ZPlanKit
```

That path exists on one machine in the world. **F-Droid must not use it.** Add
the engine as a submodule and point the build there:

```sh
git submodule add https://github.com/landerlab-apps/ZPlanKit.git engine
git commit -m "Add the decompression engine as a submodule"
```

The recipe then overrides the property with `-Pzplankit.dir=engine`, and
`submodules: true` fetches it.

Pinning to a submodule commit has a second benefit worth having: the app
version and the engine commit move together, so a published schedule cannot
change underneath a release.

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
    gradleprops:
      - zplankit.dir=engine

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

- [ ] ZPlanKit public at `github.com/landerlab-apps/ZPlanKit`, GPL-3.0
- [ ] `engine` submodule added and committed
- [ ] This repository public on GitHub
- [ ] `tools/drift-check.sh` clean
- [ ] Release tagged `v1.6.0`
- [ ] `./gradlew assembleRelease` succeeds from a clean checkout with
      `-Pzplankit.dir=engine`
- [ ] Screenshots in `fastlane/metadata/android/en-US/images/phoneScreenshots/`
- [ ] Merge request opened against `fdroiddata`

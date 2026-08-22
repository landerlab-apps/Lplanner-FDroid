# Lplanner — F-Droid build

A mixed-gas decompression planner for trained divers. ZHL-16C, VVAL-18 and
VPM-B, sharing one C decompression engine with the macOS and iOS builds.

**This software can kill you.** It is experimental and certainly contains bugs.
Never dive a schedule from it without verifying that schedule against tables or
software you trust, and never use it without formal mixed-gas decompression
training.

Free software under the [GNU General Public License v3](LICENSE).

## Why this repository exists separately

Lplanner ships from three independent projects:

| | project | store |
|---|---|---|
| macOS / iPhone / iPad | `DeveloperApple/Lplanner` | direct download |
| Android | `DeveloperAndroid/Lplanner-Android` | Google Play, paid |
| Android | **this one** | F-Droid, free |

They are separate on purpose. Any one of them can be updated, held back, or
abandoned without disturbing the others — a Play policy change does not touch
the F-Droid build, and dropping F-Droid would not mean editing the Play
project's Gradle files.

What they must never do is disagree about a decompression schedule, and they
cannot: all three compile the same `czplan.c` out of ZPlanKit. The engine is
the single source of truth. Everything above it is a front end.

## Differences from the Play build

Only three, and all three are forced:

- **Application id** is `com.landerlab.lplanner.fdroid`. Google Play signs its
  artefacts with a key Google holds; F-Droid signs with F-Droid's own. Two
  certificates under one id means Android refuses to install either build over
  the other, and a diver switching stores would have to uninstall — losing his
  plan log and his carried tissue loading. The suffix buys side-by-side
  installation.
- **App name** reads "Lplanner (F-Droid)", so two launcher icons on one device
  are distinguishable.
- **No signing config.** F-Droid builds and signs this themselves.

The Kotlin, the JNI bridge and the dependency versions are identical, and
`tools/drift-check.sh` proves it:

```sh
tools/drift-check.sh
```

Run it before tagging a release in either project. Separate trees mean a fix
made in one does not appear in the other, and nothing else will warn you.

## Building

The engine is not in this repository. Locally, `gradle.properties` points at
your ZPlanKit checkout; on F-Droid's builders it comes from the `engine`
submodule. See [FDROID.md](FDROID.md) for the submission recipe.

```sh
./gradlew assembleRelease
```

## Privacy

No Android permissions are declared, including no internet permission. The app
cannot transmit anything. See the [privacy
policy](https://github.com/landerlab-apps/lplanner/blob/main/PRIVACY.md).

## Documentation

The manual, changelog and model notes live in
[landerlab-apps/lplanner](https://github.com/landerlab-apps/lplanner).

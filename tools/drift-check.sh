#!/bin/sh
# drift-check.sh — is the F-Droid app still the same app as the Play one?
#
# The two Android projects are deliberately separate so either store can be
# updated, held back or abandoned on its own. The price of that independence is
# that a fix made in one tree does not appear in the other, and nothing warns
# you. That is not a hypothetical: KingstonData/lplanner is an abandoned copy
# that drifted ten engine versions behind before anyone noticed.
#
# This compares the source that is supposed to be identical and prints what is
# not. It changes nothing. Run it before tagging either release.
#
#   usage: tools/drift-check.sh [path-to-Lplanner-Android]

set -e
HERE=$(cd "$(dirname "$0")/.." && pwd)
PLAY=${1:-"$HERE/../../DeveloperAndroid/Lplanner-Android"}

if [ ! -d "$PLAY/app/src/main/java" ]; then
    echo "Cannot find the Play project at:"
    echo "  $PLAY"
    echo "Pass its path as the first argument."
    exit 2
fi

echo "F-Droid : $HERE"
echo "Play    : $PLAY"
echo

status=0

# ---- the Kotlin, which must match exactly --------------------------------
if diff -r -q "$PLAY/app/src/main/java" "$HERE/app/src/main/java" >/dev/null 2>&1; then
    n=$(find "$HERE/app/src/main/java" -name '*.kt' | wc -l | tr -d ' ')
    echo "Kotlin source          identical ($n files)"
else
    echo "Kotlin source          DIFFERS:"
    diff -r -q "$PLAY/app/src/main/java" "$HERE/app/src/main/java" 2>&1 | sed 's/^/    /'
    status=1
fi

# ---- the JNI bridge and CMake --------------------------------------------
if diff -r -q "$PLAY/app/src/main/cpp" "$HERE/app/src/main/cpp" >/dev/null 2>&1; then
    echo "Native bridge          identical"
else
    echo "Native bridge          DIFFERS:"
    diff -r -q "$PLAY/app/src/main/cpp" "$HERE/app/src/main/cpp" 2>&1 | sed 's/^/    /'
    status=1
fi

# ---- dependency versions --------------------------------------------------
if diff -q "$PLAY/gradle/libs.versions.toml" "$HERE/gradle/libs.versions.toml" >/dev/null 2>&1; then
    echo "Dependency versions    identical"
else
    echo "Dependency versions    DIFFER:"
    diff "$PLAY/gradle/libs.versions.toml" "$HERE/gradle/libs.versions.toml" | sed 's/^/    /'
    status=1
fi

# ---- version numbers, which SHOULD normally match -------------------------
pv=$(grep -E '^\s*versionName' "$PLAY/app/build.gradle.kts" | head -1 | sed 's/.*"\(.*\)".*/\1/')
fv=$(grep -E '^\s*versionName' "$HERE/app/build.gradle.kts" | head -1 | sed 's/.*"\(.*\)".*/\1/')
pc=$(grep -E '^\s*versionCode' "$PLAY/app/build.gradle.kts" | head -1 | sed 's/[^0-9]//g')
fc=$(grep -E '^\s*versionCode' "$HERE/app/build.gradle.kts" | head -1 | sed 's/[^0-9]//g')
if [ "$pv" = "$fv" ] && [ "$pc" = "$fc" ]; then
    echo "Version                both $pv ($pc)"
else
    echo "Version                Play $pv ($pc)   F-Droid $fv ($fc)"
    echo "                       Not a failure - the stores can legitimately"
    echo "                       be on different releases. Just know which."
fi

# ---- differences that are meant to be there -------------------------------
echo
echo "Intentionally different, not checked:"
echo "  applicationId          com.landerlab.lplanner[.fdroid]"
echo "  app_name               \"Lplanner\" / \"Lplanner (F-Droid)\""
echo "  signing                Play signs; F-Droid signs its own build"
echo "  fastlane metadata      F-Droid only"

echo
if [ $status -eq 0 ]; then
    echo "No drift in the shared source."
else
    echo "DRIFT. Reconcile before releasing, or note deliberately why not."
fi
exit $status

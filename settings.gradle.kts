// No foojay-resolver plugin here, deliberately.
//
// Android Studio adds org.gradle.toolchains.foojay-resolver-convention by
// default. It provisions JDKs by downloading them from api.foojay.io while the
// build runs, and F-Droid's scanner rejects it on sight - a third-party binary
// fetched mid-build defeats the reproducibility their whole model rests on.
// It failed the build with "Found usual suspect 'org.gradle.toolchains
// .foojay-resolver' at settings.gradle.kts".
//
// Nothing here needs it. The app asks only for Java 17 source and target
// compatibility, which any JDK 17 or later satisfies, and both Android Studio
// and F-Droid's builders ship newer. gradle/gradle-daemon-jvm.properties went
// with it - that file was nothing but foojay download URLs.
pluginManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories { google(); mavenCentral() }
}
rootProject.name = "Lplanner"
include(":app")

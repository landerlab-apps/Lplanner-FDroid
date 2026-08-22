# Lplanner Android v1.0.0 — builds the shared ZPlanKit C engine for Android.
cmake_minimum_required(VERSION 3.22.1)
project(zplan C)

# ZPLANKIT_DIR is injected by app/build.gradle.kts and points at the SAME
# ZPlanKit checkout the iOS app builds from. Nothing is copied or forked.
if(NOT DEFINED ZPLANKIT_DIR)
    # Fallback for the current layout: .../DeveloperAndroid/Lplanner-Android/app/src/main/cpp
    # -> .../DeveloperApple/ZPlanKit
    set(ZPLANKIT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../../../DeveloperApple/ZPlanKit")
endif()

set(CZPLAN_SRC "${ZPLANKIT_DIR}/Sources/CZPlan/czplan.c")
set(CZPLAN_INC "${ZPLANKIT_DIR}/Sources/CZPlan/include")

if(NOT EXISTS "${CZPLAN_SRC}")
    message(FATAL_ERROR
        "Cannot find the ZPlanKit engine at ${CZPLAN_SRC}.\n"
        "Set zplankit.dir in gradle.properties to your ZPlanKit checkout.")
endif()

add_library(zplan SHARED "${CZPLAN_SRC}" zplan_jni.c)
target_include_directories(zplan PRIVATE "${CZPLAN_INC}")
set_target_properties(zplan PROPERTIES C_STANDARD 99)
target_link_libraries(zplan m log)

# 16 KB page size alignment (Android 15+ / Play requirement).
# ANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON in build.gradle.kts covers NDK r27, and
# r28+ aligns by default. These explicit flags make it independent of the NDK
# version, so libzplan.so is 16 KB-aligned no matter which toolchain builds it.
target_link_options(zplan PRIVATE
    "-Wl,-z,max-page-size=16384"
    "-Wl,-z,common-page-size=16384")

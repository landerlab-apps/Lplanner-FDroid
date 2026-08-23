# Lplanner Android v1.0.0 — builds the shared ZPlanKit C engine for Android.
cmake_minimum_required(VERSION 3.22.1)
project(zplan C)

# ZPLANKIT_DIR is injected by app/build.gradle.kts. The engine is not vendored
# here: it is the `engine` git submodule at the root of this repository, the
# same czplan.c the Play and Apple builds compile. That is what stops the three
# front ends from ever disagreeing about a schedule.
if(NOT DEFINED ZPLANKIT_DIR)
    # Fallback: app/src/main/cpp -> repository root -> engine
    set(ZPLANKIT_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../engine")
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

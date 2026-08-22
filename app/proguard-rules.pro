# JNI entry points are called from C — keep them.
-keepclasseswithmembernames class * { native <methods>; }
-keep class com.landerlab.lplanner.ZPlan { *; }

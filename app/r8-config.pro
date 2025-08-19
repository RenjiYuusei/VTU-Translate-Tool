# R8 Configuration for VTU Translate - Ultra Optimized

# Enable maximum optimizations
-optimizations !code/allocation/variable
-optimizationpasses 7
-allowaccessmodification
-repackageclasses ''
-flattenpackagehierarchy ''
-dontpreverify
-mergeinterfacesaggressively

# Keep only essential attributes
-keepattributes Signature,*Annotation*,EnclosingMethod

# Enable class merging and method inlining
-optimizations class/merging/*,method/inlining/*,code/simplification/*,code/removal/*

# Remove unused code based on SDK version
-assumevalues class android.os.Build$VERSION {
    int SDK_INT return 24..34;
}

# Optimize away all debug and logging code
-assumenosideeffects class android.util.Log {
    public static *** v(...);
    public static *** d(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
    public static *** wtf(...);
}

# Optimize away Kotlin null checks and debug code
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static *** checkParameterIsNotNull(...);
    public static *** checkNotNullParameter(...);
    public static *** checkExpressionValueIsNotNull(...);
    public static *** checkNotNullExpressionValue(...);
    public static *** checkReturnedValueIsNotNull(...);
    public static *** checkFieldIsNotNull(...);
    public static *** checkNotNull(...);
    public static *** throwUninitializedPropertyAccessException(...);
    public static *** throwNpe(...);
    public static *** throwJavaNpe(...);
    public static *** throwAssert(...);
    public static *** throwIllegalArgument(...);
    public static *** throwIllegalState(...);
}

# Remove Compose debug and tracing code
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    public static *** isTraceInProgress();
    public static *** traceEventStart(...);
    public static *** traceEventEnd(...);
    public static *** sourceInformation(...);
    public static *** sourceInformationMarkerStart(...);
    public static *** sourceInformationMarkerEnd(...);
}

# Remove Compose debugging classes completely
-assumenosideeffects class androidx.compose.runtime.Trace {
    public static *** beginSection(...);
    public static *** endSection();
}

# Optimize coroutines debugging
-assumenosideeffects class kotlinx.coroutines.internal.DebugKt {
    public static *** getASSERTIONS_ENABLED();
    public static *** getDEBUG();
}

# Remove unused reflection and metadata
-assumenosideeffects class kotlin.Metadata {
    *;
}

# Aggressive shrinking of unused code
-dontwarn kotlin.reflect.**
-dontwarn kotlinx.serialization.descriptors.**
-dontwarn kotlinx.coroutines.debug.**
-dontwarn androidx.compose.runtime.snapshots.Snapshot$Companion

# Remove unnecessary resource processing
-adaptresourcefilenames **.properties,**.xml,**.json
-adaptresourcefilecontents **.properties,META-INF/MANIFEST.MF,**.xml

# Enable enum optimization
-optimizeenumvalues

# Class merging for smaller DEX
-optimizations class/merging/vertical,class/merging/horizontal

# Method inlining for performance
-optimizations method/inlining/short,method/inlining/unique,method/inlining/tailrecursion

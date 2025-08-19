# R8 Configuration for VTU Translate - Optimized

# Enable more optimizations
-optimizations !code/allocation/variable,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep only essential attributes
-keepattributes Signature,*Annotation*

# Aggressive obfuscation (using only repackageclasses)
-repackageclasses ''

# Remove unused code based on SDK version
-assumevalues class android.os.Build$VERSION {
    int SDK_INT return 24..34;
}

# Remove unused androidx code
-assumevalues class androidx.customview.view.AbsSavedState {
    int EMPTY_STATE return 1;
}

# Optimize away Kotlin null checks
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullParameter(java.lang.Object, java.lang.String);
    static void checkExpressionValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkNotNullExpressionValue(java.lang.Object, java.lang.String);
    static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String);
    static void checkReturnedValueIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    static void checkFieldIsNotNull(java.lang.Object, java.lang.String);
    static void checkFieldIsNotNull(java.lang.Object, java.lang.String, java.lang.String);
    static void checkNotNull(java.lang.Object);
    static void checkNotNull(java.lang.Object, java.lang.String);
}

# Remove Compose debug classes
-assumenosideeffects class androidx.compose.runtime.ComposerKt {
    public static boolean isTraceInProgress();
    public static void traceEventStart(int, int, int, java.lang.String);
    public static void traceEventEnd();
}
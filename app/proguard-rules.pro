# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Optimization settings for smaller APK
-optimizationpasses 5
-dontpreverify
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable,Signature,*Annotation*

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Google Error Prone annotations - only warnings
-dontwarn com.google.errorprone.annotations.**

# Keep only necessary Tink crypto classes
-keep class com.google.crypto.tink.shaded.protobuf.** { *; }
-keepclassmembers class com.google.crypto.tink.** {
    public static final int TINK_VERSION_JAVA;
}
-dontwarn com.google.crypto.tink.**

# Keep EncryptedSharedPreferences (minimal)
-keep class androidx.security.crypto.EncryptedSharedPreferences { *; }
-keep class androidx.security.crypto.MasterKey* { *; }

# Retrofit - keep only necessary
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-dontwarn retrofit2.**

# OkHttp - minimal
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Kotlin serialization - optimized
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.vtu.translate.**$$serializer { *; }
-keepclassmembers class com.vtu.translate.** {
    *** Companion;
}
-keepclasseswithmembers class com.vtu.translate.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep data classes for serialization
-keep class com.vtu.translate.data.model.** { *; }

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep only essential Compose classes
-keep class androidx.compose.runtime.ComposerImpl { *; }
-keep class androidx.compose.runtime.Recomposer { *; }
-keep class androidx.compose.runtime.SlotTable { *; }
-keep class androidx.compose.runtime.Applier { *; }
-keep @androidx.compose.runtime.Composable class * { *; }
-keepclasseswithmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Keep UI essentials
-keep class androidx.compose.ui.platform.** { *; }
-keep class androidx.compose.ui.node.** { *; }
-keep class androidx.compose.ui.graphics.** { *; }
-keep class androidx.compose.ui.text.** { *; }

# Keep Material3 components used in the app
-keep class androidx.compose.material3.*Button* { *; }
-keep class androidx.compose.material3.*TextField* { *; }
-keep class androidx.compose.material3.*Card* { *; }
-keep class androidx.compose.material3.*Dialog* { *; }
-keep class androidx.compose.material3.*DropdownMenu* { *; }
-keep class androidx.compose.material3.*NavigationBar* { *; }
-keep class androidx.compose.material3.*Scaffold* { *; }
-keep class androidx.compose.material3.*Surface* { *; }
-keep class androidx.compose.material3.*Text* { *; }
-keep class androidx.compose.material3.*Icon* { *; }
-keep class androidx.compose.material3.*Progress* { *; }

# CRITICAL: Keep ALL app classes to prevent translation errors
-keep class com.vtu.translate.** { *; }
-keepclassmembers class com.vtu.translate.** { *; }
-keepnames class com.vtu.translate.** { *; }

# Keep all fields and methods in data classes
-keepclassmembers class com.vtu.translate.data.** {
    <fields>;
    <methods>;
}

# Keep all API-related classes intact
-keep class com.vtu.translate.data.repository.** { *; }
-keep class com.vtu.translate.service.** { *; }
-keep class com.vtu.translate.data.model.** { *; }
-keep class com.vtu.translate.data.util.** { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class com.vtu.translate.ui.viewmodel.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Lifecycle
-keep class androidx.lifecycle.** { *; }

# Keep Coroutines
-keep class kotlinx.coroutines.** { *; }

# Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.vtu.translate.**$$serializer { *; }
-keepclassmembers class com.vtu.translate.** {
    *** Companion;
}
-keepclasseswithmembers class com.vtu.translate.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Retrofit and OkHttp completely 
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Keep all annotations
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Groq API service interface and all inner classes
-keep class com.vtu.translate.data.repository.GroqRepository { *; }
-keep class com.vtu.translate.data.repository.GroqRepository$* { *; }
-keepclassmembers class com.vtu.translate.data.repository.GroqRepository$GroqService {
    *;
}

# Keep all data classes with their fields
-keepclassmembers class com.vtu.translate.data.model.** {
    <fields>;
    <init>(...);
    <methods>;
}

# Keep JSON serialization
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class kotlinx.serialization.json.** { *; }
-keep,includedescriptorclasses class com.vtu.translate.**$$serializer { *; }
-keepclassmembers class com.vtu.translate.** {
    *** Companion;
}
-keepclasseswithmembers class com.vtu.translate.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Jakewharton converter
-keep class com.jakewharton.retrofit2.** { *; }
-keep class kotlinx.serialization.json.** { *; }

# Keep Retrofit annotations
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes RuntimeInvisibleParameterAnnotations

# Keep generic type information for Retrofit
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep Retrofit service methods
-keepclasseswithmembers class * {
    @retrofit2.* <methods>;
}
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Don't warn about missing classes
-dontwarn androidx.compose.**
-dontwarn kotlinx.**
-dontwarn androidx.**
-dontwarn kotlin.reflect.**
-dontwarn kotlin.jvm.internal.**
-dontwarn javax.annotation.**

# Remove logs in release
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
    public static int e(...);
}

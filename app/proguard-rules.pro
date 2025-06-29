# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep Kotlin Serialization
-keepattributes *Annotation*, InnerClasses, Signature, Exceptions, EnclosingMethod
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Keep Reflection information for ParameterizedType
-keepattributes ParameterizedType, GenericSignature
-keep class java.lang.reflect.** { *; }
-keep class * implements java.lang.reflect.ParameterizedType

# Keep Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep Kotlinx Serialization classes
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable <fields>;
}
-keepclasseswithmembers class ** {
    @kotlinx.serialization.Serializable <methods>;
}
-keep class ** {
    @kotlinx.serialization.Serializable *;
}
-keep @kotlinx.serialization.Serializable class *

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Tối ưu hóa Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.foundation.** { *; }

# Tối ưu hóa R8
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

# Keep Model classes
-keep class com.vtu.translate.data.model.** { *; }
-keepclassmembers class com.vtu.translate.data.model.** { *; }

# Tăng hiệu suất R8
-dontpreverify
-repackageclasses ''
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# Keep Error Prone Annotations
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }
-keep @com.google.errorprone.annotations.Immutable class * { *; }
-keep @com.google.errorprone.annotations.CanIgnoreReturnValue class * { *; }
-keep @com.google.errorprone.annotations.CheckReturnValue class * { *; }
-keep @com.google.errorprone.annotations.RestrictedApi class * { *; }
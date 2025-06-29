# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep error-prone annotations
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Keep specific error-prone annotations that are missing
-keep class com.google.errorprone.annotations.CanIgnoreReturnValue { *; }
-keep class com.google.errorprone.annotations.CheckReturnValue { *; }
-keep class com.google.errorprone.annotations.Immutable { *; }
-keep class com.google.errorprone.annotations.RestrictedApi { *; }

# Keep crypto.tink classes that use these annotations
-keep class com.google.crypto.tink.** { *; }

# Keep Google API Client classes
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**

# Keep Google HTTP Client classes
-keep class com.google.http.client.** { *; }
-dontwarn com.google.http.client.**

# Keep Joda Time classes
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**

# Keep javax.naming classes
-keep class javax.naming.** { *; }
-dontwarn javax.naming.**

# Keep org.ietf.jgss classes
-keep class org.ietf.jgss.** { *; }
-dontwarn org.ietf.jgss.**

# Keep Apache HTTP classes
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

# General rules
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception

# For native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep the R class
-keep class **.R$* {*;}
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Keep error prone annotations
-dontwarn com.google.errorprone.annotations.**
-keep class com.google.errorprone.annotations.** { *; }

# Keep rules for Google Crypto Tink
-keep class com.google.crypto.tink.** { *; }
-keepclassmembers class * {
    @com.google.crypto.tink.** *;
}

# Keep rules for Google API Client
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**

# Keep rules for Google HTTP Client
-keep class com.google.http.client.** { *; }
-dontwarn com.google.http.client.**

# Keep rules for Joda Time
-keep class org.joda.time.** { *; }
-dontwarn org.joda.time.**

# Keep rules for javax.naming
-keep class javax.naming.** { *; }
-dontwarn javax.naming.**

# Keep rules for org.ietf.jgss
-keep class org.ietf.jgss.** { *; }
-dontwarn org.ietf.jgss.**
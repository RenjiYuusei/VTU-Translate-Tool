# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Cấu hình tối ưu cho R8
-optimizations !code/simplification/arithmetic,!code/simplification/cast,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose

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

# Quy tắc bổ sung cho R8
# Giữ lại thông tin dòng cho debugging
-keepattributes SourceFile,LineNumberTable

# Nếu ứng dụng sử dụng WebView với JS, không làm rối các giao diện JS
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# Giữ lại các thành phần Android
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View

# Giữ lại các thành phần Jetpack Compose
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keepclasseswithmembers class androidx.compose.** { *; }
# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /usr/local/google/android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If you use reflection, typically to serialize or deserialize JSON objects,
# you need to include the following rules to prevent ProGuard from renaming the fields.
#-keepclassmembers class com.google.gson.examples.android.model.** { <fields>; }

# Keep Kotlinx Serialization classes
-keep class kotlin.Metadata { *; }
-keep class kotlinx.serialization.** { *; }
-keep class com.vtu.translate.network.** { *; }

# Keep data classes used for serialization
-keepclassmembers class com.vtu.translate.network.ChatCompletionRequest { *; }
-keepclassmembers class com.vtu.translate.network.Message { *; }
-keepclassmembers class com.vtu.translate.network.ChatCompletionResponse { *; }
-keepclassmembers class com.vtu.translate.network.Choice { *; }

# Keep generated serialization code
-keepnames class * implements kotlinx.serialization.KSerializer
-keepclassmembers class * {
    *** Companion;
}
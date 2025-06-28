# Add project specific ProGuard rules here.
# You can find more details on aapt_rules.txt file in build/intermediates.
-keep class kotlin.io.path.** { *; }
-keep class kotlinx.serialization.** { *; }
-keep class * extends kotlinx.serialization.internal.SerializerBase { *; }
-keep class * implements kotlinx.serialization.KSerializer { *; }
-dontwarn kotlinx.serialization.** 
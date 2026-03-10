# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep Retrofit
-keepattributes Signature
-keepattributes Exceptions

# Retrofit
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# Gson
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }
-keep class com.vaultpool.customer.data.remote.dto.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }

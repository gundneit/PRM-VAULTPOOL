# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Keep Firebase Auth
-keep class com.google.firebase.auth.** { *; }

# Keep Firestore
-keep class com.google.firebase.firestore.** { *; }

# Keep your application classes
-keep class com.lavelahotel.poolbooking.** { *; }

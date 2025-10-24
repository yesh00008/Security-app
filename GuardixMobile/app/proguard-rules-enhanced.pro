# Guardix Mobile Security - ProGuard Configuration
# Optimized for production builds with enhanced security and obfuscation

# Enable aggressive optimizations
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Keep debugging info for crash reports (remove in final release)
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Preserve annotations for runtime
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes Signature,InnerClasses,EnclosingMethod

# === ANDROIDX & JETPACK COMPOSE ===
-keep class androidx.** { *; }
-keep class androidx.compose.** { *; }
-dontwarn androidx.**

# Compose specific rules
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }

# === KOTLIN & COROUTINES ===
-keep class kotlin.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# === HILT DEPENDENCY INJECTION ===
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp
-keepclasseswithmembers class * {
    @dagger.hilt.android.AndroidEntryPoint <fields>;
}

# === RETROFIT & NETWORKING ===
-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class com.squareup.moshi.** { *; }
-dontwarn retrofit2.**
-dontwarn okhttp3.**

# Retrofit interface methods
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Moshi JSON adapter generation
-keep @com.squareup.moshi.JsonQualifier interface *
-keep class **JsonAdapter {
    <init>(...);
    <fields>;
}
-keepnames @com.squareup.moshi.JsonClass class *

# === GUARDIX SPECIFIC RULES ===

# Keep main application class
-keep class com.guardix.mobile.GuardixApplication { *; }
-keep class com.guardix.mobile.MainActivity { *; }

# Keep API DTOs and data classes
-keep class com.guardix.mobile.data.remote.dto.** { *; }
-keep class com.guardix.mobile.data.** { *; }

# Keep repository and service classes
-keep class com.guardix.mobile.repository.** { *; }
-keep class com.guardix.mobile.data.remote.** { *; }

# Keep security and authentication classes
-keep class com.guardix.mobile.data.remote.AuthenticationManager { *; }
-keep class com.guardix.mobile.data.remote.NetworkManager { *; }
-keep class com.guardix.mobile.data.remote.TokenStore { *; }

# Keep UI screens and components
-keep class com.guardix.mobile.ui.screens.** { *; }
-keep class com.guardix.mobile.ui.components.** { *; }

# Keep navigation and routing
-keep class com.guardix.mobile.navigation.** { *; }

# === SECURITY & BIOMETRIC ===
-keep class androidx.biometric.** { *; }
-keep class android.security.keystore.** { *; }
-keep class javax.crypto.** { *; }

# === GSON/MOSHI SERIALIZATION ===
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
}

# Keep model classes for JSON serialization
-keep class com.guardix.mobile.models.** { *; }

# === WEBVIEW (if used) ===
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# === CRASH REPORTING ===
-keep class com.google.firebase.crashlytics.** { *; }
-dontwarn com.google.firebase.crashlytics.**

# === REMOVE DEBUG LOGGING ===
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# === ENUM OPTIMIZATION ===
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# === PARCELABLE ===
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

# === NATIVE METHODS ===
-keepclasseswithmembernames class * {
    native <methods>;
}

# === WARNINGS TO IGNORE ===
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# Additional optimization for smaller APK
-repackageclasses ''
-allowaccessmodification
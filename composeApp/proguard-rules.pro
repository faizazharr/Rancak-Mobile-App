# Rancak POS — R8 / ProGuard rules untuk release build.
# Konfigurasi ini menjaga agar minification tidak merusak reflection-based
# libraries (kotlinx.serialization, Ktor, Koin) dan Compose runtime.

# ───────── Kotlin ─────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
-dontwarn kotlin.**
-dontwarn kotlinx.**

# ───────── kotlinx.serialization ─────────
# Keep @Serializable classes + their companion/serializer objects.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keep,includedescriptorclasses class id.rancak.app.**$$serializer { *; }
-keepclassmembers class id.rancak.app.** {
    *** Companion;
}
-keepclasseswithmembers class id.rancak.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Semua DTO + domain model (diserialize oleh Ktor ContentNegotiation).
-keep class id.rancak.app.data.remote.dto.** { *; }
-keep class id.rancak.app.domain.model.** { *; }
-keep class id.rancak.app.data.local.Pending* { *; }

# ───────── Ktor ─────────
-dontwarn io.ktor.**
-keep class io.ktor.** { *; }
-keep class io.ktor.client.plugins.** { *; }

# ───────── Koin ─────────
-keep class org.koin.** { *; }
-keep class * implements org.koin.core.module.Module { *; }
-keepclassmembers class * {
    @org.koin.core.annotation.* *;
}

# ───────── Compose ─────────
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keepclassmembers class androidx.compose.runtime.** { *; }

# ───────── AndroidX Credentials / Google Sign-In ─────────
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }

# ───────── Room ─────────
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }
-keep @androidx.room.Database class * { *; }

# ───────── WorkManager ─────────
-keep class androidx.work.** { *; }
-keep public class * extends androidx.work.Worker
-keep public class * extends androidx.work.CoroutineWorker

# ───────── Generic ─────────
# Hilangkan log di release (panggilan Log.d/Log.v dihapus).
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
}

# Hindari crash dari enum valueOf reflective access.
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

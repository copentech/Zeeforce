# R8 / ProGuard rules for Zayed SalesForce

# Preserve data models, Room entities, and Supabase DTOs
-keep class com.example.data.model.** { *; }
-keep class com.example.data.remote.** { *; }
-keep class com.example.worker.** { *; }

# Preserve WorkManager worker constructors
-keep class ** extends androidx.work.ListenableWorker { <init>(...); }

# Preserve Room generated classes
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.**

# Preserve ViewModels
-keepclassmembers class * extends androidx.lifecycle.ViewModel { <init>(...); }

# Moshi / Retrofit serialization rules
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**

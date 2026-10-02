# ProGuard rules for Naat Sharif & Kalam Sharif App

# Moshi & Serialization
-keep class com.example.model.** { *; }
-keepattributes *Annotation*,Signature,EnclosingMethod

# Room Database
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Retrofit & OkHttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.example.model.** { *; }

# Firebase
-keepattributes *Annotation*
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Preserve Line Numbers for Crash Reporting / Deobfuscation mapping generation
-keepattributes SourceFile,LineNumberTable

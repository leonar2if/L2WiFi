# Reglas generales para bibliotecas comunes
-keep class com.l2wifi.data.local.entity.** { *; }
-keep class com.l2wifi.data.local.dao.** { *; }
-keep class com.l2wifi.domain.model.** { *; }
-keep class com.l2wifi.data.remote.model.** { *; }
-keep class com.l2wifi.di.** { *; }
-keep class com.l2wifi.widget.** { *; }
# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keep class retrofit2.** { *; }
-dontwarn retrofit2.**
# OkHttp
-dontwarn okhttp3.**
# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
# Room
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
# Componer
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

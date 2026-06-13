# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Navigation Component / Safe Args
# We must keep classes used as arguments in nav_graph.xml to prevent inflation errors.
-keep class com.amarant.apps.budgetapp.entities.** { *; }
-keep class com.amarant.apps.budgetapp.ui.fragments.** { *; }

# Room
# Room uses reflection to instantiate the database and DAOs.
-keep class * extends androidx.room.RoomDatabase
-keep class com.amarant.apps.budgetapp.db.** { *; }

# Hilt / Dagger
# Hilt relies heavily on reflection and specific class names.
-keep class dagger.hilt.android.internal.managers.** { *; }
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends androidx.fragment.app.Fragment
-keep interface dagger.hilt.InstallIn { *; }
-keep @dagger.hilt.InstallIn class * { *; }
-keep class * {
    @javax.inject.Inject <fields>;
    @javax.inject.Inject <init>(...);
}

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }
-dontwarn com.github.mikephil.charting.**

# Serialization / Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

# DataStore / Protobuf (if applicable, though you use Preferences)
-dontwarn androidx.datastore.**

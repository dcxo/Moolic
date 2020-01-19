-keep class org.jaudiotagger.** { *; }

# RetroFit
-dontwarn retrofit.**
-keep class retrofit.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}

-printmapping mapping.txt
-printseeds obfuscation/seeds.txt
-printusage obfuscation/unused.txt

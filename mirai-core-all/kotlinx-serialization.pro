-keepattributes RuntimeVisibleAnnotations, InnerClasses, RuntimeVisibleTypeAnnotations, AnnotationDefault, LocalVariableTypeTable, Signature
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class ** extends java.lang.Enum {
    static <fields>;
}
-keepnames class ** extends java.lang.Enum

# Serializer on Mirai
-keep,includedescriptorclasses class net.mamoe.mirai.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembernames class net.mamoe.mirai.** {
    *** Companion;
}
-keepclassmembernames class net.mamoe.mirai.** {
    kotlinx.serialization.KSerializer serializer(...);
    static kotlinx.serialization.KSerializer serializer(...);
}

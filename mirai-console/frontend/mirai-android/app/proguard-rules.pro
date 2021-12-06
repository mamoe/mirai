# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
# 配置目前还有点问题无法使用
-keepattributes *Annotation*,Signature

-keepclasseswithmembers class * extends java.lang.Exception { *;}

#kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * { *;}
-keep class kotlinx.coroutines.** {*;}

# jvm平台的一些不存在的类

-dontwarn java.awt.**
-dontwarn javax.swing.**
-dontwarn sun.misc.**
-dontwarn org.jetbrains.kotlin.**

# mirai 配置
-keep class net.mamoe.mirai.qqandroid.QQAndroid.$Companion { *; }
-keepclasseswithmembers class * extends net.mamoe.mirai.BotFactory{ *;}

-keep class net.mamoe.mirai.console.** { *; }
-keep class net.mamoe.mirai.contact.** { *; }
-keep class net.mamoe.mirai.event.** { *; }
-keep class net.mamoe.mirai.message.** { *; }
-keep class net.mamoe.mirai.network.** { *; }
-keep class net.mamoe.mirai.utils.** { *; }
-keep class net.mamoe.mirai.* { *; }

# ktor
-keep class io.ktor.client.** { *; }

-keepclassmembers class io.ktor.** {
    volatile <fields>;
}


# json

-keep class kotlinx.serialization.json.** {*;}
-keep class kotlinx.serialization.* {*;}

# yaml

-keep class org.yaml.snakeyaml.* {*;}
-keep class org.yaml.snakeyaml.util.* {*;}

# okhttp
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-dontwarn okhttp3.internal.platform.ConscryptPlatform
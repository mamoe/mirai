-dontwarn
#-keep @io.netty.channel.ChannelHandler.Sharable class **

-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

-keepclassmembernames class kotlin.coroutines.SafeContinuation {
    volatile <fields>;
}
# Fields used by atomicfu
-keepclassmembers class net.mamoe.mirai.** {
    volatile <fields>;
}
-keepclassmembernames class net.mamoe.mirai.** {
    volatile <fields>;
}
-keepclassmembernames class ** extends kotlinx.serialization.internal.GeneratedSerializer

-keep enum net.mamoe.mirai.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#-keep class ** extends kotlin.internal.**
# Bouncy Castle
-keep class org.bouncycastle.jcajce.provider.** { *; }
-keep class org.bouncycastle.jce.provider.** { *; }
-keepnames class net.mamoe.mirai.Mirai
-keepnames class net.mamoe.mirai.MiraiImpl

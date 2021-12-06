package io.github.mzdluo123.mirai.android.utils

import android.content.Context
import android.os.Build
import io.github.mzdluo123.mirai.android.AppSettings
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.BuildConfig
import splitties.experimental.ExperimentalSplittiesApi
import java.text.SimpleDateFormat

class MiraiAndroidStatus (
    var miraiAndroidVersion:String,
    var coreVersion:String,
    var luaMiraiVersion:String,
    var releaseVersion:String,
    var sdkVersion:Int,
    var memorySize:String,
    var netType:String,
    var startTime:String,
    var logBuffer:Int
) {
    @ExperimentalSplittiesApi
    @ExperimentalUnsignedTypes
    companion object {
        var startTime: Long = 0
        fun recentStatus(context: Context = BotApplication.context): MiraiAndroidStatus =
            MiraiAndroidStatus(
                context.packageManager.getPackageInfo(context.packageName, 0).versionName,
                BuildConfig.COREVERSION,
                BuildConfig.LUAMIRAI_VERSION,
                Build.VERSION.RELEASE,
                Build.VERSION.SDK_INT,
                DeviceStatus.getSystemAvaialbeMemorySize(context.applicationContext),
                DeviceStatus.getCurrentNetType(context.applicationContext),
                SimpleDateFormat.getDateTimeInstance().format(startTime),
                AppSettings.logBuffer
            )
    }

    fun format():String = buildString{
        append("MiraiAndroid v")
        append(miraiAndroidVersion)
        append("\n")

        append("MiraiCore v")
        append(coreVersion)
        append("\n")

        append("LuaMirai v")
        append(luaMiraiVersion)
        append("\n")

        append("系统版本 ")
        append(releaseVersion)
        append(" SDK ")
        append(sdkVersion)
        append("\n")

        append("内存可用 ")
        append(memorySize)
        append("\n")

        append("网络 ")
        append(netType)
        append("\n")

        append("启动时间 ")
        append(startTime)
        append("\n")

        append("日志缓存行数 ")
        append(logBuffer)
    }
}
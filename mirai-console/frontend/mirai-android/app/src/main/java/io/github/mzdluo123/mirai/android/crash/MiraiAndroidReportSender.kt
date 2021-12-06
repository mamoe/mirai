package io.github.mzdluo123.mirai.android.crash

import android.content.Context
import org.acra.data.CrashReportData
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderException
import java.io.File
import java.io.FileWriter

//崩溃日志处理
class MiraiAndroidReportSender() : ReportSender {
    @Throws(ReportSenderException::class)
    override fun send(
        context: Context,
        report: CrashReportData
    ) {
        val outFile = File(context.getExternalFilesDir("crash"), "crashdata")
        FileWriter(outFile).also {
            it.write(report.toJSON())
        }.close()
    }
}
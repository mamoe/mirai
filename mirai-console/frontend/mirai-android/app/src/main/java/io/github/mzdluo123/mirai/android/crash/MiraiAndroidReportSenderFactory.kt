package io.github.mzdluo123.mirai.android.crash

import android.content.Context
import org.acra.config.CoreConfiguration
import org.acra.sender.ReportSender
import org.acra.sender.ReportSenderFactory

class MiraiAndroidReportSenderFactory : ReportSenderFactory {
    override fun create(
        context: Context,
        config: CoreConfiguration
    ): ReportSender = MiraiAndroidReportSender()

    override fun enabled(config: CoreConfiguration): Boolean = true

}
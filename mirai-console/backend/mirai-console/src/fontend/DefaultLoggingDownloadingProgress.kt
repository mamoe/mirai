/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.fontend

import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.util.ConsoleExperimentalApi

/**
 * [DownloadingProgress] 的简单实现, 前端应该自行实现 [DownloadingProgress]
 *
 * 此类为前端未实现 [DownloadingProgress] 时的缺省实现
 */
@ConsoleExperimentalApi
public class DefaultLoggingDownloadingProgress : DownloadingProgress {
    private var message: String = ""
    private var lastDisplay = 0L
    private var changed: Boolean = false
    private var failed: Boolean = false

    private companion object {
        private val logger by lazy { MiraiConsole.createLogger("DownloadingProgress") }
    }

    override fun updateText(txt: String) {
        this.message = txt
        changed = true
    }

    override fun initProgress(totalSize: Long) {
    }

    override fun updateProgress(processed: Long) {
    }

    override fun updateProgress(processed: Long, totalSize: Long) {
    }

    override fun markFailed() {
        failed = true
    }

    override fun rerender() {
        if (!changed) return
        changed = false
        val crtTime = System.currentTimeMillis()
        if (crtTime - lastDisplay < 1000) {
            return
        }
        lastDisplay = crtTime
        if (failed) {
            logger.error(message)
        } else {
            logger.info(message)
        }
    }

    override fun dispose() {
        if (failed) {
            logger.error(message)
        } else {
            logger.info(message)
        }
        changed = false
        message = ""
    }
}
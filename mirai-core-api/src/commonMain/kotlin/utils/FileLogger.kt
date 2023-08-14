/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

internal fun getCurrentDay() = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
internal fun getCurrentDate() = SimpleDateFormat("yyyy-MM-dd").format(Date())

private val STUB: (priority: SimpleLogger.LogPriority, message: String?, e: Throwable?) -> Unit =
    { _: SimpleLogger.LogPriority, _: String?, _: Throwable? -> error("stub") }

/**
 * 将日志写入('append')到特定文件夹中的文件. 每日日志独立保存.
 *
 * @see PlatformLogger 查看格式信息
 */
public class DirectoryLogger @JvmOverloads constructor(
    identity: String,
    private val directory: File = File(identity),
    /**
     * 保留日志文件多长时间. 毫秒数
     */
    private val retain: Long = 1.weeksToMillis
) : SimpleLogger("", STUB) {
    init {
        directory.mkdirs()
    }

    private fun checkOutdated() {
        val current = currentTimeMillis()
        directory.walk().filter(File::isFile).filter { current - it.lastModified() > retain }.forEach {
            it.delete()
        }
    }

    private var day = getCurrentDay()

    private var delegate: SingleFileLogger = SingleFileLogger(identity, File(directory, "${getCurrentDate()}.log"))
        get() {
            val currentDay = getCurrentDay()
            if (day != currentDay) {
                day = currentDay
                checkOutdated()
                field = SingleFileLogger(identity!!, File(directory, "${getCurrentDate()}.log"))
            }
            return field
        }

    override val logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit =
        { priority: LogPriority, message: String?, e: Throwable? ->
            delegate.call(priority, message, e)
        }
}
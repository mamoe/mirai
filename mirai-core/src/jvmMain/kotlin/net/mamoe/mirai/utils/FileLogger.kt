/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private val currentDay get() = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
private val currentDate get() = SimpleDateFormat("yyyy-MM-dd").format(Date())

/**
 * 将日志写入('append')到特定文件.
 *
 * @see PlatformLogger 查看格式信息
 */
public class SingleFileLogger @JvmOverloads constructor(identity: String, file: File = File("$identity-$currentDate.log")) :
    PlatformLogger(identity, { file.appendText(it + "\n") }, false) {

    init {
        file.createNewFile()
        require(file.isFile) { "Log file must be a file: $file" }
        require(file.canWrite()) { "Log file must be write: $file" }
    }
}

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
        val current = currentTimeMillis
        directory.walk().filter(File::isFile).filter { current - it.lastModified() > retain }.forEach {
            it.delete()
        }
    }

    private var day = currentDay

    private var delegate: SingleFileLogger = SingleFileLogger(identity, File(directory, "$currentDate.log"))
        get() {
            val currentDay = currentDay
            if (day != currentDay) {
                day = currentDay
                checkOutdated()
                field = SingleFileLogger(identity!!, File(directory, "$currentDate.log"))
            }
            return field
        }

    override val logger: (priority: LogPriority, message: String?, e: Throwable?) -> Unit =
        { priority: LogPriority, message: String?, e: Throwable? ->
            delegate.call(priority, message, e)
        }
}
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.internal.utils.StdoutLogger

/**
 * 当前平台的默认的日志记录器.
 * - 在 _JVM 控制台_ 端的实现为 [println]
 * - 在 _Android_ 端的实现为 `android.util.Log`
 *
 *
 * 单条日志格式 (正则) 为:
 * ```regex
 * ^([\w-]*\s[\w:]*)\s(\w)\/(.*?):\s(.+)$
 * ```
 * 其中 group 分别为: 日期与时间, 严重程度, [identity], 消息内容.
 *
 * 示例:
 * ```log
 * 2020-05-21 19:51:09 V/Bot 123456789: Send: OidbSvc.0x88d_7
 * ```
 *
 * 日期时间格式为 `yyyy-MM-dd HH:mm:ss`,
 *
 * 严重程度为 V, I, W, E. 分别对应 verbose, info, warning, error
 *
 * @see MiraiLogger.create
 */
@MiraiInternalApi
public actual open class PlatformLogger actual constructor(identity: String?) :
    MiraiLoggerPlatformBase(), MiraiLogger {

    private val delegate = StdoutLogger(identity)

    override val identity: String? get() = delegate.identity
    override val isEnabled: Boolean get() = delegate.isEnabled
    override val isVerboseEnabled: Boolean get() = delegate.isVerboseEnabled
    override val isDebugEnabled: Boolean get() = delegate.isDebugEnabled
    override val isInfoEnabled: Boolean get() = delegate.isInfoEnabled
    override val isWarningEnabled: Boolean get() = delegate.isWarningEnabled
    override val isErrorEnabled: Boolean get() = delegate.isErrorEnabled

    override fun verbose0(message: String?, e: Throwable?) {
        delegate.verbose0(message, e)
    }

    override fun debug0(message: String?, e: Throwable?) {
        delegate.debug(message, e)
    }

    override fun info0(message: String?, e: Throwable?) {
        delegate.info0(message, e)
    }

    override fun warning0(message: String?, e: Throwable?) {
        delegate.warning(message, e)
    }

    override fun error0(message: String?, e: Throwable?) {
        delegate.error0(message, e)
    }
}
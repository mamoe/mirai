/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

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
    MiraiLoggerPlatformBase() {
    override val identity: String?
        get() = TODO("Not yet implemented")

    override fun verbose0(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun debug0(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun info0(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun warning0(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }

    override fun error0(message: String?, e: Throwable?) {
        TODO("Not yet implemented")
    }
}
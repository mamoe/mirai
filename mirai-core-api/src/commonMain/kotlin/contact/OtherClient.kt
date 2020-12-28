/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.OtherClientOnlineEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PAD
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PHONE
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 其他设备. 如当 [Bot] 以 [ANDROID_PHONE] 登录时, 还可以有其他设备以 [ANDROID_PAD], iOS, PC 或其他设备登录.
 */
public interface OtherClient : Contact {
    public val info: OtherClientInfo

    /**
     * 此设备属于的 [Bot]
     */
    public override val bot: Bot

    /**
     * 与 [Bot.id] 相同
     */
    public override val id: Long get() = bot.id

    override suspend fun sendMessage(message: Message): MessageReceipt<OtherClient> {
        throw UnsupportedOperationException("OtherClientImpl.sendMessage is not yet supported.")
    }

    override suspend fun uploadImage(resource: ExternalResource): Image {
        throw UnsupportedOperationException("OtherClientImpl.uploadImage is not yet supported.")
    }
}

@MiraiInternalApi
public inline val OtherClient.appId: Int
    get() = info.appId
public inline val OtherClient.platform: Platform get() = info.platform
public inline val OtherClient.deviceName: String get() = info.deviceName
public inline val OtherClient.deviceKind: String get() = info.deviceKind

@MiraiExperimentalApi
public data class OtherClientInfo @MiraiInternalApi constructor(

    /**
     * 仅运行时识别. 随着客户端更新此 ID 可能有变化.
     *
     * 不可能有 [appId] 相同的两个客户端t在线.
     */
    public val appId: Int,

    /**
     * 登录平台
     */
    public val platform: Platform,

    /**
     * 示例：
     * - Mi 10 Pro
     * - 电脑
     * - xxx 的 iPad
     * - mirai
     */
    public val deviceName: String,

    /**
     * 示例：
     * - Mi 10 Pro
     * - DESKTOP-ABCDEFG
     * - iPad
     * - mirai
     */
    public val deviceKind: String,
)

/**
 * @see OtherClientInfo.platform
 */
public enum class Platform(
    @MiraiInternalApi public val terminalId: Int,
    @MiraiInternalApi public val platformId: Int,
) {
    IOS(3, 1),
    MOBILE(2, 2), // android
    WINDOWS(1, 3),

    UNKNOWN(0, 0)
    ;

    public companion object {
        @MiraiInternalApi
        public fun getByTerminalId(terminalId: Int): Platform? = values().find { it.terminalId == terminalId }
    }
}

/**
 * 详细设备类型. 在登录时查询到的设备列表中无此信息. 只在 [OtherClientOnlineEvent] 才有.
 */
public enum class ClientKind(
    @MiraiInternalApi public val id: Int,
) {
    ANDROID_PAD(68104),
    AOL_CHAOJIHUIYUAN(73730),
    AOL_HUIYUAN(73474),
    AOL_SQQ(69378),
    CAR(65806),
    HRTX_IPHONE(66566),
    HRTX_PC(66561),
    MC_3G(65795),
    MISRO_MSG(69634),
    MOBILE_ANDROID(65799),
    MOBILE_ANDROID_NEW(72450),
    MOBILE_HD(65805),
    MOBILE_HD_NEW(71426),
    MOBILE_IPAD(68361),
    MOBILE_IPAD_NEW(72194),
    MOBILE_IPHONE(67586),
    MOBILE_OTHER(65794),
    MOBILE_PC(65793),
    MOBILE_WINPHONE_NEW(72706),
    QQ_FORELDER(70922),
    QQ_SERVICE(71170),
    TV_QQ(69130),
    WIN8(69899),
    WINPHONE(65804);

    public companion object {
        @MiraiInternalApi
        public operator fun get(id: Int): ClientKind? = values().find { it.id == id }
    }
}

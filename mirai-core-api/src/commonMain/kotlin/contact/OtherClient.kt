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
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PAD
import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol.ANDROID_PHONE
import net.mamoe.mirai.utils.ExternalImage

/**
 * 其他设备. 如当 [Bot] 以 [ANDROID_PHONE] 登录时, 还可以有其他设备以 [ANDROID_PAD], iOS, PC 或其他设备登录.
 */
public interface OtherClient : Contact {
    /**
     * 设备类型
     */
    public val kind: ClientKind

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

    override suspend fun uploadImage(image: ExternalImage): Image {
        throw UnsupportedOperationException("OtherClientImpl.uploadImage is not yet supported.")
    }
}

/**
 * 设备类型
 */
public enum class ClientKind(
    public val id: Int,
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
    WINPHONE(65804),

    UNKNOWN(-1);

    public companion object {
        public operator fun get(id: Int): ClientKind? = values().find { it.id == id }
    }
}

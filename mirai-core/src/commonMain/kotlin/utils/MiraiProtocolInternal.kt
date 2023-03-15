/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.BotConfiguration.MiraiProtocol
import net.mamoe.mirai.utils.EnumMap
import kotlin.jvm.JvmField

internal class MiraiProtocolInternal(
    @JvmField internal val apkId: String,
    @JvmField internal val id: Long,
    @JvmField internal val ver: String,
    @JvmField internal val sdkVer: String,
    @JvmField internal val miscBitMap: Int,
    @JvmField internal val subSigMap: Int,
    @JvmField internal val mainSigMap: Int,
    @JvmField internal val sign: String,
    @JvmField internal val buildTime: Long,
    @JvmField internal val ssoVersion: Int,
    @JvmField internal val supportsQRLogin: Boolean,
) {
    internal companion object {
        internal val protocols = EnumMap<MiraiProtocol, MiraiProtocolInternal>(MiraiProtocol::class)

        operator fun get(protocol: MiraiProtocol): MiraiProtocolInternal =
            protocols[protocol] ?: error("Internal Error: Missing protocol $protocol")

        init {
            //Updated from MiraiGo (2023/3/7)
            protocols[MiraiProtocol.ANDROID_PHONE] = MiraiProtocolInternal(
                apkId = "com.tencent.mobileqq",
                id = 537151682,
                ver = "8.9.33.10335",
                sdkVer = "6.0.0.2534",
                miscBitMap = 150470524,
                subSigMap = 0x10400,
                mainSigMap = 16724722,
                sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
                buildTime = 1673599898L,
                ssoVersion = 19,
                supportsQRLogin = false,
            )
            //Updated from MiraiGo (2023/3/7)
            protocols[MiraiProtocol.ANDROID_PAD] = MiraiProtocolInternal(
                apkId = "com.tencent.mobileqq",
                id = 537151218,
                ver = "8.9.33.10335",
                sdkVer = "6.0.0.2534",
                miscBitMap = 150470524,
                subSigMap = 0x10400,
                mainSigMap = 16724722,
                sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
                buildTime = 1673599898L,
                ssoVersion = 19,
                supportsQRLogin = false,
            )
            protocols[MiraiProtocol.ANDROID_WATCH] = MiraiProtocolInternal(
                apkId = "com.tencent.qqlite",
                id = 537064446,
                ver = "2.0.5",
                sdkVer = "6.0.0.236",
                miscBitMap = 16252796,
                subSigMap = 0x10400,
                mainSigMap = 34869472,
                sign = "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
                buildTime = 1559564731L,
                ssoVersion = 5,
                supportsQRLogin = true,
            )
            protocols[MiraiProtocol.IPAD] = MiraiProtocolInternal(
                apkId = "com.tencent.minihd.qq",
                id = 537151363,
                ver = "8.9.33.614",
                sdkVer = "6.0.0.2433",
                miscBitMap = 150470524,
                subSigMap = 66560,
                mainSigMap = 1970400,
                sign = "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
                buildTime = 1640921786L,
                ssoVersion = 12,
                supportsQRLogin = false,
            )
            //Updated from MiraiGo (2023/3/15)
            protocols[MiraiProtocol.MACOS] = MiraiProtocolInternal(
                apkId = "com.tencent.qq",
                id = 537128930,
                ver = "5.8.9",
                sdkVer = "6.0.0.2433",
                miscBitMap = 150470524,
                subSigMap = 66560,
                mainSigMap = 1970400,
                sign = "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
                buildTime = 1595836208L,
                ssoVersion = 12,
                supportsQRLogin = true,
            )
        }

        inline val MiraiProtocol.asInternal: MiraiProtocolInternal get() = get(this)
    }
}

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
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
) {
    internal companion object {
        internal val protocols = EnumMap<MiraiProtocol, MiraiProtocolInternal>(MiraiProtocol::class)

        operator fun get(protocol: MiraiProtocol): MiraiProtocolInternal =
            protocols[protocol] ?: error("Internal Error: Missing protocol $protocol")

        init {
            protocols[MiraiProtocol.ANDROID_PHONE] = MiraiProtocolInternal(
                "com.tencent.mobileqq",
                537066978,
                "8.5.5",
                "6.0.0.2463",
                150470524,
                0x10400,
                16724722,
                "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
                1609322643L,
                15,
            )
            protocols[MiraiProtocol.ANDROID_PAD] = MiraiProtocolInternal(
                "com.tencent.mobileqq",
                537062409, "8.4.18",
                "6.0.0.2454",
                184024956,
                0x10400,
                34869472,
                "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
                1604580615L,
                15,
            )
            protocols[MiraiProtocol.ANDROID_WATCH] = MiraiProtocolInternal(
                "com.tencent.mobileqq",
                537061176,
                "8.2.7",
                "6.0.0.2413",
                184024956,
                0x10400,
                34869472,
                "A6 B7 45 BF 24 A2 C2 77 52 77 16 F6 F3 6E B6 8D",
                1571193922L,
                15,
            )
            protocols[MiraiProtocol.IPAD] = MiraiProtocolInternal(
                "com.tencent.minihd.qq",
                537065739,
                "5.8.9",
                "6.0.0.2433",
                150470524,
                66560,
                1970400,
                "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
                1595836208L,
                12,
            )
            protocols[MiraiProtocol.MACOS] = MiraiProtocolInternal(
                "com.tencent.minihd.qq",
                537064315,
                "5.8.9",
                "6.0.0.2433",
                150470524,
                66560,
                1970400,
                "AA 39 78 F4 1F D9 6F F9 91 4A 66 9E 18 64 74 C7",
                1595836208L,
                12,
            )
        }
    }
}

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmField

@SinceMirai("0.31.0")
sealed class HummerMessage : MessageContent {
    companion object Key : Message.Key<HummerMessage>
}

/**
 * 戳一戳
 */
@SinceMirai("0.31.0")
@OptIn(MiraiInternalAPI::class)
class PokeMessage @MiraiInternalAPI(message = "使用伴生对象中的常量") constructor(
    @MiraiExperimentalAPI
    val type: Int,
    @MiraiExperimentalAPI
    val id: Int
) : HummerMessage() {
    companion object Types : Message.Key<PokeMessage> {
        /** 戳一戳 */
        @JvmField
        val Poke = PokeMessage(1, -1)

        /** 比心 */
        @JvmField
        val ShowLove = PokeMessage(2, -1)

        /** 点赞  */
        @JvmField
        val Like = PokeMessage(3, -1)

        /** 心碎 */
        @JvmField
        val Heartbroken = PokeMessage(4, -1)

        /** 666 */
        @JvmField
        val SixSixSix = PokeMessage(5, -1)

        /** 放大招 */
        @JvmField
        val FangDaZhao = PokeMessage(6, -1)
    }

    @OptIn(MiraiExperimentalAPI::class)
    private val stringValue = "[mirai:poke:$type,$id]"

    override fun toString(): String = stringValue
    override val length: Int get() = stringValue.length
    override fun get(index: Int): Char = stringValue[index]
    override fun subSequence(startIndex: Int, endIndex: Int): CharSequence =
        stringValue.subSequence(startIndex, endIndex)

    override fun compareTo(other: String): Int = stringValue.compareTo(other)

    //businessType=0x00000001(1)
    //pbElem=08 01 18 00 20 FF FF FF FF 0F 2A 00 32 00 38 00 50 00
    //serviceType=0x00000002(2)
}
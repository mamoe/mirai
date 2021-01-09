/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "NOTHING_TO_INLINE")

package net.mamoe.mirai.message.code

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.internal.parseMiraiCodeImpl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChain.Companion.parseFromMiraiCode
import net.mamoe.mirai.utils.safeCast

/**
 * Mirai 码相关操作.
 */
public object MiraiCode {
    /**
     * 解析形如 "[mirai:]" 的 mirai 码, 即 [CodableMessage.toMiraiCode] 返回的内容.
     * @see MessageChain.parseFromMiraiCode
     */
    @JvmName("parseMiraiCode1")
    @JvmSynthetic
    public inline fun String.parseMiraiCode(contact: Contact? = null): MessageChain = parseMiraiCode(this, contact)

    /**
     * 解析形如 "[mirai:]" 的 mirai 码, 即 [CodableMessage.toMiraiCode] 返回的内容.
     * @see MessageChain.parseFromMiraiCode
     */
    @JvmOverloads
    @JvmStatic
    public fun parseMiraiCode(code: String, contact: Contact? = null): MessageChain = code.parseMiraiCodeImpl(contact)

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Iterable<Message>.toMiraiCode(): String = iterator().toMiraiCode()

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Sequence<Message>.toMiraiCode(): String = iterator().toMiraiCode()

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Array<out Message>.toMiraiCode(): String = iterator().toMiraiCode()

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Iterator<Message>.toMiraiCode(): String = buildString {
        this@toMiraiCode.forEach {
            it.safeCast<CodableMessage>()?.appendMiraiCodeTo(this)
        }
    }

    /**
     * 转换得到 mirai 码.
     * @see CodableMessage.toMiraiCode
     */
    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")// for better Java API.
    @JvmStatic
    public fun CodableMessage.toMiraiCode(): String = this.toMiraiCode() // member function
}

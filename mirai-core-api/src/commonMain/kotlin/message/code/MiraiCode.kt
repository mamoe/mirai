/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.message.code

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.internal.parseMiraiCodeImpl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageChain.Companion.deserializeFromMiraiCode
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.safeCast
import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads
import kotlin.jvm.JvmStatic
import kotlin.jvm.JvmSynthetic

/**
 * Mirai 码相关操作.
 *
 * 可在 GitHub 查看 [详细文档](https://github.com/mamoe/mirai/blob/dev/docs/Messages.md#mirai-%E7%A0%81).
 */
public object MiraiCode {
    /**
     * 解析形如 "[mirai:]" 的 mirai 码, 即 [CodableMessage.serializeToMiraiCode] 返回的内容.
     * @see MessageChain.deserializeFromMiraiCode
     */
    @JvmName("parseMiraiCode1")
    @JvmSynthetic
    public fun String.deserializeMiraiCode(contact: Contact? = null): MessageChain =
        deserializeMiraiCode(this, contact)

    /**
     * 解析形如 "[mirai:]" 的 mirai 码, 即 [CodableMessage.serializeToMiraiCode] 返回的内容.
     * @see MessageChain.deserializeFromMiraiCode
     */
    @JvmOverloads
    @JvmStatic
    public fun deserializeMiraiCode(code: String, contact: Contact? = null): MessageChain =
        code.parseMiraiCodeImpl(contact)

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Iterable<Message>.serializeToMiraiCode(): String = iterator().serializeToMiraiCode()

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Sequence<Message>.serializeToMiraiCode(): String = iterator().serializeToMiraiCode()

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Array<out Message>.serializeToMiraiCode(): String = iterator().serializeToMiraiCode()

    /**
     * 转换得到 mirai 码.
     */
    @JvmStatic
    public fun Iterator<Message>.serializeToMiraiCode(): String = buildString {
        @OptIn(MiraiExperimentalApi::class)
        this@serializeToMiraiCode.forEach {
            it.safeCast<CodableMessage>()?.appendMiraiCodeTo(this)
        }
    }

    /**
     * 转换得到 mirai 码.
     * @see CodableMessage.serializeToMiraiCode
     */
    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")// for better Java API.
    @JvmStatic
    public fun CodableMessage.serializeToMiraiCode(): String = this.serializeToMiraiCode() // member function
}

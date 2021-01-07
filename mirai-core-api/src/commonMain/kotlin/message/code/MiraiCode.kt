/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiCode")
@file:Suppress("unused")

package net.mamoe.mirai.message.code

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.code.internal.parseMiraiCodeImpl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.safeCast

/**
 * 解析形如 "[mirai:]" 的 mirai 码, 即 [Message.toString] 返回的内容.
 */
@JvmOverloads
public fun String.parseMiraiCode(contact: Contact? = null): MessageChain = parseMiraiCodeImpl(contact)

public fun Iterable<Message>.toMiraiCode(): String = iterator().toMiraiCode()
public fun Sequence<Message>.toMiraiCode(): String = iterator().toMiraiCode()
public fun Array<out Message>.toMiraiCode(): String = iterator().toMiraiCode()
public fun Iterator<Message>.toMiraiCode(): String = buildString {
    this@toMiraiCode.forEach {
        it.safeCast<CodableMessage>()?.appendMiraiCodeTo(this)
    }
}

@Suppress("EXTENSION_SHADOWED_BY_MEMBER")// for better Java API.
public fun CodableMessage.toMiraiCode(): String = this.toMiraiCode() // member function

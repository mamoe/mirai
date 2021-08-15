/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.internal.contact

import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.User
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.utils.childScopeContext
import kotlin.contracts.contract
import kotlin.coroutines.CoroutineContext

internal sealed class AbstractContact(
    final override val bot: QQAndroidBot,
    parentCoroutineContext: CoroutineContext,
) : Contact {
    final override val coroutineContext: CoroutineContext = parentCoroutineContext.childScopeContext()
}

internal inline fun Contact.impl(): AbstractContact {
    contract { returns() implies (this@impl is AbstractContact) }
    return this as AbstractContact
}

internal val Contact.groupCodeOrNull: Long? get() = if (this is Group) this.groupCode else null
internal val Contact.groupUinOrNull: Long? get() = if (this is Group) this.uin else null
internal val Contact.userIdOrNull: Long? get() = if (this is User) this.id else null
internal val Contact.uin: Long get() = if (this is Group) this.uin else this.id
/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.contact.ContactList
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import kotlin.jvm.JvmField

/**
 * 群资料
 */
@Suppress("MemberVisibilityCanBePrivate") // 将来使用
class GroupInfo(
    @JvmField internal var _group: Group,
    @JvmField internal var _owner: Member,
    @JvmField internal var _name: String,
    @JvmField internal var _announcement: String,
    @JvmField internal var _members: ContactList<Member>
) {
    val group: Group get() = _group
    val owner: Member get() = _owner
    val name: String get() = _name
    val announcement: String get() = _announcement
    val members: ContactList<Member> get() = _members

    override fun toString(): String =
        "GroupInfo(id=${group.id}, owner=$owner, name=$name, announcement=$announcement, members=${members.idContentString}"
}

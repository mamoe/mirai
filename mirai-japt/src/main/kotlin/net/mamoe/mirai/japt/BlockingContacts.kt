@file:Suppress("NOTHING_TO_INLINE", "unused")

package net.mamoe.mirai.japt

import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.QQ

inline fun Group.blocking(): BlockingGroup = BlockingContacts.createBlocking(this)
inline fun QQ.blocking(): BlockingQQ = BlockingContacts.createBlocking(this)
inline fun Member.blocking(): BlockingMember = BlockingContacts.createBlocking(this)
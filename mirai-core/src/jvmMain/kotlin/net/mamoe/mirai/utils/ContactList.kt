package net.mamoe.mirai.utils

import net.mamoe.mirai.contact.Contact

/**
 * @author Him188moe
 */
class ContactList<C : Contact> : MiraiSynchronizedLinkedHashMap<Long, C>()
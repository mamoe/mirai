package net.mamoe.mirai.utils

import net.mamoe.mirai.contact.Contact


class ContactList<C : Contact> : MutableMap<Long, C> by mutableMapOf()
@file:Suppress("EXPERIMENTAL_API_USAGE")

package net.mamoe.mirai.utils

import net.mamoe.mirai.contact.Contact


class ContactList<C : Contact> : MutableMap<UInt, C> by mutableMapOf()
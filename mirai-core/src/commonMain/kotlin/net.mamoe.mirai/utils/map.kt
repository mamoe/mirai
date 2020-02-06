@file:Suppress("NOTHING_TO_INLINE")
@file:JvmMultifileClass
@file:JvmName("Utils")


package net.mamoe.mirai.utils

import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

inline fun <K, V> Map<K, V>.firstValue(): V = this.entries.first().value

inline fun <K, V> Map<K, V>.firstValueOrNull(): V? = this.entries.firstOrNull()?.value

inline fun <K, V> Map<K, V>.firstKey(): K = this.entries.first().key

inline fun <K, V> Map<K, V>.firstKeyOrNull(): K? = this.entries.firstOrNull()?.key
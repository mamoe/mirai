package net.mamoe.mirai.utils

fun <K, V> Map<K, V>.firstValue(): V = this.entries.first().value

fun <K, V> Map<K, V>.firstValueOrNull(): V? = this.entries.firstOrNull()?.value

fun <K, V> Map<K, V>.firstKey(): K = this.entries.first().key

fun <K, V> Map<K, V>.firstKeyOrNull(): K? = this.entries.firstOrNull()?.key
/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.plugin

import kotlinx.serialization.Serializable
import java.util.concurrent.ConcurrentHashMap

@Serializable
class ConfigSection() : ConcurrentHashMap<String, Any>() {

    fun getString(key: String): String {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString()
    }

    fun getInt(key: String): Int {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toInt()
    }

    fun getFloat(key: String): Float {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toFloat()
    }

    fun getDouble(key: String): Double {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toDouble()
    }

    fun getLong(key: String): Long {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toLong()
    }

    fun getConfigSection(key: String): ConfigSection {
        return (get(key) ?: error("ConfigSection does not contain $key ")) as ConfigSection
    }

    fun getStringList(key: String): List<String> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString() }
    }

    fun getIntList(key: String): List<Int> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toInt() }
    }

    fun getFloatList(key: String): List<Float> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toFloat() }
    }

    fun getDoubleList(key: String): List<Double> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toDouble() }
    }

    fun getLongList(key: String): List<Long> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toLong() }
    }


}
/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.plugin

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.isSubclassOf

/**
 * TODO: support all config types
 */

interface Config {
    fun getConfigSection(key: String): ConfigSection
    fun getString(key: String): String
    fun getInt(key: String): Int
    fun getFloat(key: String): Float
    fun getDouble(key: String): Double
    fun getLong(key: String): Long
    fun getList(key: String): List<*>
    fun getStringList(key: String): List<String>
    fun getIntList(key: String): List<Int>
    fun getFloatList(key: String): List<Float>
    fun getDoubleList(key: String): List<Double>
    fun getLongList(key: String): List<Long>
    operator fun set(key: String, value: Any)
    operator fun get(key: String): Any?
    fun exist(key: String): Boolean
    fun asMap(): Map<String, Any>
}

inline fun <reified T : Any> Config.withDefault(crossinline defaultValue: () -> T): ReadWriteProperty<Any, T> {
    return object : ReadWriteProperty<Any, T> {
        override fun getValue(thisRef: Any, property: KProperty<*>): T {
            if (!this@withDefault.exist(property.name)) {
                return defaultValue.invoke()
            }
            return getValue(thisRef, property)
        }

        override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
            this@withDefault[property.name] = value
        }
    }
}

@Suppress("IMPLICIT_CAST_TO_ANY")
inline operator fun <reified T> ConfigSection.getValue(thisRef: Any?, property: KProperty<*>): T {
    return when (T::class) {
        String::class -> this.getString(property.name)
        Int::class -> this.getInt(property.name)
        Float::class -> this.getFloat(property.name)
        Double::class -> this.getDouble(property.name)
        Long::class -> this.getLong(property.name)
        else -> when {
            T::class.isSubclassOf(ConfigSection::class) -> this.getConfigSection(property.name)
            T::class == List::class || T::class == MutableList::class -> {
                val list = this.getList(property.name)
                return if (list.isEmpty()) {
                    list
                } else {
                    when (list[0]!!::class) {
                        String::class -> getStringList(property.name)
                        Int::class -> getIntList(property.name)
                        Float::class -> getFloatList(property.name)
                        Double::class -> getDoubleList(property.name)
                        Long::class -> getLongList(property.name)
                        else -> {
                            error("unsupported type")
                        }
                    }
                } as T
            }
            else -> {
                error("unsupported type")
            }
        }
    } as T
}

inline operator fun <reified T> ConfigSection.setValue(thisRef: Any?, property: KProperty<*>, value: T) {
    this[property.name] = value!!
}


interface ConfigSection : Config {
    override fun getConfigSection(key: String): ConfigSection {
        return (get(key) ?: error("ConfigSection does not contain $key ")) as ConfigSection
    }

    override fun getString(key: String): String {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString()
    }

    override fun getInt(key: String): Int {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toInt()
    }

    override fun getFloat(key: String): Float {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toFloat()
    }

    override fun getDouble(key: String): Double {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toDouble()
    }

    override fun getLong(key: String): Long {
        return (get(key) ?: error("ConfigSection does not contain $key ")).toString().toLong()
    }

    override fun getList(key: String): List<*> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>)
    }

    override fun getStringList(key: String): List<String> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString() }
    }

    override fun getIntList(key: String): List<Int> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toInt() }
    }

    override fun getFloatList(key: String): List<Float> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toFloat() }
    }

    override fun getDoubleList(key: String): List<Double> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toDouble() }
    }

    override fun getLongList(key: String): List<Long> {
        return ((get(key) ?: error("ConfigSection does not contain $key ")) as List<*>).map { it.toString().toLong() }
    }

    override operator fun set(key: String, value: Any) {
        this[key] = value
    }

}

@Serializable
open class ConfigSectionImpl() : ConcurrentHashMap<String, Any>(), ConfigSection {
    override operator fun get(key: String): Any? {
        return super.get(key)
    }

    override fun exist(key: String): Boolean {
        return containsKey(key)
    }

    override fun asMap(): Map<String, Any> {
        return this
    }
}


interface FileConfig {

}

@Serializable
abstract class FileConfigImpl internal constructor() : ConfigSectionImpl(), FileConfig {

}

@Serializable
class JsonConfig internal constructor() : FileConfigImpl() {

    companion object {
        @UnstableDefault
        fun load(file: File): Config {
            require(file.extension.toLowerCase() == "json")
            val content = file.apply {
                if (!this.exists()) this.createNewFile()
            }.readText()

            if (content.isEmpty() || content.isBlank()) {
                return JsonConfig()
            }
            return Json.parse(
                JsonConfig.serializer(),
                content
            )
        }

        @UnstableDefault
        fun save(file: File, config: JsonConfig) {
            require(file.extension.toLowerCase() == "json")
            val content = Json.stringify(
                JsonConfig.serializer(),
                config
            )
            file.apply {
                if (!this.exists()) this.createNewFile()
            }.writeText(content)
        }
    }
}
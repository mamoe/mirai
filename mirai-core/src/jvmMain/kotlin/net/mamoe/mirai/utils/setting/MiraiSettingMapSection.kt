package net.mamoe.mirai.utils.setting


import org.ini4j.Profile

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.stream.Collectors
import kotlin.streams.toList


/**
 * @author NaturalHG
 */
class MiraiSettingMapSection : ConcurrentHashMap<String, Any>(), MiraiSettingSection {

    operator fun <T> get(key: String?, defaultValue: T): T {
        if (key == null || key.isEmpty()) {
            return defaultValue
        }
        return if (super.containsKey(key)) {
            super.get(key) as T
        } else defaultValue
    }


    operator fun set(key: String, value: Any) {
        this[key] = value
    }

    override fun remove(key: String) {
        super.remove(key)
    }

    fun getInt(key: String): Int {
        return this.getInt(key, 0)
    }

    fun getInt(key: String, defaultValue: Int): Int {
        return Integer.parseInt(this[key, defaultValue].toString())
    }

    fun getDouble(key: String): Double {
        return this.getDouble(key, 0.0)
    }

    fun getDouble(key: String, defaultValue: Double): Double {
        return java.lang.Double.parseDouble(this[key, defaultValue].toString())
    }

    fun getFloat(key: String): Float {
        return this.getFloat(key, 0f)
    }

    fun getFloat(key: String, defaultValue: Float): Float {
        return java.lang.Float.parseFloat(this[key, defaultValue].toString())
    }

    fun getString(key: String): String {
        return this.getString(key, "")
    }

    fun getString(key: String, defaultValue: String): String {
        return this[key, defaultValue].toString()
    }

    fun getObject(key: String): Any? {
        return this[key]
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> asList(): List<T> {
        return this.values.stream().map { a -> a as T }.toList()
    }

    @Synchronized
    override fun saveAsSection(section: Profile.Section) {
        section.clear()
        this.forEach{ key, value -> section.put(key, value) }
    }

    @Throws(IOException::class)
    override fun close() {

    }

}


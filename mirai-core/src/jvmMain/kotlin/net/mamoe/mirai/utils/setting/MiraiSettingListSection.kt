package net.mamoe.mirai.utils.setting


import org.ini4j.Profile
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock


/**
 * @author NaturalHG
 */
class MiraiSettingListSection : Vector<Any>(), MiraiSettingSection {
    private val lock = ReentrantLock()

    @Suppress("UNCHECKED_CAST")
    fun <T> getAs(index: Int): T {
        return super.get(index) as T
    }

    fun getInt(index: Int): Int {
        return this.getAs(index)
    }

    fun getDouble(index: Int): Int {
        return this.getAs(index)
    }

    fun getString(index: Int): Int {
        return this.getAs(index)
    }

    fun getFloat(index: Int): Int {
        return this.getAs(index)
    }

    @Synchronized
    override fun saveAsSection(section: Profile.Section) {
        section.clear()
        val integer = AtomicInteger(0)
        this.forEach { a -> section.put(integer.getAndAdd(1).toString(), a) }
    }

    @Throws(IOException::class)
    override fun close() {

    }

}

@file:Suppress("INVISIBLE_REFERENCE", "INVISIBLE_MEMBER")

package net.mamoe.mirai.console.setting

import kotlinx.atomicfu.locks.withLock
import kotlinx.serialization.*
import kotlinx.serialization.internal.getChecked
import net.mamoe.mirai.console.setting.internal.SettingSerializerMark
import java.io.Closeable
import java.io.File
import java.util.concurrent.locks.ReentrantLock

/**
 * [Setting] 存储方式
 */
interface SettingStorage {
    interface TrackedSetting : Closeable {
        fun save()
        fun update()

        override fun close()
    }

    fun trackOn(setting: Setting): TrackedSetting

    fun saveAll()
    fun updateAll()
}

class SingleFileSettingStorage(
    val file: File
) : SettingStorage {
    private val descriptor: MutableList<Setting> = ArrayList()
    private val updaterSerializer: KSerializer<SettingSerializerMark> = object : KSerializer<SettingSerializerMark> {
        override val descriptor: SerialDescriptor = SerialDescriptor("SingleFileSettingStorage") {
            TODO()
        }

        override fun deserialize(decoder: Decoder): SettingSerializerMark {
            TODO("Not yet implemented")
        }

        override fun serialize(encoder: Encoder, value: SettingSerializerMark) {
            TODO("Not yet implemented")
        }

    }

    init {
        require(file.isFile) { "file $file is not a file" }
        require(file.canRead()) { "file $file is not readable" }
    }

    override fun trackOn(setting: Setting): SettingStorage.TrackedSetting {
        TODO("Not yet implemented")
    }

    override fun saveAll() {
        TODO("Not yet implemented")
    }

    override fun updateAll() {
        TODO("Not yet implemented")
    }
}
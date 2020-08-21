/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.*
import net.mamoe.mirai.console.data.*
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import net.mamoe.mirai.console.internal.plugin.updateWhen
import net.mamoe.mirai.console.plugin.jvm.loadPluginData
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.yamlkt.Yaml
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation


/**
 * 链接自动保存的 [PluginData].
 * 当任一相关 [Value] 的值被修改时, 将在一段时间无其他修改时保存
 *
 * 若 [AutoSavePluginDataHolder.coroutineContext] 含有 [Job], 则 [AutoSavePluginData] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
 *
 * @see loadPluginData
 */
internal open class AutoSavePluginData(
    private val owner: AutoSavePluginDataHolder,
    internal val originPluginDataClass: KClass<out PluginData>
) :
    AbstractPluginData() {
    private lateinit var storage: PluginDataStorage

    override fun setStorage(storage: PluginDataStorage) {
        check(!this::storage.isInitialized) { "storage is already initialized" }
        this.storage = storage
    }

    @JvmField
    @Volatile
    internal var lastAutoSaveJob: Job? = null

    @JvmField
    @Volatile
    internal var currentFirstStartTime = atomic(0L)

    init {
        @OptIn(InternalCoroutinesApi::class)
        owner.coroutineContext[Job]?.invokeOnCompletion(true) { doSave() }
    }

    private val updaterBlock: suspend CoroutineScope.() -> Unit = {
        currentFirstStartTime.updateWhen({ it == 0L }, { currentTimeMillis })

        delay(owner.autoSaveIntervalMillis.first.coerceAtLeast(1000)) // for safety

        if (lastAutoSaveJob == this.coroutineContext[Job]) {
            doSave()
        } else {
            if (currentFirstStartTime.updateWhen(
                    { currentTimeMillis - it >= owner.autoSaveIntervalMillis.last },
                    { 0 })
            ) doSave()
        }
    }

    @Suppress("RedundantVisibilityModifier")
    @ConsoleInternalAPI
    public final override fun onValueChanged(value: Value<*>) {
        lastAutoSaveJob = owner.launch(block = updaterBlock)
    }

    private fun doSave() = storage.store(owner, this)
}

internal class MemoryPluginDataStorageImpl(
    private val onChanged: MemoryPluginDataStorage.OnChangedCallback
) : PluginDataStorage, MemoryPluginDataStorage,
    MutableMap<Class<out PluginData>, PluginData> by mutableMapOf() {

    internal inner class MemoryPluginDataImpl : AbstractPluginData() {
        @ConsoleInternalAPI
        override fun onValueChanged(value: Value<*>) {
            onChanged.onChanged(this@MemoryPluginDataStorageImpl, value)
        }

        override fun setStorage(storage: PluginDataStorage) {
            check(storage is MemoryPluginDataStorageImpl) { "storage is not MemoryPluginDataStorageImpl" }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : PluginData> load(holder: PluginDataHolder, dataClass: Class<T>): T = (synchronized(this) {
        this.getOrPut(dataClass) {
            dataClass.kotlin.run {
                objectInstance ?: createInstanceOrNull() ?: kotlin.run {
                    if (dataClass != PluginData::class.java) {
                        throw IllegalArgumentException(
                            "Cannot create PluginData instance. Make sure dataClass is PluginData::class.java or a Kotlin's object, " +
                                    "or has a constructor which either has no parameters or all parameters of which are optional"
                        )
                    }
                    MemoryPluginDataImpl()
                }
            }
        }
    } as T).also { it.setStorage(this) }

    override fun store(holder: PluginDataHolder, pluginData: PluginData) {
        synchronized(this) {
            this[pluginData::class.java] = pluginData
        }
    }
}

@Suppress("RedundantVisibilityModifier") // might be public in the future
internal open class MultiFilePluginDataStorageImpl(
    public final override val directory: File
) : PluginDataStorage, MultiFilePluginDataStorage {
    init {
        directory.mkdir()
    }

    public override fun <T : PluginData> load(holder: PluginDataHolder, dataClass: Class<T>): T =
        with(dataClass.kotlin) {
            @Suppress("UNCHECKED_CAST")
            val instance = objectInstance ?: this.createInstanceOrNull() ?: kotlin.run {
                require(dataClass == PluginData::class.java) {
                    "Cannot create PluginData instance. Make sure dataClass is PluginData::class.java or a Kotlin's object, " +
                            "or has a constructor which either has no parameters or all parameters of which are optional"
                }
                if (holder is AutoSavePluginDataHolder) {
                    AutoSavePluginData(holder, this) as T?
                } else null
            } ?: throw IllegalArgumentException(
                "Cannot create PluginData instance. Make sure 'holder' is a AutoSavePluginDataHolder, " +
                        "or 'data' is an object or has a constructor which either has no parameters or all parameters of which are optional"
            )

            val file = getPluginDataFile(holder, this)
            file.createNewFile()
            check(file.exists() && file.isFile && file.canRead()) { "${file.absolutePath} cannot be read" }
            val text = file.readText()
            if (text.isNotBlank()) {
                Yaml.default.decodeFromString(instance.updaterSerializer, file.readText())
            }
            instance
        }.also { it.setStorage(this) }

    protected open fun getPluginDataFile(holder: PluginDataHolder, clazz: KClass<*>): File = with(clazz) {
        val name = findASerialName()

        val dir = File(directory, holder.name)
        if (dir.isFile) {
            error("Target directory ${dir.path} for holder $holder is occupied by a file therefore data $qualifiedNameOrTip can't be saved.")
        }
        dir.mkdir()

        val file = File(directory, name)
        if (file.isDirectory) {
            error("Target file $file is occupied by a directory therefore data $qualifiedNameOrTip can't be saved.")
        }
        return file
    }

    @ConsoleExperimentalAPI
    public override fun store(holder: PluginDataHolder, pluginData: PluginData) {
        val file =
            getPluginDataFile(
                holder,
                if (pluginData is AutoSavePluginData) pluginData.originPluginDataClass else pluginData::class
            )

        if (file.exists() && file.isFile && file.canRead()) {
            file.writeText(Yaml.default.encodeToString(pluginData.updaterSerializer, Unit))
        }
    }
}

@JvmSynthetic
internal fun <T : Any> KClass<T>.createInstanceOrNull(): T? {
    val noArgsConstructor = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) }
        ?: return null

    return noArgsConstructor.callBy(emptyMap())
}

@JvmSynthetic
internal fun KClass<*>.findASerialName(): String =
    findAnnotation<SerialName>()?.value
        ?: qualifiedName
        ?: throw IllegalArgumentException("Cannot find a serial name for $this")
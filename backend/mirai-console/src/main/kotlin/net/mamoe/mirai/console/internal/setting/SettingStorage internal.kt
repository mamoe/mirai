/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.setting

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.internal.command.qualifiedNameOrTip
import net.mamoe.mirai.console.internal.plugin.updateWhen
import net.mamoe.mirai.console.plugin.jvm.loadSetting
import net.mamoe.mirai.console.setting.*
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.yamlkt.Yaml
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation


/**
 * 链接自动保存的 [Setting].
 * 当任一相关 [Value] 的值被修改时, 将在一段时间无其他修改时保存
 *
 * 若 [AutoSaveSettingHolder.coroutineContext] 含有 [Job], 则 [AutoSaveSetting] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
 *
 * @see loadSetting
 */
internal open class AutoSaveSetting(private val owner: AutoSaveSettingHolder) :
    AbstractSetting() {
    private lateinit var storage: SettingStorage

    override fun setStorage(storage: SettingStorage) {
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
        owner.coroutineContext[Job]?.invokeOnCompletion { doSave() }
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

internal class MemorySettingStorageImpl(
    private val onChanged: MemorySettingStorage.OnChangedCallback
) : SettingStorage, MemorySettingStorage,
    MutableMap<Class<out Setting>, Setting> by mutableMapOf() {

    internal inner class MemorySettingImpl : AbstractSetting() {
        @ConsoleInternalAPI
        override fun onValueChanged(value: Value<*>) {
            onChanged.onChanged(this@MemorySettingStorageImpl, value)
        }

        override fun setStorage(storage: SettingStorage) {
            check(storage is MemorySettingStorageImpl) { "storage is not MemorySettingStorageImpl" }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T = (synchronized(this) {
        this.getOrPut(settingClass) {
            settingClass.kotlin.run {
                objectInstance ?: createInstanceOrNull() ?: kotlin.run {
                    if (settingClass != Setting::class.java) {
                        throw IllegalArgumentException(
                            "Cannot create Setting instance. Make sure settingClass is Setting::class.java or a Kotlin's object, " +
                                    "or has a constructor which either has no parameters or all parameters of which are optional"
                        )
                    }
                    MemorySettingImpl()
                }
            }
        }
    } as T).also { it.setStorage(this) }

    override fun store(holder: SettingHolder, setting: Setting) {
        synchronized(this) {
            this[setting::class.java] = setting
        }
    }
}

@Suppress("RedundantVisibilityModifier") // might be public in the future
internal open class MultiFileSettingStorageImpl(
    public final override val directory: File
) : SettingStorage, MultiFileSettingStorage {
    public override fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T =
        with(settingClass.kotlin) {
            val file = getSettingFile(holder, this)

            @Suppress("UNCHECKED_CAST")
            val instance = objectInstance ?: this.createInstanceOrNull() ?: kotlin.run {
                require(settingClass == Setting::class.java) {
                    "Cannot create Setting instance. Make sure settingClass is Setting::class.java or a Kotlin's object, " +
                            "or has a constructor which either has no parameters or all parameters of which are optional"
                }
                if (holder is AutoSaveSettingHolder) {
                    AutoSaveSetting(holder) as T?
                } else null
            } ?: throw IllegalArgumentException(
                "Cannot create Setting instance. Make sure 'holder' is a AutoSaveSettingHolder, " +
                        "or 'setting' is an object or has a constructor which either has no parameters or all parameters of which are optional"
            )
            file.createNewFile()
            check(file.exists() && file.isFile && file.canRead()) { "${file.absolutePath} cannot be read" }
            Yaml.default.decodeFromString(instance.updaterSerializer, file.readText())
            instance
        }

    protected open fun getSettingFile(holder: SettingHolder, clazz: KClass<*>): File = with(clazz) {
        val name = findASerialName()

        val dir = File(directory, holder.name)
        if (dir.isFile) {
            error("Target directory ${dir.path} for holder $holder is occupied by a file therefore setting $qualifiedNameOrTip can't be saved.")
        }

        val file = File(directory, name)
        if (file.isDirectory) {
            error("Target file $file is occupied by a directory therefore setting $qualifiedNameOrTip can't be saved.")
        }
        return file
    }

    @ConsoleExperimentalAPI
    public override fun store(holder: SettingHolder, setting: Setting): Unit = with(setting::class) {
        val file = getSettingFile(holder, this)

        if (file.exists() && file.isFile && file.canRead()) {
            file.writeText(Yaml.default.encodeToString(setting.updaterSerializer, Unit))
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
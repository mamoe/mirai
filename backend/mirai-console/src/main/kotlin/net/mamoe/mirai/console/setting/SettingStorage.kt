@file:Suppress("NOTHING_TO_INLINE")

package net.mamoe.mirai.console.setting

import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.mamoe.mirai.console.command.internal.qualifiedNameOrTip
import net.mamoe.mirai.console.plugin.internal.updateWhen
import net.mamoe.mirai.console.plugin.jvm.getSetting
import net.mamoe.mirai.console.setting.AutoSaveSettingHolder.AutoSaveSetting
import net.mamoe.mirai.console.utils.ConsoleExperimentalAPI
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.mirai.utils.minutesToSeconds
import net.mamoe.mirai.utils.secondsToMillis
import net.mamoe.yamlkt.Yaml
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.findAnnotation

/**
 * [Setting] 存储容器
 *
 * @see SettingHolder
 */
public interface SettingStorage {
    /**
     * 读取一个实例
     */
    public fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T

    /**
     * 保存一个实例
     */
    public fun store(holder: SettingHolder, setting: Setting)
}

// TODO: 2020/7/11 document
public interface MemorySettingStorage : SettingStorage {
    public companion object {
        @JvmStatic
        @JvmName("create")
        public operator fun invoke(): MemorySettingStorage = MemorySettingStorageImpl()
    }
}

// TODO: 2020/7/11 document
public interface MultiFileSettingStorage : SettingStorage {
    public val directory: File

    public companion object {
        @JvmStatic
        @JvmName("create")
        public operator fun invoke(directory: File): MultiFileSettingStorage = MultiFileSettingStorageImpl(directory)
    }
}


// TODO: 2020/7/11 here or companion?
public inline fun <T : Setting> SettingStorage.load(holder: SettingHolder, settingClass: KClass<T>): T =
    this.load(holder, settingClass.java)

// TODO: 2020/7/11 here or companion?
public inline fun <reified T : Setting> SettingStorage.load(holder: SettingHolder): T =
    this.load(holder, T::class)

/**
 * 可以持有相关 [Setting] 的对象.
 *
 * @see SettingStorage.load
 * @see SettingStorage.store
 *
 * @see AutoSaveSettingHolder 自动保存
 */
public interface SettingHolder {
    /**
     * 保存时使用的分类名
     */
    public val name: String
}

/**
 * 可以持有相关 [AutoSaveSetting] 的对象.
 *
 * @see net.mamoe.mirai.console.plugin.jvm.JvmPlugin
 */
public interface AutoSaveSettingHolder : SettingHolder, CoroutineScope {
    /**
     * [AutoSaveSetting] 每次自动保存时间间隔
     *
     * - 区间的左端点为最小间隔, 一个 [Value] 被修改后, 若此时间段后无其他修改, 将触发自动保存; 若有, 将重新开始计时.
     * - 区间的右端点为最大间隔, 一个 [Value] 被修改后, 最多不超过这个时间段后就会被保存.
     *
     * 若 [coroutineContext] 含有 [Job], 则 [AutoSaveSetting] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
     *
     * @see LongRange Java 用户使用 [LongRange] 的构造器创建
     * @see Long.rangeTo Kotlin 用户使用 [Long.rangeTo] 创建, 如 `3000..50000`
     */
    public val autoSaveIntervalMillis: LongRange
        get() = 30.secondsToMillis..10.minutesToSeconds

    /**
     * 链接自动保存的 [Setting].
     * 当任一相关 [Value] 的值被修改时, 将在一段时间无其他修改时保存
     *
     * 若 [AutoSaveSettingHolder.coroutineContext] 含有 [Job], 则 [AutoSaveSetting] 会通过 [Job.invokeOnCompletion] 在 Job 完结时触发自动保存.
     *
     * @see getSetting
     */
    public open class AutoSaveSetting(private val owner: AutoSaveSettingHolder, private val storage: SettingStorage) :
        AbstractSetting() {
        @Volatile
        internal var lastAutoSaveJob: Job? = null

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

        final override fun onValueChanged(value: Value<*>) {
            lastAutoSaveJob = owner.launch(block = updaterBlock)
        }

        private fun doSave() = storage.store(owner, this)
    }

}

// internal

internal class MemorySettingStorageImpl : SettingStorage, MemorySettingStorage {
    private val list = mutableMapOf<Class<out Setting>, Setting>()

    internal class MemorySettingImpl : AbstractSetting() {
        override fun onValueChanged(value: Value<*>) {
            // nothing to do
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T {
        return synchronized(list) {
            list.getOrPut(settingClass) {
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
        } as T
    }

    override fun store(holder: SettingHolder, setting: Setting) {
        synchronized(list) {
            list[setting::class.java] = setting
        }
    }
}

public open class MultiFileSettingStorageImpl(
    public final override val directory: File
) : SettingStorage, MultiFileSettingStorage {
    public override fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T =
        with(settingClass.kotlin) {
            val file = getSettingFile(holder, settingClass::class)

            @Suppress("UNCHECKED_CAST")
            val instance = objectInstance ?: this.createInstanceOrNull() ?: kotlin.run {
                if (settingClass != Setting::class.java) {
                    throw IllegalArgumentException(
                        "Cannot create Setting instance. Make sure settingClass is Setting::class.java or a Kotlin's object, " +
                                "or has a constructor which either has no parameters or all parameters of which are optional"
                    )
                }
                if (holder is AutoSaveSettingHolder) {
                    AutoSaveSetting(holder, this@MultiFileSettingStorageImpl) as T?
                } else null
            } ?: throw IllegalArgumentException(
                "Cannot create Setting instance. Make sure 'holder' is a AutoSaveSettingHolder, " +
                        "or 'setting' is an object or has a constructor which either has no parameters or all parameters of which are optional"
            )
            if (file.exists() && file.isFile && file.canRead()) {
                Yaml.default.parse(instance.updaterSerializer, file.readText())
            }
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
            file.writeText(Yaml.default.stringify(setting.updaterSerializer, Unit))
        }
    }
}

internal fun <T : Any> KClass<T>.createInstanceOrNull(): T? {
    val noArgsConstructor = constructors.singleOrNull { it.parameters.all(KParameter::isOptional) }
        ?: return null

    return noArgsConstructor.callBy(emptyMap())
}

internal fun KClass<*>.findASerialName(): String =
    findAnnotation<SerialName>()?.value
        ?: qualifiedName
        ?: throw IllegalArgumentException("Cannot find a serial name for $this")
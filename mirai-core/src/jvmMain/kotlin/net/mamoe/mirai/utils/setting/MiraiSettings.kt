package net.mamoe.mirai.utils.setting

import org.ini4j.Config
import org.ini4j.Ini
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Thread-safe Mirai Config <br></br>
 * Only supports `INI` format <br></br>
 * Supports [Map] and [List]
 *
 * @author NaturalHG
 */
class MiraiSettings(file: File)
/*
    public MiraiSettings(MiraiPluginBase pluginBase, String filename) {
        // TODO: 2019/9/6 每个插件独立文件夹存放
        this(new File(filename));
    }*/ {

    private val file: File

    private var ini: Ini

    private val cacheSection = ConcurrentHashMap<String, MiraiSettingSection>()

    init {
        val f = file.takeIf { it.name.contains(".") } ?: File(file.path + ".ini")
        this.file = f
        if (!f.exists() && !f.createNewFile()) {
            throw RuntimeException("cannot create config file $f")
        }
        val config = Config()
        config.isMultiSection = true
        ini = Ini()
        ini.config = config
        ini.load(this.file.toURI().toURL())
    }

    @Synchronized
    fun setSection(key: String, section: MiraiSettingSection) {
        cacheSection[key] = section
    }


    @Synchronized
    fun getMapSection(key: String): MiraiSettingMapSection {
        if (!cacheSection.containsKey(key)) {
            val section = MiraiSettingMapSection()
            if (ini.containsKey(key)) {
                section.putAll(ini[key]!!)
            }
            cacheSection[key] = section
        }
        return cacheSection[key] as MiraiSettingMapSection
    }

    @Synchronized
    fun getListSection(key: String): MiraiSettingListSection {
        if (!cacheSection.containsKey(key)) {
            val section = MiraiSettingListSection()
            if (ini.containsKey(key)) {
                section.addAll(ini[key]!!.values)
            }
            cacheSection[key] = section
        }
        return cacheSection[key] as MiraiSettingListSection
    }


    @Synchronized
    fun save() {
        cacheSection.forEach { (k, a) ->
            if (!ini.containsKey(k)) {
                ini.put(k, "", HashMap<Any, Any>())
            }
            a.saveAsSection(ini[k]!!)
        }
        this.clearCache()
        try {
            ini.store(file)
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    @Synchronized
    fun clearCache() {
        cacheSection.clear()
    }
}


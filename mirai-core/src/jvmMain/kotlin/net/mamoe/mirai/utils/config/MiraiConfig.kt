package net.mamoe.mirai.utils.config

import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.util.*

/**
 * YAML-TYPE CONFIG
 * Thread SAFE
 *
 * @author NaturalHG
 */
class MiraiConfig(private val root: File) : MiraiConfigSection<Any>(parse(Objects.requireNonNull(root))) {

    @Synchronized
    fun save() {
        val dumperOptions = DumperOptions()
        dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        val yaml = Yaml(dumperOptions)
        val content = yaml.dump(this)
        try {
            ByteArrayInputStream(content.toByteArray()).transferTo(FileOutputStream(this.root))
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    companion object {
        @Suppress("UNCHECKED_CAST")
        private fun parse(file: File): MutableMap<String, Any>? {
            /*
            if (!file.toURI().getPath().contains(MiraiServer.getInstance().getParentFolder().getPath())) {
                file = new File(MiraiServer.getInstance().getParentFolder().getPath(), file.getName());
            }*/
            if (!file.exists()) {
                try {
                    if (!file.createNewFile()) {
                        return linkedMapOf()
                    }
                } catch (e: IOException) {
                    e.printStackTrace()
                    return linkedMapOf()
                }

            }
            val dumperOptions = DumperOptions()
            dumperOptions.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
            val yaml = Yaml(dumperOptions)
            return yaml.loadAs<LinkedHashMap<*, *>>(file.readLines(Charset.defaultCharset()).joinToString("\n"), LinkedHashMap::class.java) as MutableMap<String, Any>?
        }
    }
}

package io.github.mzdluo123.mirai.android.script

import android.content.Context
import android.net.Uri
import android.util.Log
import io.github.mzdluo123.mirai.android.BotApplication
import io.github.mzdluo123.mirai.android.utils.copyToFileDir
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import java.io.File

class ScriptManager(
    private var context: Context,
    private var scriptDir: File,
    private var configDir: File
) {
    val hosts = mutableListOf<ScriptHost>()
    private val bots = mutableListOf<Bot>()
    val botsSize: Int
        get() = bots.size

    @ExperimentalUnsignedTypes
    companion object {
        val instance: ScriptManager by lazy {
            val context: Context = BotApplication.context
            val scriptDir = context.getExternalFilesDir("scripts")
            val configDir = context.getExternalFilesDir("data")
            ScriptManager(context, scriptDir!!, configDir!!)
        }

        fun unPackHostInfos(infoStrings: Array<String>): List<ScriptHost.ScriptInfo> =
            List(infoStrings.size) {
                Json.decodeFromString(ScriptHost.ScriptInfo.serializer(), infoStrings[it])
            }

        fun copyFileToScriptDir(context: Context, uri: Uri, name: String): File =
            context.copyToFileDir(
                uri,
                name,
                context.getExternalFilesDir("scripts")!!.absolutePath
            )

    }

    init {
        if (!scriptDir.exists()) scriptDir.mkdirs()
        if (!configDir.exists()) configDir.mkdirs()
        loadScripts()
    }

    fun addBot(bot: Bot) {
        bots.add(bot)
        hosts.forEach {
            it.installBot(bot)
        }
    }

    fun editConfig(index: Int, editor: ScriptHost.ScriptConfig.() -> Unit) {
        hosts[index].config.editor()
    }

    fun delete(index: Int) {
        hosts[index].disable()
        hosts[index].scriptFile.getConfigFile().delete()
        hosts[index].scriptFile.delete()
        hosts.removeAt(index)
    }

    private fun loadScripts() {
        scriptDir.listFiles()?.forEach { scriptFile ->
            //scriptFile.delete()
            //scriptFile.getConfigFile().delete()
            if (scriptFile.isFile)
                hosts.addHost(scriptFile, scriptFile.getConfigFile(), ScriptHostFactory.UNKNOWN)
        }
    }

//    fun createScriptFromUri(fromUri: Uri, type: Int): Boolean {
//        fromUri.getName(context).let { name ->
//            val scriptFile = context.copyToFileDir(
//                fromUri,
//                name!!,
//                scriptDir.absolutePath
//            )
//
//            hosts.addHost(scriptFile, scriptFile.getConfigFile(), type)?.let { host ->
//                bots.forEach { bot -> host.installBot(bot) }
//                return true
//            } ?: return false
//        }
//    }

    fun createScriptFromFile(scriptFile: File, type: Int): Boolean {
        hosts.addHost(scriptFile, scriptFile.getConfigFile(), type)?.let { host ->
            bots.forEach { bot -> host.installBot(bot) }
            return true
        } ?: return false
    }

    fun enable(index: Int) {
        Log.i("enable", index.toString())
        if (hosts[index].config.enable) return
        hosts[index].enable()
        hosts[index].config.enable = true
        hosts[index].info.enable = true
        hosts[index].saveConfig()
        bots.forEach { hosts[index].installBot(it) }
    }

    fun enableAll() = hosts.forEach { host -> host.enable() }

    fun disable(index: Int) {
        Log.i("disable", index.toString())
        if (!hosts[index].config.enable) return
        hosts[index].disable()
        hosts[index].config.enable = false
        hosts[index].info.enable = false
        hosts[index].saveConfig()
    }

    fun disableAll() = hosts.forEach { it.disable() }

    fun reload(index: Int) {
        hosts[index].disable()
        hosts[index].load()
        hosts[index].enableIfPossible()
        bots.forEach {
            hosts[index].installBot(it)
        }
    }

    fun reloadAll() = hosts.forEach {
        it.disable()
        it.load()
        it.enableIfPossible()
        bots.forEach { bot ->
            it.installBot(bot)
        }
    }

    fun getHostInfoStrings(): Array<String> = List(hosts.size) {
        hosts[it].getInfoString()
    }.toTypedArray()

    private fun MutableList<ScriptHost>.addHost(
        scriptFile: File,
        configFile: File,
        type: Int
    ): ScriptHost? {
        try {
            //Log.e("loading:", "${scriptFile.absolutePath}")
            val host = ScriptHostFactory.getScriptHost(scriptFile, scriptFile.getConfigFile(), type)
            host ?: throw Exception("未知的脚本类型！${scriptFile.absolutePath}")
            host.load()
            host.enableIfPossible()
            add(host)
            return host
        } catch (e: Exception) {
            Log.e("loadScriptError", e.message ?: return null)
        }
        return null
    }

    private fun File.getConfigFile() = File(configDir, name)
//    fun Uri.getName(context: Context) =
//      context.askFileName()
}

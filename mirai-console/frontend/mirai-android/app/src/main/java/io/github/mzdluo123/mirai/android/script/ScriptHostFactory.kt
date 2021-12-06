package io.github.mzdluo123.mirai.android.script

import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileReader

object ScriptHostFactory {
    const val UNKNOWN = 0
    const val LUA = 1
    const val JAVASCRIPT = 2
    const val PYTHON = 3
    const val KOTLINSCRIPT = 4
    val NAMES = arrayOf("Unknown", "Lua", "JavaScript", "Python", "KotlinScript")
    fun getTypeFromSuffix(suffix: String) = when (suffix) {
        "lua" -> LUA
        "js" -> JAVASCRIPT
        "py" -> PYTHON
        "kts" -> KOTLINSCRIPT
        else -> UNKNOWN
    }

    fun getScriptHost(scriptFile: File, configFile: File, type: Int): ScriptHost? {
        var trueType: Int = type
        if (trueType == UNKNOWN) {
            if (configFile.exists()) {
                FileReader(configFile).apply {
                    trueType =
                        Json.decodeFromString(ScriptHost.ScriptConfig.serializer(), readText()).type
                }.close()
                if (trueType == UNKNOWN) trueType = getTypeFromSuffix(scriptFile.getSuffix())
            } else {
                trueType = getTypeFromSuffix(scriptFile.getSuffix())
            }
        }
        return when (trueType) {
            LUA -> LuaScriptHost(scriptFile, configFile).also {
                it.config = ScriptHost.ScriptConfig(trueType, false, "")
            }
            JAVASCRIPT -> JavaScriptHost(scriptFile, configFile).also {
                it.config = ScriptHost.ScriptConfig(trueType, false, "")
            }
            else -> null
        }
    }

    private fun File.getSuffix() = name.split(".").last()

}
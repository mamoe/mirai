package io.github.mzdluo123.mirai.android.script

import android.util.Log
import com.ooooonly.luaMirai.lua.MiraiGlobals
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import org.luaj.vm2.LuaTable
import org.luaj.vm2.LuaValue
import java.io.File


class LuaScriptHost(scriptFile: File, configFile: File) : ScriptHost(scriptFile, configFile) {
    private lateinit var globals : MiraiGlobals

    override fun onCreate(): ScriptInfo {
        globals = MiraiGlobals(logger)
        globals.loadfile(scriptFile.absolutePath).call()
        var name = scriptFile.name.split(".").first()
        var author = "MiraiAndroid"
        var version = "0.1"
        var description = "MiraiAndroid Lua脚本"
        globals.get("Info").takeIf { it is LuaTable }?.let {
            var table = it as LuaTable
            name = table.get("name").takeUnless { it == LuaValue.NIL }?.toString()?:name
            author = table.get("author").takeUnless { it == LuaValue.NIL }?.toString() ?: author
            version = table.get("version").takeUnless { it == LuaValue.NIL }?.toString() ?: version
            description = table.get("description").takeUnless { it == LuaValue.NIL }?.toString()
                ?: description
        }
        return ScriptInfo(name, author, version, description, scriptFile.length())
    }

    override fun onFetchBot(bot: Bot) {
        Log.i("fetchBot", bot.id.toString())
        if (!config.enable) return
        bot.launch {
            globals.onLoad(bot)
        }
    }

    override fun onDisable() {
        globals.onFinish()
        globals.unSubsribeAll()
        Log.i("uninstall", info.name)
    }

    override fun onEnable() {

    }
}
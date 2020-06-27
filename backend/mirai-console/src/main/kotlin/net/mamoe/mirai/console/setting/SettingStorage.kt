package net.mamoe.mirai.console.setting

import net.mamoe.mirai.console.utils.ConsoleExperimentalAPI
import kotlin.reflect.KClass

interface SettingStorage {
    @JvmDefault
    fun <T : Setting> load(holder: SettingHolder, settingClass: KClass<T>): T = this.load(holder, settingClass.java)

    fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T

    @ConsoleExperimentalAPI
    fun store(holder: SettingHolder, setting: Setting)
}

@ConsoleExperimentalAPI
interface SettingHolder {
    val name: String
}
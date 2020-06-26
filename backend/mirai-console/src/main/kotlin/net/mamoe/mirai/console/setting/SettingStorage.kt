package net.mamoe.mirai.console.setting

import net.mamoe.mirai.utils.MiraiExperimentalAPI
import kotlin.reflect.KClass

interface SettingStorage {
    @JvmDefault
    fun <T : Setting> load(holder: SettingHolder, settingClass: KClass<T>): T = this.load(holder, settingClass.java)

    fun <T : Setting> load(holder: SettingHolder, settingClass: Class<T>): T

    @MiraiExperimentalAPI
    fun store(holder: SettingHolder, setting: Setting)
}

@MiraiExperimentalAPI
interface SettingHolder {
    val name: String
}
package net.mamoe.mirai.utils.setting

import org.ini4j.Profile

import java.io.Closeable

/**
 * @author NaturalHG
 */
interface MiraiSettingSection : Closeable {
    fun saveAsSection(section: Profile.Section)
}
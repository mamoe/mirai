import java.util.*

/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

object PublishSettings {
    val projKeySettings = findProjectDir().resolve("keys.properties").let { store ->
        println("SST: $store")
        val prop = Properties()
        kotlin.runCatching {
            if (store.isFile) {
                store.bufferedReader().use { prop.load(it) }
            }
        }.onFailure { it.printStackTrace(System.out) }
        prop
    }

    val isSnapshot = projKeySettings.getProperty("isSnapshot")?.toBoolean() ?: false

    operator fun get(key: String): String = projKeySettings.getProperty(key) ?: error("No property $key")
    operator fun get(key: String, def: String): String = projKeySettings.getProperty(key, def) ?: def

    fun getIfSnapshot(key: String): String? = if (isSnapshot) {
        projKeySettings.getProperty(key)
    } else null
}

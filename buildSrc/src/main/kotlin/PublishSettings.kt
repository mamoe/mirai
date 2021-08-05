/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import com.google.gson.JsonObject
import com.google.gson.JsonParser

object PublishSettings {
    val projKeySettings: JsonObject = findProjectDir().resolve("token.txt").let { store ->
        kotlin.runCatching {
            if (store.isFile) {
                store.bufferedReader().use {
                    JsonParser().parse(it).asJsonObject
                }
            } else null
        }.onFailure { it.printStackTrace(System.out) }
            .getOrNull() ?: JsonObject()
    }

    val isSnapshot = projKeySettings.getAsJsonPrimitive("isSnapshot")?.asBoolean ?: false

    operator fun get(key: String): String = projKeySettings[key]?.asString ?: error("No property $key")
    operator fun get(key: String, def: String): String = projKeySettings[key]?.asString ?: def

    fun getIfSnapshot(key: String): String? = if (isSnapshot) {
        projKeySettings[key]?.asString
    } else null
}

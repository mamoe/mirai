/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.dokka

import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer


fun main() {
    val currentVersion = System.getenv("mirai_ver") ?: error("version not found")

    val versions = pages.resolve("versions.json")

    json.decodeFromString(ListSerializer(String.serializer()), versions.readText()).toMutableList().let { list ->
        if (currentVersion in list) return@let
        list.add(currentVersion)
        versions.writeText(json.encodeToString(ListSerializer(String.serializer()), list))
    }

    pages.resolve("snapshot").renameTo(pages.resolve(currentVersion))
}

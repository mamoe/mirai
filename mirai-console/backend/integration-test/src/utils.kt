/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.console.integrationtest

import org.junit.jupiter.api.fail
import java.io.File

internal fun readStringListFromEnv(key: String): MutableList<String> {
    val size = System.getenv(key)!!.toInt()
    val rsp = mutableListOf<String>()
    for (i in 0 until size) {
        rsp.add(System.getenv("${key}_$i")!!)
    }
    return rsp
}

internal fun saveStringListToEnv(key: String, value: Collection<String>, env: MutableMap<String, String>) {
    env[key] = value.size.toString()
    value.forEachIndexed { index, v ->
        env["${key}_$index"] = v
    }
}

// region assertion kits
public fun File.assertNotExists() {
    if (exists()) {
        fail { "Except ${this.absolutePath} not exists but this file exists in disk" }
    }
}
// endregion

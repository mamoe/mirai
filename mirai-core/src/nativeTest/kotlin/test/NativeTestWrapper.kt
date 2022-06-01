/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.test

import kotlinx.cinterop.toKString
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.internal.local.main
import platform.posix.getenv

internal actual fun startNativeTestIfNeeded() {
    if (getenv("mirai.native.test.main")?.toKString() == "true") {
        runBlocking { main() } // If you see unresolved reference, create a file "TestMain.kt" in `/local` and add a `main` function in it.
    }
}
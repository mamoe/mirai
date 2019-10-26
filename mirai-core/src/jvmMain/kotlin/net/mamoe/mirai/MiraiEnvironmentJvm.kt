@file:Suppress("MayBeConstant", "unused")

package net.mamoe.mirai

import java.io.File

actual typealias MiraiEnvironment = MiraiEnvironmentJvm

object MiraiEnvironmentJvm {
    /**
     * JVM only, 临时文件夹
     */
    val TEMP_DIR: File = createTempDir().apply { deleteOnExit() }
}
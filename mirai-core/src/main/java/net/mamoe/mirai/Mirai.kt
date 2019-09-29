package net.mamoe.mirai

import java.io.File

/**
 * @author Him188moe
 */
object Mirai {
    val VERSION: String get() = internal.version

    val WORKING_DIRECTORY: File get() = internal.workingDirectory


    internal lateinit var internal: MiraiInternal

    internal abstract class MiraiInternal {
        abstract val workingDirectory: File
        abstract val version: String
    }
}


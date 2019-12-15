@file:Suppress("MayBeConstant", "unused")

package net.mamoe.mirai

actual object MiraiEnvironment {
    @JvmStatic
    actual val platform: Platform
        get() = Platform.JVM
}
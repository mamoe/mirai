@file:Suppress("MayBeConstant", "unused")

package net.mamoe.mirai

actual object MiraiEnvironment {
    actual val platform: Platform get() = Platform.JVM
}
@file:Suppress("MayBeConstant", "unused")

package net.mamoe.mirai

import kotlinx.io.core.buildPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.contact.GroupInternalId
import net.mamoe.mirai.contact.toId
import net.mamoe.mirai.utils.io.toByteArray
import net.mamoe.mirai.utils.io.toUHexString

actual object MiraiEnvironment {
    @JvmStatic
    actual val platform: Platform
        get() = Platform.JVM
}
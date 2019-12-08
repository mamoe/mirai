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

@ExperimentalUnsignedTypes
fun main() {
    println(GroupInternalId(2793514141u).toId().value.toLong())
    println(GroupInternalId(2040208217u).toId().value.toLong())
    println(289942298u.toByteArray().toUHexString())
    println(1040400290u.toByteArray().toUHexString())
    println(buildPacket {
        writeStringUtf8("信用卡")
    }.readBytes().toUByteArray().toUHexString())
}
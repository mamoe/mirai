/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline
import kotlin.jvm.JvmStatic

/**
 * 核心数据保护器
 *
 * ### Why
 *
 * 有时候可能会发生 `OutOfMemoryError`, 如果存在 `-XX:+HeapDumpOnOutOfMemoryError`, 则 JVM 会生成一份系统内存打印以供 debug.
 * 该报告包含全部内存信息, 包括各种数据, 核心数据以及, 机密数据 (如密码)
 *
 * 该内存报告唯一没有包含的数据就是 Native层数据, 包括且不限于
 *
 * - `sun.misc.Unsafe.allocate()`
 * - `java.nio.ByteBuffer.allocateDirect()` (Named `DirectByteBuffer`)
 * - C/C++ (或其他语言) 的数据
 *
 * *试验数据来源 `openjdk version "17" 2021-09-14, 64-Bit Server VM (build 17+35-2724, mixed mode, sharing)`*
 *
 * ### How it works
 *
 * 因为 Heap Dump 不存在 `DirectByteBuffer` 的实际数据, 所以可以通过该类隐藏关键数据. 等需要的时候在读取出来.
 * 因为数据并没有直接存在于某个类字段中, 缺少数据关联, 也很难分析相关数据是什么数据
 */
@Suppress("NOTHING_TO_INLINE", "UsePropertyAccessSyntax")
//@MiraiExperimentalApi
public object SecretsProtection {

    @JvmInline
    @Serializable(EscapedStringSerializer::class)
    public value class EscapedString(
        public val data: Any,
    ) {
        public val asString: String
            get() = SecretsProtectionPlatform.impl_asString(data)

        public constructor(data: ByteArray) : this(escape(data))
        public constructor(data: String) : this(escape(data.encodeToByteArray()))
    }

    @JvmInline
    @Serializable(EscapedByteBufferSerializer::class)
    public value class EscapedByteBuffer(
        public val data: Any,
    ) {
        public val size: Int get() = SecretsProtectionPlatform.impl_getSize(data)

        public val asByteArray: ByteArray
            get() = SecretsProtectionPlatform.impl_asByteArray(data)

        public constructor(data: ByteArray) : this(escape(data))
    }

    @JvmStatic
    public fun escape(data: ByteArray): Any {
        return SecretsProtectionPlatform.escape(data)
    }


    public object EscapedStringSerializer :
        KSerializer<EscapedString> by SecretsProtectionPlatform.EscapedStringSerializer

    public object EscapedByteBufferSerializer :
        KSerializer<EscapedByteBuffer> by SecretsProtectionPlatform.EscapedByteBufferSerializer
}


internal expect object SecretsProtectionPlatform {
    fun impl_asString(data: Any): String
    fun impl_asByteArray(data: Any): ByteArray
    fun impl_getSize(data: Any): Int

    fun escape(data: ByteArray): Any

    object EscapedStringSerializer : KSerializer<SecretsProtection.EscapedString>

    object EscapedByteBufferSerializer : KSerializer<SecretsProtection.EscapedByteBuffer>
}
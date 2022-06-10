/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ByteArraySerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.serializer
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

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
    private class NativeBufferWithLock(
        @JvmField val buffer: ByteBuffer,
        val lock: Lock = ReentrantLock(),
        @JvmField @field:Volatile var lowRemainingHit: Int = 0,
        @JvmField @field:Volatile var unusedHit: Int = 0,
    ) {
        companion object {
            internal val lowRemainingHitUpdater = AtomicIntegerFieldUpdater.newUpdater(
                NativeBufferWithLock::class.java, "lowRemainingHit"
            )

            internal val unusedHitUpdater = AtomicIntegerFieldUpdater.newUpdater(
                NativeBufferWithLock::class.java, "unusedHit"
            )
        }
    }

    private val bufferSize = systemProp(
        "mirai.secrets.protection.buffer.size", 0
    ).toInt().coerceAtLeast(2048)

    private val lowRemainingThreshold = bufferSize / 128

    private val lowRemainingHitThreshold = systemProp(
        "mirai.secrets.protection.threshold.low.remaining.hit", 10
    ).toInt().coerceAtLeast(1)

    private val pool = ConcurrentLinkedDeque<NativeBufferWithLock>()

    init {
        val tmbuffer = ByteBuffer.allocateDirect(bufferSize)
        if (!systemProp("mirai.secrets.protection.ignore.warning", false)) {
            if (tmbuffer.javaClass === ByteBuffer.allocate(1).javaClass) {
                val ps = System.err
                synchronized(ps) {
                    ps.println("========================================================================================")
                    ps.println("Mirai SecretsProtection WARNING:")
                    ps.println()
                    ps.println("当前 JRE 实现没有为 `ByteBuffer.allocateDirect` 直接分配本地内存, 请更换其他 JRE.")
                    ps.println("这很有可能导致您的密码等敏感信息被带入内存报告中")
                    ps.println("可添加 JVM 参数 -Dmirai.secrets.protection.ignore.warning=true 来忽略此警告")
                    ps.println()
                    ps.println("Current JRE Implementation not using native memory for `ByteBuffer.allocateDirect`.")
                    ps.println("Please use another JRE.")
                    ps.println("It may cause your passwords to be dumped by other processes.")
                    ps.println("Suppress this warning by adding jvm option -Dmirai.secrets.protection.ignore.warning=true")
                    ps.println()
                    ps.println("========================================================================================")
                }
            }
        }
        pool.add(NativeBufferWithLock(tmbuffer))
    }

    /*
    Implementation note:

    1. 如果数据超过单个缓冲区大小，直接分配系统内存并返回
    2. 查找首个可以放下数据的缓冲区，放入缓冲区并返回对应镜像
    3. 如果没有可用缓冲区，分配新缓冲区并加入缓冲区池内

     */
    @JvmStatic
    public fun allocate(size: Int): ByteBuffer {
        if (size >= bufferSize) {
            return ByteBuffer.allocateDirect(size)
        }

        fun putInto(buffer: NativeBufferWithLock): ByteBuffer {
            // @context: buffer.locked = true
            // @context: buffer.remaining >= buffer.size

            // 返回存储 data 的数据的 DirectByteBuffer
            //   ByteBuffer.slice(): DirectByteBuffer[start=pos, end=limit]
            val mirror = buffer.buffer.let { buffer0 ->
                buffer0.limit = buffer0.pos + size
                buffer0.slice()
            }
            // 原始缓冲区复位
            buffer.buffer.let { buf -> buf.limit = buf.capacity() }
            buffer.buffer.pos += size

            // 此缓冲区已无可用空间
            if (!buffer.buffer.hasRemaining()) {
                pool.remove(buffer)
            }

            return mirror
        }

        pool.forEach bufferLoop@{ buffer ->

            val bufferRemaining = buffer.buffer.remaining
            if (bufferRemaining >= size) {
                if (buffer.lock.tryLock()) {
                    try {
                        if (buffer.buffer.hasRemaining(size)) {
                            NativeBufferWithLock.unusedHitUpdater.getAndIncrement(buffer)
                            return putInto(buffer)
                        }
                    } finally {
                        buffer.lock.unlock()
                    }
                }
            }
            NativeBufferWithLock.unusedHitUpdater.getAndDecrement(buffer)

            // OOM Avoid
            if (bufferRemaining <= lowRemainingThreshold) {
                NativeBufferWithLock.lowRemainingHitUpdater.getAndDecrement(buffer)
                if (buffer.lowRemainingHit >= lowRemainingHitThreshold) {
                    pool.remove(buffer)
                }
            }
            if (buffer.unusedHit >= 20) {
                pool.remove(buffer)
            }

        }
        val newBuffer = NativeBufferWithLock(ByteBuffer.allocateDirect(bufferSize))
        val rsp = putInto(newBuffer)
        if (newBuffer.buffer.hasRemaining()) {
            pool.add(newBuffer)
        }
        return rsp
    }

    @JvmStatic
    public fun escape(data: ByteArray): ByteBuffer {
        return allocate(data.size).also {
            it.put(data)
            it.pos = 0
        }
    }

    @JvmInline
    @Serializable(EscapedStringSerializer::class)
    public value class EscapedString(
        public val data: ByteBuffer,
    ) {
        public val asString: String
            get() = data.duplicate().readString()
    }

    @JvmInline
    @Serializable(EscapedByteBufferSerializer::class)
    public value class EscapedByteBuffer(
        public val data: ByteBuffer,
    )

    public object EscapedStringSerializer : KSerializer<EscapedString> by String.serializer().map(
        String.serializer().descriptor.copy("EscapedString"),
        deserialize = { EscapedString(escape(it.toByteArray())) },
        serialize = { it.data.duplicate().readString() }
    )

    public object EscapedByteBufferSerializer : KSerializer<EscapedByteBuffer> by ByteArraySerializer().map(
        ByteArraySerializer().descriptor.copy("EscapedByteBuffer"),
        deserialize = { EscapedByteBuffer(escape(it)) },
        serialize = { it.data.duplicate().readBytes() }
    )


}

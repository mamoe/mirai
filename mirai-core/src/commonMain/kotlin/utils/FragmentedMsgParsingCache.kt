/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.atomicfu.locks.reentrantLock
import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.utils.currentTimeMillis

/**
 * fragmented message
 */
internal abstract class FragmentedMsgParsingCache<T> {
    class PkgMsg<T>(
        val size: Int,
        val divSeq: Int,
        val data: MutableMap<Int, T>,
    ) {
        val createTime = currentTimeMillis()
    }

    private val deque = ArrayList<PkgMsg<T>>(16)
    private val accessLock = reentrantLock()
    private fun clearInvalid() {
        deque.removeAll {
            currentTimeMillis() - it.createTime > 10000L
        }
    }

    internal abstract val T.contentHead: MsgComm.ContentHead?

    fun tryMerge(msg: T): List<T> {
        val head = msg.contentHead ?: return listOf(msg)
        val size = head.pkgNum
        if (size < 2) return listOf(msg)
        accessLock.withLock {
            clearInvalid()
            val seq = head.divSeq
            val index = head.pkgIndex
            val pkgMsg = deque.find {
                it.divSeq == seq
            } ?: PkgMsg<T>(size, seq, mutableMapOf()).also { deque.add(it) }
            pkgMsg.data[index] = msg
            if (pkgMsg.data.size == pkgMsg.size) {
                deque.removeAll { it.divSeq == seq }
                return pkgMsg.data.entries.asSequence()
                    .sortedBy { it.key }
                    .map { it.value }
                    .toList()
            }
            return emptyList()
        }
    }
}

/**
 * fragmented message
 */
internal class GroupPkgMsgParsingCache : FragmentedMsgParsingCache<MsgOnlinePush.PbPushMsg>() {
    override val MsgOnlinePush.PbPushMsg.contentHead: MsgComm.ContentHead?
        get() = this.msg.contentHead
}

/**
 * fragmented message
 */
internal class C2CPkgMsgParsingCache : FragmentedMsgParsingCache<MsgComm.Msg>() {
    @Suppress("EXTENSION_SHADOWED_BY_MEMBER")
    override val MsgComm.Msg.contentHead: MsgComm.ContentHead?
        get() = this.contentHead
}
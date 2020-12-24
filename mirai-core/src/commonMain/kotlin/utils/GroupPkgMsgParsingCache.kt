/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.utils

import kotlinx.atomicfu.locks.withLock
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgOnlinePush
import net.mamoe.mirai.utils.currentTimeMillis
import java.util.concurrent.locks.ReentrantLock

/**
 * fragmented message
 */
internal class GroupPkgMsgParsingCache {
    class PkgMsg(
        val size: Int,
        val divSeq: Int,
        val data: MutableMap<Int, MsgOnlinePush.PbPushMsg>,
    ) {
        val createTime = currentTimeMillis()
    }

    private val deque = ArrayList<PkgMsg>(16)
    private val accessLock = ReentrantLock()
    private fun clearInvalid() {
        deque.removeIf {
            currentTimeMillis() - it.createTime > 10000L
        }
    }

    fun tryMerge(msg: MsgOnlinePush.PbPushMsg): List<MsgOnlinePush.PbPushMsg> {
        val head = msg.msg.contentHead ?: return listOf(msg)
        val size = head.pkgNum
        if (size < 2) return listOf(msg)
        accessLock.withLock {
            clearInvalid()
            val seq = head.divSeq
            val index = head.pkgIndex
            val pkgMsg = deque.find {
                it.divSeq == seq
            } ?: PkgMsg(size, seq, mutableMapOf()).also { deque.add(it) }
            pkgMsg.data[index] = msg
            if (pkgMsg.data.size == pkgMsg.size) {
                deque.removeIf { it.divSeq == seq }
                return pkgMsg.data.entries.asSequence()
                    .sortedBy { it.key }
                    .map { it.value }
                    .toList()
            }
            return emptyList()
        }
    }
}
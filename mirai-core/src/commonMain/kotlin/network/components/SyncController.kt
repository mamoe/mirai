/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import net.mamoe.mirai.internal.AbstractBot
import net.mamoe.mirai.internal.network.QQAndroidClient
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.network.protocol.SyncingCacheList
import net.mamoe.mirai.internal.network.protocol.data.jce.MsgInfo
import net.mamoe.mirai.internal.network.protocol.data.proto.MsgComm
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans
import net.mamoe.mirai.utils.EMPTY_BYTE_ARRAY
import net.mamoe.mirai.utils.currentTimeSeconds

internal interface SyncController {
    val firstNotify: AtomicBoolean
    var latestMsgNewGroupTime: Long
    var latestMsgNewFriendTime: Long

    var syncCookie: ByteArray?
    var pubAccountCookie: ByteArray
    var msgCtrlBuf: ByteArray

    fun syncOnlinePush(uid: Long, sequence: Short, time: Long): Boolean
    fun syncNewFriend(sequence: Long, time: Long): Boolean
    fun syncNewGroup(sequence: Long, time: Long): Boolean
    fun syncGetMessage(uid: Long, sequence: Int, time: Int): Boolean
    fun syncPushTrans(uid: Long, sequence: Int, time: Int): Boolean

    fun syncGroupMessageReceipt(messageRandom: Int): Boolean
    fun containsGroupMessageReceipt(messageRandom: Int): Boolean

    companion object : ComponentKey<SyncController> {
        val AbstractBot.syncController get() = this.components[SyncController]
        val QQAndroidClient.syncController get() = bot.syncController
        var QQAndroidClient.syncCookie
            get() = bot.syncController.syncCookie
            set(value) {
                bot.syncController.syncCookie = value
            }
    }
}

internal fun SyncController.syncPushTrans(content: OnlinePushTrans.PbMsgInfo): Boolean =
    syncPushTrans(content.msgUid, content.msgSeq, content.msgTime)

internal fun SyncController.syncGetMessage(
    msgHead: MsgComm.MsgHead,
) = msgHead.run {
    syncGetMessage(msgUid, msgSeq, msgTime)
}

internal fun SyncController.syncOnlinePush(
    msgInfo: MsgInfo,
) = syncOnlinePush(
    uid = msgInfo.lMsgUid ?: 0,
    sequence = msgInfo.shMsgSeq,
    time = msgInfo.uMsgTime,
)

internal class SyncControllerImpl : SyncController {
    override val firstNotify: AtomicBoolean = atomic(true)

    @Volatile
    override var latestMsgNewGroupTime: Long = currentTimeSeconds()

    @Volatile
    override var latestMsgNewFriendTime: Long = currentTimeSeconds()

    @Volatile
    override var syncCookie: ByteArray? = null

    @Volatile
    override var pubAccountCookie = EMPTY_BYTE_ARRAY

    @Volatile
    override var msgCtrlBuf: ByteArray = EMPTY_BYTE_ARRAY

    private val pbGetMessageCacheList = SyncingCacheList<PbGetMessageSyncId>()
    private val systemMsgNewGroupCacheList = SyncingCacheList<SystemMsgNewSyncId>(10)
    private val systemMsgNewFriendCacheList = SyncingCacheList<SystemMsgNewSyncId>(10)
    private val pbPushTransMsgCacheList = SyncingCacheList<PbPushTransMsgSyncId>(10)
    private val onlinePushReqPushCacheList = SyncingCacheList<OnlinePushReqPushSyncId>(50)
    private val pendingGroupMessageReceiptCacheList = SyncingCacheList<PendingGroupMessageReceiptSyncId>(50)

    override fun syncOnlinePush(uid: Long, sequence: Short, time: Long): Boolean =
        onlinePushReqPushCacheList.addCache(OnlinePushReqPushSyncId(uid, sequence, time))

    override fun syncNewFriend(sequence: Long, time: Long): Boolean =
        systemMsgNewFriendCacheList.addCache(SystemMsgNewSyncId(sequence, time))

    override fun syncNewGroup(sequence: Long, time: Long): Boolean =
        systemMsgNewGroupCacheList.addCache(SystemMsgNewSyncId(sequence, time))

    override fun syncGetMessage(uid: Long, sequence: Int, time: Int): Boolean =
        pbGetMessageCacheList.addCache(PbGetMessageSyncId(uid, sequence, time))

    override fun syncPushTrans(uid: Long, sequence: Int, time: Int): Boolean =
        pbPushTransMsgCacheList.addCache(PbPushTransMsgSyncId(uid, sequence, time))

    override fun syncGroupMessageReceipt(messageRandom: Int): Boolean =
        pendingGroupMessageReceiptCacheList.addCache(PendingGroupMessageReceiptSyncId(messageRandom))

    override fun containsGroupMessageReceipt(messageRandom: Int): Boolean =
        pendingGroupMessageReceiptCacheList.contains { it.messageRandom == messageRandom }

    data class PbGetMessageSyncId(
        val uid: Long,
        val sequence: Int,
        val time: Int,
    )

    data class SystemMsgNewSyncId(
        val sequence: Long,
        val time: Long,
    )

    data class PbPushTransMsgSyncId(
        val uid: Long,
        val sequence: Int,
        val time: Int,
    )

    data class OnlinePushReqPushSyncId(
        val uid: Long,
        val sequence: Short,
        val time: Long,
    )

    data class PendingGroupMessageReceiptSyncId(
        val messageRandom: Int,
    )

}
package net.mamoe.mirai.qqandroid

import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.*
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.message.data.CustomFaceFromFile
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.NotOnlineImageFromFile
import net.mamoe.mirai.qqandroid.io.serialization.readProtoBuf
import net.mamoe.mirai.qqandroid.network.highway.Highway
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.CSDataHighwayHead
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.qqandroid.network.protocol.packet.withUse
import net.mamoe.mirai.qqandroid.utils.toIpV4AddressString
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.getValue
import net.mamoe.mirai.utils.io.PlatformSocket
import net.mamoe.mirai.utils.io.discardExact
import net.mamoe.mirai.utils.unsafeWeakRef
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

internal abstract class ContactImpl : Contact

internal class QQImpl(bot: QQAndroidBot, override val coroutineContext: CoroutineContext, override val id: Long) : ContactImpl(), QQ {
    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        bot.network.run {
            check(
                MessageSvc.PbSendMsg.ToFriend(
                    bot.client,
                    id,
                    message
                ).sendAndExpect<MessageSvc.PbSendMsg.Response>() is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed" }
        }
    }

    override suspend fun uploadImage(image: ExternalImage): Image {
        TODO("not implemented")
    }

    override suspend fun queryProfile(): Profile {
        TODO("not implemented")
    }

    override suspend fun queryPreviousNameList(): PreviousNameList {
        TODO("not implemented")
    }

    override suspend fun queryRemark(): FriendNameRemark {
        TODO("not implemented")
    }

    override fun equals(other: Any?): Boolean {
        return other is QQ && other.id == this.id
    }
}


internal class MemberImpl(
    qq: QQImpl,
    override var groupCard: String,
    group: GroupImpl,
    override val coroutineContext: CoroutineContext,
    override val permission: MemberPermission
) : ContactImpl(), Member, QQ by qq {
    override val group: GroupImpl by group.unsafeWeakRef()
    val qq: QQImpl by qq.unsafeWeakRef()

    override val bot: QQAndroidBot get() = qq.bot

    override suspend fun mute(durationSeconds: Int): Boolean {
        if (bot.uin == this.qq.id) {
            return false
        }
        //判断有无禁言权限
        val myPermission = group.botPermission
        val targetPermission = this.permission
        if (myPermission != MemberPermission.OWNER) {
            if (targetPermission == MemberPermission.OWNER || targetPermission == MemberPermission.ADMINISTRATOR) {
                return false
            }
        } else if (myPermission == MemberPermission.MEMBER) {
            return false
        }
        return try {
            bot.network.run {
                TroopManagement.Mute(
                    client = bot.client,
                    groupCode = group.id,
                    memberUin = this@MemberImpl.id,
                    timeInSecond = durationSeconds
                ).sendAndExpect<TroopManagement.Mute.Response>()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun unmute(): Boolean {
        return mute(0)
    }

    override fun equals(other: Any?): Boolean {
        return other is Member && other.id == this.id
    }
}


/**
 * 对GroupImpl
 * 中name/announcement的更改会直接向服务器异步汇报
 */
@UseExperimental(MiraiInternalAPI::class)
internal class GroupImpl(
    bot: QQAndroidBot, override val coroutineContext: CoroutineContext,
    override val id: Long,
    val uin: Long,
    initName: String,
    initAnnouncement: String,
    initAllowMemberInvite: Boolean,
    initConfessTalk: Boolean,
    initMuteAll: Boolean,
    initAutoApprove: Boolean,
    initAnonymousChat: Boolean,
    override var members: ContactList<Member>
) : ContactImpl(), Group {

    override var name by Delegates.observable(initName) { _, oldValue, newValue ->
        if (this.botPermission != MemberPermission.MEMBER && oldValue != newValue) {
            this.bot.launch {
                bot.network.run {
                    TroopManagement.updateGroupInfo.name(
                        client = bot.client,
                        groupCode = id,
                        newName = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }

    override var announcement: String by Delegates.observable(initAnnouncement) { _, oldValue, newValue ->
        if (this.botPermission != MemberPermission.MEMBER && oldValue != newValue) {
            this.bot.launch {
                bot.network.run {
                    TroopManagement.updateGroupInfo.memo(
                        client = bot.client,
                        groupCode = id,
                        newMemo = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }


    override var allowMemberInvite: Boolean by Delegates.observable(initAllowMemberInvite) { _, oldValue, newValue ->
        if (this.botPermission != MemberPermission.MEMBER && oldValue != newValue) {
            this.bot.launch {
                bot.network.run {
                    TroopManagement.updateGroupInfo.allowMemberInvite(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }

    override var autoApprove: Boolean by Delegates.observable(initAutoApprove) { _, oldValue, newValue ->
        //暂时也不能改
    }

    override val anonymousChat: Boolean by Delegates.observable(initAnonymousChat) { _, oldValue, newValue ->
        //暂时不能改
    }

    override var confessTalk: Boolean by Delegates.observable(initConfessTalk) { _, oldValue, newValue ->
        if (this.botPermission != MemberPermission.MEMBER && oldValue != newValue) {
            this.bot.launch {
                bot.network.run {
                    TroopManagement.updateGroupInfo.confessTalk(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }


    override var muteAll: Boolean by Delegates.observable(initMuteAll) { _, oldValue, newValue ->
        if (this.botPermission != MemberPermission.MEMBER && oldValue != newValue) {
            this.bot.launch {
                bot.network.run {
                    TroopManagement.updateGroupInfo.muteAll(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }


    override lateinit var owner: Member
    override var botPermission: MemberPermission = MemberPermission.MEMBER

    override suspend fun quit(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }


    override operator fun get(id: Long): Member {
        return members.delegate.filteringGetOrNull { it.id == id } ?: throw NoSuchElementException("for group id $id")
    }

    override fun contains(id: Long): Boolean {
        return members.delegate.filteringGetOrNull { it.id == id } != null
    }

    override fun getOrNull(id: Long): Member? {
        return members.delegate.filteringGetOrNull { it.id == id }
    }

    override val bot: QQAndroidBot by bot.unsafeWeakRef()

    override suspend fun sendMessage(message: MessageChain) {
        bot.network.run {
            val response = MessageSvc.PbSendMsg.ToGroup(
                bot.client,
                id,
                message
            ).sendAndExpect<MessageSvc.PbSendMsg.Response>()
            check(
                response is MessageSvc.PbSendMsg.Response.SUCCESS
            ) { "send message failed: $response" }
        }
    }

    override suspend fun uploadImage(image: ExternalImage): Image {
        bot.network.run {
            val response: ImgStore.GroupPicUp.Response = ImgStore.GroupPicUp(
                bot.client,
                uin = bot.uin,
                groupCode = id,
                md5 = image.md5,
                size = image.inputSize,
                picWidth = image.width,
                picHeight = image.height,
                picType = image.imageType,
                filename = image.filename
            ).sendAndExpect()

            when (response) {
                is ImgStore.GroupPicUp.Response.Failed -> error("upload group image failed with reason ${response.message}")
                is ImgStore.GroupPicUp.Response.FileExists -> {
                    val resourceId = image.calculateImageResourceId()
                    return NotOnlineImageFromFile(
                        resourceId = resourceId,
                        md5 = response.fileInfo.fileMd5,
                        filepath = resourceId,
                        fileLength = response.fileInfo.fileSize.toInt(),
                        height = response.fileInfo.fileHeight,
                        width = response.fileInfo.fileWidth,
                        imageType = response.fileInfo.fileType
                    )
                }
                is ImgStore.GroupPicUp.Response.RequireUpload -> {

                    val socket = PlatformSocket()
                    socket.connect(response.uploadIpList.first().toIpV4AddressString().also { println("serverIp=$it") }, response.uploadPortList.first())
                    // socket.use {
                    socket.send(
                        Highway.RequestDataTrans(
                            uin = bot.uin,
                            command = "PicUp.DataUp",
                            sequenceId = bot.client.nextHighwayDataTransSequenceId(),
                            uKey = response.uKey,
                            data = image.input,
                            dataSize = image.inputSize.toInt(),
                            md5 = image.md5
                        )
                    )
                    //  }

                    //0A 3C 08 01 12 0A 31 39 39 34 37 30 31 30 32 31 1A 0C 50 69 63 55 70 2E 44 61 74 61 55 70 20 E9 A7 05 28 00 30 BD DB 8B 80 02 38 80 20 40 02 4A 0A 38 2E 32 2E 30 2E 31 32 39 36 50 84 10 12 3D 08 00 10 FD 08 18 00 20 FD 08 28 C6 01 38 00 42 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 4A 10 D4 1D 8C D9 8F 00 B2 04 E9 80 09 98 EC F8 42 7E 50 89 92 A2 FB 06 58 00 60 00 18 53 20 01 28 00 30 04 3A 00 40 E6 B7 F7 D9 80 2E 48 00 50 00
                    socket.read().withUse {
                        discardExact(1)
                        val headLength = readInt()
                        discardExact(4)
                        val proto = readProtoBuf(CSDataHighwayHead.RspDataHighwayHead.serializer(), length = headLength)
                        check(proto.errorCode == 0) { "image upload failed: Transfer errno=${proto.errorCode}" }
                    }
                    socket.close()
                    val resourceId = image.calculateImageResourceId()

                    return CustomFaceFromFile(
                        md5 = image.md5,
                        filepath = resourceId,
                        fileId = response.fileId.toInt(),
                        fileType = 66, // ?
                        height = image.height,
                        width = image.width,
                        imageType = image.imageType,
                        bizType = 0,
                        serverIp = response.uploadIpList.first(),
                        serverPort = response.uploadPortList.first(),
                        signature = image.md5,
                        size = image.inputSize.toInt(),
                        useful = 1,
                        source = 200,
                        origin = 1,
                        pbReserve = byteArrayOf(0x78, 0x02)
                    )
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is Group && other.id == this.id
    }
}
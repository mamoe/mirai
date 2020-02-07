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
import net.mamoe.mirai.qqandroid.network.highway.HighwayHelper
import net.mamoe.mirai.qqandroid.network.protocol.data.proto.Cmd0x352
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.TroopManagement
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.ImgStore
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.image.LongConn
import net.mamoe.mirai.qqandroid.network.protocol.packet.chat.receive.MessageSvc
import net.mamoe.mirai.qqandroid.utils.toIpV4AddressString
import net.mamoe.mirai.utils.*
import net.mamoe.mirai.utils.io.toUHexString
import kotlin.coroutines.CoroutineContext
import kotlin.properties.Delegates

internal abstract class ContactImpl : Contact {
    override fun hashCode(): Int {
        var result = bot.hashCode()
        result = 31 * result + id.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Contact) return false
        if (this::class != other::class) return false
        return this.id == other.id && this.bot == other.bot
    }
}

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
        bot.network.run {
            val response = LongConn.OffPicUp(
                bot.client, Cmd0x352.TryUpImgReq(
                    srcUin = bot.uin.toInt(),
                    dstUin = id.toInt(),
                    fileId = 0,
                    fileMd5 = image.md5,
                    fileSize = image.inputSize.toInt(),
                    fileName = image.md5.toUHexString("") + ".jpg",
                    imgOriginal = 1,
                    imgWidth = image.width,
                    imgHeight = image.height,
                    imgType = image.imageType,
                    buType = 0
                )
            ).sendAndExpect<LongConn.OffPicUp.Response>()

            return when (response) {
                is LongConn.OffPicUp.Response.FileExists -> NotOnlineImageFromFile(
                    filepath = response.resourceId,
                    md5 = response.imageInfo.fileMd5,
                    fileLength = response.imageInfo.fileSize.toInt(),
                    height = response.imageInfo.fileHeight,
                    width = response.imageInfo.fileWidth,
                    resourceId = response.resourceId
                )
                is LongConn.OffPicUp.Response.RequireUpload -> {
                    HighwayHelper.uploadImage(
                        client = bot.client,
                        uin = bot.uin,
                        serverIp = response.serverIp[0].toIpV4AddressString(),
                        serverPort = response.serverPort[0],
                        imageInput = image.input,
                        inputSize = image.inputSize.toInt(),
                        md5 = image.md5,
                        uKey = response.uKey,
                        commandId = 1
                    )

                    return NotOnlineImageFromFile(
                        filepath = response.resourceId,
                        md5 = image.md5,
                        fileLength = image.inputSize.toInt(),
                        height = image.height,
                        width = image.width,
                        resourceId = response.resourceId
                    )
                }
                is LongConn.OffPicUp.Response.Failed -> error(response.message)
            }
        }
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
        if (this === other) return true
        return other is QQ && other.id == this.id
    }

    override fun hashCode(): Int = super.hashCode()
}


internal class MemberImpl(
    qq: QQImpl,
    initGroupCard: String,
    initSpecialTitle: String,
    group: GroupImpl,
    override val coroutineContext: CoroutineContext,
    override val permission: MemberPermission
) : ContactImpl(), Member, QQ by qq {
    override val group: GroupImpl by group.unsafeWeakRef()
    val qq: QQImpl by qq.unsafeWeakRef()


    override var groupCard: String by Delegates.observable(initGroupCard) { _, old, new ->
        group.checkBotPermissionOperator()
        if (new != old) {

            launch {
                bot.network.run {
                    TroopManagement.EditGroupNametag(
                        bot.client,
                        this@MemberImpl,
                        new
                    ).sendWithoutExpect()
                }
            }
        }
    }

    override var specialTitle: String by Delegates.observable(initSpecialTitle) { _, old, new ->
        group.checkBotPermissionOperator()
        if (new != old) {
            launch {
                bot.network.run {
                    TroopManagement.EditSpecialTitle(
                        bot.client,
                        this@MemberImpl,
                        new
                    ).sendWithoutExpect()
                }
            }
        }
    }

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
        if (this === other) return true
        return other is Member && other.id == this.id
    }

    override fun hashCode(): Int = super.hashCode()
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
    override val members: ContactList<Member>
) : ContactImpl(), Group {

    override var name by Delegates.observable(initName) { _, oldValue, newValue ->
        this.checkBotPermissionOperator()
        if (oldValue != newValue) {
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.name(
                        client = bot.client,
                        groupCode = id,
                        newName = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }

    override var announcement: String by Delegates.observable(initAnnouncement) { _, oldValue, newValue ->
        this.checkBotPermissionOperator()
        if (oldValue != newValue) {
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.memo(
                        client = bot.client,
                        groupCode = id,
                        newMemo = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }


    override var allowMemberInvite: Boolean by Delegates.observable(initAllowMemberInvite) { _, oldValue, newValue ->
        this.checkBotPermissionOperator()
        if (oldValue != newValue) {
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.allowMemberInvite(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }

    override var autoApprove: Boolean by Delegates.observable(initAutoApprove) { _, oldValue, newValue ->
        TODO("Group.autoApprove implementation")
    }

    override val anonymousChat: Boolean by Delegates.observable(initAnonymousChat) { _, oldValue, newValue ->
        TODO("Group.anonymousChat implementation")
    }

    override var confessTalk: Boolean by Delegates.observable(initConfessTalk) { _, oldValue, newValue ->
        this.checkBotPermissionOperator()
        if (oldValue != newValue) {
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.confessTalk(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }


    override var muteAll: Boolean by Delegates.observable(initMuteAll) { _, oldValue, newValue ->
        this.checkBotPermissionOperator()
        if (oldValue != newValue) {
            launch {
                bot.network.run {
                    TroopManagement.GroupOperation.muteAll(
                        client = bot.client,
                        groupCode = id,
                        switch = newValue
                    ).sendWithoutExpect()
                }
            }
        }
    }


    override lateinit var owner: Member
    @UseExperimental(MiraiExperimentalAPI::class)
    override var botPermission: MemberPermission = MemberPermission.MEMBER

    override suspend fun quit(): Boolean {
        check(botPermission != MemberPermission.OWNER) { "An owner cannot quit from a owning group" }
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
//                    return NotOnlineImageFromFile(
//                        resourceId = resourceId,
//                        md5 = response.fileInfo.fileMd5,
//                        filepath = resourceId,
//                        fileLength = response.fileInfo.fileSize.toInt(),
//                        height = response.fileInfo.fileHeight,
//                        width = response.fileInfo.fileWidth,
//                        imageType = response.fileInfo.fileType,
//                        fileId = response.fileId.toInt()
//                    )
                    //  println("NMSL")
                    return CustomFaceFromFile(
                        md5 = image.md5,
                        filepath = resourceId
                    )
                }
                is ImgStore.GroupPicUp.Response.RequireUpload -> {

                    HighwayHelper.uploadImage(
                        client = bot.client,
                        uin = bot.uin,
                        serverIp = response.uploadIpList.first().toIpV4AddressString(),
                        serverPort = response.uploadPortList.first(),
                        imageInput = image.input,
                        inputSize = image.inputSize.toInt(),
                        md5 = image.md5,
                        uKey = response.uKey,
                        commandId = 2
                    )
                    val resourceId = image.calculateImageResourceId()
                    // return NotOnlineImageFromFile(
                    //     resourceId = resourceId,
                    //     md5 = image.md5,
                    //     filepath = resourceId,
                    //     fileLength = image.inputSize.toInt(),
                    //     height = image.height,
                    //     width = image.width,
                    //     imageType = image.imageType,
                    //     fileId = response.fileId.toInt()
                    // )
                    return CustomFaceFromFile(
                        md5 = image.md5,
                        filepath = resourceId
                    )
                    /*
                        fileId = response.fileId.toInt(),
                        fileType = 0, // ?
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
                        original = 1,
                        pbReserve = EMPTY_BYTE_ARRAY
                     */
                }
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return other is Group && other.id == this.id
    }

    override fun hashCode(): Int = super.hashCode()
}
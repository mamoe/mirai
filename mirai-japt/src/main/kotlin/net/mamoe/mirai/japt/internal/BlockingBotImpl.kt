/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.japt.internal

import kotlinx.coroutines.runBlocking
import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.readBytes
import net.mamoe.mirai.Bot
import net.mamoe.mirai.BotAccount
import net.mamoe.mirai.contact.QQ
import net.mamoe.mirai.data.AddFriendResult
import net.mamoe.mirai.data.GroupInfo
import net.mamoe.mirai.data.MemberInfo
import net.mamoe.mirai.japt.BlockingBot
import net.mamoe.mirai.japt.BlockingGroup
import net.mamoe.mirai.japt.BlockingQQ
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.MiraiInternalAPI
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.toList
import java.io.OutputStream
import java.util.stream.Stream
import kotlin.streams.asStream

internal class BlockingBotImpl(private val bot: Bot) : BlockingBot {
    @MiraiInternalAPI
    override fun getAccount(): BotAccount = bot.account

    override fun getUin(): Long = bot.uin
    @MiraiExperimentalAPI
    override fun getNick(): String = bot.nick

    override fun getLogger(): MiraiLogger = bot.logger
    override fun getSelfQQ(): QQ = bot.selfQQ

    override fun queryGroupMemberList(groupUin: Long, groupCode: Long, ownerId: Long): Stream<MemberInfo> =
        runBlocking { bot.queryGroupMemberList(groupUin, groupCode, ownerId) }.asStream()

    @UseExperimental(MiraiInternalAPI::class)
    override fun getFriendList(): List<BlockingQQ> = bot.qqs.delegate.toList().map { it.blocking() }

    override fun getFriend(id: Long): BlockingQQ = bot.getFriend(id).blocking()
    override fun queryGroupList(): Stream<Long> = runBlocking { bot.queryGroupList() }.asStream()

    @UseExperimental(MiraiInternalAPI::class)
    override fun getGroupList(): List<BlockingGroup> = bot.groups.delegate.toList().map { it.blocking() }

    override fun queryGroupInfo(code: Long): GroupInfo = runBlocking { bot.queryGroupInfo(code) }

    override fun getGroup(id: Long): BlockingGroup = runBlocking { bot.getGroup(id).blocking() }
    override fun getNetwork(): BotNetworkHandler = bot.network
    override fun login() = runBlocking { bot.login() }
    override fun downloadAsByteArray(image: Image): ByteArray = bot.run { runBlocking { image.download().readBytes() } }
    override fun download(image: Image): ByteReadPacket = bot.run { runBlocking { image.download() } }
    override fun download(image: Image, outputStream: OutputStream) = bot.run { runBlocking { image.downloadTo(outputStream) } }

    override fun addFriend(id: Long, message: String?, remark: String?): AddFriendResult = runBlocking { bot.addFriend(id, message, remark) }
    override fun approveFriendAddRequest(id: Long, remark: String?) = runBlocking { bot.approveFriendAddRequest(id, remark) }
    override fun dispose(throwable: Throwable?) = bot.close(throwable)
}
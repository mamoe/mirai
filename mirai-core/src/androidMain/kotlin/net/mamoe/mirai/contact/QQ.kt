@file:Suppress("unused")

package net.mamoe.mirai.contact

import kotlinx.coroutines.CoroutineScope
import net.mamoe.mirai.Bot
import net.mamoe.mirai.data.FriendNameRemark
import net.mamoe.mirai.data.PreviousNameList
import net.mamoe.mirai.data.Profile
import net.mamoe.mirai.event.events.BeforeImageUploadEvent
import net.mamoe.mirai.event.events.EventCancelledException
import net.mamoe.mirai.event.events.ImageUploadEvent
import net.mamoe.mirai.event.events.MessageSendEvent.FriendMessageSendEvent
import net.mamoe.mirai.event.events.MessageSendEvent.GroupMessageSendEvent
import net.mamoe.mirai.message.MessageReceipt
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.OfflineFriendImage
import net.mamoe.mirai.utils.ExternalImage
import net.mamoe.mirai.utils.MiraiExperimentalAPI
import net.mamoe.mirai.utils.OverFileSizeMaxException

/**
 * QQ 对象.
 * 注意: 一个 [QQ] 实例并不是独立的, 它属于一个 [Bot].
 * 它不能被直接构造. 任何时候都应从 [Bot.getFriend] 或事件中获取.
 *
 * 对于同一个 [Bot] 任何一个人的 [QQ] 实例都是单一的.
 *
 * A QQ instance helps you to receive event from or sendPacket event to.
 * Notice that, one QQ instance belong to one [Bot], that is, QQ instances from different [Bot] are NOT the same.
 */
@Suppress("INAPPLICABLE_JVM_NAME")
actual abstract class QQ : Contact(), CoroutineScope {
    /**
     * 请求头像下载链接
     */
    // @MiraiExperimentalAPI
    //suspend fun queryAvatar(): AvatarLink
    /**
     * QQ 号码
     */
    actual abstract override val id: Long
    /**
     * 昵称
     */
    actual abstract val nick: String

    /**
     * 查询用户资料
     */
    @MiraiExperimentalAPI("还未支持")
    actual abstract suspend fun queryProfile(): Profile

    /**
     * 头像下载链接
     */
    actual val avatarUrl: String
        get() = "http://q1.qlogo.cn/g?b=qq&nk=$id&s=640"

    /**
     * 查询曾用名.
     *
     * 曾用名可能是:
     * - 昵称
     * - 共同群内的群名片
     */
    @MiraiExperimentalAPI("还未支持")
    actual abstract suspend fun queryPreviousNameList(): PreviousNameList

    /**
     * 查询机器人账号给这个人设置的备注
     */
    @MiraiExperimentalAPI("还未支持")
    actual abstract suspend fun queryRemark(): FriendNameRemark

    /**
     * 向这个对象发送消息.
     *
     * 单条消息最大可发送 4500 字符或 50 张图片.
     *
     * @see FriendMessageSendEvent 发送好友信息事件, cancellable
     * @see GroupMessageSendEvent  发送群消息事件. cancellable
     *
     * @throws EventCancelledException 当发送消息事件被取消时抛出
     * @throws BotIsBeingMutedException 发送群消息时若 [Bot] 被禁言抛出
     * @throws MessageTooLargeException 当消息过长时抛出
     *
     * @return 消息回执. 可进行撤回 ([MessageReceipt.recall])
     */
    @JvmSynthetic
    actual abstract override suspend fun sendMessage(message: Message): MessageReceipt<QQ>

    /**
     * 上传一个图片以备发送.
     *
     * @see BeforeImageUploadEvent 图片发送前事件, cancellable
     * @see ImageUploadEvent 图片发送完成事件
     *
     * @throws EventCancelledException 当发送消息事件被取消
     * @throws OverFileSizeMaxException 当图片文件过大而被服务器拒绝上传时. (最大大小约为 20 MB)
     */
    @JvmSynthetic
    actual abstract override suspend fun uploadImage(image: ExternalImage): OfflineFriendImage
}
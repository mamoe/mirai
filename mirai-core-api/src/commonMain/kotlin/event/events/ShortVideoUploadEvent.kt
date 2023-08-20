/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import net.mamoe.mirai.event.events.ShortVideoUploadEvent.Failed
import net.mamoe.mirai.event.events.ShortVideoUploadEvent.Succeed
import net.mamoe.mirai.internal.event.VerboseEvent
import net.mamoe.mirai.message.data.ShortVideo
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 短视频上传前. 可以阻止上传.
 *
 * 此事件总是在 [ShortVideoUploadEvent] 之前广播.
 * 若此事件被取消, [ShortVideoUploadEvent] 不会广播.
 *
 * @see Contact.uploadShortVideo 上传短视频. 为广播这个事件的唯一途径
 * @since 2.16
 */
@OptIn(MiraiInternalApi::class)
public class BeforeShortVideoUploadEvent @MiraiInternalApi constructor(
    public val target: Contact,
    public val thumbnailSource: ExternalResource,
    public val videoSource: ExternalResource
) : BotEvent, BotActiveEvent, AbstractEvent(), CancellableEvent, VerboseEvent {
    public override val bot: Bot
        get() = target.bot
}

/**
 * 短视频上传完成.
 *
 * 此事件总是在 [BeforeImageUploadEvent] 之后广播.
 * 若 [BeforeImageUploadEvent] 被取消, 此事件不会广播.
 *
 * @see Contact.uploadShortVideo 上传短视频. 为广播这个事件的唯一途径
 * @see Succeed
 * @see Failed
 * @since 2.16
 */
@OptIn(MiraiInternalApi::class)
public sealed class ShortVideoUploadEvent : BotEvent, BotActiveEvent, AbstractEvent(), VerboseEvent {
    public abstract val target: Contact
    public abstract val thumbnailSource: ExternalResource
    public abstract val videoSource: ExternalResource
    public override val bot: Bot
        get() = target.bot

    public class Succeed @MiraiInternalApi constructor(
        override val target: Contact,
        override val thumbnailSource: ExternalResource,
        override val videoSource: ExternalResource,
        public val video: ShortVideo
    ) : ShortVideoUploadEvent() {
        override fun toString(): String {
            return "ShortVideoUploadEvent.Succeed(target=$target, " +
                    "thumbnailSource=$thumbnailSource, " +
                    "videoSource=$videoSource, " +
                    "video=$video)"
        }
    }

    public class Failed @MiraiInternalApi constructor(
        override val target: Contact,
        override val thumbnailSource: ExternalResource,
        override val videoSource: ExternalResource,
        public val errno: Int,
        public val message: String
    ) : ShortVideoUploadEvent() {
        override fun toString(): String {
            return "ShortVideoUploadEvent.Failed(target=$target, " +
                    "thumbnailSource=$thumbnailSource, " +
                    "videoSource=$videoSource, " +
                    "errno=$errno, message='$message')"
        }
    }
}

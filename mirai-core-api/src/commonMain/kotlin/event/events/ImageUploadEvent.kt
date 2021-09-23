/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("BotEventsKt")

package net.mamoe.mirai.event.events

import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.contact.Contact.Companion.uploadImage
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.event.CancellableEvent
import net.mamoe.mirai.event.events.ImageUploadEvent.Failed
import net.mamoe.mirai.event.events.ImageUploadEvent.Succeed
import net.mamoe.mirai.internal.event.VerboseEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 图片上传前. 可以阻止上传.
 *
 * 此事件总是在 [ImageUploadEvent] 之前广播.
 * 若此事件被取消, [ImageUploadEvent] 不会广播.
 *
 * @see Contact.uploadImage 上传图片. 为广播这个事件的唯一途径
 */
public data class BeforeImageUploadEvent @MiraiInternalApi constructor(
    public val target: Contact,
    public val source: ExternalResource
) : BotEvent, BotActiveEvent, AbstractEvent(), CancellableEvent, VerboseEvent {
    public override val bot: Bot
        get() = target.bot
}

/**
 * 图片上传完成.
 *
 * 此事件总是在 [BeforeImageUploadEvent] 之后广播.
 * 若 [BeforeImageUploadEvent] 被取消, 此事件不会广播.
 *
 * @see Contact.uploadImage 上传图片. 为广播这个事件的唯一途径
 *
 * @see Succeed
 * @see Failed
 */
public sealed class ImageUploadEvent : BotEvent, BotActiveEvent, AbstractEvent(), VerboseEvent {
    public abstract val target: Contact
    public abstract val source: ExternalResource
    public override val bot: Bot
        get() = target.bot

    public data class Succeed @MiraiInternalApi constructor(
        override val target: Contact,
        override val source: ExternalResource,
        val image: Image
    ) : ImageUploadEvent() {
        override fun toString(): String {
            return "ImageUploadEvent.Succeed(target=$target, source=$source, image=$image)"
        }
    }

    public data class Failed @MiraiInternalApi constructor(
        override val target: Contact,
        override val source: ExternalResource,
        val errno: Int,
        val message: String
    ) : ImageUploadEvent() {
        override fun toString(): String {
            return "ImageUploadEvent.Failed(target=$target, source=$source, errno=$errno, message='$message')"
        }
    }
}

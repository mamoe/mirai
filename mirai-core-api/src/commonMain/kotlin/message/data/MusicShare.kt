/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused")

package net.mamoe.mirai.message.data

import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.safeCast

/**
 * QQ 互联通道音乐分享
 *
 * @since 2.1
 */
@MiraiExperimentalApi("This is very experimental")
public class MusicShare @MiraiExperimentalApi("Constructor is subject to change") constructor(
    public val type: MusicType,
    public val title: String,
    public val summary: String,
    public val brief: String,
    public val url: String,
    public val pictureUrl: String,
    public val musicUrl: String,
) : MessageContent, ConstrainSingle {

    override val key: MessageKey<*> get() = Key

    @MiraiExperimentalApi
    override fun contentToString(): String {
        return "[$title]"
    }

    override fun toString(): String {
        return "MusicShare(type=$type, title='$title', summary='$summary', brief='$brief', url='$url', pictureUrl='$pictureUrl', musicUrl='$musicUrl')"
    }


    // don't make this class 'data' unless we made it stable.

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MusicShare

        if (type != other.type) return false
        if (title != other.title) return false
        if (summary != other.summary) return false
        if (brief != other.brief) return false
        if (url != other.url) return false
        if (pictureUrl != other.pictureUrl) return false
        if (musicUrl != other.musicUrl) return false

        return true
    }

    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + title.hashCode()
        result = 31 * result + summary.hashCode()
        result = 31 * result + brief.hashCode()
        result = 31 * result + url.hashCode()
        result = 31 * result + pictureUrl.hashCode()
        result = 31 * result + musicUrl.hashCode()
        return result
    }


    public companion object Key :
        AbstractPolymorphicMessageKey<MessageContent, MusicShare>(MessageContent, { it.safeCast() })
}

/**
 * @since 2.1
 */
@MiraiExperimentalApi
public enum class MusicType constructor(
    @MiraiInternalApi public val appId: Long,
    @MiraiInternalApi public val platform: Int,
    @MiraiInternalApi public val sdkVersion: String,
    @MiraiInternalApi public val packageName: String,
    @MiraiInternalApi public val signature: String
) {
    NeteaseCloudMusic(
        100495085,
        1,
        "0.0.0",
        "com.netease.cloudmusic",
        "da6b069da1e2982db3e386233f68d76d"
    ),
    QQMusic(
        100497308,
        1,
        "0.0.0",
        "com.tencent.qqmusic",
        "cbd27cd7c861227d013a25b2d10f0799"
    ),
    MiguMusic(
        1101053067,
        1,
        "0.0.0",
        "cmccwm.mobilemusic",
        "6cdc72a439cef99a3418d2a78aa28c73"
    )
}
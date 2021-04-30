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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.mamoe.mirai.message.code.CodableMessage
import net.mamoe.mirai.message.code.internal.appendStringAsMiraiCode
import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.safeCast

/**
 * QQ 互联通道音乐分享.
 *
 * 构造实例即可使用.
 *
 * @since 2.1
 */
@Serializable
@SerialName(MusicShare.SERIAL_NAME)
public data class MusicShare(
    /**
     * 音乐应用类型
     */
    public val kind: MusicKind, // 'type' is reserved by serialization
    /**
     * 消息卡片标题. 例如 `"ファッション"`
     */
    public val title: String,
    /**
     * 消息卡片内容. 例如 `"rinahamu/Yunomi"`
     */
    public val summary: String,
    /**
     * 点击卡片跳转网页 URL. 例如 `"http://music.163.com/song/1338728297/?userid=324076307"`
     */
    public val jumpUrl: String,
    /**
     * 消息卡片图片 URL. 例如 `"http://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg"`
     */
    public val pictureUrl: String,
    /**
     * 音乐文件 URL. 例如 `"http://music.163.com/song/media/outer/url?id=1338728297&userid=324076307"`
     */
    public val musicUrl: String,
    /**
     * 在消息列表显示. 例如 `"[分享]ファッション"`
     */
    public val brief: String,
) : MessageContent, ConstrainSingle, CodableMessage {

    /*
     * 想试试? 可以构造:

       // Kotlin
       MusicShare(
           kind = MusicKind.NeteaseCloudMusic,
           title = "ファッション",
           summary = "rinahamu/Yunomi",
           brief = "",
           jumpUrl = "http://music.163.com/song/1338728297/?userid=324076307",
           pictureUrl = "http://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg",
           musicUrl = "http://music.163.com/song/media/outer/url?id=1338728297&userid=324076307"
       )

       // Java
       new MusicShare(
           MusicKind.NeteaseCloudMusic,
           "ファッション",
           "rinahamu/Yunomi",
           "http://music.163.com/song/1338728297/?userid=324076307",
           "http://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg",
           "http://music.163.com/song/media/outer/url?id=1338728297&userid=324076307"
       );

     */

    public constructor(
        /**
         * 音乐应用类型
         */
        kind: MusicKind,
        /**
         * 消息卡片标题
         */
        title: String,
        /**
         * 消息卡片内容
         */
        summary: String,
        /**
         * 点击卡片跳转网页 URL
         */
        jumpUrl: String,
        /**
         * 消息卡片图片 URL
         */
        pictureUrl: String,
        /**
         * 音乐文件 URL
         */
        musicUrl: String,
    ) : this(kind, title, summary, jumpUrl, pictureUrl, musicUrl, "[分享]$title")
    // kotlinx serialization doesn't support default arguments.

    override val key: MessageKey<*> get() = Key

    override fun contentToString(): String =
        brief.takeIf { it.isNotBlank() } ?: "[分享]$title" // empty content is not accepted by `sendMessage`

    override fun appendMiraiCodeTo(builder: StringBuilder) {
        builder.append("[mirai:musicshare:")
            .append(kind.name)
            .append(',').appendStringAsMiraiCode(title)
            .append(',').appendStringAsMiraiCode(summary)
            .append(',').appendStringAsMiraiCode(jumpUrl)
            .append(',').appendStringAsMiraiCode(pictureUrl)
            .append(',').appendStringAsMiraiCode(musicUrl)
            .append(',').appendStringAsMiraiCode(brief)
            .append(']')
    }


    /**
     * 注意, baseKey [MessageContent] 不稳定. 未来可能会有变更.
     */
    public companion object Key :
        AbstractPolymorphicMessageKey<@MiraiExperimentalApi MessageContent, MusicShare>
            (MessageContent, { it.safeCast() }) {

        /**
         * @since 2.3
         */
        public const val SERIAL_NAME: String = "MusicShare"
    }
}

/**
 * @see MusicShare.kind
 * @since 2.1
 */
public enum class MusicKind constructor(
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
    ),
    /**
     * @since 2.7
     */
    KugouMusic(
        205141,
        1,
        "0.0.0",
        "com.kugou.android",
        "fe4a24d80fcf253a00676a808f62c2c6"
    ),
    /**
     * @since 2.7
     */
    KuwoMusic(
        100243533,
        1,
        "0.0.0",
        "cn.kuwo.player",
        "bf9ff4ffb4c558a34ee3fd52c223ebf5"
    )

    // add more?  https://github.com/mamoe/mirai/issues/new/choose
}

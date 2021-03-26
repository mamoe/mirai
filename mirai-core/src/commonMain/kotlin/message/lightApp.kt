/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.contact.Contact
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.safeCast

internal data class LightAppInternal(
    override val content: String
) : RichMessage, RefinableMessage {
    companion object Key :
        AbstractPolymorphicMessageKey<RichMessage, LightAppInternal>(RichMessage, { it.safeCast() })

    override suspend fun refine(contact: Contact, context: MessageChain): Message {
        val struct = tryDeserialize() ?: return LightApp(content)
        struct.run {
            if (meta.music != null) {
                MusicKind.values().find { it.appId.toInt() == meta.music.appid }?.let { musicType ->
                    meta.music.run {
                        return MessageOrigin(
                            LightApp(content),
                            null,
                            RichMessageKind.MUSIC_SHARE
                        ) + MusicShare(
                            kind = musicType, title = title, summary = desc,
                            jumpUrl = jumpUrl, pictureUrl = preview, musicUrl = musicUrl, brief = prompt
                        )
                    }
                }
            }
        }

        return LightApp(content)
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

internal fun LightAppInternal.tryDeserialize(): LightAppStruct? {
    return kotlin.runCatching {
        json.decodeFromString(LightAppStruct.serializer(), this.content)
    }.getOrNull()
}

/*
EXAMPLE LightAppStruct for MusicShare

{
  "app": "com.tencent.structmsg",
  "config": {
    "autosize": true,
    "ctime": 1611339208,
    "forward": true,
    "token": "1f27c2b5687e0320549992a4652c8465",
    "type": "normal"
  },
  "desc": "音乐",
  "extra": {
    "app_type": 1,
    "appid": 100495085, // NeteaseCloudMusic
    "uin": 123456789 // qq uin
  },
  "meta": {
    "music": {
      "action": "",
      "android_pkg_name": "",
      "app_type": 1,
      "appid": 100495085,
      "desc": "rinahamu/Yunomi",
      "jumpUrl": "http://music.163.com/song/1338728297/?userid=324076307",
      "musicUrl": "http://music.163.com/song/media/outer/url?id=1338728297&userid=324076307",
      "preview": "http://p2.music.126.net/y19E5SadGUmSR8SZxkrNtw==/109951163785855539.jpg",
      "sourceMsgId": "0",
      "source_icon": "",
      "source_url": "",
      "tag": "网易云音乐",
      "title": "ファッション"
    }
  },
  "prompt": "[分享]ファッション",
  "ver": "0.0.0.1",
  "view": "music"
}
 */

@Serializable
internal data class LightAppStruct(
    @SerialName("app")
    val app: String = "",
    @SerialName("config")
    val config: Config = Config(),
    @SerialName("desc")
    val desc: String = "",
    @SerialName("extra")
    val extra: Extra = Extra(),
    @SerialName("meta")
    val meta: Meta = Meta(),
    @SerialName("prompt")
    val prompt: String = "",
    @SerialName("ver")
    val ver: String = "",
    @SerialName("view")
    val view: String = ""
) {
    @Serializable
    data class Config(
        @SerialName("autosize")
        val autosize: Boolean = false,
        @SerialName("ctime")
        val ctime: Int = 0,
        @SerialName("forward")
        val forward: Boolean = false,
        @SerialName("token")
        val token: String = "",
        @SerialName("type")
        val type: String = ""
    )

    @Serializable
    data class Extra(
        @SerialName("app_type")
        val appType: Int = 0,
        @SerialName("appid")
        val appid: Int = 0,
        @SerialName("uin")
        val uin: Int = 0
    )

    @Serializable
    data class Meta(
        @SerialName("music")
        val music: Music? = null
    ) {
        @Serializable
        data class Music(
            @SerialName("action")
            val action: String = "",
            @SerialName("android_pkg_name")
            val androidPkgName: String = "",
            @SerialName("app_type")
            val appType: Int = 0,
            @SerialName("appid")
            val appid: Int = 0,
            @SerialName("desc")
            val desc: String = "",
            @SerialName("jumpUrl")
            val jumpUrl: String = "",
            @SerialName("musicUrl")
            val musicUrl: String = "",
            @SerialName("preview")
            val preview: String = "",
            @SerialName("source_icon")
            val sourceIcon: String = "",
            @SerialName("sourceMsgId")
            val sourceMsgId: String = "",
            @SerialName("source_url")
            val sourceUrl: String = "",
            @SerialName("tag")
            val tag: String = "",
            @SerialName("title")
            val title: String = ""
        )
    }
}

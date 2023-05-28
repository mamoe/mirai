/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.message.data

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.internal.message.RefinableMessage
import net.mamoe.mirai.internal.message.RefineContext
import net.mamoe.mirai.message.data.*
import net.mamoe.mirai.utils.map
import net.mamoe.mirai.utils.safeCast

internal data class LightAppInternal(
    override val content: String,
) : RichMessage, RefinableMessage {
    companion object Key :
        AbstractPolymorphicMessageKey<RichMessage, LightAppInternal>(RichMessage, { it.safeCast() })

    override fun tryRefine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message {
        val contentJson = runCatching {
            json.parseToJsonElement(content).jsonObject
        }.getOrNull() ?: return LightApp(content)

        val struct = contentJson.tryDeserialize() ?: return LightApp(content)

        return lightRefine(struct, contentJson) ?: LightApp(content)
    }

    private fun lightRefine(struct: LightAppStruct, contentJson: JsonObject): Message? {

        struct.run {
            if (meta.music != null) {
                MusicKind.values().find { it.appId == meta.music.appid }?.let { musicType ->
                    meta.music.run {
                        return MessageOrigin(
                            LightApp(content),
                            null,
                            MessageOriginKind.MUSIC_SHARE
                        ) + MusicShare(
                            kind = musicType, title = title, summary = desc,
                            jumpUrl = jumpUrl, pictureUrl = preview, musicUrl = musicUrl, brief = prompt
                        )
                    }
                }
            }
        }

        return null
    }

    override suspend fun refine(bot: Bot, context: MessageChain, refineContext: RefineContext): Message? {
        val contentJson = runCatching {
            json.parseToJsonElement(content).jsonObject
        }.getOrNull() ?: return LightApp(content)

        val struct = contentJson.tryDeserialize() ?: return LightApp(content)

        if (struct.app == "com.tencent.multimsg") {
            runCatching {
                json.decodeFromJsonElement(
                    LightAppStruct.Meta.MultiMsgDetail.serializer(),
                    contentJson["meta"]!!.jsonObject["detail"]!!
                )
            }.onSuccess { detail ->
                return MessageOrigin(
                    LightApp(content),
                    detail.resId,
                    MessageOriginKind.FORWARD,
                ) + ForwardMessage(
                    preview = listOf(), // FIXME preview with LightApp
                    title = detail.source.trim(),
                    brief = struct.prompt.trim(),
                    source = detail.source.trim(),
                    summary = detail.summary.trim(),
                    nodeList = Mirai.downloadForwardMessage(bot, detail.resId),
                )
            }.onFailure { err ->
                bot.logger.warning("Exception when refining forward message", err)
            }
        }


        return lightRefine(struct, contentJson) ?: LightApp(content)
    }
}

private val json = Json {
    ignoreUnknownKeys = true
    isLenient = true
}

private fun JsonElement.tryDeserialize(): LightAppStruct? {
    return kotlin.runCatching {
        json.decodeFromJsonElement(LightAppStruct.serializer(), this)
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
/*
EXAMPLE LightAppStruct for ForwardMessage

{
    "app": "com.tencent.multimsg",
    "desc": "[聊天记录]",
    "bizsrc": "",
    "view": "contact",
    "ver": "0.0.0.5",
    "prompt": "[聊天记录]",
    "appID": "",
    "sourceName": "",
    "actionData": "",
    "actionData_A": "",
    "sourceUrl": "",
    "meta": {
        "detail": {
            "news": [
                {
                    "text": "纤绫·洛雨:  [动画表情]"
                },
                {
                    "text": "纤绫·洛雨:  [图片]"
                }
            ],
            "uniseq": "7238251206428406430",
            "resid": "...",
            "summary": "查看2条转发消息",
            "source": "群聊的聊天记录"
        }
    },
    "config": {
        "round": 1,
        "forward": 1,
        "autosize": 1,
        "type": "normal",
        "width": 300
    },
    "text": "",
    "sourceAd": "",
    "extra": "{\"tsum\":2,\"filename\":\"7238251206428406430\"}"
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
    // @SerialName("extra")
    // val extra: Extra = Extra(),
    @SerialName("meta")
    val meta: Meta = Meta(),
    @SerialName("prompt")
    val prompt: String = "",
    @SerialName("ver")
    val ver: String = "",
    @SerialName("view")
    val view: String = "",
) {
    @Serializable
    data class Config(
        @SerialName("autosize")
        @Serializable(BadBooleanSerializer::class)
        val autosize: Boolean = false,
        @SerialName("ctime")
        val ctime: Long = 0,
        @SerialName("forward")
        @Serializable(BadBooleanSerializer::class)
        val forward: Boolean = false,
        @SerialName("token")
        val token: String = "",
        @SerialName("type")
        val type: String = "",
    )

    @Serializable
    data class Extra(
        @SerialName("app_type")
        val appType: Long = 0,
        @SerialName("appid")
        val appid: Long = 0,
        @SerialName("uin")
        val uin: Long = 0,
    )

    @Serializable
    data class Meta(
        @SerialName("music")
        val music: Music? = null,
    ) {
        @Serializable
        data class Music(
            @SerialName("action")
            val action: String = "",
            @SerialName("android_pkg_name")
            val androidPkgName: String = "",
            @SerialName("app_type")
            val appType: Long = 0,
            @SerialName("appid")
            val appid: Long = 0,
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
            val title: String = "",
        )

        @Serializable
        data class MultiMsgDetail(
            @SerialName("uniseq")
            val fileName: String,
            @SerialName("resid")
            val resId: String,

            @SerialName("summary")
            val summary: String = "",
            @SerialName("source")
            val source: String = "",
        )
    }
}

private object BadBooleanSerializer : KSerializer<Boolean> by JsonPrimitive.serializer().map(
    JsonPrimitive.serializer().descriptor,
    deserialize = { prime ->
        prime.booleanOrNull?.let { return@map it }

        prime.intOrNull?.let { return@map it != 0 }

        return@map prime.content.toBoolean()
    },
    serialize = { JsonPrimitive(it) }
)


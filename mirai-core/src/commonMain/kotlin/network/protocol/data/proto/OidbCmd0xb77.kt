/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "SpellCheckingInspection")

package net.mamoe.mirai.internal.network.protocol.data.proto

import kotlinx.serialization.Serializable
import kotlinx.serialization.protobuf.ProtoNumber
import net.mamoe.mirai.internal.network.Packet
import net.mamoe.mirai.internal.utils.io.ProtoBuf

@Serializable
internal class OidbCmd0xb77 : ProtoBuf {
    @Serializable
    internal class ArkJsonBody(
        @JvmField @ProtoNumber(10) val jsonStr: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ArkMsgBody(
        @JvmField @ProtoNumber(1) val app: String = "",
        @JvmField @ProtoNumber(2) val view: String = "",
        @JvmField @ProtoNumber(3) val prompt: String = "",
        @JvmField @ProtoNumber(4) val ver: String = "",
        @JvmField @ProtoNumber(5) val desc: String = "",
        @JvmField @ProtoNumber(6) val featureId: Int = 0,
        @JvmField @ProtoNumber(10) val meta: String = "",
        @JvmField @ProtoNumber(11) val metaUrl1: String = "",
        @JvmField @ProtoNumber(12) val metaUrl2: String = "",
        @JvmField @ProtoNumber(13) val metaUrl3: String = "",
        @JvmField @ProtoNumber(14) val metaText1: String = "",
        @JvmField @ProtoNumber(15) val metaText2: String = "",
        @JvmField @ProtoNumber(16) val metaText3: String = "",
        @JvmField @ProtoNumber(20) val config: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ArkV1MsgBody(
        @JvmField @ProtoNumber(1) val app: String = "",
        @JvmField @ProtoNumber(2) val view: String = "",
        @JvmField @ProtoNumber(3) val prompt: String = "",
        @JvmField @ProtoNumber(4) val ver: String = "",
        @JvmField @ProtoNumber(5) val desc: String = "",
        @JvmField @ProtoNumber(6) val featureId: Int = 0,
        @JvmField @ProtoNumber(10) val meta: String = "",
        @JvmField @ProtoNumber(11) val items: List<TemplateItem> = emptyList(),
        @JvmField @ProtoNumber(20) val config: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ClientInfo(
        @JvmField @ProtoNumber(1) val platform: Int = 0,
        @JvmField @ProtoNumber(2) val sdkVersion: String = "",
        @JvmField @ProtoNumber(3) val androidPackageName: String = "",
        @JvmField @ProtoNumber(4) val androidSignature: String = "",
        @JvmField @ProtoNumber(5) val iosBundleId: String = "",
        @JvmField @ProtoNumber(6) val pcSign: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ImageInfo(
        @JvmField @ProtoNumber(1) val md5: String = "",
        @JvmField @ProtoNumber(2) val uuid: String = "",
        @JvmField @ProtoNumber(3) val imgType: Int = 0,
        @JvmField @ProtoNumber(4) val fileSize: Int = 0,
        @JvmField @ProtoNumber(5) val width: Int = 0,
        @JvmField @ProtoNumber(6) val height: Int = 0,
        @JvmField @ProtoNumber(7) val original: Int = 0,
        @JvmField @ProtoNumber(101) val fileId: Int = 0,
        @JvmField @ProtoNumber(102) val serverIp: Int = 0,
        @JvmField @ProtoNumber(103) val serverPort: Int = 0
    ) : ProtoBuf

    @Serializable
    internal class MiniAppMsgBody(
        @JvmField @ProtoNumber(1) val miniAppAppid: Long = 0L,
        @JvmField @ProtoNumber(2) val miniAppPath: String = "",
        @JvmField @ProtoNumber(3) val webPageUrl: String = "",
        @JvmField @ProtoNumber(4) val miniAppType: Int = 0,
        @JvmField @ProtoNumber(5) val title: String = "",
        @JvmField @ProtoNumber(6) val desc: String = "",
        @JvmField @ProtoNumber(10) val jsonStr: String = ""
    ) : ProtoBuf

    @Serializable
    internal class ReqBody(
        @JvmField @ProtoNumber(1) val appid: Long = 0L,
        @JvmField @ProtoNumber(2) val appType: Int = 0,
        @JvmField @ProtoNumber(3) val msgStyle: Int = 0,
        @JvmField @ProtoNumber(4) val senderUin: Long = 0L,
        @JvmField @ProtoNumber(5) val clientInfo: ClientInfo? = null,
        // @JvmField @ProtoNumber(6) val textMsg: String? = null,
        @JvmField @ProtoNumber(7) val extInfo: ExtInfo? = null,
        @JvmField @ProtoNumber(10) val sendType: Int = 0,
        @JvmField @ProtoNumber(11) val recvUin: Long = 0L,
        @JvmField @ProtoNumber(12) val richMsgBody: RichMsgBody? = null,
        @JvmField @ProtoNumber(13) val arkMsgBody: ArkMsgBody? = null,
        // @JvmField @ProtoNumber(14) val recvOpenid: String? = null, // don't be ""
        @JvmField @ProtoNumber(15) val arkv1MsgBody: ArkV1MsgBody? = null,
        @JvmField @ProtoNumber(16) val arkJsonBody: ArkJsonBody? = null,
        @JvmField @ProtoNumber(17) val xmlMsgBody: XmlMsgBody? = null,
        @JvmField @ProtoNumber(18) val miniAppMsgBody: MiniAppMsgBody? = null
    ) : ProtoBuf

    @Serializable
    internal class ExtInfo(
        @ProtoNumber(1) @JvmField val customFeatureId: List<Int> = emptyList(),
        @ProtoNumber(2) @JvmField val apnsWording: String = "",
        @ProtoNumber(3) @JvmField val groupSaveDbFlag: Int = 0,
        @ProtoNumber(4) @JvmField val receiverAppId: Int = 0,
        @ProtoNumber(5) @JvmField val msgSeq: Long = 0L,
    ) : ProtoBuf

    @Serializable
    internal class RichMsgBody(
        @JvmField @ProtoNumber(1) val usingArk: Boolean = false,
        @JvmField @ProtoNumber(10) val title: String = "",
        @JvmField @ProtoNumber(11) val summary: String = "",
        @JvmField @ProtoNumber(12) val brief: String = "",
        @JvmField @ProtoNumber(13) val url: String = "",
        @JvmField @ProtoNumber(14) val pictureUrl: String = "",
        @JvmField @ProtoNumber(15) val action: String = "",
        @JvmField @ProtoNumber(16) val musicUrl: String = "",
        @JvmField @ProtoNumber(21) val imageInfo: ImageInfo? = null
    ) : ProtoBuf

    @Serializable
    internal class RspBody(
        @JvmField @ProtoNumber(1) val wording: String = "",
        @JvmField @ProtoNumber(2) val jumpResult: Int = 0,
        @JvmField @ProtoNumber(3) val jumpUrl: String = "",
        @JvmField @ProtoNumber(4) val level: Int = 0,
        @JvmField @ProtoNumber(5) val subLevel: Int = 0,
        @JvmField @ProtoNumber(6) val developMsg: String = ""
    ) : ProtoBuf, Packet

    @Serializable
    internal class TemplateItem(
        @JvmField @ProtoNumber(1) val key: String = "",
        @JvmField @ProtoNumber(2) val type: Int = 0,
        @JvmField @ProtoNumber(3) val value: String = ""
    ) : ProtoBuf

    @Serializable
    internal class XmlMsgBody(
        @JvmField @ProtoNumber(11) val serviceId: Int = 0,
        @JvmField @ProtoNumber(12) val xml: String = ""
    ) : ProtoBuf
}
        
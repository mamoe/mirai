/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.protocol.data.richstatus

import kotlin.jvm.JvmField

internal class RichStatus(
    @JvmField var actId: Int = 0,
    @JvmField var actionId: Int = 0,
    @JvmField var actionText: String? = null,

    @JvmField var dataId: Int = 0,
    @JvmField var dataText: String? = null,

    @JvmField var feedsId: String? = null,

    @JvmField var fontId: Int = 0,
    @JvmField var fontType: Int = 0,


    @JvmField var key: ByteArray? = null,

    @JvmField var latitude: Int = 0,
    @JvmField var locationPosition: Int = 0,
    @JvmField var locationText: String? = null,
    @JvmField var lontitude: Int = 0,

    // @JvmField var mStickerInfos: MutableList<StickerInfo>? = null,
    // @JvmField var mUins: MutableList<String>? = null,
    @JvmField var plainText: MutableList<String>? = null,

    // public HashMap<Integer, /**/> sigZanInfo,
    @JvmField var signType: Int = 0,
    @JvmField var time: Long = 0,

    // var topics: MutableList<Pair<Integer, String>> = mutableListOf(),
    // var topicsPos: MutableList<Pair<Integer, Integer>> = mutableListOf(),
    @JvmField var tplId: Int = 0,
    @JvmField var tplType: Int = 0,
) {

    fun addPlainText(var1: String) {
        var pts = this.plainText
        if (pts == null) {
            pts = mutableListOf()
            this.plainText = pts
        }
        pts.add(var1)
    }

    companion object {
        fun parseStatus(rawData: ByteArray?): RichStatus {
            return parseRichStatusImpl(rawData)
        }
    }
}


internal expect fun parseRichStatusImpl(rawData: ByteArray?): RichStatus
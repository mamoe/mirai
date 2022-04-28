/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.message.protocol.impl

import net.mamoe.mirai.internal.message.UNSUPPORTED_POKE_MESSAGE_PLAIN
import net.mamoe.mirai.internal.message.protocol.*
import net.mamoe.mirai.internal.network.protocol.data.proto.HummerCommelem
import net.mamoe.mirai.internal.network.protocol.data.proto.ImMsgBody
import net.mamoe.mirai.internal.utils.io.serialization.loadAs
import net.mamoe.mirai.internal.utils.io.serialization.toByteArray
import net.mamoe.mirai.message.data.PokeMessage

internal class PokeMessageProtocol : MessageProtocol() {
    override fun ProcessorCollector.collectProcessorsImpl() {
        add(Encoder())
        add(Decoder())
    }

    private class Encoder : MessageEncoder<PokeMessage> {
        override suspend fun MessageEncoderContext.process(data: PokeMessage) {
            collect(
                ImMsgBody.Elem(
                    commonElem = ImMsgBody.CommonElem(
                        serviceType = 2,
                        businessType = data.pokeType,
                        pbElem = HummerCommelem.MsgElemInfoServtype2(
                            pokeType = data.pokeType,
                            vaspokeId = data.id,
                            vaspokeMinver = "7.2.0",
                            vaspokeName = data.name
                        ).toByteArray(HummerCommelem.MsgElemInfoServtype2.serializer())
                    )
                )
            )
            processAlso(UNSUPPORTED_POKE_MESSAGE_PLAIN)
        }

    }

    private class Decoder : MessageDecoder {
        override suspend fun MessageDecoderContext.process(data: ImMsgBody.Elem) {
            if (data.commonElem == null) return
            if (data.commonElem.serviceType != 2) return

            val proto =
                data.commonElem.pbElem.loadAs(HummerCommelem.MsgElemInfoServtype2.serializer())
            val name = proto.vaspokeName.takeIf { it.isNotEmpty() }
                ?: PokeMessage.values.firstOrNull { it.id == proto.vaspokeId && it.pokeType == proto.pokeType }?.name
                    .orEmpty()
            collect(
                PokeMessage(
                    name = name,
                    pokeType = proto.pokeType,
                    id = proto.vaspokeId
                )
            )
        }
    }
}
/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.notice

import kotlinx.io.core.discardExact
import kotlinx.io.core.readUByte
import net.mamoe.mirai.internal.message.contextualBugReportException
import net.mamoe.mirai.internal.network.components.PipelineContext
import net.mamoe.mirai.internal.network.components.SimpleNoticeProcessor
import net.mamoe.mirai.internal.network.protocol.data.proto.OnlinePushTrans.PbMsgInfo
import net.mamoe.mirai.internal.utils._miraiContentToString
import net.mamoe.mirai.utils.read

internal class BinaryMessageProcessor : SimpleNoticeProcessor<PbMsgInfo>(type()), GroupEventProcessorContext {
    override suspend fun PipelineContext.process0(data: PbMsgInfo) {
        data.msgData.read<Unit> {
            when (data.msgType) {
                44 -> {
                    //                  3D C4 33 DD 01 FF CD 76 F4 03 C3 7E 2E 34
                    //      群转让
                    //      start with  3D C4 33 DD 01 FF
                    //                  3D C4 33 DD 01 FF C3 7E 2E 34 CD 76 F4 03
                    // 权限变更
                    //                  3D C4 33 DD 01 00/01 .....
                    //                  3D C4 33 DD 01 01 C3 7E 2E 34 01
                    this.discardExact(5)
                    when (val mode = readUByte().toInt()) {
                        0xFF -> {
                            TODO("removed")
                        }
                        else -> {
                            TODO("removed")
                        }
                    }
                }
                34 -> {
                    TODO("removed")
                }
                else -> {
                    when {
                        data.msgType == 529 && data.msgSubtype == 9 -> {
                            /*
                            PbMsgInfo#1773430973 {
fromUin=0x0000000026BA1173(649728371)
generalFlag=0x00000001(1)
msgData=0A 07 70 72 69 6E 74 65 72 10 02 1A CD 02 0A 1F 53 61 6D 73 75 6E 67 20 4D 4C 2D 31 38 36 30 20 53 65 72 69 65 73 20 28 55 53 42 30 30 31 29 0A 16 4F 6E 65 4E 6F 74 65 20 66 6F 72 20 57 69 6E 64 6F 77 73 20 31 30 0A 19 50 68 61 6E 74 6F 6D 20 50 72 69 6E 74 20 74 6F 20 45 76 65 72 6E 6F 74 65 0A 11 4F 6E 65 4E 6F 74 65 20 28 44 65 73 6B 74 6F 70 29 0A 1D 4D 69 63 72 6F 73 6F 66 74 20 58 50 53 20 44 6F 63 75 6D 65 6E 74 20 57 72 69 74 65 72 0A 16 4D 69 63 72 6F 73 6F 66 74 20 50 72 69 6E 74 20 74 6F 20 50 44 46 0A 15 46 6F 78 69 74 20 50 68 61 6E 74 6F 6D 20 50 72 69 6E 74 65 72 0A 03 46 61 78 32 09 0A 03 6A 70 67 10 01 18 00 32 0A 0A 04 6A 70 65 67 10 01 18 00 32 09 0A 03 70 6E 67 10 01 18 00 32 09 0A 03 67 69 66 10 01 18 00 32 09 0A 03 62 6D 70 10 01 18 00 32 09 0A 03 64 6F 63 10 01 18 01 32 0A 0A 04 64 6F 63 78 10 01 18 01 32 09 0A 03 74 78 74 10 00 18 00 32 09 0A 03 70 64 66 10 01 18 01 32 09 0A 03 70 70 74 10 01 18 01 32 0A 0A 04 70 70 74 78 10 01 18 01 32 09 0A 03 78 6C 73 10 01 18 01 32 0A 0A 04 78 6C 73 78 10 01 18 01
msgSeq=0x00001AFF(6911)
msgSubtype=0x00000009(9)
msgTime=0x5FDF21A3(1608458659)
msgType=0x00000211(529)
msgUid=0x010000005FDEE04C(72057595646369868)
realMsgTime=0x5FDF21A3(1608458659)
svrIp=0x3E689409(1047041033)
toUin=0x0000000026BA1173(649728371)
}
                             */
                            /*
                            *
printer
Samsung ML-1860 Series (USB001)
OneNote for Windows 10
Phantom Print to Evernote
OneNote (Desktop)
Microsoft XPS Document Writer
Microsoft Print to PDF
Foxit Phantom Printer
Fax2
jpg2

jpeg2
png2
gif2
bmp2
doc2

docx2
txt2
pdf2
ppt2

pptx2
xls2

xlsx*/
                            return
                        }
                    }
                    throw contextualBugReportException(
                        "解析 OnlinePush.PbPushTransMsg, msgType=${data.msgType}",
                        data._miraiContentToString(),
                        null,
                        "并描述此时机器人是否被踢出, 或是否有成员列表变更等动作."
                    )
                }
            }
        }
    }
}
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.testFramework.codegen.test.visitors

import net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg
import net.mamoe.mirai.internal.testFramework.codegen.RemoveDefaultValuesVisitor
import net.mamoe.mirai.internal.testFramework.codegen.ValueDescAnalyzer
import net.mamoe.mirai.internal.testFramework.codegen.analyze
import net.mamoe.mirai.internal.testFramework.codegen.descriptors.accept
import net.mamoe.mirai.internal.testFramework.codegen.visitors.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ValueDescAnalyzerTest {

    private inline fun <reified T> analyzeAndRender(
        value: T
    ): String {
        return ValueDescAnalyzer.analyze(value).renderToString(
            rendererContext = RendererContext(
                indenter = Indenter.NoIndent,
                classFormatter = object : ClassFormatter() {
                    override fun formatClassProperty(
                        context: ClassFormatterContext,
                        propertyString: String?,
                        valueString: String
                    ): String = "$propertyString=$valueString"
                }
            ),
        )
    }

    @Test
    fun `test plain`() {
        assertEquals(
            "\"test\"",
            analyzeAndRender("test")
        )
        assertEquals(
            "1",
            analyzeAndRender(1)
        )
        assertEquals(
            "1.0",
            analyzeAndRender(1.0)
        )
    }

    @Test
    fun `test array`() {
        assertEquals(
            "arrayOf(1, 2)",
            analyzeAndRender(arrayOf(1, 2))
        )
        assertEquals(
            "arrayOf(5.0)",
            analyzeAndRender(arrayOf(5.0))
        )
        assertEquals(
            "arrayOf(\"1\")",
            analyzeAndRender(arrayOf("1"))
        )
        assertEquals(
            """
                arrayOf(
                arrayOf(1), 
                )
            """.trimIndent(),
            analyzeAndRender(arrayOf(arrayOf(1)))
        )
    }

    data class TestClass(
        val value: String
    )

    data class TestClass2(
        val value: Any
    )

    @Test
    fun `test class`() {
        assertEquals(
            """
                ${TestClass::class.qualifiedName!!}(
                value="test",
                )
            """.trimIndent(),
            analyzeAndRender(TestClass("test"))
        )
        assertEquals(
            """
                ${TestClass2::class.qualifiedName!!}(
                value="test",
                )
            """.trimIndent(),
            analyzeAndRender(TestClass2("test"))
        )
        assertEquals(
            """
                ${TestClass2::class.qualifiedName!!}(
                value=1,
                )
            """.trimIndent(),
            analyzeAndRender(TestClass2(1))
        )
    }

    data class TestNesting(
        val nested: Nested
    ) {
        data class Nested(
            val value: String
        )
    }

    @Test
    fun `test nesting`() {
        assertEquals(
            """
                ${TestNesting::class.qualifiedName}(
                nested=${TestNesting.Nested::class.qualifiedName}(
                value="test",
                ),
                )
            """.trimIndent(),
            analyzeAndRender(TestNesting(TestNesting.Nested("test")))
        )
    }

    @Test
    fun `test complex nesting`() {
        assertEquals(
            """
            net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.StructMsg(
                version = 1,
                msgType = 2,
                msgSeq = 1630,
                msgTime = 1630,
                reqUin = 1230,
                msg = net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.SystemMsg(
                    subType = 1,
                    msgTitle = "邀请加群",
                    msgDescribe = "邀请你加入 %group_name%",
                    actions = mutableListOf(
                        net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.SystemMsgAction(
                            name = "拒绝",
                            result = "已拒绝",
                            actionInfo = net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.SystemMsgActionInfo(
                                type = 12,
                                groupCode = 2230203,
                            ),
                            detailName = "拒绝",
                        ), 
                        net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.SystemMsgAction(
                            name = "同意",
                            result = "已同意",
                            actionInfo = net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.SystemMsgActionInfo(
                                type = 11,
                                groupCode = 2230203,
                            ),
                            detailName = "同意",
                        ), 
                        net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.SystemMsgAction(
                            name = "忽略",
                            result = "已忽略",
                            actionInfo = net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.SystemMsgActionInfo(
                                type = 14,
                                groupCode = 2230203,
                            ),
                            detailName = "忽略",
                        ), 
                    ),
                    groupCode = 2230203,
                    actionUin = 1230001,
                    groupMsgType = 2,
                    groupInviterRole = 1,
                    groupInfo = net.mamoe.mirai.internal.network.protocol.data.proto.Structmsg.GroupInfo(
                        appPrivilegeFlag = 67698880,
                    ),
                    reqUinNick = "user3",
                    groupName = "testtest",
                    actionUinNick = "user1",
                    groupExtFlag = 1075905600,
                    actionUinQqNick = "user1",
                    reqUinGender = 255,
                    c2cInviteJoinGroupFlag = 1,
                ),
            )
            """.trimIndent(),
            ValueDescAnalyzer.analyze(
                Structmsg.StructMsg(
                    version = 1,
                    msgType = 2,
                    msgSeq = 1630,
                    msgTime = 1630,
                    reqUin = 1230,
                    msg = Structmsg.SystemMsg(
                        subType = 1,
                        msgTitle = "邀请加群",
                        msgDescribe = "邀请你加入 %group_name%",
                        actions = mutableListOf(
                            Structmsg.SystemMsgAction(
                                name = "拒绝",
                                result = "已拒绝",
                                actionInfo = Structmsg.SystemMsgActionInfo(
                                    type = 12,
                                    groupCode = 2230203,
                                ),
                                detailName = "拒绝",
                            ),
                            Structmsg.SystemMsgAction(
                                name = "同意",
                                result = "已同意",
                                actionInfo = Structmsg.SystemMsgActionInfo(
                                    type = 11,
                                    groupCode = 2230203,
                                ),
                                detailName = "同意",
                            ),
                            Structmsg.SystemMsgAction(
                                name = "忽略",
                                result = "已忽略",
                                actionInfo = Structmsg.SystemMsgActionInfo(
                                    type = 14,
                                    groupCode = 2230203,
                                ),
                                detailName = "忽略",
                            ),
                        ),
                        groupCode = 2230203,
                        actionUin = 1230001,
                        groupMsgType = 2,
                        groupInviterRole = 1,
                        groupInfo = Structmsg.GroupInfo(
                            appPrivilegeFlag = 67698880,
                        ),
                        reqUinNick = "user3",
                        groupName = "testtest",
                        actionUinNick = "user1",
                        groupExtFlag = 1075905600,
                        actionUinQqNick = "user1",
                        reqUinGender = 255,
                        c2cInviteJoinGroupFlag = 1,
                    ),
                )
            ).apply {
                val def = AnalyzeDefaultValuesMappingVisitor()
                accept(def)
                accept(RemoveDefaultValuesVisitor(def.mappings))
            }.renderToString(ValueDescToStringRenderer())
        )
    }
}
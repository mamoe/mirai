/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:JvmMultifileClass
@file:JvmName("MiraiCode")

package net.mamoe.mirai.message.code

import net.mamoe.mirai.message.code.internal.parseMiraiCodeImpl
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.utils.SinceMirai
import kotlin.jvm.JvmMultifileClass
import kotlin.jvm.JvmName

/**
 * 解析形如 "[mirai:]" 的 mirai 码, 即 [Message.toString] 返回的内容.
 */
@SinceMirai("1.1.0")
fun String.parseMiraiCode(): MessageChain = parseMiraiCodeImpl()
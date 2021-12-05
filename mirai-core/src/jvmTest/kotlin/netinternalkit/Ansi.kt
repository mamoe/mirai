/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.netinternalkit

// Copied from mirai-console/backend/mirai-console/src/util/AnsiMessageBuilder.kt


@Suppress("RegExpRedundantEscape")
private val DROP_CSI_PATTERN = """\u001b\[([\u0030-\u003F])*?([\u0020-\u002F])*?[\u0040-\u007E]""".toRegex()
private val DROP_ANSI_PATTERN = """\u001b[\u0040–\u005F]""".toRegex()

internal fun String.dropAnsi(): String = this
    .replace(DROP_CSI_PATTERN, "") // 先进行 CSI 剔除后进行 ANSI 剔除
    .replace(DROP_ANSI_PATTERN, "")

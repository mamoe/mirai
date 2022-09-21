/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.active

import net.mamoe.mirai.utils.MiraiInternalApi


/**
 * 群成员头衔详情
 * @property title 当前佩戴的头衔
 * @property color 当前佩戴的头衔的颜色
 * @property wearing 当前佩戴的头衔类型
 * @property medals 拥有的所有头衔
 * @since 2.13
 */
public class MemberMedalInfo @MiraiInternalApi public constructor(
    public val title: String,
    public val color: String,
    public val wearing: MemberMedalType,
    public val medals: Set<MemberMedalType>,
)
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 头像的规格, [size] 单位为 px.
 * @since 2.11
 */
public enum class AvatarSpec(@MiraiInternalApi public val size: Int) : Comparable<AvatarSpec> {
    /**
     * 最高压缩等级
     */
    SMALLEST(40),

    /**
     * 群员列表中的显示大小, 实际上是 40 px, 但会比 [SMALLEST] 好一些
     */
    SMALL(41),

    /**
     * 联系人列表中的显示大小
     */
    MEDIUM(100),

    /**
     * 消息列表中的显示大小
     */
    LARGE(140),

    /**
     * 联系人详情页面中的显示大小
     */
    LARGEST(640),

    /**
     * 原图
     */
    ORIGINAL(0);
}
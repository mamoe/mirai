/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.active

import net.mamoe.mirai.data.GroupHonorType

/**
 * 群荣耀历史数据
 * @property type 群荣誉类型
 * @property current 当前荣耀持有者 (龙王，壕礼皇冠, 善财福禄寿)
 * @property records 群荣耀历史记录
 * @since 2.13.0
 */
public class ActiveHonorList internal constructor(
    public val type: GroupHonorType,
    public val current: ActiveHonorCurrent?,
    public val records: List<ActiveHonorRecord>
)
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.active

import net.mamoe.mirai.utils.MiraiExperimentalApi
import net.mamoe.mirai.contact.Member

public interface Active {

    /**
     * 等级头衔列表，key 是 等级，value 是 头衔
     *
     * set 时传入的等级头衔 将会异步发送给api，并刷新等级头衔。
     *
     * 如果传入 map 为空，则相当于刷新等级头衔缓存
     *
     * @see Member.rank
     */
    @MiraiExperimentalApi
    public var rankTitles: Map<Int, String>
}
/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.utils.MiraiInternalApi

/**
 * 临时会话用户, 非群成员.
 *
 * [#429](https://github.com/mamoe/mirai/issues/429)
 */
@MiraiInternalApi("其他渠道的临时会话暂未支持. ")
public interface TempUser : User
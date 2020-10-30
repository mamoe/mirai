/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.internal.util.md5
import net.mamoe.mirai.console.internal.util.toUHexString

internal object AutoLoginConfig : AutoSavePluginConfig("AutoLogin") {
    @ValueDescription(
        """
        账号和明文密码列表
    """
    )
    val plainPasswords: MutableMap<Long, String> by value(mutableMapOf(123456654321L to "example"))


    @ValueDescription(
        """
        账号和 MD5 密码列表
    """
    )
    val md5Passwords: MutableMap<Long, String> by value(
        mutableMapOf(
            123456654321L to "example".toByteArray().md5().toUHexString()
        )
    )
}
/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.internal.data.builtins

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.YamlDynamicSerializer

@ValueDescription("自动登录配置")
internal object AutoLoginConfig : AutoSavePluginConfig("AutoLogin") {

    @Serializable
    data class Account(
        @Comment("账号, 现只支持 QQ 数字账号")
        val account: String,
        val password: Password,
        @Comment("""
            账号配置. 可用配置列表 (注意大小写):
            "protocol": "ANDROID_PHONE" / "ANDROID_PAD" / "ANDROID_WATCH"
        """)
        val configuration: Map<ConfigurationKey, @Serializable(with = YamlDynamicSerializer::class) Any> = mapOf(),
    ) {
        @Serializable
        data class Password(
            @Comment("密码种类, 可选 PLAIN 或 MD5")
            val kind: PasswordKind,
            @Comment("密码内容, PLAIN 时为密码文本, MD5 时为 16 进制")
            val value: String,
        )

        @Suppress("EnumEntryName")
        @Serializable
        enum class ConfigurationKey {
            protocol,

        }

        @Serializable
        enum class PasswordKind {
            PLAIN,
            MD5
        }
    }

    val accounts: MutableList<Account> by value(mutableListOf(
        Account("123456", Account.Password(Account.PasswordKind.PLAIN, "pwd"), mapOf(Account.ConfigurationKey.protocol to "ANDROID_PHONE"))
    ))
}
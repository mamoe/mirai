/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPOSED_SUPER_CLASS")

package net.mamoe.mirai.console.internal.data.builtins

import kotlinx.serialization.Serializable
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.descriptor.CommandValueArgumentParser
import net.mamoe.mirai.console.command.descriptor.InternalCommandValueArgumentParserExtensions
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.yamlkt.Comment
import net.mamoe.yamlkt.YamlDynamicSerializer

@ConsoleExperimentalApi
@ValueDescription("自动登录配置")
public object AutoLoginConfig : AutoSavePluginConfig("AutoLogin") {

    @Serializable
    public data class Account(
        @Comment("账号, 现只支持 QQ 数字账号")
        val account: String,
        val password: Password,
        @Comment(
            """
            账号配置. 可用配置列表 (注意大小写):
            "protocol": "ANDROID_PHONE" / "ANDROID_PAD" / "ANDROID_WATCH"
            "device": "device.json" 
            "enable": true
        """
        )
        val configuration: Map<ConfigurationKey, @Serializable(with = YamlDynamicSerializer::class) Any> = mapOf(),
    ) {
        @Serializable
        public data class Password(
            @Comment("密码种类, 可选 PLAIN 或 MD5")
            val kind: PasswordKind,
            @Comment("密码内容, PLAIN 时为密码文本, MD5 时为 16 进制")
            val value: String,
        )

        @Suppress("EnumEntryName")
        @Serializable
        public enum class ConfigurationKey {
            protocol,
            device,
            enable,

            ;

            public object Parser : CommandValueArgumentParser<ConfigurationKey>,
                InternalCommandValueArgumentParserExtensions<ConfigurationKey>() {
                override fun parse(raw: String, sender: CommandSender): ConfigurationKey {
                    val key = values().find { it.name.equals(raw, ignoreCase = true) }
                    if (key != null) return key
                    illegalArgument("未知配置项, 可选值: ${values().joinToString()}")
                }
            }
        }

        @Serializable
        public enum class PasswordKind {
            PLAIN,
            MD5;

            public object Parser : CommandValueArgumentParser<ConfigurationKey>,
                InternalCommandValueArgumentParserExtensions<ConfigurationKey>() {
                override fun parse(raw: String, sender: CommandSender): ConfigurationKey {
                    val key = ConfigurationKey.values().find { it.name.equals(raw, ignoreCase = true) }
                    if (key != null) return key
                    illegalArgument("未知配置项, 可选值: ${ConfigurationKey.values().joinToString()}")
                }
            }
        }
    }

    public val accounts: MutableList<Account> by value(
        mutableListOf(
            Account(
                account = "123456",
                password = Account.Password(Account.PasswordKind.PLAIN, "pwd"),
                configuration = mapOf(
                    Account.ConfigurationKey.protocol to "ANDROID_PHONE",
                    Account.ConfigurationKey.device to "device.json",
                    Account.ConfigurationKey.enable to true
                )
            )
        )
    )
}
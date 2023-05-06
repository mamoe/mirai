/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:OptIn(ConsoleFrontEndImplementation::class, ConsoleExperimentalApi::class)

package net.mamoe.mirai.console.internal.command.builtin

import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import net.mamoe.mirai.Bot
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation.ConsoleDataScope.Companion.get
import net.mamoe.mirai.console.command.*
import net.mamoe.mirai.console.internal.data.builtins.AutoLoginConfig
import net.mamoe.mirai.console.internal.data.builtins.DataScope
import net.mamoe.mirai.console.util.ConsoleExperimentalApi
import net.mamoe.mirai.console.util.scopeWith
import net.mamoe.mirai.message.nextMessageOrNull
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.Either
import net.mamoe.mirai.utils.Either.Companion.flatMapNull
import net.mamoe.mirai.utils.Either.Companion.fold
import net.mamoe.mirai.utils.hexToBytes
import net.mamoe.mirai.utils.secondsToMillis

@Suppress("RESTRICTED_CONSOLE_COMMAND_OWNER") // IDE plugin
internal open class LoginCommandImpl : SimpleCommand(
    ConsoleCommandOwner, "login", "登录",
    description = "登录一个账号",
), BuiltInCommandInternal {
    internal open suspend fun doLogin(bot: Bot) {
        kotlin.runCatching {
            bot.login()
            this
        }.onFailure { bot.close() }.getOrThrow()
    } // workaround since LoginCommand is an object

    @Handler
    suspend fun CommandSender.handle(
        id: Long,
        password: String? = null,
        protocol: BotConfiguration.MiraiProtocol? = null,
    ) {
        fun BotConfiguration.setup(protocol: BotConfiguration.MiraiProtocol?): BotConfiguration {
            val config = DataScope.get<AutoLoginConfig>()
            val account = config.accounts.firstOrNull { it.account == id.toString() }
            if (account != null) {
                account.configuration[AutoLoginConfig.Account.ConfigurationKey.protocol]?.let { proto ->
                    try {
                        this.protocol = BotConfiguration.MiraiProtocol.valueOf(proto.toString())
                    } catch (_: Throwable) {
                        //
                    }
                }
                account.configuration[AutoLoginConfig.Account.ConfigurationKey.heartbeatStrategy]?.let { heartStrate ->
                    try {
                        this.heartbeatStrategy = BotConfiguration.HeartbeatStrategy.valueOf(heartStrate.toString())
                    } catch (_: Throwable) {
                        //
                    }
                }
                account.configuration[AutoLoginConfig.Account.ConfigurationKey.device]?.let { device ->
                    fileBasedDeviceInfo(device.toString())
                }
            }
            if (protocol != null) {
                this.protocol = protocol
            }
            return this
        }

        fun getPassword(id: Long): Either<ByteArray, String?> {
            val config = DataScope.get<AutoLoginConfig>()
            val acc = config.accounts.firstOrNull { it.account == id.toString() } ?: return Either.right(null)
            val strv = acc.password.value
            return if (acc.password.kind == AutoLoginConfig.Account.PasswordKind.MD5) Either.left(strv.hexToBytes()) else Either.right(
                strv
            )
        }

        val pwd = Either.right<ByteArray, String?>(password).flatMapNull { getPassword(id) }
        kotlin.runCatching {
            pwd.fold(
                onLeft = { pass ->
                    MiraiConsole.addBot(id, pass) { setup(protocol) }.also { doLogin(it) }
                },
                onRight = { pass ->
                    if (pass == null) {
                        sendMessage("Could not find '$id' in AutoLogin config. Please specify password.")
                        return
                    }
                    MiraiConsole.addBot(id, pass) { setup(protocol) }.also { doLogin(it) }
                }
            )
        }.fold(
            onSuccess = { scopeWith(ConsoleCommandSender).sendMessage("${it.nick} ($id) Login successful") },
            onFailure = { throwable ->
                scopeWith(ConsoleCommandSender).sendMessage(
                    "Login failed: ${throwable.localizedMessage ?: throwable.message ?: throwable.toString()}" +
                            if (this is CommandSenderOnMessage<*>) {
                                MiraiConsole.launch(CoroutineName("stacktrace delayer from Login")) {
                                    fromEvent.nextMessageOrNull(60.secondsToMillis) { it.message.contentEquals("stacktrace") }
                                }
                                "\n 1 分钟内发送 stacktrace 以获取堆栈信息"
                            } else ""
                )

                throw throwable
            }
        )
    }
}

/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress(
    "INVISIBLE_MEMBER",
    "INVISIBLE_REFERENCE",
    "CANNOT_OVERRIDE_INVISIBLE_MEMBER",
    "INVISIBLE_SETTER",
    "INVISIBLE_GETTER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPER",
    "INVISIBLE_ABSTRACT_MEMBER_FROM_SUPE_WARNING"
)
@file:OptIn(ConsoleInternalAPI::class)

package net.mamoe.mirai.console.pure

import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.console.MiraiConsole
import net.mamoe.mirai.console.MiraiConsoleImplementation.Companion.start
import net.mamoe.mirai.console.command.CommandManager.INSTANCE.register
import net.mamoe.mirai.console.command.CommandPermission
import net.mamoe.mirai.console.command.CommandSender
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.SimpleCommand
import net.mamoe.mirai.console.util.ConsoleInternalAPI
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.contact.nameCardOrNick
import net.mamoe.mirai.message.data.Message
import net.mamoe.mirai.message.data.content
import net.mamoe.mirai.utils.DefaultLogger
import java.io.PrintStream

/**
 * mirai-console-pure CLI 入口点
 */
object MiraiConsolePureLoader {
    @JvmStatic
    fun main(args: Array<String>?) {
        startup()
        YellowCommand.register()
        runBlocking { MiraiConsole.addBot(1994701021, "Asd123456789asd").alsoLogin() }
    }
}

object YellowCommand : SimpleCommand(
    net.mamoe.mirai.console.command.ConsoleCommandOwner, "睡", "sleep",
    prefixOptional = true,
    description = "睡一个人",
    permission = CommandPermission.Any
) {
    @Handler
    suspend fun CommandSender.handle(target: Member) {
        target.mute(1)

        sendMessage("${this.name} 睡了 ${target.nameCardOrNick}")
    }
}

internal fun startup() {
    DefaultLogger = { MiraiConsoleFrontEndPure.loggerFor(it) }
    overrideSTD()
    MiraiConsoleImplementationPure().start()
    startupConsoleThread()
}

internal fun overrideSTD() {
    System.setOut(
        PrintStream(
            BufferedOutputStream(
                logger = DefaultLogger("stdout").run { ({ line: String? -> info(line) }) }
            )
        )
    )
    System.setErr(
        PrintStream(
            BufferedOutputStream(
                logger = DefaultLogger("stderr").run { ({ line: String? -> warning(line) }) }
            )
        )
    )
}


internal object ConsoleCommandSenderImpl : ConsoleCommandSender() {
    override suspend fun sendMessage(message: Message) {
        kotlin.runCatching {
            ConsoleUtils.lineReader.printAbove(message.contentToString())
        }.onFailure {
            println(message.content)
            it.printStackTrace()
        }
    }
}
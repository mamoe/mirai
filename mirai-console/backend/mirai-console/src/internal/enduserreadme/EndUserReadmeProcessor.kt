/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.internal.enduserreadme

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import net.mamoe.mirai.console.ConsoleFrontEndImplementation
import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.enduserreadme.EndUserReadme
import net.mamoe.mirai.console.events.EndUserReadmeInitializeEvent
import net.mamoe.mirai.console.internal.MiraiConsoleImplementationBridge
import net.mamoe.mirai.console.internal.data.builtins.EndUserReadmeData
import net.mamoe.mirai.console.util.ConsoleInput
import net.mamoe.mirai.console.util.sendAnsiMessage
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.utils.MiraiInternalApi
import net.mamoe.mirai.utils.sha256
import net.mamoe.mirai.utils.toUHexString
import java.io.File
import java.net.InetAddress

internal object EndUserReadmeProcessor {
    private val PADDING = "=".repeat(100)
    private fun StringBuilder.pad(size: Int) {
        var size0 = size

        while (size0 > 0) {
            val padded = size0.coerceAtMost(PADDING.length)
            append(PADDING, 0, padded)
            size0 -= padded
        }
    }

    private fun header(title: String): String {
        val padding = 100 - title.length

        val lpadding = padding / 2
        val rpadding = padding - lpadding

        return buildString {
            pad(lpadding)
            append(" [ ").append(title).append(" ] ")
            pad(rpadding)
        }
    }

    private val systemDefaultNames = hashSetOf<String>(
        "ubuntu", "debian", "arch",
        "centos", "fedora", "localhost",
    )

    private fun getComputerName(): String {
        System.getenv("COMPUTERNAME")?.takeUnless(String::isBlank)?.let { return it }
        System.getenv("HOSTNAME")?.takeUnless(String::isBlank)?.let { return it }

        runCatching {
            InetAddress.getLocalHost().hostName
                ?.takeIf { it.lowercase() !in systemDefaultNames }
                ?.takeUnless(String::isBlank)
                ?.let { return it }
        }

        runCatching {
            File("/etc/machine-id").readText().takeUnless(String::isBlank)?.let { return it.trim() }
        }
        return "Unknown Computer"
    }

    @OptIn(MiraiInternalApi::class, ConsoleFrontEndImplementation::class)
    fun process(console: MiraiConsoleImplementationBridge) {
        if (System.getenv("CI") == "true") return
        if (System.getProperty("mirai.console.skip-end-user-readme") in listOf<String?>("", "true", "yes")) return

        val pcName = getComputerName()
        val dataObject = EndUserReadmeData()
        console.consoleDataScope.addAndReloadConfig(dataObject)


        runBlocking {
            val readme = EndUserReadme()
            runCatching {
                EndUserReadmeProcessor::class.java.getResourceAsStream("readme.txt")?.bufferedReader()?.use {
                    readme.putAll(it.readText())
                }
            }.onFailure { console.mainLogger.error(it) }

            EndUserReadmeInitializeEvent(readme).broadcast()

            // region Remove already read

            val pcNameBCode = pcName.toByteArray()
            var changed = false

            readme.pages.asSequence().map { (key, value) ->
                return@map key to value.sha256()
            }.onEach { (_, hash) ->
                for (i in hash.indices) {
                    hash[i] = hash[i].toInt().xor(pcNameBCode[i % pcNameBCode.size].toInt()).toByte()
                }
            }.map { (k, v) ->
                return@map k to v.toUHexString()
            }.toList().forEach { (key, hash) ->
                if (dataObject.data[key] == hash) {
                    readme.pages.remove(key)
                } else {
                    dataObject.data[key] = hash
                    changed = true
                }
            }
            // endregion

            suspend fun wait(seconds: Int) {
                if (seconds < 1) return

                var printWaiting = true

                repeat(seconds) { counter ->
                    val suffix = (seconds - counter).toString() + "s"
                    withTimeoutOrNull(1000L) {
                        if (printWaiting) {
                            ConsoleInput.requestInput("Please wait $suffix...")
                            printWaiting = false
                        }
                        while (true) {
                            ConsoleInput.requestInput("Please read before continuing ($suffix)")
                        }
                    }
                }

            }

            suspend fun pause() {
                ConsoleInput.requestInput("Enter to continue")
            }

            if (readme.pages.isNotEmpty()) {
                listOf(
                    header("End User Readme"),
                    "最终用户须知有更新，在您继续使用前，您必须完整阅读新的用户须知。",
                ).forEach { ConsoleCommandSender.sendMessage(it) }
            }

            readme.pages.forEach { (category, message) ->
                ConsoleCommandSender.sendMessage(header(category))
                message.lines().forEach { command ->
                    val ctrim = command.trim()
                    if (ctrim == EndUserReadme.PAUSE) {
                        pause()
                    } else if (ctrim == EndUserReadme.DELAY) {
                        wait(3)
                    } else if (ctrim.startsWith(EndUserReadme.DELAY)) {
                        wait(ctrim.removePrefix(EndUserReadme.DELAY).trim().toIntOrNull() ?: 3)
                    } else {
                        ConsoleCommandSender.sendAnsiMessage(command)
                    }
                }
                wait(3)
                pause()
            }


            if (changed) {
                dataObject.saveNow()
            }

        }
    }
}
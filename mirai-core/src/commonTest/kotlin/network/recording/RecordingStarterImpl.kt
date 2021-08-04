/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.recording

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.mamoe.mirai.Bot
import net.mamoe.mirai.internal.BotAccount
import net.mamoe.mirai.internal.QQAndroidBot
import net.mamoe.mirai.internal.network.component.ConcurrentComponentStorage
import net.mamoe.mirai.internal.network.component.MutableComponentStorage
import net.mamoe.mirai.internal.network.component.SharedRandomProvider
import net.mamoe.mirai.internal.network.component.setAll
import net.mamoe.mirai.internal.network.components.PacketHandler
import net.mamoe.mirai.internal.network.components.RandomProvider
import net.mamoe.mirai.internal.network.components.plus
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.currentTimeMillis
import net.mamoe.yamlkt.Yaml
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.random.Random
import kotlin.system.exitProcess

internal object RecordingStarterImpl {
    @Serializable
    class Configuration(
        val account: Account,
        val workingDir: String?,
        val recordingsDir: String,
        val note: String
    ) {
        @Serializable
        class Account(
            val id: Long,
            val password: String
        )
    }

    // cannot start main in commonTest
    fun main() {
        val config = Yaml.decodeFromString(
            Configuration.serializer(),
            this::class.java.classLoader.getResource("recording/configs/local.config.yml")!!.readText()
        )

        val seed = Random.nextInt()
        val date = SimpleDateFormat("yyyy-MM-dd-HH-mm", Locale.SIMPLIFIED_CHINESE).format(Date())

        val workingDir = if (config.workingDir == null) {
            File("test/recordings/temp/$date").also { dir ->
                Runtime.getRuntime().addShutdownHook(thread(false) { dir.deleteRecursively() })
            }
        } else {
            File(config.workingDir)
        }

        workingDir.mkdirs()
        println("Working at ${workingDir.absolutePath}")

        val data = runBlocking {
            PacketRecordBundle(
                version = 1,
                time = currentTimeMillis(),
                note = config.note,
                seed = seed,
                records = runRecoder(
                    BotAccount(config.account.id, config.account.password),
                    workingDir,
                    components = {
                        set(RandomProvider, SharedRandomProvider(Random(seed)))
                    }
                ) {

                }.records.toList()
            )
        }

        val out = File(config.recordingsDir, "$date.recording")
        data.saveTo(out)

        println("Saved ${data.records.size} records to ${out.absolutePath}")
        exitProcess(0)
    }

    private suspend inline fun runRecoder(
        account: BotAccount,
        workingDir: File,
        crossinline components: MutableComponentStorage.() -> Unit,
        crossinline configuration: BotConfiguration.() -> Unit = {},
        block: Bot.() -> Unit = {},
    ): RecordingPacketHandler {
        contract {
            callsInPlace(block, InvocationKind.EXACTLY_ONCE)
            callsInPlace(components, InvocationKind.EXACTLY_ONCE)
        }

        val recorder = RecordingPacketHandler()
        val bot = object : QQAndroidBot(account, BotConfiguration {
            deviceInfo = {
                Json.decodeFromString(
                    DeviceInfo.serializer(),
                    this::class.java.classLoader.getResource("recording/configs/local.device.json")!!.readText()
                )
            }
            this.workingDir = workingDir
            configuration()
        }) {
            override fun createBotLevelComponents(): ConcurrentComponentStorage {
                return ConcurrentComponentStorage().apply {
                    setAll(super.createBotLevelComponents())
                    set(PacketHandler, get(PacketHandler) + recorder)
                    components()
                }
            }
        }

        bot.login()
        bot.block()

        return recorder.also {
            bot.close()
        }
    }
}
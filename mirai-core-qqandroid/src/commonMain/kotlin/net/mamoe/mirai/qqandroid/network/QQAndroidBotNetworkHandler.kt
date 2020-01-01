package net.mamoe.mirai.qqandroid.network

import kotlinx.coroutines.*
import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.network.BotNetworkHandler
import net.mamoe.mirai.qqandroid.QQAndroidBot
import net.mamoe.mirai.qqandroid.network.protocol.packet.login.LoginPacket
import net.mamoe.mirai.utils.io.*
import kotlin.coroutines.CoroutineContext

internal class QQAndroidBotNetworkHandler(override val bot: QQAndroidBot) : BotNetworkHandler() {
    override val supervisor: CompletableJob = SupervisorJob(bot.coroutineContext[Job])

    private val channel: PlatformDatagramChannel = PlatformDatagramChannel("wtlogin.qq.com", 8000)

    override suspend fun login() {
        launch { processReceive() }

        val buffer = IoBuffer.Pool.borrow()
        buffer.writePacket(LoginPacket(bot.client).delegate)
        val shouldBeSent = buffer.readRemaining
        check(channel.send(buffer) == shouldBeSent) {
            "Buffer is not entirely sent. " +
                    "Required sent length=$shouldBeSent, but after channel.send, " +
                    "buffer remains ${buffer.readBytes().toUHexString()}"
        }
        buffer.release(IoBuffer.Pool)
        println("Login sent")
    }

    private suspend fun processReceive() {
        while (channel.isOpen) {
            val buffer = IoBuffer.Pool.borrow()

            try {
                channel.read(buffer)// JVM: withContext(IO)
            } catch (e: ClosedChannelException) {
                dispose()
                return
            } catch (e: ReadPacketInternalException) {
                bot.logger.error("Socket channel read failed: ${e.message}")
                continue
            } catch (e: CancellationException) {
                return
            } catch (e: Throwable) {
                bot.logger.error("Caught unexpected exceptions", e)
                continue
            } finally {
                if (!buffer.canRead() || buffer.readRemaining == 0) {//size==0
                    //bot.logger.debug("processReceive: Buffer cannot be read")
                    buffer.release(IoBuffer.Pool)
                    continue
                }// sometimes exceptions are thrown without this `if` clause
            }

            //buffer.resetForRead()
            launch(CoroutineName("handleServerPacket")) {
                // `.use`: Ensure that the packet is consumed **totally**
                // so that all the buffers are released
                ByteArrayPool.useInstance {
                    val length = buffer.readRemaining - 1
                    buffer.readFully(it, 0, length)
                    buffer.resetForWrite()
                    buffer.writeFully(it, 0, length)
                }
                ByteReadPacket(buffer, IoBuffer.Pool).use { input ->
                    try {
                        input.debugPrint("Received")
                    } catch (e: Exception) {
                        bot.logger.error(e)
                    }
                }
            }
        }
    }

    override suspend fun awaitDisconnection() {
        while (true) {
            delay(100)
            // TODO: 2019/12/31
        }
    }

    override fun dispose(cause: Throwable?) {
        println("Closed")
        super.dispose(cause)
    }

    override val coroutineContext: CoroutineContext = bot.coroutineContext
}
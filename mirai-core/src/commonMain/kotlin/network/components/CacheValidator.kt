/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import io.ktor.utils.io.core.*
import net.mamoe.mirai.internal.network.component.ComponentKey
import net.mamoe.mirai.internal.utils.MiraiProtocolInternal
import net.mamoe.mirai.internal.utils.io.writeShortLVString
import net.mamoe.mirai.utils.*

/**
 * Validator for checking caches is usable for current bot or not.
 */
internal interface CacheValidator {
    fun register(cache: Cacheable)

    fun validate()

    companion object : ComponentKey<CacheValidator>
}

internal interface Cacheable {
    fun invalidate()
}

internal class CacheValidatorImpl(
    private val ssoProcessorContext: SsoProcessorContext,
    private val hashFile: MiraiFile,
    private val logger: MiraiLogger,
) : CacheValidator {
    private val caches: MutableList<Cacheable> = mutableListOf()

    override fun register(cache: Cacheable) {
        caches.add(cache)
    }

    override fun validate() {

        val hash: ByteArray = buildPacket {
            val botConf = ssoProcessorContext.configuration
            writeInt(botConf.protocol.ordinal)
            val internalProtocol = MiraiProtocolInternal[botConf.protocol]
            writeShortLVString(internalProtocol.apkId)
            writeLong(internalProtocol.id)
            writeShortLVString(internalProtocol.sdkVer)
            writeInt(internalProtocol.miscBitMap)
            writeInt(internalProtocol.subSigMap)
            writeInt(internalProtocol.mainSigMap)
            writeShortLVString(internalProtocol.sign)
            writeLong(internalProtocol.buildTime)
            writeInt(internalProtocol.ssoVersion)

            val device = ssoProcessorContext.device

            @Suppress("INVISIBLE_MEMBER")
            writeFully(device.serializeToString().encodeToByteArray())
        }.let { pkg ->
            try {
                pkg.readBytes()
            } finally {
                pkg.release()
            }
        }.sha1()

        if (!hashFile.exists()) {
            logger.verbose { "Invalidate caches because hash file not available." }

            invalidate()

            kotlin.runCatching {
                hashFile.writeBytes(hash)
            }.onFailure { logger.warning("Exception in writing hash to validation file", it) }
            return
        }
        if (!hashFile.isFile) {
            logger.verbose { "hash file isn't a file." }
            invalidate()

            kotlin.runCatching {
                hashFile.deleteRecursively()
                hashFile.writeBytes(hash)
            }.onFailure { logger.warning("Exception in writing hash to validation file", it) }
            return
        }

        try {
            val hashInFile = hashFile.readBytes()
            if (hashInFile.contentEquals(hash)) {
                logger.verbose { "Validated caches." }
                return
            }

            logger.verbose { "Hash not match. Invaliding caches....." }
            invalidate()

            hashFile.writeBytes(hash)
        } catch (e: Throwable) {
            logger.warning("Exception in validation. Invalidating.....", e)
            invalidate()
        }

    }

    private fun invalidate() {
        caches.forEach { it.invalidate() }
    }
}

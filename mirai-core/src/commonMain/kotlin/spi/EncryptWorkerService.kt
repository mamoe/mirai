/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")

package net.mamoe.mirai.internal.spi

import net.mamoe.mirai.spi.BaseService
import net.mamoe.mirai.spi.SPIServiceLoader
import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.jvm.JvmStatic

/**
 * @since 2.15.0
 */
@MiraiExperimentalApi
internal interface EncryptWorkerService : BaseService {

    suspend fun doTLVEncrypt(id: Long, tlvType: Int, payLoad: ByteArray, vararg extraArgs: Any?): ByteArray?

    companion object : EncryptWorkerService {
        private val loader = SPIServiceLoader(object : EncryptWorkerService {
            override suspend fun doTLVEncrypt(
                id: Long,
                tlvType: Int,
                payLoad: ByteArray,
                vararg extraArgs: Any?
            ): ByteArray? = null
        }, EncryptWorkerService::class)

        override suspend fun doTLVEncrypt(
            id: Long,
            tlvType: Int,
            payLoad: ByteArray,
            vararg extraArgs: Any?
        ): ByteArray? {
            return loader.service.doTLVEncrypt(id, tlvType, payLoad, extraArgs)
        }

        @JvmStatic
        fun setService(service: EncryptWorkerService) {
            loader.service = service
        }
    }
}
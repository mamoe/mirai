/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.spi

import net.mamoe.mirai.utils.MiraiExperimentalApi
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmStatic

/**
 * 处理难以直接实现的加密 SPI
 *
 * @since 2.15.0
 */
@MiraiExperimentalApi
public interface EncryptWorkerService : BaseService {
    /**
     * implementation note:
     *
     * 如果出现不支持的类型，返回 null 即可
     *
     */
    @Throws(IllegalStateException::class, CancellationException::class)
    public suspend fun doTLVEncrypt(id: Long, tlvType: Int, payLoad: ByteArray, vararg extraArgs: Any?): ByteArray?

    @MiraiExperimentalApi
    public companion object : EncryptWorkerService {
        private val loader = SPIServiceLoader(object : EncryptWorkerService {
            override suspend fun doTLVEncrypt(
                id: Long,
                tlvType: Int,
                payLoad: ByteArray,
                vararg extraArgs: Any?
            ): ByteArray? = null
        }, EncryptWorkerService::class)

        @Throws(IllegalStateException::class, CancellationException::class)
        override suspend fun doTLVEncrypt(
            id: Long,
            tlvType: Int,
            payLoad: ByteArray,
            vararg extraArgs: Any?
        ): ByteArray? {
            return loader.service.doTLVEncrypt(id, tlvType, payLoad)
        }

        @JvmStatic
        public fun setService(service: EncryptWorkerService) {
            loader.service = service
        }
    }
}
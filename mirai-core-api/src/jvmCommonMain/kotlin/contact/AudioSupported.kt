/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge

package net.mamoe.mirai.contact

import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.message.data.Audio
import net.mamoe.mirai.message.data.OfflineAudio
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.OverFileSizeMaxException

/**
 * 支持发送语音的 [Contact]
 *
 * @since 2.7
 */
@NotStableForInheritance
public interface AudioSupported : Contact {
    /**
     * 上传一个语音文件以备发送. [resource] 需要调用方[关闭][ExternalResource.close].
     *
     * 多次调用 [uploadAudio] 使用同一个 [resource] 时, 将会发生多次上传, 且有可能产生不同的 [OfflineAudio] 对象, 因为服务器不会提供有关文件是否已经存在于服务器的信息.
     *
     * 返回的 [OfflineAudio] 支持序列化, 可以保存后在将来使用, 而不需要立即[发送][Contact.sendMessage]. 但不建议保存太久, 无法确定服务器保留一个文件的时间.
     *
     * 建议使用同一个 [Contact] 进行 [uploadAudio] 和 [sendMessage]. 目标对象不同时的行为是不确定的.
     *
     * 要获取更多语音相关的信息, 参阅 [Audio].
     *
     * @param resource 支持 AMR 和 SILK 格式. 若要支持 MP3 格式, 请参考 [mirai-silk-converter](https://github.com/project-mirai/mirai-silk-converter)
     *
     * @throws OverFileSizeMaxException 当语音文件过大而被服务器拒绝上传时. (最大大小约为 1 MB)
     * **注意**: 由于服务器不一定会检查大小, 该异常就不一定会因大小超过 1MB 而抛出.
     *
     * @since 2.7
     */
    public suspend fun uploadAudio(resource: ExternalResource): OfflineAudio
}

/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.kjbb.JvmBlockingBridge
import net.mamoe.mirai.message.data.Voice
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.uploadAsVoice
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.OverFileSizeMaxException

/**
 * 支持发送语音的 [Contact]
 *
 * @since 2.7
 */
@NotStableForInheritance
public interface VoiceSupported : Contact {
    /**
     * 上传一个语音消息以备发送.
     *
     * - **请手动关闭 [resource]**
     * - 请使用 amr 或 silk 格式
     *
     * @since 2.7
     * @see ExternalResource.uploadAsVoice
     * @throws OverFileSizeMaxException 当语音文件过大而被服务器拒绝上传时. (最大大小约为 1 MB)
     */
    @JvmBlockingBridge
    public suspend fun uploadVoice(resource: ExternalResource): Voice

}

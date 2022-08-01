/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

@file:JvmBlockingBridge
@file:Suppress("OVERLOADS_INTERFACE")

package net.mamoe.mirai.contact.file

import io.ktor.utils.io.errors.*
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.message.data.FileMessage
import net.mamoe.mirai.utils.NotStableForInheritance

/**
 * 绝对文件标识. 精确表示一个远程文件. 不会受同名文件或目录的影响.
 *
 * @since 2.8
 * @see RemoteFiles
 * @see AbsoluteFolder
 * @see AbsoluteFileFolder
 */
@NotStableForInheritance
public interface AbsoluteFile : AbsoluteFileFolder {
    /**
     * 文件到期时间戳, 单位秒.
     */
    public val expiryTime: Long

    /**
     * 文件大小 (占用空间), 单位 byte.
     */
    public val size: Long

    /**
     * 文件内容 SHA-1.
     */
    public val sha1: ByteArray

    /**
     * 文件内容 MD5.
     */
    public val md5: ByteArray

    /**
     * 移动远程文件到 [folder] 目录下. 成功时返回 `true`, 当远程文件不存在时返回 `false`.
     *
     * 注意该操作有可能产生同名文件或目录 (当 [folder] 中已经存在一个名称为 [name] 的文件或目录时).
     *
     * @throws IOException 当发生网络错误时可能抛出
     * @throws IllegalStateException 当发生已知的协议错误时抛出
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    public suspend fun moveTo(folder: AbsoluteFolder): Boolean

    /**
     * 获得下载链接 URL 字符串. 当远程文件不存在时返回 `null`.
     */
    public suspend fun getUrl(): String?

    /**
     * 得到 [AbsoluteFile] 所对应的 [FileMessage].
     *
     * 注: 在 [上传文件][RemoteFiles.uploadNewFile] 时就已经发送了文件消息, [FileMessage] 不可手动发送
     */
    public fun toMessage(): FileMessage

    /**
     * 返回更新了文件或目录信息 ([lastModifiedTime] 等) 的, 指向相同文件的 [AbsoluteFileFolder].
     * 不会更新当前 [AbsoluteFileFolder] 对象.
     *
     * 当远程文件或目录不存在时返回 `null`.
     *
     * 该函数会遍历上级目录的所有文件并匹配当前文件, 因此可能会非常慢, 请不要频繁使用.
     */
    override suspend fun refreshed(): AbsoluteFile?
}
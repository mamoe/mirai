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

import kotlinx.coroutines.flow.Flow
import me.him188.kotlin.jvm.blocking.bridge.JvmBlockingBridge
import net.mamoe.mirai.contact.FileSupported
import net.mamoe.mirai.contact.PermissionDeniedException
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.NotStableForInheritance
import net.mamoe.mirai.utils.ProgressionCallback
import kotlin.jvm.JvmOverloads

/**
 * 表示远程文件列表 (管理器).
 *
 * [RemoteFiles] 包含一些协议接口,
 *
 * # 文件和目录操作
 *
 * 文件和目录的父类型是 [AbsoluteFileFolder].
 *
 * - [AbsoluteFile] 表示一个文件
 * - [AbsoluteFolder] 表示一个目录
 *
 * 每个文件或目录都拥有一个唯一 ID: [AbsoluteFileFolder.id]. 该 ID 由服务器提供, 在重命名或移动时不会变化.
 *
 * 文件名可以通过 [AbsoluteFileFolder.name] 获得, 但注意文件名和其他属性都会随重命名或移动等操作更新.
 *
 * 除根目录 [root] 外, 每个文件或目录都拥有父目录 [AbsoluteFileFolder.parent].
 *
 * # 根目录
 *
 * 除了 [RemoteFiles] 中定义的捷径外, 一切文件目录操作都以获取根目录开始. 可通过 [RemoteFiles.root] 获取表示根目录的 [AbsoluteFolder].
 *
 * # 绝对路径与相对路径
 *
 * mirai 文件系统的绝对路径与相对路径与 Java [java.io.File] 实现的相同.
 *
 * 以 `/` 起始的路径表示绝对路径, 基于根目录 [root] 处理. 其他路径均表示相对路径.
 *
 * 可由 [AbsoluteFileFolder.absolutePath] 获取其绝对路径. 值得注意的是, 所有文件与目录对象都表示绝对路径下的目标, 因此它们都总是精确地表示一个目标, 而不受环境影响.
 *
 * 除重命名外, 所有文件和目录操作都默认同时支持上述两种路径.
 *
 * # 操作 [AbsoluteFileFolder]
 *
 * ## 重命名, 移动
 *
 * [AbsoluteFileFolder.renameTo], [AbsoluteFile.moveTo] 提供重命名和移动功能. 注意目录不支持移动.
 *
 * ## 获取目录中的子目录和文件列表
 *
 * 一个目录 ([AbsoluteFolder]) 可以包含多个子文件, 根目录还可以包含多个子目录 (详见下文 '目录结构限制').
 *
 * 使用 [AbsoluteFolder.children] 可以获得其内子目录和文件列表 [Flow]. [AbsoluteFolder.childrenStream] 提供适合 Java 的 [java.util.stream.Stream] 实现.
 * 使用 [AbsoluteFolder.folders] 或 [AbsoluteFolder.files] 可以特定地只获取子目录或文件列表. 这些函数也有其 `*Stream` 实现.
 *
 * 若要根据确定的文件或目录名称获取其 [AbsoluteFileFolder] 实例, 可使用 [AbsoluteFolder.resolveFiles] 或 [AbsoluteFolder.resolveFiles].
 * 注意 [AbsoluteFolder.resolveFiles] 返回 [Flow] (其 Stream 版返回 [java.util.stream.Stream]), 因为服务器允许多个文件有相同名称. (详见下文 '允许重名').
 *
 * 若已知文件 [AbsoluteFile.id], 可通过 [AbsoluteFolder.resolveFileById] 获得该文件.
 *
 * ## 上传新文件
 * 可使用 [AbsoluteFolder.uploadNewFile] 上传新文件. 也可以通过 [RemoteFiles.uploadNewFile] 直接上传而跳过获取目录的步骤 (因为目录不允许同名).
 *
 * ## 覆盖一个旧文件
 * 服务器不允许覆盖文件. 只能通过 [AbsoluteFile.delete] 删除文件后再上传新文件. 注意新旧文件的 [AbsoluteFile.id] 会不同.
 *
 * # 操作权限
 * 操作一个目录时总是需要管理员权限. 若群设置 "允许任何人上传文件", 则上传文件和操作自己上传的文件时都不需要特殊权限. 注意, 操作他人的文件时总是需要管理员权限.
 *
 * # 服务器限制
 *
 * ## 目录结构限制
 *
 * 在 mirai 2.8.0 发布时, 服务器仅允许两层目录结构. 也就是说只允许根目录存在子目录, 子目录不能包含另一个子目录.
 *
 * 为了考虑将来服务器可能升级, mirai 没有做实现上的限制. mirai 所有操作都支持多层目录, 但进行这样的操作时将会得到服务器错误, 方法会抛出 [IllegalStateException].
 *
 * ## 允许重名
 *
 * 服务器允许同名目录和文件存在. 如下同名的三个文件与一个目录是允许的, 但它们的 [AbsoluteFileFolder.id] 都互不相同:
 * ```
 * foo
 *  |- test (目录)
 *  |- test (文件)
 *  |- test (文件)
 *  |- test (文件)
 * ```
 * 注意, 目录不允许同名.
 *
 * [AbsoluteFileFolder] 依据 [AbsoluteFileFolder.id] 定位文件, 而不是通过文件名. 因此 [AbsoluteFileFolder] 总是精确地代表一个文件或目录.
 *
 * @since 2.8
 * @see FileSupported
 */
@NotStableForInheritance
public interface RemoteFiles {
    /**
     * 获取表示根目录的 [AbsoluteFolder]
     */
    public val root: AbsoluteFolder

    /**
     * 该对象所属 [FileSupported]
     */
    public val contact: FileSupported

    /**
     * 上传一个文件到指定精确路径. 返回指代该远程文件的 [AbsoluteFile].
     *
     * 会在必要时尝试创建远程目录.
     *
     * 也可以使用 [AbsoluteFolder.uploadNewFile].
     *
     * @param filepath 文件路径, **包含目标文件名**. 如 `/foo/bar.txt`. 若是相对目录则基于 [根目录][root] 处理.
     * @param content 文件内容
     * @param callback 下载进度回调, 传递的 `progression` 是已下载字节数.
     *
     * @throws PermissionDeniedException 当无管理员权限时抛出 (若群仅允许管理员上传)
     */
    @JvmOverloads
    public suspend fun uploadNewFile(
        filepath: String,
        content: ExternalResource,
        callback: ProgressionCallback<AbsoluteFile, Long>? = null,
    ): AbsoluteFile = root.uploadNewFile(filepath, content, callback)
}


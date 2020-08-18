/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("EXPERIMENTAL_API_USAGE", "unused")

package net.mamoe.mirai.utils

import kotlinx.io.core.Input
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.internal.DeferredReusableInput
import net.mamoe.mirai.utils.internal.asReusableInput
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URL

/*
 * 将各类型图片容器转为 [ExternalImage]
 */


/**
 * 将 [BufferedImage] 保存为临时文件, 然后构造 [ExternalImage]
 */
@JvmOverloads
public fun BufferedImage.toExternalImage(formatName: String = "png"): ExternalImage =
    ExternalImage(DeferredReusableInput(this, formatName))

/**
 * 将文件作为 [ExternalImage] 使用. 只会在需要的时候打开文件并读取数据.
 * @param deleteOnClose 若为 `true`, 图片发送后将会删除这个文件
 */
@JvmOverloads
public fun File.toExternalImage(deleteOnClose: Boolean = false): ExternalImage {
    require(this.isFile) { "File must be a file" }
    require(this.exists()) { "File must exist" }
    require(this.canRead()) { "File must can be read" }
    return ExternalImage(asReusableInput(deleteOnClose))
}

/**
 * 将 [InputStream] 委托为 [ExternalImage].
 * 只会在上传图片时才读取 [InputStream] 的内容. 具体行为取决于相关 [Bot] 的 [FileCacheStrategy]
 */
public fun InputStream.toExternalImage(): ExternalImage = ExternalImage(DeferredReusableInput(this, null))

/**
 * 将 [URL] 委托为 [ExternalImage].
 *
 * 只会在上传图片时才读取 [URL] 的内容. 具体行为取决于相关 [Bot] 的 [FileCacheStrategy]
 */
@Deprecated(
    "请自行通过 URL.openConnection 得到 InputStream 后调用其扩展",
    replaceWith = ReplaceWith("this.openConnection().toExternalImage"),
    level = DeprecationLevel.WARNING
)
public fun URL.toExternalImage(): ExternalImage = ExternalImage(DeferredReusableInput(this, null))

/**
 * 将 [Input] 委托为 [ExternalImage].
 * 只会在上传图片时才读取 [Input] 的内容. 具体行为取决于相关 [Bot] 的 [FileCacheStrategy]
 */
@Deprecated(
    "已弃用对 kotlinx.io 的支持",
    level = DeprecationLevel.ERROR
)
public fun Input.toExternalImage(): ExternalImage = ExternalImage(DeferredReusableInput(this, null))


@PlannedRemoval("1.2.0")
@Suppress("RedundantSuspendModifier", "DEPRECATION_ERROR")
@Deprecated("no need", ReplaceWith("toExternalImage()"), level = DeprecationLevel.HIDDEN)
public suspend fun Input.suspendToExternalImage(): ExternalImage = toExternalImage()

@Suppress("RedundantSuspendModifier")
@PlannedRemoval("1.2.0")
@Deprecated("no need", ReplaceWith("toExternalImage()"), level = DeprecationLevel.HIDDEN)
public suspend fun InputStream.suspendToExternalImage(): ExternalImage = toExternalImage()

@Suppress("RedundantSuspendModifier", "DEPRECATION")
@PlannedRemoval("1.2.0")
@Deprecated("no need", ReplaceWith("toExternalImage()"), level = DeprecationLevel.HIDDEN)
public suspend fun URL.suspendToExternalImage(): ExternalImage = toExternalImage()

@Suppress("RedundantSuspendModifier")
@PlannedRemoval("1.2.0")
@Deprecated("no need", ReplaceWith("toExternalImage()"), level = DeprecationLevel.HIDDEN)
public suspend fun File.suspendToExternalImage(): ExternalImage = toExternalImage()

@Suppress("RedundantSuspendModifier")
@PlannedRemoval("1.2.0")
@Deprecated("no need", ReplaceWith("toExternalImage()"), level = DeprecationLevel.HIDDEN)
public suspend fun BufferedImage.suspendToExternalImage(): ExternalImage = toExternalImage()

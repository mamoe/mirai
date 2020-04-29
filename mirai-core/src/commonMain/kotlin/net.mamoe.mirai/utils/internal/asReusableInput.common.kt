/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils.internal

import kotlinx.coroutines.io.ByteReadChannel
import kotlinx.io.InputStream
import kotlinx.io.core.Input
import kotlinx.serialization.InternalSerializationApi
import net.mamoe.mirai.utils.ExternalImage


internal expect fun ByteReadChannel.asReusableInput(): ExternalImage.ReusableInput
internal expect fun Input.asReusableInput(): ExternalImage.ReusableInput

@OptIn(InternalSerializationApi::class)
internal expect fun InputStream.asReusableInput(): ExternalImage.ReusableInput

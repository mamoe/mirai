/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.wizard

import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.validation.DialogValidation
import org.jetbrains.annotations.Nls


fun interface DialogValidationExt : DialogValidation {
    override fun validate(): ValidationInfo? = validateImpl()

    fun DialogValidationExt.validateImpl(): ValidationInfo?
}

fun DialogValidationExt.e(@Nls message: String): ValidationInfo = ValidationInfo(message)
fun DialogValidationExt.w(@Nls message: String): ValidationInfo = ValidationInfo(message).asWarning()


/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij.diagnostics.fix
/*

import com.intellij.openapi.editor.Editor
import net.mamoe.mirai.console.intellij.resolve.resolveStringConstantValues
import org.jetbrains.kotlin.idea.intentions.SelfTargetingIntention
import org.jetbrains.kotlin.psi.KtPsiFactory
import org.jetbrains.kotlin.psi.KtStringTemplateExpression

@Suppress("IntentionDescriptionNotFoundInspection") //
class AbuseYellowIntention :
    SelfTargetingIntention<KtStringTemplateExpression>(KtStringTemplateExpression::class.java, { "Abuse yellow" }, { "Abuse yellow" }) {
    override fun applyTo(element: KtStringTemplateExpression, editor: Editor?) {
        element.replace(KtPsiFactory(element).createExpression("\"弱智黄色\""))
    }

    override fun isApplicableTo(element: KtStringTemplateExpression, caretOffset: Int): Boolean {
        return element.resolveStringConstantValues().firstOrNull() == "黄色"
    }

}*/
/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.intellij

import com.intellij.codeInsight.intention.IntentionAction
import net.mamoe.mirai.console.compiler.common.diagnostics.MiraiConsoleErrors
import net.mamoe.mirai.console.intellij.diagnostics.fix.*
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.idea.quickfix.KotlinIntentionActionsFactory
import org.jetbrains.kotlin.idea.quickfix.QuickFixContributor
import org.jetbrains.kotlin.idea.quickfix.QuickFixes

class QuickFixRegistrar : QuickFixContributor {
    override fun registerQuickFixes(quickFixes: QuickFixes) {
        fun DiagnosticFactory<*>.registerFactory(vararg factory: KotlinIntentionActionsFactory) {
            quickFixes.register(this, *factory)
        }

        @Suppress("unused")
        fun DiagnosticFactory<*>.registerActions(vararg action: IntentionAction) {
            quickFixes.register(this, *action)
        }

        MiraiConsoleErrors.UNSERIALIZABLE_TYPE.registerFactory(AddSerializerFix)
        MiraiConsoleErrors.NOT_CONSTRUCTABLE_TYPE.registerFactory(ProvideDefaultValueFix)
        MiraiConsoleErrors.READ_ONLY_VALUE_CANNOT_BE_VAR.registerFactory(ConvertToValFix)

        MiraiConsoleErrors.USING_DERIVED_MAP_TYPE.registerFactory(ConvertToMapFix)
        MiraiConsoleErrors.USING_DERIVED_MUTABLE_MAP_TYPE.registerFactory(ConvertToMutableMapFix)
        MiraiConsoleErrors.USING_DERIVED_CONCURRENT_MAP_TYPE.registerFactory(ConvertToConcurrentMapFix)
        MiraiConsoleErrors.USING_DERIVED_LIST_TYPE.registerFactory(ConvertToListFix)
        MiraiConsoleErrors.USING_DERIVED_MUTABLE_LIST_TYPE.registerFactory(ConvertToMutableListFix)
    }
}

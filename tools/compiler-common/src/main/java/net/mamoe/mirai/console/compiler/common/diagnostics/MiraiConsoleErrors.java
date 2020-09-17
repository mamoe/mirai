/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.compiler.common.diagnostics;

import com.intellij.psi.PsiElement;
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory1;
import org.jetbrains.kotlin.diagnostics.Errors;

import static org.jetbrains.kotlin.diagnostics.Severity.ERROR;

public interface MiraiConsoleErrors {
    DiagnosticFactory1<PsiElement, String> ILLEGAL_PLUGIN_ID = DiagnosticFactory1.create(ERROR);
    DiagnosticFactory1<PsiElement, String> ILLEGAL_PLUGIN_NAME = DiagnosticFactory1.create(ERROR);

    @Deprecated
    Object _init = new Object() {
        {
            Errors.Initializer.initializeFactoryNamesAndDefaultErrorMessages(
                    MiraiConsoleErrors.class,
                    MiraiConsoleErrorsRendering.INSTANCE
            );
        }
    };
}

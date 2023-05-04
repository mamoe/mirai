/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.jetbrains.kotlin.gradle.dsl.KotlinCompile

/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

private val EXPLICIT_API = "-Xexplicit-api=strict"

// Workaround for explicit API in androidMain
// https://youtrack.jetbrains.com/issue/KT-37652/Support-explicit-mode-for-Android-projects
// https://youtrack.jetbrains.com/issue/KT-37652/Support-explicit-mode-for-Android-projects#focus=Comments-27-4501224.0-0

project.tasks
    .matching { it is KotlinCompile<*> && !it.name.contains("test", ignoreCase = true) }
    .configureEach {
        if (!project.hasProperty("kotlin.optOutExplicitApi")) {
            val kotlinCompile = this as KotlinCompile<*>
            if (EXPLICIT_API !in kotlinCompile.kotlinOptions.freeCompilerArgs) {
                kotlinCompile.kotlinOptions.freeCompilerArgs += EXPLICIT_API
            }
        }
    }

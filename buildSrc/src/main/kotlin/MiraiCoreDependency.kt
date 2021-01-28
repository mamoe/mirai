/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("ObjectPropertyName", "HasPlatformType")

import org.gradle.api.Project

/*
 * For compatibility with composite mirai-core and mirai-console builds and dedicated mirai-console builds.
 *
 * If you're in mirai project, see also root/buildSrc/MiraiCoreDependency.kt (likely path)
 */


val Project.`mirai-core-api` get() = rootProject.project(":mirai-core-api")
val Project.`mirai-core` get() = rootProject.project(":mirai-core")
val Project.`mirai-core-utils` get() = rootProject.project(":mirai-core-utils")

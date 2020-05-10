/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * 表明这个 API 是为了让 Java 使用者调用更方便.
 *
 * 一般有一定的性能损失, 且不能在 JVM/Android 以外平台使用. 不要在 Kotlin 调用它.
 */
@MiraiInternalAPI
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
internal annotation class JavaFriendlyAPI

/**
 * [Bot] 中为了让 Java 使用者调用更方便的 API 列表.
 *
 * **注意**: 不应该把这个类作为一个类型, 只应使用其中的方法
 */
@MiraiInternalAPI
@Suppress("FunctionName", "INAPPLICABLE_JVM_NAME", "unused")
expect abstract class BotJavaFriendlyAPI() { // 不要使用 interface, 会无法添加默认实现
}

// 保留多平台结构, 以避免在 Android 和 JVM 都定义这个类 ---- 这会造成代码重复.
// 待 https://youtrack.jetbrains.com/issue/KT-27801 实现后修改为 hierarchical MPP 架构

// 待 https://youtrack.jetbrains.com/issue/KT-36740 修复后添加 Future 相关 API 到 hierarchical MPP 架构中
/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */
package net.mamoe.mirai

/**
 * 表明这个 API 是为了让 Java 使用者调用更方便.
 *
 * 一般有一定的性能损失, 且不能在 JVM/Android 以外平台使用. 不要在 Kotlin 调用它.
 */
@RequiresOptIn(level = RequiresOptIn.Level.ERROR)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE, AnnotationTarget.CLASS)
internal annotation class JavaFriendlyAPI
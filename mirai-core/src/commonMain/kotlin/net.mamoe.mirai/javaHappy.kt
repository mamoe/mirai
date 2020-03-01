/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai

import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageSource
import kotlin.jvm.JvmName

/**
 * 表明这个 API 是为了让 Java 使用者调用更方便.
 */
@Experimental(level = Experimental.Level.ERROR)
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.FUNCTION, AnnotationTarget.TYPE)
annotation class JavaHappyAPI

/**
 * [Bot] 中为了让 Java 使用者调用更方便的 API 列表.
 */
@Suppress("FunctionName", "INAPPLICABLE_JVM_NAME", "unused")
expect abstract class BotJavaHappyAPI() {
    @JvmName("login")
    open fun __loginBlockingForJava__()

    @JvmName("recall")
    open fun __recallBlockingForJava__(source: MessageSource)

    @JvmName("queryImageUrl")
    open fun __queryImageUrl__(image: Image): String
}
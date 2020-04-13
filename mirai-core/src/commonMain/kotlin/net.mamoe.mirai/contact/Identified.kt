/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.SinceMirai

/**
 * 拥有 [id] 的对象.
 * 此为 [Contact] 与 [Bot] 的唯一公共接口.
 * **注意:** 此接口为实验性接口, 将来可能会发生不兼容的更名.
 *
 * @see Contact
 * @see Bot
 */
@ExperimentalIdentification("classname may change")
@SinceMirai("0.37.2")
interface Identified {
    /**
     * QQ 号或群号.
     */
    val id: Long
}


/**
 * 标记使用实验性 [Identified] 接口.
 */
@Retention(AnnotationRetention.SOURCE)
@RequiresOptIn(level = RequiresOptIn.Level.WARNING)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY, AnnotationTarget.FIELD, AnnotationTarget.CONSTRUCTOR)
annotation class ExperimentalIdentification(
    val message: String = ""
)
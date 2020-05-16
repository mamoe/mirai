/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.JavaFriendlyAPI

/**
 * [Contact] 中为了让 `Java` 更容易调用的 API.
 * 不要用它作为一个类型, 只应使用其中的方法
 */
@JavaFriendlyAPI
internal expect interface ContactJavaFriendlyAPI

/**
 * [Member] 中为了让 `Java` 更容易调用的 API
 * 不要用它作为一个类型, 只应使用其中的方法
 */
@Suppress("DEPRECATION_ERROR")
@JavaFriendlyAPI
internal expect interface MemberJavaFriendlyAPI
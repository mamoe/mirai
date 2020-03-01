/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.contact

import net.mamoe.mirai.JavaHappyAPI
import net.mamoe.mirai.utils.MiraiInternalAPI

/**
 * [Contact] 中为了让 `Java` 更容易调用的 API
 */
@MiraiInternalAPI
@JavaHappyAPI
expect abstract class ContactJavaHappyAPI

/**
 * [Member] 中为了让 `Java` 更容易调用的 API
 */
@MiraiInternalAPI
@JavaHappyAPI
expect abstract class MemberJavaHappyAPI : QQ
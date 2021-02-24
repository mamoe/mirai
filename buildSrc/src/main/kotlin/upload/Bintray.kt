/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import keys.SecretKeys
import org.gradle.api.Project

fun Project.isBintrayAvailable() = Bintray.isBintrayAvailable(project)

@Suppress("DuplicatedCode")
object Bintray {

    @JvmStatic
    fun isBintrayAvailable(project: Project): Boolean {
        return kotlin.runCatching {
            getUser(project)
            getKey(project)
        }.isSuccess
    }

    @JvmStatic
    fun getUser(project: Project): String {
        return SecretKeys.getCache(project)
            .loadKey("bintray")
            .requireNotInvalid()
            .user
    }

    @JvmStatic
    fun getKey(project: Project): String {
        return SecretKeys.getCache(project)
            .loadKey("bintray")
            .requireNotInvalid()
            .password
    }

}
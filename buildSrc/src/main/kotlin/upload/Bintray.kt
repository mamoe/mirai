package upload

import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */
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
        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val bintray_user: String by project
            return bintray_user
        }

        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val bintray_user: String by project.rootProject
            return bintray_user
        }

        System.getProperty("bintray_user", null)?.let {
            return it.trim()
        }

        File(File(System.getProperty("user.dir")).parent, "/bintray.user.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        File(File(System.getProperty("user.dir")), "/bintray.user.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        error(
            "Cannot find bintray user, " +
                    "please specify by creating a file bintray.user.txt in project dir, " +
                    "or by providing JVM parameter 'bintray_user'"
        )
    }

    @JvmStatic
    fun getKey(project: Project): String {
        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val bintray_key: String by project
            return bintray_key
        }

        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val bintray_key: String by project.rootProject
            return bintray_key
        }

        System.getProperty("bintray_key", null)?.let {
            return it.trim()
        }

        File(File(System.getProperty("user.dir")).parent, "/bintray.key.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        File(File(System.getProperty("user.dir")), "/bintray.key.txt").let { local ->
            if (local.exists()) {
                return local.readText().trim()
            }
        }

        error(
            "Cannot find bintray key, " +
                    "please specify by creating a file bintray.key.txt in project dir, " +
                    "or by providing JVM parameter 'bintray_key'"
        )
    }

}
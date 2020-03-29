/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package upload

import org.gradle.api.Project
import org.gradle.kotlin.dsl.provideDelegate
import java.io.File

object CuiCloud {
    private fun getUrl(project: Project): String {
        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val cui_cloud_url: String by project
            return cui_cloud_url
        }

        System.getProperty("cui_cloud_url", null)?.let {
            return it.trim()
        }
        error("cannot find url for CuiCloud")
    }

    private fun getKey(project: Project): String {
        kotlin.runCatching {
            @Suppress("UNUSED_VARIABLE", "LocalVariableName")
            val cui_cloud_key: String by project
            return cui_cloud_key
        }

        System.getProperty("cui_cloud_key", null)?.let {
            return it.trim()
        }
        error("cannot find key for CuiCloud")
    }

    fun upload(file: File, project: Project) {
        val cuiCloudUrl = getUrl(project)
        val key = getUrl(project)


    }
}
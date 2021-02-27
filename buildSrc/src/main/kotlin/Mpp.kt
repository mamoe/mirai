/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectProvider
import org.gradle.api.Project
import java.util.*

/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

private object ProjectAndroidSdkAvailability {
    val map: MutableMap<String, Boolean> = mutableMapOf()

    @Suppress("UNUSED_PARAMETER", "UNREACHABLE_CODE")
    @Synchronized
    operator fun get(project: Project): Boolean {
        return true
        if (map[project.path] != null) return map[project.path]!!

        val projectAvailable = project.runCatching {
            val keyProps = Properties().apply {
                file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
            }
            keyProps.getProperty("sdk.dir", "").isNotEmpty()
        }.getOrElse { false }


        fun impl(): Boolean {
            if (project === project.rootProject) return projectAvailable
            return projectAvailable || get(project.rootProject)
        }
        map[project.path] = impl()
        return map[project.path]!!
    }
}

val Project.isAndroidSDKAvailable: Boolean get() = ProjectAndroidSdkAvailability[this]

val <T> NamedDomainObjectCollection<T>.androidMain: NamedDomainObjectProvider<T>
    get() = named("androidMain")

val <T> NamedDomainObjectCollection<T>.jvmMain: NamedDomainObjectProvider<T>
    get() = named("jvmMain")

val <T> NamedDomainObjectCollection<T>.androidTest: NamedDomainObjectProvider<T>
    get() = named("androidTest")

val <T> NamedDomainObjectCollection<T>.jvmTest: NamedDomainObjectProvider<T>
    get() = named("jvmTest")

val <T> NamedDomainObjectCollection<T>.commonMain: NamedDomainObjectProvider<T>
    get() = named("commonMain")

fun Project.printAndroidNotInstalled() {
    println(
        """Android SDK 可能未安装. $name 的 Android 目标编译将不会进行. 这不会影响 Android 以外的平台的编译.
            """.trimIndent()
    )
    println(
        """Android SDK might not be installed. Android target of $name will not be compiled. It does no influence on the compilation of other platforms.
            """.trimIndent()
    )
}
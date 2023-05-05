/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.Project
import java.util.*

/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

object BuildSrcRootProjectHolder {
    lateinit var value: Project
    var lastUpdateTime: Long = 0
}

val rootProject: Project get() = BuildSrcRootProjectHolder.value

fun <T> projectLazy(action: () -> T): Lazy<T> {
    val projLazy = object : Lazy<T> {
        private lateinit var delegate: Lazy<T>
        private var holdTime: Long = -1

        override val value: T
            get() {
                if (holdTime != BuildSrcRootProjectHolder.lastUpdateTime) {
                    synchronized(this) {
                        if (holdTime != BuildSrcRootProjectHolder.lastUpdateTime) {
                            delegate = lazy(action)
                            holdTime = BuildSrcRootProjectHolder.lastUpdateTime
                        }
                    }
                }
                return delegate.value
            }

        override fun isInitialized(): Boolean {
            if (!::delegate.isInitialized) return false

            if (holdTime == BuildSrcRootProjectHolder.lastUpdateTime) {
                return delegate.isInitialized()
            }
            return false
        }
    }
    return projLazy
}


private lateinit var localProperties: Properties
private var localPropertiesEdition: Long = 0

private fun Project.loadLocalPropertiesIfNecessary() {
    val theFile = rootProject.projectDir.resolve("local.properties")

    fun isNecessary(): Boolean {
        if (!::localProperties.isInitialized) return true

        if (theFile.exists()) {
            if (localPropertiesEdition != theFile.lastModified()) {
                return true
            }
        } else {
            if (localPropertiesEdition != 0L) { // deleted
                return true
            }
        }

        return false
    }

    if (!isNecessary()) return

    localProperties = Properties().apply {
        localPropertiesEdition = if (theFile.exists()) {
            theFile.bufferedReader().use { load(it) }
            theFile.lastModified()
        } else {
            0
        }
    }
}

fun Project.getLocalProperty(name: String): String? {
    loadLocalPropertiesIfNecessary()
    return localProperties.getProperty(name)
}

fun Project.getLocalProperty(name: String, default: String): String {
    return getLocalProperty(name) ?: default
}

fun Project.getLocalProperty(name: String, default: Int): Int {
    return getLocalProperty(name)?.toInt() ?: default
}

fun Project.getLocalProperty(name: String, default: Boolean): Boolean {
    return getLocalProperty(name)?.toBoolean() ?: default
}


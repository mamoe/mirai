/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.JavaVersion

/**
 * ```
 * mirai {
 *   // 配置
 * }
 * ```
 */
// must be open
open class MiraiConsoleExtension {
    /**
     * 为 `true` 时不自动添加 mirai-core 的依赖
     *
     * 默认: `false`
     */
    var noCore: Boolean = false

    /**
     * 为 `true` 时不自动为 test 模块添加 mirai-core-qqandroid 的依赖.
     *
     * 默认: `false`
     */
    var noTestCoreQQAndroid: Boolean = false

    /**
     * 为 `true` 时不自动添加 mirai-console 的依赖.
     *
     * 默认: `false`
     */
    var noConsole: Boolean = false

    /**
     * 自动添加的 mirai-core 和 mirai-core-qqandroid 的版本.
     *
     * 默认: 与本 Gradle 插件编译时的 mirai-core 版本相同. [VersionConstants.CORE_VERSION]
     */
    var coreVersion: String = VersionConstants.CORE_VERSION

    /**
     * 自动添加的 mirai-console 后端和前端的版本.
     *
     * 默认: 与本 Gradle 插件版本相同. [VersionConstants.CONSOLE_VERSION]
     */
    var consoleVersion: String = VersionConstants.CONSOLE_VERSION

    /**
     * 自动为 test 模块添加的前端依赖名称
     *
     * 为 `null` 时不自动为 test 模块添加前端依赖.
     *
     * 默认: [MiraiConsoleFrontEndKind.TERMINAL]
     */
    var useTestConsoleFrontEnd: MiraiConsoleFrontEndKind? = MiraiConsoleFrontEndKind.TERMINAL

    /**
     * Java 和 Kotlin 编译目标. 至少为 [JavaVersion.VERSION_1_8].
     *
     * 一般人不需要修改此项.
     *
     * 默认: [JavaVersion.VERSION_1_8]
     */
    var jvmTarget: JavaVersion = JavaVersion.VERSION_1_8

    /**
     * 默认会配置 Kotlin 编译器参数 "-Xjvm-default=all". 将此项设置为 `false` 可避免配置.
     *
     * 一般人不需要修改此项.
     *
     * 默认: `false`
     */
    var dontConfigureKotlinJvmDefault: Boolean = false

    internal val shadowConfigurations: MutableList<ShadowJar.() -> Unit> = mutableListOf()
    internal val excludedDependencies: MutableSet<ExcludedDependency> = mutableSetOf()

    internal data class ExcludedDependency(
        val group: String,
        val name: String
    )

    /**
     * 配置 [ShadowJar] (即打包插件)
     */
    fun configureShadow(configure: ShadowJar.() -> Unit) {
        shadowConfigurations.add(configure)
    }

    /**
     * 在插件打包时忽略一个依赖
     *
     * @param notation 格式为 "groupId:name". 如 "org.jetbrains.kotlin:kotlin-stdlib"
     */
    fun excludeDependency(notation: String) {
        requireNotNull(notation.count { it == ':' } == 1) { "Invalid dependency notation $notation." }
        excludedDependencies.add(ExcludedDependency(notation.substringBefore(':'), notation.substringAfter(':')))
    }

    /**
     * 在插件打包时忽略一个依赖
     *
     * @param group 如 "org.jetbrains.kotlin"
     * @param name 如 "kotlin-stdlib"
     */
    fun excludeDependency(group: String, name: String) {
        excludedDependencies.add(ExcludedDependency(group, name))
    }
}

enum class MiraiConsoleFrontEndKind {
    TERMINAL,
}
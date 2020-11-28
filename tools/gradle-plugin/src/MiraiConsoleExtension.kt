/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package net.mamoe.mirai.console.gradle

import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayExtension
import org.gradle.api.JavaVersion
import org.gradle.api.XmlProvider
import org.gradle.api.publish.maven.MavenPublication

/**
 * ```
 * mirai {
 *   // 配置
 * }
 * ```
 */
// must be open
public open class MiraiConsoleExtension {
    /**
     * 为 `true` 时不自动添加 mirai-core 的依赖
     *
     * 默认: `false`
     */
    public var noCore: Boolean = false

    /**
     * 为 `true` 时不自动为 test 模块添加 mirai-core-qqandroid 的依赖.
     *
     * 默认: `false`
     */
    public var noTestCoreQQAndroid: Boolean = false

    /**
     * 为 `true` 时不自动添加 mirai-console 的依赖.
     *
     * 默认: `false`
     */
    public var noConsole: Boolean = false

    /**
     * 自动添加的 mirai-core 和 mirai-core-qqandroid 的版本.
     *
     * 默认: 与本 Gradle 插件编译时的 mirai-core 版本相同. [VersionConstants.CORE_VERSION]
     */
    public var coreVersion: String = VersionConstants.CORE_VERSION

    /**
     * 自动添加的 mirai-console 后端和前端的版本.
     *
     * 默认: 与本 Gradle 插件版本相同. [VersionConstants.CONSOLE_VERSION]
     */
    public var consoleVersion: String = VersionConstants.CONSOLE_VERSION

    /**
     * 自动为 test 模块添加的前端依赖名称
     *
     * 为 `null` 时不自动为 test 模块添加前端依赖.
     *
     * 默认: [MiraiConsoleFrontEndKind.TERMINAL]
     */
    public var useTestConsoleFrontEnd: MiraiConsoleFrontEndKind? = MiraiConsoleFrontEndKind.TERMINAL

    /**
     * Java 和 Kotlin 编译目标. 至少为 [JavaVersion.VERSION_1_8].
     *
     * 一般人不需要修改此项.
     *
     * 默认: [JavaVersion.VERSION_1_8]
     */
    public var jvmTarget: JavaVersion = JavaVersion.VERSION_1_8

    /**
     * 默认会配置 Kotlin 编译器参数 "-Xjvm-default=all". 将此项设置为 `false` 可避免配置.
     *
     * 一般人不需要修改此项.
     *
     * 默认: `false`
     */
    public var dontConfigureKotlinJvmDefault: Boolean = false

    internal val shadowConfigurations: MutableList<ShadowJar.() -> Unit> = mutableListOf()
    internal val excludedDependencies: MutableSet<ExcludedDependency> = mutableSetOf()

    internal data class ExcludedDependency(
        val group: String,
        val name: String
    )

    /**
     * 配置 [ShadowJar] (即打包插件)
     */
    public fun configureShadow(configure: ShadowJar.() -> Unit) {
        shadowConfigurations.add(configure)
    }

    /**
     * 在插件打包时忽略一个依赖
     *
     * @param notation 格式为 "groupId:name". 如 "org.jetbrains.kotlin:kotlin-stdlib"
     */
    public fun excludeDependency(notation: String) {
        requireNotNull(notation.count { it == ':' } == 1) { "Invalid dependency notation $notation." }
        excludedDependencies.add(ExcludedDependency(notation.substringBefore(':'), notation.substringAfter(':')))
    }

    /**
     * 在插件打包时忽略一个依赖
     *
     * @param group 如 "org.jetbrains.kotlin"
     * @param name 如 "kotlin-stdlib"
     */
    public fun excludeDependency(group: String, name: String) {
        excludedDependencies.add(ExcludedDependency(group, name))
    }

    /**
     * Bintray 插件成品 JAR 发布 配置.
     *
     * @see Publishing
     * @since 1.1.0
     */
    public val publishing: Publishing = Publishing()

    /**
     * 控制自动配置 Bintray 发布
     * @since 1.1.0
     */
    public var publishingEnabled: Boolean = true

    /**
     * Bintray 插件成品 JAR 发布 配置.
     *
     * @see [Publishing]
     * @since 1.1.0
     */
    public inline fun publishing(block: Publishing.() -> Unit): Unit = publishing.run(block)

    /**
     * @see publishingEnabled
     * @since 1.1.0
     */
    public fun disablePublishing() {
        publishingEnabled = false
    }

    /**
     * Bintray 插件成品 JAR 发布 配置.
     *
     * 对于一个属性 PROP, 会按以下顺序依次尝试读取:
     * 1. Gradle 参数
     *   - "gradle.properties"
     *   - ext
     *   - Gradle -P 启动参数
     * 2. [System.getProperty] "PROP"
     * 3. 当前和所有父 project 根目录下 "PROP" 文件的内容
     * 4. [System.getenv] "PROP"
     *
     * @see publishing
     * @since 1.1.0
     */
    public class Publishing internal constructor() {
        ///////////////////////////////////////////////////////////////////////////
        // Required arguments
        ///////////////////////////////////////////////////////////////////////////

        /**
         * Bintray 账户名. 必须.
         * 若为 `null`, 将会以 [Publishing] 中描述的步骤获取 "bintray.user"
         *
         * @see [Publishing]
         */
        public var user: String? = null

        /**
         * Bintray 账户 key. 必须.
         * 若为 `null`, 将会以 [Publishing] 中描述的步骤获取 "bintray.key"
         */
        public var key: String? = null

        /**
         * 目标仓库名称. 必须.
         * 若为 `null`, 将会以 [Publishing] 中描述的步骤获取 "bintray.repo"
         */
        public var repo: String? = null

        /**
         * 目标仓库名称. 必须.
         * 若为 `null`, 将会以 [Publishing] 中描述的步骤获取 "bintray.package"
         */
        public var packageName: String? = null


        ///////////////////////////////////////////////////////////////////////////
        // Optional arguments
        ///////////////////////////////////////////////////////////////////////////

        // Artifact

        /**
         * 发布的 artifact id. 默认为 `project.name`.
         *
         * artifact id 是类似于 "net.mamoe:mirai-console:1.1.0" 中的 "mirai-console"
         */
        public var artifactId: String? = null

        /**
         * 发布的 group id. 默认为 `project.group`.
         *
         * group id 是类似于 "net.mamoe:mirai-console:1.1.0" 中的 "net.mamoe"
         */
        public var groupId: String? = null

        /**
         * 发布的版本号, 默认为 `project.version`
         *
         * 版本号是类似于 "net.mamoe:mirai-console:1.1.0" 中的 "1.1.0"
         */
        public var version: String? = null

        // Bintray

        /**
         * Bintray organization 名. 可选.
         * 若为 `null`, 将会以 [Publishing] 中描述的步骤获取 "bintray.org".
         * 仍然无法获取时发布到 [user] 账号下的仓库 [repo], 否则发布到指定 [org] 下的仓库 [repo].
         */
        public var org: String? = null

        /**
         * 上传后自动发布. 默认 `true`.
         */
        public var publish: Boolean = true

        /**
         * 当文件冲突时覆盖. 默认 `false`.
         */
        public var override: Boolean = false

        // Custom configurations

        internal val bintrayConfigs = mutableListOf<BintrayExtension.() -> Unit>()
        internal val bintrayPackageConfigConfigs = mutableListOf<BintrayExtension.PackageConfig.() -> Unit>()
        internal val mavenPomConfigs = mutableListOf<XmlProvider.() -> Unit>()
        internal val mavenPublicationConfigs = mutableListOf<MavenPublication.() -> Unit>()

        /**
         * 自定义配置 [BintrayExtension]，覆盖
         */
        public fun bintray(config: BintrayExtension.() -> Unit) {
            bintrayConfigs.add(config)
        }

        /**
         * 自定义配置 [BintrayExtension.PackageConfig]
         */
        public fun packageConfig(config: BintrayExtension.PackageConfig.() -> Unit) {
            bintrayPackageConfigConfigs.add(config)
        }

        /**
         * 自定义配置 maven pom.xml [XmlProvider]
         */
        public fun mavenPom(config: XmlProvider.() -> Unit) {
            mavenPomConfigs.add(config)
        }

        /**
         * 自定义配置 [MavenPublication]
         */
        public fun mavenPublication(config: MavenPublication.() -> Unit) {
            mavenPublicationConfigs.add(config)
        }
    }
}

/**
 * @see MiraiConsoleExtension.useTestConsoleFrontEnd
 */
public enum class MiraiConsoleFrontEndKind {
    TERMINAL,
}
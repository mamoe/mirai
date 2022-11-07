/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.gradle.api.DomainObjectCollection
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Dependency
import org.gradle.kotlin.dsl.extra
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import java.io.File

/**
 * # 非常重要的提示 — 有关 relocation — 在看完全部本文前, 不要进行任何操作
 *
 * Mirai 会 relocate 一些内部使用的依赖库, 来允许调用方 (依赖 mirai 的模块).
 * 例如, mirai 使用 2.0, 但调用方可以使用 Ktor 1.0 而不会有 classpath 冲突.
 *
 * 这是通过 relocation 完成的. 在 mirai 项目构建中, Relocation 是指将一个模块的一些 class 的包名替换为另一个包名的过程.
 * 继续使用 Ktor 示例, `io.ktor.utils.io.core.ByteReadPacket` 将会被 relocate 为
 * `net.mamoe.mirai.internal.deps.io.ktor.utils.io.core.ByteReadPacket`, 即放到内部包内.
 *
 * ## 哪些模块被 relocate 了?
 *
 * 在 2.13.0 mirai 只 relocate `io.ktor` 下的所有模块.
 *
 * ## 如何配置 relocation?
 *
 * Relocation 是模块范围的. 可以为 group id `io.ktor` 下的所有模块执行 relocation, 也可以仅为某一个精确的模块比如 `io.ktor:ktor-client-core` 执行.
 *
 * 要增加一条 relocation 规则, 使用 [relocateAllFromGroupId] 或者 [relocateExactArtifact]. 不要现在就过去用, 你必须先读完本文全部.
 *
 * ## 间接依赖不会被处理
 *
 * 被 relocate 的模块的间接依赖不会被处理. 所以 `io.ktor:ktor-client-okhttp` 依赖的 okhttp 不会被 relocate!
 * 因此你必须手动检查目标依赖的全部间接依赖并添加 relocation 配置. 不要轻易升级经过了 relocation 的依赖, 因为有可能他们的新版本会使用新的依赖!
 * 这个过程无法自动化, 因为你 relocate 的模块可能会依赖 kotlin-stlib 等你不会想要 relocation 的依赖. 为什么你不会想 relocate kotlin-stlib? 继续阅读
 *
 * ## 不能 relocate 参与 mirai 公开 API/ABI 的库
 *
 * 有些库的部分定义参与组成 mirai 的公开 API/ABI. 例如 kotlin-stdlib 提供 `String` 等类型, kotlinx-coroutines 提供 `CoroutineScope`等.
 * mirai 已经发布了使用这些类型的 API, 为了保证 ABI 兼容, 不能 relocate 它们.
 * 要知道哪些 API 参与了 ABI, 执行 `./gradlew :mirai-core:dependencies` (把 `mirai-core` 换成你想了解的模块), 查看 `runtimeDependencies` 之类的.
 *
 * ## 考虑在运行时包含被 relocate 的依赖
 *
 * relocate 依赖之后, 你的程序在运行时必须要有 relocate 之后的类, 比如 `net.mamoe.mirai.internal.deps.io.ktor.utils.io.core.ByteReadPacket`.
 * 在配置 relocation 的时候, 你可以选择在当前模块包含或不包含被 relocate 的依赖. 这在单模块下很简单, 只要一直包含就行了.
 *
 * 但在多模块下, 比如 mirai-core 依赖 mirai-core-api, 而它们都需要使用 Ktor, 就需要让_最早_依赖 Ktor 的 mirai-core-api 模块在运行时包含, 而 mirai-core 不包含.
 * 运行时 mirai-core 就会使用来自 mirai-core-api 的 `net.mamoe.mirai.internal.deps.io.ktor.utils.io.core.ByteReadPacket`.
 *
 * ## relocation 发生的时机晚于编译
 *
 * mirai-core-utils relocate 了 Ktor, 然后 mirai-core 在 `build.gradle.kts` 使用了 `implementation(project(":mirai-core-utils"))`.
 * 在 mirai-core 编译时, 编译器仍然会使用 relocate 之前的 `io.ktor`. 要在 mirai-core 将对 `io.ktor` 的调用转为对 `net.mamoe.mirai.internal.deps.io.ktor` 的调用, 需要配置 relocation.
 * 所以此时就不能让 mirai-core 也打包 relocation 后的 ktor 而在运行时携带, 否则因为用户依赖 mirai-core 时 Maven 会同时下载 mirai-core-utils, 用户 classpath 就会有两份被 relocate 的 Ktor, 导致冲突.
 *
 * 所以你需要为所有依赖了 mirai-core-utils 的模块都分别配置 relocation, 并避免它们在运行时携带.
 *
 * ### "在运行时包含" 是如何实现的?
 *
 * 被 relocate 的类会被直接当做是当前模块的类打包进 jar.
 * 比如 ktor-io 的所有代码就会被变换包名后全部打包进 "mirai-core-utils.jar",
 * 而不是在 maven pom 中定义而以后从 Maven Central 等远程仓库下载.
 *
 * 目前 mirai-core-utils 和 mirai-core-all 会进行这个操作.
 * 可以查看它们的 `build.gradle.kts` 中的 [relocateKtorForCore] 调用, 你会发现 `includeInRuntime` 为 `true`.
 *
 * 阅读 [relocateAllFromGroupId] 可以获得更多信息.
 *
 * ----
 *
 * 如果你已经懂了以上内容, 你就可以修改 relocation 相关了. 修改之后一定在 mirai-deps-test 模块中添加测试.
 */
object RelocationNotes

/**
 * 配置 Ktor 依赖.
 * @see RelocationNotes
 * @see relocateKtorForCore
 */
fun NamedDomainObjectContainer<KotlinSourceSet>.configureKtorClientImplementationDependencies(addDep: KotlinDependencyHandler.(String) -> Dependency?) {
    findByName("jvmBaseMain")?.apply {
        dependencies {
            addDep(`ktor-client-okhttp`)
        }
    }

    configure(WIN_TARGETS.map { getByName(it + "Main") }) {
        dependencies {
            addDep(`ktor-client-curl`)
        }
    }

    configure(LINUX_TARGETS.map { getByName(it + "Main") }) {
        dependencies {
            addDep(`ktor-client-cio`)
        }
    }

    findByName("darwinMain")?.apply {
        dependencies {
            addDep(`ktor-client-darwin`)
        }
    }
}

fun <T : Dependency> KotlinDependencyHandler.relocate(dependency: T?, includeInRuntime: Boolean): T {
    dependency!!
    project.relocateExactArtifact(
        groupId = dependency.group ?: throw IllegalArgumentException("group must not be null"),
        artifactId = dependency.name,
        includeInRuntime = includeInRuntime
    )
    return dependency
}

fun KotlinDependencyHandler.relocateRuntime(dependencyNotation: String): Dependency {
    return relocate(implementation(dependencyNotation), includeInRuntime = true)
}

fun KotlinDependencyHandler.relocateCompileOnly(dependencyNotation: String): Dependency {
    return relocate(compileOnly(dependencyNotation), includeInRuntime = false)
}

inline fun <T> configure(list: Iterable<T>, function: T.() -> Unit) {
    list.forEach(function)
}


/**
 * 使用之前阅读 [RelocationNotes]
 */
fun Project.relocateKtorForCore(includeInRuntime: Boolean) {
    // WARNING: You must also consider relocating transitive dependencies.
    // Otherwise, user will get NoClassDefFound error when using mirai as a classpath dependency. See #2263.

    relocateAllFromGroupId("io.ktor", includeInRuntime)
    relocateAllFromGroupId("com.squareup.okhttp3", includeInRuntime, listOf("okhttp3"))
    relocateAllFromGroupId("com.squareup.okio", includeInRuntime, listOf("okio"))
}

/**
 * relocate 一个 [groupId] 下的所有模块.
 *
 * 如果要 relocate 的依赖来自你依赖的另一个模块, 必须传 [includeInRuntime] 为 `false`, 否则会导致运行时类冲突.
 * 如果要 relocate 的依赖不是来自你依赖的另一个模块, 即是你自己新加的, 必须传 [includeInRuntime] 为 `true`, 否则会导致运行时类缺失.
 * 而在依赖你的模块中, 如果依赖方也要使用到你 relocate 的这个依赖, 依赖方也必须配置一样的 [relocateAllFromGroupId], 但是传 [includeInRuntime] 为 `false`, 以避免运行时冲突.
 * 如果你忘记为依赖方配置 relocation, 你可能会能正常编译和测试, 但在发布版本后遇到上述严重运行时问题.
 *
 * 要了解 relocation, 阅读 [RelocationNotes]. **你必须阅读至少一遍**这个备注才能进行任何 relocation 修改.
 *
 * @param groupId 例如 `io.ktor`
 * @param includeInRuntime 将 relocate 后的依赖本体包含在运行时 classpath.
 * @param packages 被 relocate 的模块的全部顶层包. 如 `com.squareup.okhttp3:okhttp` 的顶层包是 `okhttp3`
 */
fun Project.relocateAllFromGroupId(
    groupId: String,
    includeInRuntime: Boolean,
    packages: List<String> = listOf(groupId),
) {
    relocationFilters.add(
        RelocationFilter(
            groupId,
            packages = packages,
            includeInRuntime = includeInRuntime
        )
    )
}

/**
 * 精确地 relocate 一个依赖.
 */
fun Project.relocateExactArtifact(groupId: String, artifactId: String, includeInRuntime: Boolean) {
    relocationFilters.add(RelocationFilter(groupId, artifactId, includeInRuntime = includeInRuntime))
}


data class RelocationFilter(
    val groupId: String,
    val artifactId: String? = null,
    val packages: List<String> = listOf(groupId),
    val filesFilter: String = groupId.replace(".", "/"),
    /**
     * Pack relocated dependency into the fat jar. If set to `false`, dependencies will be removed.
     * This is to avoid duplicated classes. See #2291.
     */ // #2291
    val includeInRuntime: Boolean,
) {

    fun matchesFile(file: File): Boolean {
        val path = file.absolutePath.replace("\\", "/")
        return filesFilter in path
                || groupId in path
    }

    fun matchesDependency(groupId: String?, artifactId: String?): Boolean {
        if (this.groupId == groupId) return true
        if (this.artifactId != null && this.artifactId == artifactId) return true

        return false
    }
}

val Project.relocationFilters: DomainObjectCollection<RelocationFilter>
    get() {
        if (project.extra.has("relocationFilters")) {
            @Suppress("UNCHECKED_CAST")
            return project.extra.get("relocationFilters") as DomainObjectCollection<RelocationFilter>

        } else {
            val container = project.objects.domainObjectSet(RelocationFilter::class.java)
            project.extra.set("relocationFilters", container)
            return container
        }
    }

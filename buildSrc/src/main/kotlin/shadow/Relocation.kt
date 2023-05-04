/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package shadow

import ExcludeProperties
import RelocatableDependency
import RelocatedDependency
import org.gradle.api.Action
import org.gradle.api.DomainObjectCollection
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.kotlin.dsl.accessors.runtime.addDependencyTo
import org.gradle.kotlin.dsl.extra
import org.gradle.kotlin.dsl.invoke
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler

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
 * 在 2.13.0 mirai 只 relocate `io.ktor`, `okhttp3`, `okio` 下的所有类.
 *
 * ## 如何配置 relocation?
 *
 * Relocation 的范围是通过 [Configuration] 指定的.
 *
 * 在通常的 `dependencies` 配置中, 使用 [relocateCompileOnly] 和 [relocateImplementation]
 * 可分别创建 `compileOnly` 或 `implementation` 的依赖, 并为其配置 relocation.
 *
 * ## 不能 relocate 参与 mirai 公开 API/ABI 的库
 *
 * 有些库的部分定义参与组成 mirai 的公开 API/ABI. 例如 kotlin-stdlib 提供 `String` 等类型, kotlinx-coroutines 提供 `CoroutineScope`等.
 * mirai 已经发布了使用这些类型的 API, 为了保证 ABI 兼容, 不能 relocate 它们 (你就需要在 [Configuration] `exclude`).
 * 要知道哪些 API 参与了 ABI, 执行 `./gradlew :mirai-core:dependencies` (把 `mirai-core` 换成你想了解的模块), 查看 `runtimeDependencies` 之类的.
 *
 * ## 考虑是否在运行时包含你的依赖 — 选择 [relocateCompileOnly] 和 [relocateImplementation]
 *
 * 根据 [Configuration] 配置不同, 被 relocate 的模块的间接依赖可能会或不会被处理.
 * 例如
 *
 * 所以 `io.ktor:ktor-client-okhttp` 依赖的 okhttp 不会被 relocate!
 * 因此你必须手动检查目标依赖的全部间接依赖并添加 relocation 配置. 不要轻易升级经过了 relocation 的依赖, 因为有可能他们的新版本会使用新的依赖!
 * 这个过程无法自动化, 因为你 relocate 的模块可能会依赖 kotlin-stlib 等你不会想要 relocation 的依赖. 为什么你不会想 relocate kotlin-stlib? 继续阅读
 *
 * relocate 依赖之后, 你的程序在运行时必须要有 relocate 之后的类, 比如 `net.mamoe.mirai.internal.deps.io.ktor.utils.io.core.ByteReadPacket`.
 *
 * [relocateCompileOnly] 会为你添加通常的 `compileOnly`, 然后配置 relocate, 但不会在打包 JAR 时包含被 relocate 的库. 而 [relocateImplementation] 则会包含.
 *
 * 在独立模块下很简单, 你只要一直使用 [relocateImplementation] 就行了.
 *
 * 但在有依赖关系的模块, 比如 mirai-core-api 依赖 mirai-core-utils, 而它们都需要使用 ktor-io, 就需要让_最早_依赖 ktor-io 的 mirai-core-utils 模块在运行时包含 ([relocateImplementation]), 而 mirai-core-api 不包含 ([relocateCompileOnly]).
 * 运行时 mirai-core-api 就会使用来自 mirai-core-utils 的 `net.mamoe.mirai.internal.deps.io.ktor.utils.io.core.ByteReadPacket`.
 *
 * 如果你都使用 [relocateImplementation], 就会导致在 Android 平台发生 'Duplicated Class' 问题. 如果你都使用 [relocateCompileOnly] 则会在 clinit 阶段遇到 [NoClassDefFoundError]
 *
 * ## relocation 发生的时机晚于编译 (Jar)
 *
 * mirai-core-utils relocate 了 ktor-io, 然后 mirai-core 在 `build.gradle.kts` 使用了 `implementation(project(":mirai-core-utils"))`.
 * 在 mirai-core 编译时, 编译器仍然会使用 relocate 之前的 `io.ktor`. 为了在 mirai-core 将对 `io.ktor` 的调用转为对 `net.mamoe.mirai.internal.deps.io.ktor` 的调用, 需要配置 relocation.
 * 所以此时就不能让 mirai-core 也打包 relocation 后的 ktor 而在运行时携带, 否则因为用户依赖 mirai-core 时 Maven 会同时下载 mirai-core-utils, 用户 classpath 就会有两份被 relocate 的 Ktor, 导致冲突.
 *
 * 所以你需要为所有依赖了 mirai-core-utils 的模块都分别配置 [relocateCompileOnly].
 *
 * ## relocation 仅在发布 (e.g. `publishToMavenLocal`) 时自动使用
 *
 * 其他任何时候, 比如在 mirai-console 编译时, mirai-console 依赖的是未 relocate 的 JAR. 使用 `jar` 任务打包的也是未 relocate 的.
 *
 * 若需要 relocated 的 JAR, 使用 `relocateJvmDependencies`. 其中 `Jvm` 可换为其他启动了 relocate 的 Kotlin target 名.
 * 可在 IDEA Gradle 视图中找到 mirai 文件夹, 查看可用的 task 列表.
 *
 * ### "在运行时包含" 是如何实现的?
 *
 * 被 relocate 的类会被直接当做是当前模块的类打包进 JAR.
 * 比如 ktor-io 的所有代码就会被变换包名后全部打包进 "mirai-core-utils.jar",
 * 而不是在 maven POM 中定义而以后从 Maven Central 等远程仓库下载.
 *
 * ----
 *
 * 如果你已经懂了以上内容, 你就可以修改 relocation 相关了. 修改之后一定在 mirai-deps-test 模块中添加测试.
 */
object RelocationNotes

/**
 * 添加一个通常的 [compileOnly][KotlinDependencyHandler.compileOnly] 依赖, 并按 [relocatedDependency] 定义的配置 relocate.
 *
 * 在发布版本时, 全部对 [RelocatedDependency.packages] 中的 API 的调用**都不会**被 relocate 到 [RelocationConfig.RELOCATION_ROOT_PACKAGE].
 * 运行时 (runtime) **不会**包含被 relocate 的依赖及其所有间接依赖.
 *
 * @see RelocationNotes
 */
fun KotlinDependencyHandler.relocateCompileOnly(
    relocatedDependency: RelocatedDependency,
): ExternalModuleDependency {
    val dependency = compileOnly(relocatedDependency.notation) {
        relocatedDependency.exclusionAction(this)
    }
    project.relocationFilters.add(
        RelocationFilter(
            relocatedDependency.notationsToExcludeInPom,
            relocatedDependency.packages.toList(),
            includeInRuntime = false,
        )
    )
    // Don't add to runtime
    return dependency
}

/**
 * 添加一个通常的 [compileOnly][KotlinDependencyHandler.compileOnly] 依赖, 并按 [relocatedDependency] 定义的配置 relocate.
 *
 * 在发布版本时, 全部对 [RelocatedDependency.packages] 中的 API 的调用**都不会**被 relocate 到 [RelocationConfig.RELOCATION_ROOT_PACKAGE].
 * 运行时 (runtime) **不会**包含被 relocate 的依赖及其所有间接依赖.
 *
 * @see RelocationNotes
 */
fun DependencyHandler.relocateCompileOnly(
    project: Project,
    relocatedDependency: RelocatedDependency,
): Dependency {
    val dependency =
        addDependencyTo(this, "compileOnly", relocatedDependency.notation, Action<ExternalModuleDependency> {
            relocatedDependency.exclusionAction(this)
        })
    project.relocationFilters.add(
        RelocationFilter(
            relocatedDependency.notationsToExcludeInPom,
            relocatedDependency.packages.toList(),
            includeInRuntime = false,
        )
    )
    // Don't add to runtime
    return dependency
}

/**
 * 添加一个通常的 [implementation][KotlinDependencyHandler.implementation] 依赖, 并按 [relocatedDependency] 定义的配置 relocate.
 *
 * 在发布版本时, 全部对 [RelocatedDependency.packages] 中的 API 的调用**都会**被 relocate 到 [RelocationConfig.RELOCATION_ROOT_PACKAGE].
 * 运行时 (runtime) 将**会**包含被 relocate 的依赖及其所有间接依赖.
 *
 * @see RelocationNotes
 */
fun KotlinDependencyHandler.relocateImplementation(
    relocatedDependency: RelocatedDependency,
    action: ExternalModuleDependency.() -> Unit = {}
): ExternalModuleDependency {
    val dependency = implementation(relocatedDependency.notation) {
        relocatedDependency.exclusionAction(this)
    }
    project.relocationFilters.add(
        RelocationFilter(
            relocatedDependency.notationsToExcludeInPom, relocatedDependency.packages.toList(), includeInRuntime = true,
        )
    )
    val configurationName = RelocationConfig.SHADOW_RELOCATION_CONFIGURATION_NAME
    project.configurations.maybeCreate(configurationName)
    addDependencyTo(
        project.dependencies,
        configurationName,
        relocatedDependency.notation,
        Action<ExternalModuleDependency> {
            relocatedDependency.exclusionAction(this)
            intrinsicExclusions()
            action()
        }
    )
    return dependency
}

/**
 * 添加一个通常的 [implementation][KotlinDependencyHandler.implementation] 依赖, 并按 [relocatedDependency] 定义的配置 relocate.
 *
 * 在发布版本时, 全部对 [RelocatedDependency.packages] 中的 API 的调用都会被 relocate 到 [RelocationConfig.RELOCATION_ROOT_PACKAGE].
 * 运行时 (runtime) 将会包含被 relocate 的依赖及其所有间接依赖.
 *
 * @see RelocationNotes
 */
fun DependencyHandler.relocateImplementation(
    project: Project,
    relocatedDependency: RelocatedDependency,
    action: Action<ExternalModuleDependency> = Action {}
): ExternalModuleDependency {
    val dependency =
        addDependencyTo(this, "implementation", relocatedDependency.notation, Action<ExternalModuleDependency> {
            relocatedDependency.exclusionAction(this)
        })
    project.relocationFilters.add(
        RelocationFilter(
            relocatedDependency.notationsToExcludeInPom, relocatedDependency.packages.toList(), includeInRuntime = true,
        )
    )
    val configurationName = RelocationConfig.SHADOW_RELOCATION_CONFIGURATION_NAME
    project.configurations.maybeCreate(configurationName)
    addDependencyTo(
        project.dependencies,
        configurationName,
        relocatedDependency.notation,
        Action<ExternalModuleDependency> {
            relocatedDependency.exclusionAction(this)
            intrinsicExclusions()
            action(this)
        }
    )
    return dependency
}

private val ExternalModuleDependency.groupNotNull: String get() = group.toString()

private fun ExternalModuleDependency.intrinsicExclusions() {
    exclude(ExcludeProperties.`everything from kotlin`)
    exclude(ExcludeProperties.`everything from kotlinx`)
}


data class RelocationFilter(
    val notations: RelocatableDependency,
    val packages: List<String>,
//    val filesFilter: String = groupId.replace(".", "/"),
    /**
     * Pack relocated dependency into the fat jar. If set to `false`, dependencies will be removed.
     * This is to avoid duplicated classes. See #2291.
     */ // #2291
    val includeInRuntime: Boolean,
) {

    fun matchesDependency(groupId: String?, artifactId: String?): Boolean {
        return notations.notations().any {
            it.groupId == groupId && it.artifactId == artifactId
        }
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

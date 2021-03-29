/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.console.intellij.creator.steps

import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VirtualFile
import net.mamoe.mirai.console.intellij.creator.MiraiProjectModel
import net.mamoe.mirai.console.intellij.creator.build.GradleGroovyProjectCreator
import net.mamoe.mirai.console.intellij.creator.build.GradleKotlinProjectCreator
import net.mamoe.mirai.console.intellij.creator.build.ProjectCreator

enum class BuildSystemType {
    GradleKt {
        override fun createBuildSystem(module: Module, root: VirtualFile, model: MiraiProjectModel): ProjectCreator =
            GradleKotlinProjectCreator(module, root, model)

        override fun toString(): String = "Gradle Kotlin DSL"
    },
    GradleGroovy {
        override fun createBuildSystem(module: Module, root: VirtualFile, model: MiraiProjectModel): ProjectCreator =
            GradleGroovyProjectCreator(module, root, model)

        override fun toString(): String = "Gradle Groovy DSL"
    }, ;

    abstract fun createBuildSystem(module: Module, root: VirtualFile, model: MiraiProjectModel): ProjectCreator

    companion object {
        val DEFAULT = GradleKt
    }
}
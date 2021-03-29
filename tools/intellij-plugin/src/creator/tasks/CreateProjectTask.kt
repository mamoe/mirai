/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.intellij.creator.tasks

import com.intellij.ide.ui.UISettings
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.StatusBarEx
import net.mamoe.mirai.console.intellij.creator.MiraiProjectModel
import org.jetbrains.kotlin.idea.util.application.invokeLater
import org.jetbrains.plugins.gradle.service.project.open.linkAndRefreshGradleProject
import java.nio.file.Files
import java.nio.file.Path

class CreateProjectTask(
    private val root: Path,
    private val module: Module,
    private val model: MiraiProjectModel,
) : Task.Backgroundable(module.project, "Creating project", false) {
    override fun shouldStartInBackground() = false

    override fun run(indicator: ProgressIndicator) {
        if (module.isDisposed || project.isDisposed) return

        Files.createDirectories(root)

        invokeAndWait {
            VfsUtil.markDirtyAndRefresh(false, true, true, root.vf)
        }

        val build = model.buildSystemType.createBuildSystem(module, root.vf, model)

        build.createProject(module, root.vf, model)
        build.doFinish(indicator)

        invokeLater {
            VfsUtil.markDirtyAndRefresh(false, true, true, root.vf)
        }

        invokeLater {
            @Suppress("UnstableApiUsage")
            (linkAndRefreshGradleProject(root.toAbsolutePath().toString(), project))
            showProgress(project)
        }
    }

}

private fun showProgress(project: Project) {
    if (!UISettings.instance.showStatusBar || UISettings.instance.presentationMode) {
        return
    }

    val statusBar = WindowManager.getInstance().getStatusBar(project) as? StatusBarEx ?: return
    statusBar.isProcessWindowOpen = true
}

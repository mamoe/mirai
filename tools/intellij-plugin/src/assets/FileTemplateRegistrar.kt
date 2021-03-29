/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.intellij.assets

import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor

class FileTemplateRegistrar : com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory {
    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        return FileTemplateGroupDescriptor("Mirai", Icons.PluginMainDeclaration).apply {
            addTemplate(FileTemplateDescriptor(FT.BuildGradleKts))
            addTemplate(FileTemplateDescriptor(FT.BuildGradle))

            addTemplate(FileTemplateDescriptor(FT.PluginMainKt))
            addTemplate(FileTemplateDescriptor(FT.PluginMainJava))

            addTemplate(FileTemplateDescriptor(FT.GradleProperties))

            addTemplate(FileTemplateDescriptor(FT.SettingsGradleKts))
            addTemplate(FileTemplateDescriptor(FT.SettingsGradle))

            addTemplate(FileTemplateDescriptor(FT.Gitignore))
        }
    }

}


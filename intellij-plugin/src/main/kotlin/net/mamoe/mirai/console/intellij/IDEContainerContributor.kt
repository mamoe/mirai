package net.mamoe.mirai.console.intellij

import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor

class IDEContainerContributor : StorageComponentContainerContributor {
    override fun registerModuleComponents(
        container: StorageComponentContainer,
        platform: org.jetbrains.kotlin.platform.TargetPlatform,
        moduleDescriptor: ModuleDescriptor,
    ) {
        container.useInstance(MiraiConsoleDeclarationChecker())
    }
}
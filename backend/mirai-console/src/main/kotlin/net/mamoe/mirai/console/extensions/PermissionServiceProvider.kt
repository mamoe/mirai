package net.mamoe.mirai.console.extensions

import net.mamoe.mirai.console.extension.AbstractExtensionPoint
import net.mamoe.mirai.console.extension.SingletonExtension
import net.mamoe.mirai.console.permission.PermissionService
import net.mamoe.mirai.console.util.ConsoleExperimentalAPI

@ConsoleExperimentalAPI
public interface PermissionServiceProvider : SingletonExtension<PermissionService> {
    public companion object ExtensionPoint :
        AbstractExtensionPoint<PermissionServiceProvider>(PermissionServiceProvider::class)
}
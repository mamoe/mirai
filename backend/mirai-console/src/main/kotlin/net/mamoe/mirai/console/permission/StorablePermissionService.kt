package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginDataExtensions.withDefault
import net.mamoe.mirai.console.data.value
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@ExperimentalPermission
public interface StorablePermissionService<P : Permission> : PermissionService<P> {
    /**
     * The config to be stored
     */
    public val config: PluginConfig

    @ExperimentalPermission
    public class ConcurrentSaveData<P : Permission>(
        public override val saveName: String,
        delegate: PluginConfig
    ) : PluginConfig by delegate {
        public val permissions: MutableMap<PermissionId, P> by value<MutableMap<PermissionId, P>>(ConcurrentHashMap())

        public val grantedPermissionMap: MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>> by
        value<MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>>(ConcurrentHashMap()).withDefault { CopyOnWriteArrayList() }
    }
}

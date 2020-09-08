package net.mamoe.mirai.console.permission

import net.mamoe.mirai.console.data.PluginConfig
import net.mamoe.mirai.console.data.PluginDataExtensions.withDefault
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.data.valueFromKType
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.reflect.KType
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.full.createType

@ExperimentalPermission
public interface StorablePermissionService<P : Permission> : PermissionService<P> {
    /**
     * The config to be stored
     */
    public val config: PluginConfig

    @ExperimentalPermission
    public class ConcurrentSaveData<P : Permission> private constructor(
        permissionType: KType,
        public override val saveName: String,
        delegate: PluginConfig,
        @Suppress("UNUSED_PARAMETER") primaryConstructorMark: Any?
    ) : PluginConfig by delegate {
        public val permissions: MutableMap<PermissionId, P>
                by valueFromKType<MutableMap<PermissionId, P>>(
                    MutableMap::class.createType(
                        listOf(
                            KTypeProjection(KVariance.INVARIANT, PermissionId::class.createType()),
                            KTypeProjection(KVariance.INVARIANT, permissionType),
                        )
                    ),
                    ConcurrentHashMap()
                )

        public val grantedPermissionMap: MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>
                by value<MutableMap<PermissionId, MutableCollection<PermissibleIdentifier>>>(ConcurrentHashMap())
                    .withDefault { CopyOnWriteArrayList() }

        public companion object {
            @JvmStatic
            public operator fun <P : Permission> invoke(
                permissionType: KType,
                saveName: String,
                delegate: PluginConfig,
            ): ConcurrentSaveData<P> = ConcurrentSaveData(permissionType, saveName, delegate, null)
        }
    }
}

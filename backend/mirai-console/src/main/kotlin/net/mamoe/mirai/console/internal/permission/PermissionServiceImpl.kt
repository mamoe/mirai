package net.mamoe.mirai.console.internal.permission

import net.mamoe.mirai.console.command.ConsoleCommandSender
import net.mamoe.mirai.console.command.UserCommandSender
import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.PluginDataExtensions.withEmptyDefault
import net.mamoe.mirai.console.data.Value
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.extensions.PermissionServiceProvider
import net.mamoe.mirai.console.permission.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

/**
 * [PermissionServiceProvider]
 */
@Suppress("RedundantVisibilityModifier")
@ExperimentalPermission
internal abstract class AbstractPermissionService<TPermission : Permission, TPermissibleIdentifier> :
    PermissionService<TPermission> {
    protected abstract val Permissible.identifier: TPermissibleIdentifier

    @JvmField
    protected val permissions: MutableMap<PermissionIdentifier, TPermission> = ConcurrentHashMap()

    @JvmField
    protected val grantedPermissionMap: MutableMap<TPermissibleIdentifier, MutableList<PermissionIdentifier>> =
        ConcurrentHashMap()

    public override fun getGrantedPermissions(permissible: Permissible): Sequence<TPermission> =
        grantedPermissionMap[permissible.identifier]?.asSequence()?.mapNotNull { get(it) }.orEmpty()

    public override operator fun get(identifier: PermissionIdentifier): TPermission? = permissions[identifier]

    public override fun testPermission(permissible: Permissible, permission: TPermission): Boolean =
        permissible.getGrantedPermissions().any { it == permission }
}

/**
 * [PermissionServiceProvider]
 */
@Suppress("RedundantVisibilityModifier")
@ExperimentalPermission
internal abstract class AbstractHotDeploymentSupportPermissionService<TPermission : Permission, TPermissibleIdentifier> :
    PermissionService<TPermission>,
    HotDeploymentSupportPermissionService<TPermission>, AutoSavePluginConfig() {

    protected abstract val Permissible.identifier: TPermissibleIdentifier

    @JvmField
    protected val permissions: MutableMap<PermissionIdentifier, TPermission> = ConcurrentHashMap()

    @JvmField
    protected val grantedPermissionMap: Value<MutableMap<TPermissibleIdentifier, MutableList<PermissionIdentifier>>> =
        value<MutableMap<TPermissibleIdentifier, MutableList<PermissionIdentifier>>>().withEmptyDefault()

    public override fun getGrantedPermissions(permissible: Permissible): Sequence<TPermission> =
        grantedPermissionMap.value[permissible.identifier]?.asSequence()?.mapNotNull { get(it) }.orEmpty()

    public override operator fun get(identifier: PermissionIdentifier): TPermission? = permissions[identifier]

    public override fun testPermission(permissible: Permissible, permission: TPermission): Boolean =
        permissible.getGrantedPermissions().any { it == permission }
}


internal data class LiteralPermissibleIdentifier(
    val context: String,
    val value: String
)

@OptIn(ExperimentalPermission::class)
private object PermissionServiceImpl :
    AbstractHotDeploymentSupportPermissionService<PermissionImpl, LiteralPermissibleIdentifier>() {

    override fun register(
        identifier: PermissionIdentifier,
        description: String,
        base: PermissionIdentifier?
    ): PermissionImpl = PermissionImpl(identifier, description, base)

    override fun grant(permissible: Permissible, permission: PermissionImpl) {
        grantedPermissionMap.value[permissible.identifier]!!.add(permission.identifier)
    }

    override fun deny(permissible: Permissible, permission: PermissionImpl) {
        grantedPermissionMap.value[permissible.identifier]!!.remove(permission.identifier)
    }

    override val permissionType: KClass<PermissionImpl>
        get() = PermissionImpl::class
    override val Permissible.identifier: LiteralPermissibleIdentifier
        get() = LiteralPermissibleIdentifier(
            "",
            when (this) {
                is ConsoleCommandSender -> "CONSOLE"
                is UserCommandSender -> this.user.id.toString()
                else -> ""
            }
        )
}
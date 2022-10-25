package net.mamoe.mirai.clikt.core

import net.mamoe.mirai.clikt.parameters.groups.ParameterGroup
import net.mamoe.mirai.clikt.parameters.options.Option

@DslMarker
public annotation class ParameterHolderDsl

@ParameterHolderDsl
public interface ParameterHolder {
    /**
     * Register an option with this command or group.
     *
     * This is called automatically for the built in options, but you need to call this if you want to add a
     * custom option.
     */
    public fun registerOption(option: GroupableOption)
}

public interface StaticallyGroupedOption : Option {
    /** The name of the group, or null if this option should not be grouped in the help output. */
    public val groupName: String?
}

/**
 * An option that can be added to a [ParameterGroup]
 */
public interface GroupableOption : StaticallyGroupedOption {
    /** The group that this option belongs to, or null. Set by the group. */
    public var parameterGroup: ParameterGroup?

    /** The name of the group, or null if this option should not be grouped in the help output. */
    override var groupName: String?
}
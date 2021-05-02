/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.internal.network.components

import net.mamoe.mirai.internal.network.component.ComponentKey
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Reset bot and others components to initialize state for testing
 */
internal interface BotReinitActions {
    val enabled: Boolean

    fun addAction0(action: () -> Unit)

    //@VisitOnlyForTests
    fun invokeActions()

    companion object : ComponentKey<BotReinitActions> {
        val EMPTY: BotReinitActions = EmptyBotReinitActions

        fun byDelegate(supplier: () -> BotReinitActions): BotReinitActions {
            return object : BotReinitActions {
                override val enabled: Boolean
                    get() = supplier().enabled

                override fun addAction0(action: () -> Unit) {
                    supplier().addAction0(action)
                }

                override fun invokeActions() {
                    supplier().invokeActions()
                }
            }
        }
    }
}

internal inline fun BotReinitActions.addAction(crossinline action: () -> Unit) {
    if (enabled) {
        addAction0 { action() }
    }
}

internal class BotReinitActionsImpl : BotReinitActions {
    private val actions = ConcurrentLinkedQueue<() -> Unit>()
    override val enabled: Boolean
        get() = true

    override fun addAction0(action: () -> Unit) {
        actions.add(action)
    }

    override fun invokeActions() {
        actions.forEach { it() }
    }
}

private object EmptyBotReinitActions : BotReinitActions {
    override val enabled: Boolean
        get() = false

    override fun addAction0(action: () -> Unit) {
    }

    override fun invokeActions() {
    }
}
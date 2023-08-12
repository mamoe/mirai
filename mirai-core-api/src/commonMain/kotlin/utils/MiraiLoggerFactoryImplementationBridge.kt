/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.utils

import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.loop
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * @since 2.13
 */
internal object MiraiLoggerFactoryImplementationBridge : MiraiLogger.Factory {
    private var _instance by lateinitMutableProperty {
        createPlatformInstance()
    }

    internal val instance get() = _instance

    // It is required for MiraiConsole because default implementation
    // queries stdout on every message printing
    // It creates an infinite loop (StackOverflowError)
    internal var defaultLoggerFactory: (() -> MiraiLogger.Factory) = ::DefaultFactory

    fun createPlatformInstance() = loadService(MiraiLogger.Factory::class, defaultLoggerFactory)

    private val frozen = atomic(false)

    fun freeze(): Boolean {
        return frozen.compareAndSet(expect = false, update = true)
    }

    @TestOnly
    fun reinit() {
        defaultLoggerFactory = ::DefaultFactory
        frozen.loop { value ->
            _instance = createPlatformInstance()
            if (frozen.compareAndSet(value, false)) return
        }
    }

    fun setInstance(instance: MiraiLogger.Factory) {
        if (frozen.value) {
            error(
                "LoggerFactory instance had been frozen, so it's impossible to override it." +
                        "If you are using Mirai Console and you want to override platform logging implementation, " +
                        "please do so before initialization of MiraiConsole, that is, before `MiraiConsoleImplementation.start()`. " +
                        "Plugins are not allowed to override logging implementation, and this is done in the very fundamental implementation of Mirai Console so there is no way to escape that." +
                        "Normally it is only sensible for Mirai Console frontend implementor to do that." +
                        "If you are just using mirai-core, this error should not happen. There should be no limitation in overriding logging implementation with mirai-core. " +
                        "Check if you actually did use mirai-console somewhere, or please file an issue on https://github.com/mamoe/mirai/issues/new/choose"
            )
        }
        this._instance = instance
    }

    inline fun wrapCurrent(mapper: (current: MiraiLogger.Factory) -> MiraiLogger.Factory) {
        contract { callsInPlace(mapper, InvocationKind.EXACTLY_ONCE) }
        setInstance(this.instance.let(mapper))
    }

    override fun create(requester: KClass<*>, identity: String?): MiraiLogger {
        return instance.create(requester, identity)
    }

    override fun create(requester: Class<*>, identity: String?): MiraiLogger {
        return instance.create(requester, identity)
    }

    override fun create(requester: KClass<*>): MiraiLogger {
        return instance.create(requester)
    }

    override fun create(requester: Class<*>): MiraiLogger {
        return instance.create(requester)
    }
}


// used by Mirai Console
private class DefaultFactory : MiraiLogger.Factory {
    @OptIn(MiraiInternalApi::class)
    override fun create(requester: Class<*>, identity: String?): MiraiLogger {
        return PlatformLogger(identity ?: requester.kotlin.simpleName ?: requester.simpleName)
    }
}

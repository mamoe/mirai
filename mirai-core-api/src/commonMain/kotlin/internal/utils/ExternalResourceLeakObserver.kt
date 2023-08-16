/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.MiraiLogger
import net.mamoe.mirai.utils.error
import net.mamoe.mirai.utils.warning
import java.lang.ref.ReferenceQueue
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentLinkedDeque

internal object ExternalResourceLeakObserver : Runnable {
    private val queue = ReferenceQueue<Any>()
    private val references = ConcurrentLinkedDeque<ERReference>()
    private val logger by lazy {
        MiraiLogger.Factory.create(ExternalResourceLeakObserver::class, "ExternalResourceLeakObserver")
    }

    internal class ERReference : WeakReference<Any> {
        constructor(resource: ExternalResourceInternal) : super(resource, queue) {
            this.holder = resource.holder
        }

        constructor(resource: ExternalResource, holder: ExternalResourceHolder) : super(resource, queue) {
            this.holder = holder
        }

        @JvmField
        internal val holder: ExternalResourceHolder
    }

    class ExternalResourceCreateStackTrace : Throwable() {
        override fun fillInStackTrace(): Throwable {
            return this
        }
    }


    @JvmStatic
    fun register(resource: ExternalResource) {
        if (resource !is ExternalResourceInternal) return
        references.add(ERReference(resource))
    }

    @JvmStatic
    fun register(resource: ExternalResource, holder: ExternalResourceHolder) {
        references.add(ERReference(resource, holder))
    }

    init {
        val thread = Thread(this, "Mirai ExternalResource Leak Observer Thread")
        thread.isDaemon = true
        thread.start()
    }

    override fun run() {
        while (true) {

            try {
                loop@
                while (true) {
                    val reference = queue.poll() ?: break@loop
                    if (reference !is ERReference) {
                        logger.warning { "Unknown reference $reference (#${reference.javaClass}) was entered queue. Skipping" }
                        reference.clear()
                        continue@loop
                    }
                    val holder = reference.holder
                    reference.clear()
                    references.remove(reference)
                    if (holder.isClosed) {
                        continue@loop
                    }
                    val stackException = holder.createStackTrace?.let { stack ->
                        ExternalResourceCreateStackTrace().also { it.stackTrace = stack }
                    }
                    kotlin.runCatching { // Observer should avoid all possible errors
                        logger.error(
                            {
                                "A resource leak occurred, use ExternalResource.close to avoid it!! (holder=$holder)" + if (isExternalResourceCreationStackEnabled) {
                                    ""
                                } else ". Add jvm option `-D$isExternalResourceCreationStackEnabledName=true` to show creation stack track"
                            },
                            stackException
                        )
                    }
                    try {
                        holder.close()
                    } catch (exceptionInClose: Throwable) {
                        kotlin.runCatching { // Observer should avoid all possible errors
                            logger.error(
                                { "Exception in closing a leaked resource (holder=$holder)" },
                                exceptionInClose.also {
                                    if (stackException != null) {
                                        it.addSuppressed(stackException)
                                    }
                                }
                            )
                        }
                    }
                }
            } catch (throwable: Throwable) {
                kotlin.runCatching { // Observer should avoid all possible errors
                    logger.error(
                        "Exception in queue loop",
                        throwable
                    )
                }
            }

            Thread.sleep(60 * 1000L)
        }
    }
}

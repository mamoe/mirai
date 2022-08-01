/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils

import net.mamoe.mirai.utils.MiraiLogger

/**
 * 内部添加 [Marker] 支持, 并兼容旧 [MiraiLogger] API.
 */
internal interface MarkedMiraiLogger : MiraiLogger {
    val marker: Marker?

    /**
     * Create an implementation-specific [MarkedMiraiLogger].
     *
     * Do not call the extension `MiraiLogger.subLogger` inside the function body.
     */
    fun subLogger(name: String): MarkedMiraiLogger
}

internal fun Marker(name: String, parents: Marker?): Marker {
    return MarkerManager.getMarker(name).apply { if (parents != null) addParents(parents) }
}

internal fun Marker(name: String, vararg parents: Marker?): Marker {
    return MarkerManager.getMarker(name).apply {
        parents.forEach { if (it != null) addParents(it) }
    }
}

internal val MiraiLogger.markerOrNull get() = (this as? MarkedMiraiLogger)?.marker

// This clashes with the same declaration in mirai-core (same package), for native.
//
///**
// * Create a marked logger whose marker is a child of this' marker.
// *
// * Calling [MarkedMiraiLogger.subLogger] if possible, and creating [MiraiLoggerMarkedWrapper] otherwise.
// */
//@JvmName("subLoggerImpl2")
//@CName("", "subLogger2")
//internal fun MiraiLogger.subLogger(name: String): MiraiLogger {
//    return subLoggerImpl(this, name)
//}

// used by mirai-core
internal fun subLoggerImpl(origin: MiraiLogger, name: String): MiraiLogger {
    return if (origin is MarkedMiraiLogger) {
        // origin can be Log4JAdapter or MiraiLoggerMarkedWrapper which delegates a non-Log4JAdapter.
        origin.subLogger(name) // Log4JAdapter natively supports Markers.
    } else {
        return origin
        // origin will never use the MiraiLoggerMarkedWrapper.marker so wrapping it is meaningless.
    }
}

/**
 * 仅当日志系统使用的不是 Log4J 时才会构造 [MiraiLoggerMarkedWrapper].
 */
private class MiraiLoggerMarkedWrapper(
    val origin: MiraiLogger,
    override val marker: Marker
) : MiraiLogger by origin, MarkedMiraiLogger {
    override fun subLogger(name: String): MarkedMiraiLogger = MiraiLoggerMarkedWrapper(origin, Marker(name, marker))
}
/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.console.enduserreadme

import java.util.*

/**
 * 最终用户须知
 *
 * @since 2.16.0
 */
public class EndUserReadme {
    public companion object {
        public const val PAUSE: String = "<pause>"
        public const val DELAY: String = "<delay>"
    }

    internal val pages: MutableMap<String, String> = linkedMapOf()

    public class Render {
        private val msgs = mutableListOf<String>()

        @KeepDetermination
        public operator fun String.unaryPlus() {
            msg(this)
        }

        @KeepDetermination
        public operator fun plusAssign(s: String) {
            msg(s)
        }

        @KeepDetermination
        public fun pause() {
            msg(PAUSE)
        }

        @KeepDetermination
        public fun delay() {
            msg(DELAY)
        }

        /**
         * 延迟一段时间
         *
         * @param time 单位：秒
         */
        @KeepDetermination
        public fun delay(time: Int) {
            msg(DELAY + time)
        }

        @KeepDetermination
        public fun msg(message: String) {
            msgs.add(message)
        }

        public fun render(): String = msgs.joinToString(separator = "\n")
    }

    @KeepDetermination
    public fun put(category: String, render: Render.() -> Unit) {
        pages[category] = Render().also(render).render()
    }

    /**
     * 同时添加多个须知定义
     *
     * 格式:
     * ```text
     *
     * ::category.c1
     *
     * Here is c1
     *
     * delay 2s
     * <delay>2
     *
     * paused
     * <pause>
     *
     * ::category.c1
     *
     * Here is c2
     *
     * ```
     */
    @KeepDetermination
    public fun putAll(fullText: String) {
        if (fullText.isBlank()) return
        val lines = LinkedList(fullText.lines())

        var category: String
        val buffer = mutableListOf<String>()

        while (true) {
            if (lines.isEmpty()) return
            val rm = lines.removeFirst()

            if (rm.isBlank()) continue
            if (rm.startsWith("::")) {
                category = rm.substring(2)
                break
            }
            throw IllegalArgumentException("First non-empty line must be category define: $rm")
        }

        fun flush() {
            while (buffer.isNotEmpty()) {
                if (buffer.first().isBlank()) {
                    buffer.removeAt(0)
                    continue
                }
                break
            }


            while (buffer.isNotEmpty()) {
                if (buffer.last().isBlank()) {
                    buffer.removeAt(buffer.lastIndex)
                    continue
                }
                break
            }

            pages[category] = buffer.joinToString(separator = "\n")
            buffer.clear()
        }

        while (lines.isNotEmpty()) {
            val rm = lines.removeFirst()
            if (rm.startsWith("::")) {
                flush()
                category = rm.substring(2)
                continue
            }
            buffer.add(rm)
        }

        flush()
    }

    @DslMarker
    private annotation class KeepDetermination
}
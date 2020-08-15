/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

/**
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */

import kotlinx.coroutines.CompletableDeferred
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.SwingUtilities

// 隔离类代码
internal object WindowHelperJvm {
    internal val isDesktopSupported: Boolean =
        kotlin.runCatching {
            System.getProperty("mirai.no-desktop") === null && Desktop.isDesktopSupported()
        }.getOrElse {
            false
        }
}

internal class WindowInitialzier(private val initializer: WindowInitialzier.(JFrame) -> Unit) {
    private lateinit var frame0: JFrame
    val frame: JFrame get() = frame0
    fun java.awt.Component.append() {
        frame.add(this, BorderLayout.NORTH);
    }

    fun java.awt.Component.last() {
        frame.add(this);
    }

    internal fun init(frame: JFrame) {
        this.frame0 = frame;
        initializer(frame)
    }
}

internal suspend fun openWindow(title: String = "", initializer: WindowInitialzier.(JFrame) -> Unit = {}): String {
    return openWindow(title, WindowInitialzier(initializer))
}

internal suspend fun openWindow(title: String = "", initializer: WindowInitialzier = WindowInitialzier {}): String {
    val frame = JFrame()
    val value = JTextField()
    val def = CompletableDeferred<String>()
    value.addKeyListener(object : KeyListener {
        override fun keyTyped(e: KeyEvent?) {
        }

        override fun keyPressed(e: KeyEvent?) {
            when (e!!.keyCode) {
                27, 10 -> {
                    def.complete(value.text)
                }
            }
        }

        override fun keyReleased(e: KeyEvent?) {
        }
    })
    frame.layout = BorderLayout(10, 5)
    frame.add(value, BorderLayout.SOUTH)
    initializer.init(frame)

    frame.pack()
    frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
    frame.addWindowListener(object : WindowAdapter() {
        override fun windowClosing(e: WindowEvent?) {
            def.complete(value.text)
        }
    })
    frame.setLocationRelativeTo(null)
    frame.title = title
    frame.isVisible = true

    val result = def.await().trim()
    SwingUtilities.invokeLater {
        frame.dispose()
    }
    return result
}

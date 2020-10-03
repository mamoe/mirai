/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 license that can be found via the following link.
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
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JTextField
import javax.swing.SwingUtilities

// 隔离类代码
@Suppress("DEPRECATION")
internal object WindowHelperJvm {
    internal val isDesktopSupported: Boolean = kotlin.run {
        if (System.getProperty("mirai.no-desktop") === null) {
            kotlin.runCatching {
                Class.forName("java.awt.Desktop")
                Class.forName("java.awt.Toolkit")
            }.onFailure { return@run false } // Android OS
            kotlin.runCatching {
                Toolkit.getDefaultToolkit()
            }.onFailure { // AWT Error, #270
                return@run false
            }
            kotlin.runCatching {
                Desktop.isDesktopSupported().also { stat ->
                    if (stat) {
                        MiraiLogger.info(
                            """
                                Mirai 正在使用桌面环境,
                                如果你正在使用SSH, 或无法访问桌面等,
                                请将 `mirai.no-desktop` 添加到 JVM 系统属性中 (-Dmirai.no-desktop)
                                然后重启 Mirai
                            """.trimIndent()
                        )
                        MiraiLogger.info(
                            """
                                Mirai using DesktopCaptcha System.
                                If you are running on SSH, cannot access desktop or more.
                                Please add `mirai.no-desktop` to JVM properties (-Dmirai.no-desktop)
                                Then restart mirai
                            """.trimIndent()
                        )
                    }
                }
            }.getOrElse {
                // Should not happen
                MiraiLogger.warning("Exception in checking desktop support.", it)
                false
            }
        } else {
            false
        }
    }
}

internal class WindowInitializer(private val initializer: WindowInitializer.(JFrame) -> Unit) {
    private lateinit var frame0: JFrame
    val frame: JFrame get() = frame0
    fun java.awt.Component.append() {
        frame.add(this, BorderLayout.NORTH)
    }

    fun java.awt.Component.last() {
        frame.add(this)
    }

    internal fun init(frame: JFrame) {
        this.frame0 = frame
        initializer(frame)
    }
}

internal val windowIcon: BufferedImage? by lazy {
    WindowHelperJvm::class.java.getResourceAsStream("project-mirai.png")?.use {
        ImageIO.read(it)
    }
}

internal suspend fun openWindow(title: String = "", initializer: WindowInitializer.(JFrame) -> Unit = {}): String {
    return openWindow(title, WindowInitializer(initializer))
}

internal suspend fun openWindow(title: String = "", initializer: WindowInitializer = WindowInitializer {}): String {
    val frame = JFrame()
    frame.iconImage = windowIcon
    frame.minimumSize = Dimension(228, 62) // From Windows 10
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

    return def.await().trim().also {
        SwingUtilities.invokeLater {
            frame.dispose()
        }
    }
}

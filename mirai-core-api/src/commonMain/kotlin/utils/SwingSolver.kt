/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

/**
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */

import kotlinx.coroutines.CompletableDeferred
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.*
import java.awt.BorderLayout
import java.awt.Desktop
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.*
import java.awt.image.BufferedImage
import java.net.URI
import javax.imageio.ImageIO
import javax.swing.*

@MiraiExperimentalApi
public object SwingSolver : LoginSolver() {
    public override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String {
        return openWindow("Mirai PicCaptcha(${bot.id})") {
            val image = ImageIO.read(data.inputStream())
            JLabel(ImageIcon(image)).append()
        }
    }

    public override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        return openWindow("Mirai SliderCaptcha(${bot.id})") {
            JLabel(
                """
                <html>
                需要滑动验证码, 完成后请输入ticket<br/>
                @see: https://github.com/project-mirai/mirai-login-solver-selenium
                """.trimIndent()
            ).append()
            JTextField(url).last()
        }.takeIf { it.isNotEmpty() }
    }

    public override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String {
        val title = "Mirai UnsafeDeviceLoginVerify(${bot.id})"
        return openWindow(title) {
            JLabel(
                """
                <html>
                需要进行账户安全认证<br>
                该账户有[设备锁]/[不常用登录地点]/[不常用设备登录]的问题<br>
                完成以下账号认证即可成功登录|理论本认证在mirai每个账户中最多出现1次<br>
                成功后请关闭该窗口
            """.trimIndent()
            ).append()
            HyperLinkLabel(url, "设备锁验证", title).last()
        }
    }
}


// 隔离类代码
// 在 jvm 中, 使用 WindowHelperJvm 不会加载 SwingSolverKt
// 不会触发各种 NoDefClassError
@Suppress("DEPRECATION")
internal object WindowHelperJvm {
    enum class PlatformKind {
        ANDROID,
        SWING,
        CLI
    }

    internal val platformKind: PlatformKind = kotlin.run {
        if (kotlin.runCatching { Class.forName("android.util.Log") }.isSuccess) {
            // Android platform
            return@run PlatformKind.ANDROID
        }
        if (System.getProperty("mirai.no-desktop") != null) return@run PlatformKind.CLI
        kotlin.runCatching {
            Class.forName("java.awt.Desktop")
            Class.forName("java.awt.Toolkit")
            Toolkit.getDefaultToolkit()

            if (Desktop.isDesktopSupported()) {
                MiraiLogger.TopLevel.info(
                    """
                                    Mirai 正在使用桌面环境. 如遇到验证码将会弹出对话框. 可添加 JVM 属性 `mirai.no-desktop` 以关闭.
                                """.trimIndent()
                )
                MiraiLogger.TopLevel.info(
                    """
                                    Mirai is using desktop. Captcha will be thrown by window popup. You can add `mirai.no-desktop` to JVM properties (-Dmirai.no-desktop) to disable it.
                                """.trimIndent()
                )
                return@run PlatformKind.SWING
            } else {
                return@run PlatformKind.CLI
            }
        }.getOrElse {
            return@run PlatformKind.CLI
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

internal val windowImage: BufferedImage? by lazy {
    WindowHelperJvm::class.java.getResourceAsStream("project-mirai.png")?.use {
        ImageIO.read(it)
    }
}

internal val windowIcon: Icon? by lazy {
    windowImage?.let(::ImageIcon)
}

internal suspend fun openWindow(title: String = "", initializer: WindowInitializer.(JFrame) -> Unit = {}): String {
    return openWindow(title, WindowInitializer(initializer))
}

internal suspend fun openWindow(title: String = "", initializer: WindowInitializer = WindowInitializer {}): String {
    val frame = JFrame()
    frame.iconImage = windowImage
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

/**
 * @param url 打开的链接
 * @param text 显示的提示内容
 * @param fallbackTitle 无法打开链接时的提醒窗口标题
 */
internal class HyperLinkLabel constructor(
    url: String,
    text: String,
    fallbackTitle: String
) : JLabel() {
    init {
        super.setText("<html><a href='$url'>$text</a></html>")
        addMouseListener(object : MouseAdapter() {

            override fun mouseClicked(e: MouseEvent) {
                // Try to open browser safely. #694
                try {
                    Desktop.getDesktop().browse(URI(url))
                } catch (ex: Exception) {
                    JOptionPane.showInputDialog(
                        this@HyperLinkLabel,
                        "Mirai 无法直接打开浏览器, 请手动复制以下 URL 打开",
                        fallbackTitle,
                        JOptionPane.WARNING_MESSAGE,
                        windowIcon,
                        null,
                        url
                    )
                }
            }
        })
    }
}

/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AFFERO GENERAL PUBLIC LICENSE version 3 with Mamoe Exceptions license that can be found via the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

import net.mamoe.mirai.Bot
import java.awt.Desktop
import java.net.URI
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.JLabel
import javax.swing.JTextField

/**
 * @author Karlatemp <karlatemp@vip.qq.com> <https://github.com/Karlatemp>
 */
@MiraiExperimentalAPI
object SwingSolver : LoginSolver() {
    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        return openWindow("Mirai PicCaptcha(${bot.id})") {
            val image = ImageIO.read(data.inputStream())
            JLabel(ImageIcon(image)).append()
        }
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        return openWindow("Mirai SliderCaptcha(${bot.id})") {
            JLabel("需要滑动验证码, 完成后请关闭该窗口").append()
            Desktop.getDesktop().browse(URI(url))
        }
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return openWindow("Mirai UnsafeDeviceLoginVerify(${bot.id})") {
            HyperLinkLabel(url, "设备锁验证").last()
            JTextField("点击下方链接进行设备锁验证, 验证通过后关闭本窗口").append()
        }
    }
}
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
public object SwingSolver : LoginSolver() {
    public  override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        return openWindow("Mirai PicCaptcha(${bot.id})") {
            val image = ImageIO.read(data.inputStream())
            JLabel(ImageIcon(image)).append()
        }
    }

    public  override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        return openWindow("Mirai SliderCaptcha(${bot.id})") {
            JLabel("需要滑动验证码, 完成后请关闭该窗口").append()
            Desktop.getDesktop().browse(URI(url))
        }
    }

    public   override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return openWindow("Mirai UnsafeDeviceLoginVerify(${bot.id})") {
            JLabel(
                """
                <html>
                需要进行账户安全认证<br>
                该账户有[设备锁]/[不常用登录地点]/[不常用设备登录]的问题<br>
                完成以下账号认证即可成功登录|理论本认证在mirai每个账户中最多出现1次<br>
                请将该链接在QQ浏览器中打开并完成认证<br>
                成功后请关闭该窗口<br>
                这步操作将在后续的版本中优化
                """.trimIndent()
            ).last()
            JTextField(url).append()
        }
    }
}
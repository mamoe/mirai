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
import java.io.File

actual typealias Throws = kotlin.jvm.Throws

/**
 * 验证码, 设备锁解决器
 */
actual abstract class LoginSolver {
    actual abstract suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String?
    actual abstract suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String?
    actual abstract suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String?

    actual companion object {
        actual val Default: LoginSolver
            get() = object : LoginSolver() {
                override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
                    error("should be implemented manually by you")
                }

                override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
                    error("should be implemented manually by you")
                }

                override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
                    error("should be implemented manually by you")
                }
            }
    }

}

internal actual fun getFileBasedDeviceInfoSupplier(filename: String): ((Context) -> DeviceInfo)? {
    return {
        File(filename).loadAsDeviceInfo(it)
    }
}
/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.spi

import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.DeviceInfoManager
import net.mamoe.mirai.utils.MiraiExperimentalApi

/**
 * 生成设备信息
 *
 * @author cssxsh
 */
@MiraiExperimentalApi
public interface DeviceInfoService : BaseService {
    
    public fun load(bot: Bot): DeviceInfo

    public fun generate(): DeviceInfo

    public object Default : DeviceInfoService {
        override fun load(bot: Bot): DeviceInfo {
            val file = bot.configuration.workingDir.resolve("device.json")
            if (!file.exists() || file.length() == 0L) {
                return generate().also {
                    file.writeText(DeviceInfoManager.serialize(it))
                }
            }
            return DeviceInfoManager.deserialize(file.readText())
        }

        override fun generate(): DeviceInfo {
            return DeviceInfo.random()
        }
    }

    public companion object INSTANCE : DeviceInfoService {
        private val loader = SPIServiceLoader(Default, DeviceInfoService::class.java)
        
        override fun load(bot: Bot): DeviceInfo {
            return loader.service.load(bot)
        }

        override fun generate(): DeviceInfo {
            return loader.service.generate()
        }

        @JvmStatic
        public fun setService(service: DeviceInfoService) {
            loader.service = service
        }
    }
}
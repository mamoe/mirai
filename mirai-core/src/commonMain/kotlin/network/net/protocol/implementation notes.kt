/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

@file:Suppress("ClassName")

package net.mamoe.mirai.internal.network.net.protocol

/*
 * 垃圾分类
 */


private object `skey refresh` {

//    suspend fun refreshKeysNow() {
//        WtLogin15(bot.client).sendAndExpect()
//    }

    /*
    val bot = (bot as QQAndroidBot)
            if (bot.firstLoginSucceed && bot.client.wLoginSigInfoInitialized) {
                launch {
                    while (isActive) {
                        bot.client.wLoginSigInfo.vKey.run {
                            //由过期时间最短的且不会被skey更换更新的vkey计算重新登录的时间
                            val delay = (expireTime - creationTime).seconds - 5.minutes
                            logger.info { "Scheduled refresh login session in ${delay.toHumanReadableString()}." }
                            delay(delay)
                        }
                        runCatching {
                            doFastLogin()
                            registerClientOnline()
                        }.onFailure {
                            logger.warning("Failed to refresh login session.", it)
                        }
                    }
                }
                launch {
                    while (isActive) {
                        bot.client.wLoginSigInfo.sKey.run {
                            val delay = (expireTime - creationTime).seconds - 5.minutes
                            logger.info { "Scheduled key refresh in ${delay.toHumanReadableString()}." }
                            delay(delay)
                        }
                        runCatching {
                            refreshKeysNow()
                        }.onFailure {
                            logger.error("Failed to refresh key.", it)
                        }
                    }
                }
            }
     */
}
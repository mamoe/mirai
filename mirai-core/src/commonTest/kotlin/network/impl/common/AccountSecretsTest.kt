/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.network.impl.common

import net.mamoe.mirai.internal.network.components.AccountSecretsImpl
import net.mamoe.mirai.internal.network.components.AccountSecretsManager
import net.mamoe.mirai.internal.network.components.FileCacheAccountSecretsManager
import net.mamoe.mirai.internal.network.framework.AbstractCommonNHTest
import net.mamoe.mirai.internal.network.handler.NetworkHandler
import net.mamoe.mirai.internal.test.runBlockingUnit
import net.mamoe.mirai.internal.utils.accountSecretsFile
import net.mamoe.mirai.utils.DeviceInfo
import net.mamoe.mirai.utils.SecretsProtection
import net.mamoe.mirai.utils.getRandomByteArray
import net.mamoe.mirai.utils.writeBytes
import kotlin.test.Test
import kotlin.test.assertEquals

internal class AccountSecretsTest : AbstractCommonNHTest() {
    init {
        overrideComponents.remove(AccountSecretsManager)
        bot.account.accountSecretsKeyBuffer = SecretsProtection.EscapedByteBuffer(ByteArray(16))
    }

    @Test
    fun `can login with no secrets`() = runBlockingUnit {
        val file = bot.configuration.accountSecretsFile()
        file.delete()
        bot.login()
        bot.network.assertState(NetworkHandler.State.OK)
    }

    @Test
    fun `can login with good secrets`() = runBlockingUnit {
        val file = bot.configuration.accountSecretsFile()
        val s = AccountSecretsImpl(DeviceInfo.random())
        FileCacheAccountSecretsManager.saveSecretsToFile(file, bot.account, s)
        bot.login()
        bot.network.assertState(NetworkHandler.State.OK)
        assertEquals(s, bot.components[AccountSecretsManager].getSecrets(bot.account))
    }

    @Test
    fun `can login with bad secrets`() = runBlockingUnit {
        val file = bot.configuration.accountSecretsFile()
        file.writeBytes(getRandomByteArray(16))
        bot.login()
        bot.network.assertState(NetworkHandler.State.OK)
    }
}
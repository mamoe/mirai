/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.mock

import net.mamoe.mirai.BotFactory
import net.mamoe.mirai.Mirai
import net.mamoe.mirai.mock.database.MessageDatabase
import net.mamoe.mirai.mock.internal.MockBotFactoryImpl
import net.mamoe.mirai.mock.internal.MockMiraiImpl
import net.mamoe.mirai.mock.resserver.TmpResourceServer
import net.mamoe.mirai.mock.userprofile.UserProfileService
import net.mamoe.mirai.mock.utils.AvatarGenerator
import net.mamoe.mirai.mock.utils.NameGenerator
import net.mamoe.mirai.utils.BotConfiguration

public interface MockBotFactory : BotFactory {

    public interface BotBuilder {
        public fun id(value: Long): BotBuilder

        public fun nick(value: String): BotBuilder

        public fun configuration(value: BotConfiguration): BotBuilder

        public fun nameGenerator(value: NameGenerator): BotBuilder

        public fun tmpResourceServer(server: TmpResourceServer): BotBuilder

        public fun msgDatabase(db: MessageDatabase): BotBuilder

        public fun userProfileService(service: UserProfileService): BotBuilder

        public fun avatarGenerator(avatarGenerator: AvatarGenerator): BotBuilder

        public fun create(): MockBot

        public fun createNoInstanceRegister(): MockBot
    }

    public fun newMockBotBuilder(): BotBuilder

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    public companion object : MockBotFactory by MockBotFactoryImpl() {
        init {
            Mirai
            net.mamoe.mirai._MiraiInstance.set(MockMiraiImpl())
        }

        @JvmStatic
        public fun initialize() {
            // noop
        }
    }
}

public inline fun MockBotFactory.BotBuilder.configuration(
    block: BotConfiguration.() -> Unit
): MockBotFactory.BotBuilder = configuration(BotConfiguration(block))

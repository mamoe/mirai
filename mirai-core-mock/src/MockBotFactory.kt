/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
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
import net.mamoe.mirai.mock.fsserver.TmpFsServer
import net.mamoe.mirai.mock.internal.MockBotFactoryImpl
import net.mamoe.mirai.mock.internal.MockMiraiImpl
import net.mamoe.mirai.mock.userprofile.UserProfileService
import net.mamoe.mirai.mock.utils.NameGenerator
import net.mamoe.mirai.utils.BotConfiguration

public interface MockBotFactory : BotFactory {

    @MockBotDSL
    public interface BotBuilder {
        @MockBotDSL
        public fun id(value: Long): BotBuilder

        @MockBotDSL
        public fun nick(value: String): BotBuilder

        @MockBotDSL
        public fun configuration(value: BotConfiguration): BotBuilder

        @MockBotDSL
        public fun nameGenerator(value: NameGenerator): BotBuilder

        @MockBotDSL
        public fun tmpFsServer(server: TmpFsServer): BotBuilder

        @MockBotDSL
        public fun msgDatabase(db: MessageDatabase): BotBuilder

        @MockBotDSL
        public fun userProfileService(service: UserProfileService): BotBuilder

        @MockBotDSL
        public fun create(): MockBot

        @MockBotDSL
        public fun createNoInstanceRegister(): MockBot
    }

    @MockBotDSL
    public fun newMockBotBuilder(): BotBuilder

    @Suppress("INVISIBLE_MEMBER", "INVISIBLE_REFERENCE")
    public companion object : MockBotFactory by MockBotFactoryImpl() {
        init {
            Mirai
            net.mamoe.mirai._MiraiInstance.set(MockMiraiImpl())
        }
    }
}

@MockBotDSL
public inline fun MockBotFactory.BotBuilder.configuration(
    block: BotConfiguration.() -> Unit
): MockBotFactory.BotBuilder = configuration(BotConfiguration(block))

/*
 * Copyright 2019-2020 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

import kotlin.coroutines.CoroutineContext;
import net.mamoe.mirai.Bot;
import net.mamoe.mirai.BotFactory;
import net.mamoe.mirai.event.EventHandler;
import net.mamoe.mirai.event.Events;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.SimpleListenerHost;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import net.mamoe.mirai.utils.BotConfiguration;
import org.jetbrains.annotations.NotNull;

/**
 * 仅用来测试调用，不会被单元测试运行
 */
public class JavaApiTests {
    public static void main(String[] args) {
        Bot bot = BotFactory.INSTANCE.newBot(11, "", configuration -> {
            configuration.fileBasedDeviceInfo();
            configuration.setProtocol(BotConfiguration.MiraiProtocol.ANDROID_PHONE);
        });

        bot.login();

        bot.getAsFriend().sendMessage("test"); // blocking bridge
        bot.getOtherClients().getOrFail(1).getBot();

        bot.getEventChannel().subscribe(MessageEvent.class, event -> {

            return ListeningStatus.LISTENING;
        });

        bot.getEventChannel().subscribeAlways(GroupMessageEvent.class, event -> {
            Bot b = event.getBot();

        });

        SimpleListenerHost slh = new SimpleListenerHost() {

            @Override
            public void handleException(@NotNull CoroutineContext context, @NotNull Throwable exception) {
                super.handleException(context, exception);
            }

            @EventHandler
            public void onMsg(GroupMessageEvent e) {

            }
        };

        Events.registerEvents(slh);
    }
}

package demo;

import net.mamoe.mirai.japt.BlockingBot;
import net.mamoe.mirai.japt.BlockingContacts;
import net.mamoe.mirai.japt.BlockingQQ;
import net.mamoe.mirai.japt.Events;
import net.mamoe.mirai.message.GroupMessage;
import net.mamoe.mirai.message.data.At;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageUtils;
import net.mamoe.mirai.utils.BotConfiguration;
import net.mamoe.mirai.utils.SystemDeviceInfoKt;

import java.io.File;

class BlockingTest {

    public static void main(String[] args) throws InterruptedException {
        // 使用自定义的配置
        BlockingBot bot = BlockingBot.newInstance(123456, "", new BotConfiguration() {
            {
                setDeviceInfo(context ->
                        SystemDeviceInfoKt.loadAsDeviceInfo(new File("deviceInfo.json"), context)
                );
                setHeartbeatPeriodMillis(50 * 1000);
            }
        });

        // 使用默认的配置
        // BlockingBot bot = BlockingBot.newInstance(123456, "");

        bot.login();

        bot.getFriendList().forEach(friend -> {
            System.out.println(friend.getNick());
        });

        Events.subscribeAlways(GroupMessage.class, (GroupMessage message) -> {
            final BlockingQQ sender = BlockingContacts.createBlocking(message.getSender());
            sender.sendMessage("Hello World!");
            System.out.println("发送完了");

            sender.sendMessage(MessageUtils.newChain()
                    .plus(new At(message.getSender()))
                    .plus(Image.fromId("{xxxx}.jpg"))
                    .plus("123465")
            );
        });

        Thread.sleep(999999999);
    }
}

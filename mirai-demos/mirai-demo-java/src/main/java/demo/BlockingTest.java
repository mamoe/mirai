package demo;

import net.mamoe.mirai.japt.BlockingBot;
import net.mamoe.mirai.japt.BlockingContacts;
import net.mamoe.mirai.japt.BlockingQQ;
import net.mamoe.mirai.japt.Events;
import net.mamoe.mirai.message.GroupMessage;

class BlockingTest {

    public static void main(String[] args) throws InterruptedException {
        BlockingBot bot = BlockingBot.newInstance(123456, "");

        bot.login();

        bot.getFriendList().forEach(friend -> {
            System.out.println(friend.getNick());
        });

        Events.subscribeAlways(GroupMessage.class, (GroupMessage message) -> {
            final BlockingQQ sender = BlockingContacts.createBlocking(message.getSender());

            sender.sendMessage("Hello");
        });

        Thread.sleep(999999999);
    }
}

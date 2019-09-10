package net.mamoe.mirai;


import net.mamoe.mirai.event.MiraiEventHook;
import net.mamoe.mirai.event.MiraiEventManager;
import net.mamoe.mirai.event.events.qq.FriendMessageEvent;

/**
 * @author Him188moe
 */
public final class MiraiMain {
    private static MiraiServer server;

    public static void main(String[] args) {
        server = new MiraiServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown()));
    }
}

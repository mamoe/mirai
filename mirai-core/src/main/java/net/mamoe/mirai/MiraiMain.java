package net.mamoe.mirai;


import net.mamoe.mirai.event.MiraiEventHook;
import net.mamoe.mirai.event.events.server.ServerDisableEvent;

/**
 * @author Him188moe @ Mirai Project
 */
public final class MiraiMain {
    private static MiraiServer server;
    public static void main(String[] args) {
        server = new MiraiServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));
        server.getEventManager().register(
                new MiraiEventHook<>(ServerDisableEvent.class)
                .setHandler(a -> System.out.println("close"))
        );
    }
}

package net.mamoe.mirai;


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

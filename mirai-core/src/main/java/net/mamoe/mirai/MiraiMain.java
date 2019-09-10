package net.mamoe.mirai;


/**
 * @author NaturalHG
 */
public final class MiraiMain {
    private static MiraiServer server;

    public static void main(String[] args) {
        server = new MiraiServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown()));
    }
}
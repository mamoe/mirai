package net.mamoe.mirai;


/**
 * @author NaturalHG
 */
public final class MiraiMain {
    private static MiraiServer server;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown()));
    }
}
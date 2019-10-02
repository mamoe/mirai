package net.mamoe.mirai;


import lombok.Getter;

/**
 * @author NaturalHG
 */
public final class MiraiMain {
    @Getter
    private static MiraiServer server;

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown()));
    }
}
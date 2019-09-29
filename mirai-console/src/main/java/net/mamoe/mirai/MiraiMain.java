package net.mamoe.mirai;


import lombok.Getter;

import java.io.PrintStream;

/**
 * @author NaturalHG
 */
public final class MiraiMain {
    @Getter
    private static MiraiServer server;

    public static void main(String[] args) {
        server = new MiraiServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.shutdown()));
    }
}
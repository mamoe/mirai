package net.mamoe.mirai;

import java.util.ArrayList;
import java.util.List;

/**
 * MiraiAPI provides
 * - the status of the Mirai-Core
 * - the fundamental bot operations.
 * - the plugin status.
 * <p>
 * It was designed for users, not developers,
 * Web-based controller, UI controller or console is depending on Mirai-API
 * <p>
 * Mirai-API does NOT contains fancy objects, and this means there are less functions it can do compare with Mirai-Core
 * <p>
 * Again, for extending/developing Mirai, you should refer to Mirai-Core
 * for only using                       , you should refer to Mirai-API
 */
public class MiraiAPI {

    public static void startMirai(String[] args) {
        MiraiMain.main(args);
    }

    public static void closeMirai() {
        MiraiServer.INSTANCE.shutdown();
    }

    public static void restartMirai(String[] args) {
        MiraiServer.INSTANCE.shutdown();
        MiraiMain.main(args);
    }

    public static String getMiraiVersion() {
        return MiraiServer.MIRAI_VERSION;
    }

    public static String getQQPortocolVersion() {
        return MiraiServer.QQ_VERSION;
    }

    public static boolean isMiraiEnabled() {
        // TODO: 2019/10/2
        return false;
    }

    public static List<String> getEnabledPluginList() {
        return new ArrayList<>();
    }

    public static List<Long> getEnabledBots() {
        return new ArrayList<>();
    }

    public static Bot getBot(long qq) {
        return new Bot(qq);
    }

    public static void addBot(long qq, String password) {

    }

}

package net.mamoe.mirai;


import net.mamoe.mirai.utils.config.MiraiConfig;
import net.mamoe.mirai.utils.config.MiraiConfigSection;

/**
 * @author Him188moe
 */
public final class MiraiMain {
    private static MiraiServer server;
    public static void main(String[] args) {
        server = new MiraiServer();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            server.shutdown();
        }));
        MiraiConfig config = new MiraiConfig("QQ.yml");
        MiraiConfigSection<Object> data = config.getSection("123123");
        data.put("account","123123a");
        try {
            Robot robot = new Robot(data);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


    }
}

package net.mamoe.mirai.network.packet;

/**
 * @author Him188moe @ Mirai Project
 */
public final class PacketUtil {

    /**
     * 易语言返回的 string(valueof
     */
    public static int getGTK(String sKey) {
        int init = 5381;
        int i = 0;
        int length = sKey.length();
        while (i++ < length) {
            init += init << 5 + sKey.charAt(i);
        }
        return init & 2147483647;
    }
}

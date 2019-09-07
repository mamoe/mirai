package net.mamoe.mirai.utils;

/**
 * @author Him188moe
 */
public enum ClientLoginStatus {
    /**
     * 我在线上
     */
    ONLINE(0x0A);

    // TODO: 2019/8/31 add more ClientLoginStatus

    public final int id;//1 ubyte

    ClientLoginStatus(int id) {
        this.id = id;
    }
}

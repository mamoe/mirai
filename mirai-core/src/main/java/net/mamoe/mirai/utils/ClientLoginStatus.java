package net.mamoe.mirai.utils;

/**
 * @author Him188moe
 */
public enum ClientLoginStatus {
    ONLINE(0x0A);

    // TODO: 2019/8/31 add more
    public final int id;//1byte

    ClientLoginStatus(int id) {
        this.id = id;
    }
}

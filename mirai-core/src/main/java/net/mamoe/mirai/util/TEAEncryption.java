package net.mamoe.mirai.util;

/**
 * @author Him188moe @ Mirai Project
 */
public final class TEAEncryption {
    public static byte[] encrypt(byte[] source, byte[] key) {
        return new _TEAEncryption().encrypt(source, key);
    }

    public byte[] decrypt(byte[] source, byte[] key) {
        return new _TEAEncryption().decrypt(source, key);
    }

    public byte[] decrypt(byte[] source, int offset, int length, byte[] key) {
        return new _TEAEncryption().decrypt(source, offset, length, key);
    }

}


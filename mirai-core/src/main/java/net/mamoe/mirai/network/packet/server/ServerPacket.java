package net.mamoe.mirai.network.packet.server;

import net.mamoe.mirai.network.packet.Packet;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

/**
 * @author Him188moe @ Mirai Project
 */
public abstract class ServerPacket extends DataInputStream implements Packet {
    public ServerPacket(@NotNull InputStream in) {
        super(in);
    }

    public ServerPacket(@NotNull byte[] in) {
        this(new ByteArrayInputStream(in));
    }

    public abstract void decode();
}

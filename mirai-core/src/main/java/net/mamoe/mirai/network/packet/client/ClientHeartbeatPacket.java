package net.mamoe.mirai.network.packet.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.mamoe.mirai.network.Protocol;
import net.mamoe.mirai.network.packet.PacketId;
import net.mamoe.mirai.util.TEAEncryption;

import java.io.IOException;

/**
 * @author Him188moe @ Mirai Project
 */
@EqualsAndHashCode(callSuper = true)
@Data
@PacketId(0x00_58)
public class ClientHeartbeatPacket extends ClientPacket {
    public long qq;
    public byte[] sessionKey;//登录后获得

    @Override
    public void encode() throws IOException {
        this.writeRandom(2);
        this.writeQQ(qq);
        this.writeHex(Protocol.fixVer);
        this.write(TEAEncryption.encrypt(new byte[]{0x00, 0x01, 0x00, 0x01}, sessionKey));
    }
}

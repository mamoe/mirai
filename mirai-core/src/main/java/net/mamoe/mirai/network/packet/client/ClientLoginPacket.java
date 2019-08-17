package net.mamoe.mirai.network.packet.client;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.mamoe.mirai.network.Protocol;
import net.mamoe.mirai.network.packet.PacketId;
import net.mamoe.mirai.util.TEAEncryption;
import net.mamoe.mirai.util.Utils;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author Him188moe @ Mirai Project
 */
@EqualsAndHashCode(callSuper = true)
@Data
@PacketId(0x08_25_31_01)
public class ClientLoginPacket extends ClientPacket {
    public long qq;

    @Override
    public void encode() throws IOException {
        this.writeQQ(qq);
        this.writeHex(Protocol.fixVer);
        this.writeHex(Protocol._0825key);


        //TEA 加密
        this.write(TEAEncryption.encrypt(new ClientPacket() {
            @Override
            public void encode() throws IOException {
                this.writeHex(Protocol._0825data0);
                this.writeHex(Protocol._0825data2);
                this.writeQQ(qq);
                this.writeHex("00 00 00 00 03 09 00 08 00 01");
                //this.writeIp(Protocol.SERVER_IP.get(2));
                this.writeIp("123456789");
                this.writeHex("00 02 00 36 00 12 00 02 00 01 00 00 00 00 00 00 00 00 00 00 00 00 00 00 01 14 00 1D 01 02 00 19");
                this.writeHex(Protocol.publicKey);
            }
        }.encodeToByteArray(), Protocol.hexToBytes(Protocol._0825key)));
    }


    public static void main(String[] args) throws IOException {
        try {
            var pk = new ClientLoginPacket();
            pk.qq = 1994701021;
            pk.encode();
            pk.writeHex(Protocol.tail);
            System.out.println("pk.toByteArray() = " + Arrays.toString(pk.toByteArray()));
            System.out.println(Utils.INSTANCE.toHexString(pk.toByteArray()));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}

import net.mamoe.mirai.network.packet.server.Server0825Packet;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;

/**
 * @author Him188moe @ Mirai Project
 */
public class NetworkTest {
    public static void main(String[] args) {

        /*
        System.out.println(Short.valueOf("37 13", 16));

        System.out.println(1040400290L & 0x0FFFFFFFF);
        System.out.println(Long.valueOf("3E033FA2", 16));
        */


    }


    public static String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for(int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if(hex.length() < 2){
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }


}

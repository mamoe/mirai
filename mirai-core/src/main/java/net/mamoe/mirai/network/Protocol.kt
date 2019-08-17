package net.mamoe.mirai.network;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Him188moe @ Mirai Project
 */
public interface Protocol {
    List<String> SERVER_IP = new ArrayList<>() {{
        add("183.60.56.29");

        List.of(
                "sz2.tencent.com",
                "sz3.tencent.com",
                "sz4.tencent.com",
                "sz5.tencent.com",
                "sz6.tencent.com",
                "sz8.tencent.com",
                "sz9.tencent.com"
        ).forEach(s -> {
            try {
                SERVER_IP.add(InetAddress.getByName(s).getHostAddress());
            } catch (UnknownHostException ignored) {
            }
        });
    }};


    String head = "02";
    String ver = "37 13 ";
    String fixVer = "03 00 00 00 01 2E 01 00 00 68 52 00 00 00 00 ";
    String tail = " 03";
    String _fixVer = "02 00 00 00 01 01 01 00 00 68 20 ";
    String _0825data0 = "00 18 00 16 00 01 ";
    String _0825data2 = "00 00 04 53 00 00 00 01 00 00 15 85 ";
    String _0825key = "A4 F1 91 88 C9 82 14 99 0C 9E 56 55 91 23 C8 3D";
    String redirectionKey = "A8 F2 14 5F 58 12 60 AF 07 63 97 D6 76 B2 1A 3B";
    String publicKey = "02 6D 28 41 D2 A5 6F D2 FC 3E 2A 1F 03 75 DE 6E 28 8F A8 19 3E 5F 16 49 D3";
    String shareKey = "1A E9 7F 7D C9 73 75 98 AC 02 E0 80 5F A9 C6 AF";
    String _0836fix = "06 A9 12 97 B7 F8 76 25 AF AF D3 EA B4 C8 BC E7 ";

    String _00BaKey = "C1 9C B8 C8 7B 8C 81 BA 9E 9E 7A 89 E1 7A EC 94";
    String _00BaFixKey = "69 20 D1 14 74 F5 B3 93 E4 D5 02 B3 71 1A CD 2A";

    String encryptKey = "“BA 42 FF 01 CF B4 FF D2 12 F0 6E A7 1B 7C B3 08”";


    static byte[] hexToBytes(String hex) {
        var list = Arrays.stream(hex.split(" ")).map(String::trim).map(s -> Byte.valueOf(s, 16)).collect(Collectors.toList());
        var buff = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            buff[i] = list.get(i);
        }
        return buff;
    }
}

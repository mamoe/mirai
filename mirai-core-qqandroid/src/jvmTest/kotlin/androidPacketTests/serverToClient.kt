@file:Suppress("EXPERIMENTAL_API_USAGE")

package androidPacketTests

import kotlinx.io.core.*
import kotlinx.io.pool.useInstance
import net.mamoe.mirai.qqandroid.network.protocol.packet.DECRYPTER_16_ZERO
import net.mamoe.mirai.qqandroid.network.protocol.packet.KnownPacketFactories
import net.mamoe.mirai.qqandroid.network.protocol.packet.PacketLogger
import net.mamoe.mirai.qqandroid.network.protocol.packet.withUse
import net.mamoe.mirai.utils.cryptor.ECDH
import net.mamoe.mirai.utils.cryptor.adjustToPublicKey
import net.mamoe.mirai.utils.cryptor.decryptBy
import net.mamoe.mirai.utils.io.*
import net.mamoe.mirai.utils.unzip

/*
 */

fun main() {
    val data = """
   06141ef4481120da22db75080800450805aad52640003506283d71600dd0c0a8030a1f90fe5008cee55e8b8c5585501000f989000000000009080000000b01000000000e313939343730313032312dde8c38f7075e6bb46a3d04dfd5d720d12afb2eb067ba4f4595edbefd367615ce43b590f822f561d979fa1d05d94a02da20da629c562f7bd431e8969d5c36ea635429b9e5e0bff6ce8dd15ec796ce2edcd73637ff1f7eb4e1b1e7668abe6f29a9e94a0bba03220c95e89b927307c037a7c9bc2b5255647189b14c50ceb05c5c68d05ac732e23253b365d98295fe6c472db908e1ff173b4d43cda220f8bab1271fbb923b3df0585d582617d27369a1cfa0166b3fb680a0f608fb117f3355bdcdbef7d65d6de5f69b59be79b3836fc6cff3c0892cbb87a4cb1569955a866924509c933d79d423caea20b933b0738df02de26a9d057924191b34a2ec4c153c047886a0160ad536874bf67316c9776348b5a6f0bc4f542d3cab81dfa1198c19e4eeadc8484c4fbe47a3a290a57d02762937ff289a7c3859219d34d19cf00595674e063c1bf6d3f02199a1bbc2dd816c3760bd359b10160050a8bcadae6adcba3901cabb41f0c12d0f740a49e4508468cc60ab016527488ee921ec029d45db3b552bd73cb78942385ea7c1f3eb9b329ac3ad899547e7919a76c43953123e01221e656a2de4a307f068978e87b19db48ca05ac82860043869c8f1675e514dbb828ec98ee0591684a74143807d6a422927af4f448251b7d592cb7a4c8b34a8f13d9f9cf54e37e4c344f9aa54ac44be66b5450f237731fd24c4f42633078cd10907928987a25bdcb4cbe44ae507c700579680e8fb64ccd61dad8e6a9ceeac91ee9419f466bad22d6ac288c1219b00d4fb9009e170d7b5fcac2a672c11aced6a5eb6dd0b3dde1f29446ab6ddef6763661ed69bc8f0ab3520cfc332cdbe53355395bba697f1b6262507f62b1090e0733e351a76647e779d746f46685d36de7ba3d6a37485bba566060c55929f26e4773675ba4680d940e74c94b8cf64054ab298f100ebc7c19950443534a1fa342b53a315275b0004fc07ac95896620adc2f8befbdde38ef75c912cecd5e3a128d2a3f63773fdd0a4507debaddd527b31a19f63ff383b8ce6a00a0f5e2d7fbedcbd3eb9f2746f561b69e8cf0e9a9939e902a6158eea32e2add8f2975889174cbeb382596a3113e2741d47490382245ad4c405a9079779d989116df6fff14b89d1725907f809c4c7a9ff145852330bcde5f3ba26af3953a23b23dfdcaaab5905eaf5c063134ab26e0573dac18bbff2937479c2d658cff8ded19359fe1414c426dcd99bca93d93d1adfe768110837a7d8deb99359b5049babbfc37534609fff1674c549e1fc3f778579c91a8a10fdd2c75dfef12898353631643ce635ce91bd439ef5a0604afd722a3bd79d7bb65c6b17cc71473c9a65ba1562571aa9cd90fc6d245f2d66cb53530236000af5114bf827938bb76bece6549931f47e87d98ddbab02dabda7bd253fd28e7f7508fd48d0291a399eb1b0cb4a0d286838cdcf782de178a1657ad9373e1ef6d776bf6991bfeab8c1b99f17b7457e397f2afed8c4db005e45564b0b6bb8ba4231e014eaa94e113b981e7e7f854e8bdd849653634b51d10d703fed840a73831e228e737fd707a6649af0c3acb1eca585ff2390d8b714f75f2b5a2fed4b946166e26145a845a417a4ddbb93fa87526a18336c63437dc9561e9ff0b9e82440893553e5cb127b73405828adbc4bcc16e2628bece509adf4b5b4c213e30e47ce2b833cf1ae63b66f98e8b0ab82f4699f81101e8fc45c02c392f73d56a05e393c4ef26cc9cb18bda6f06b3c3c5e17256cdd413b1c7e53a36be6e5f8327ef2d74479b0f758f6867099a7954a25170f0598b88d0178e4264597d539f49db904f5af0c5fbaa0d1d3f6e267d271fd88d651d63a5d076b6c9cb83baeeeb9ee07119d5cc0d3301b8580c6f13f01193fe744accaf23d4998a328ba0153e091ff47e72115790ac2ee8bc2760389f6afa1aad5c6fde22b7e030e54260a66c82b
       06141ef4481120da22db750808004508040ed5274000350629d871600dd0c0a8030a1f90fe5008ceeae08b8c5855501800f9a1740000352b369d0f6a6ccc3241ebaffcf56f1448539de4424794e92d376150a7d779cbe3bddb9ddfcaa8a5516508f0e151f14905709d11cf5731f02d7a17594878a148d3513e3a40cf2b2083d94e4473933919183c689da0d9870cac1045e1177de021dc8cf35876b628efacc94f76c8e5ac1c52bcd063c40c1c7a507a7bfb6e7cbce4573c10de847f154540c455bcd3fa044dde17f833a53b457011148b769418de4fa0cb03f0f0003548307c15c798f83eff4dbd9ed1ac79fc2ab1e4a699a6ebfd7a2ab8aa41f212480dad50f92a41eef9bab19d8f5c535973e283fe09fefbb96d9c714528c5d05fc83d158ffecd438c30a95f38d58d3df3c554d5b3c0fb2fd75ae0fa38de87092bca3a79e7d934b673d7fcc3d2fb8b35874c199dbd82f51fd2903a3dd3195f0d5eac55c260cd63d4dae3ad90c13979401face609ca6608828a49e91cfc4e0861996bbc608ee8f94d7b32253ee99a627d0d14a0660cac7a4a0f814cb43cff81983b4ff2f9bb19933371be4e837bfea8af9f50810994b3c543eee966b178d423a73837533666efc50ff9c3b0ddca442e85505117ad1ee3d618048e77ff5f151e1305d04c0ed4ab12f38e587003d8648fb1c48328f0d97062b9e1d1e298991572bdc488532211f249b87f554d243ece0359d79c501df79feff20b6e64dee4f72d0eeb4258959dbaa0cdcd5b78544935f2bf8fdc8610623f0380cc3443597cfd2d969eb4b9c8c91583c1a09ad40784309daedc300f1231a51658e050d8146c116974dc8e1b80ccdd9de0e809cc5cf099ff31f3ba0aef36291e885750b482b999b1e8a12cc9831641b6c7afcee924cd9d95ecd4571145963f1f1dd65da39ed33274e57d8103139e8ea523c2c51ca3df4ba25f0d762fa708df0308adc600039f07d5f7968b48b921f6e2a5e597722343456d5e836a3478f907dbd26c9ffff11491be61937ce4e951d3e27321c5ced54c0cbe807eb6b555d63303b1e5835d867bed9237e50eca36b953a52c3ace2106cf0a76e616e64e43f3caf1b3e4c36a6f07ef36a8e4d0c459abb835a8937b011cd7fe22c8cb3a1915d3b35ec071d3ea30149024ddcb5cc6688fb1aaefdfa691fcc1e1abb789c93d9a50a3ae5a3c42f962bb1ea18d0ee41f947025dfe11b553710437c553acc05cd71eb8bcf7312ae7ba93e0999a7d250b3534f8a12ffb0863f813e50099b5ab99488bec6c9a7c3ddbabba689d8d5d074ac7a8a51be0dbd24e83946212900bda167da74502df6a4ca19ea0e96dccac3000000600000000b01000000000e3139393437303130323124091c1e3f66dd4380dfb31d6672e8247ca8334d3bafad5222abe08994b3752544f7fff26d145f9096623e882a351abcd8d62ccb381df30e6087034a250896c23148902ab7673325
              06141ef4481120da22db7508080045080326d525400035062ac271600dd0c0a8030a1f90fe5008cee2608b8c5585501800f9cc090000869d266455aceba6d61bb5545b381d7eb65ef8f78b409dce42d67276a3e6d8e26cc58a5b713dad2e5c2690dbab11f9e3750beb6e6213e6c4b852c664a180f0c81421fdc2e5ee1e79aa094eb225e1d54f10706dd69088247dd5a813991d0a776f28023a45177a21a6dabc9b9180c2e9efcccafb1b6801d4b7a548efc5a523be32fa19ab786fc9eb0ab7c5c4bf9d92d77b95125fe19c4cd36ff5c4f55c4dc375a09b2d5c20ebe1025e8d410ca48e27926f18e71e5c12653d7f57e2ab861a7e07881056b1553b54f69602f18e49ccd2f37e80884b16eaa351eb988ce3d683255b29c19361f2b1c3df9384fc51ca53e62ca849ea9d7f84fe042238094200a80f30587ca1f8e02648d1c227a1609f72dac4fa87be933c89c7b8370882fd78c9395501ec38152a84209062fa5f05da560ffc27c8323b785175f6eb9c097a79aa989c7c635ea8d3b70447fe8dc0fd083fd2e3c587857e7fbe39c9153bbb482b8cc61718e4552b1ba2fe4857e4efc9c8bdbd556a48af3ce635f898f32d2f81063b4614c763126ded43cfec3c9a6ee69ffb22ad07ecdc0da74d2a5525969af87a39ea98ccb7fc26e281ed012ba411b215e6be4ee9510a1b66a0176d2569490c8236b5c8749c54fff7873a535efb9f6b0d087c5b84c7f8f78936f2a068678e3666187ce351965995cb62bd9991b9953f3fedefd37c773733cd19f7da499cf81ce27ada2cfdc9834a571e1bb2966d78ade710a4edd4494770f10304f3456f80302c5526187c8fe0a1ede70ea2d456adc593726445181b27ebf450d1414c502aadde9ef2707ea128e8601e4948fbee57f6c2ebfbeec38cdb48364db069ca62119002ba19570cd6b744669f30565b4ebc3ed470394f6dc85da8a085351b10234dc82bd33543780a730904566620ebcebf56c8ab84545847737d55babb66cccee94054e6fc1ffd0bbf0c801c7e29b477a2e2baae339b37b815a8ce8811c7fb87b8fd3be55a1174f6f55f35b5d42c4d96cfb40053fa55efa31a9ae334248567e06d421d227fb9a30765dc4e85b938aabbcedac32901598bc5772ecde797779c7034c22cd81a

       """.trimIndent()
        .trim().split("\n").filterNot { it.isBlank() }.map {
            val bytes = it.trim().autoHexToBytes()
            if (bytes[0].toInt() == 0) {
                bytes
            } else bytes.dropTCPHead()
        }.flatMap { it.toList() }.toByteArray()
    data.read { decodeMultiServerToClientPackets() }
}

fun ByteReadPacket.decodeMultiServerToClientPackets() {
    PacketLogger.enable()
    println("=======================处理服务器到客户端客户端=======================")
    var count = 0
    while (remaining != 0L) {
        processFullPacketWithoutLength(readBytes(readInt() - 4).toReadPacket())
        if (remaining != 0L) {
            println()
            println()
            println()
            println()
            count++
        } else {
            DebugLogger.info("=======================共有 $count 个包=======================")
        }
    }
    println()
}

private fun processFullPacketWithoutLength(packet: ByteReadPacket) {
    packet.debugPrintThis("正在处理").apply {
        require(remaining < Int.MAX_VALUE) { "rawInput is too long" }
        // login
        val flag1 = readInt()

        PacketLogger.verbose("flag1(0A/0B) = ${flag1.toUByte().toUHexString()}")
        // 00 00 05 30
        // 00 00 00 0A // flag 1
        // 01 // packet type. 02: sso, 01: uni
        //
        // 00 00 00 00 0E 31 39 39 34 37 30 31 30 32 31 40 3C 63 DC A2 8F FC E7 09 66 62 11 A3 5A B6 AB DC 6E A1 CA CF E2 0A 6F A8 6D 36 64 4E 22 4B A9 8A ED 07 7A 0A 9E F3 C7 7B 72 EF C1 C7 6E 9A 28 27 10 F8 2A 7F 37 49 B6 48 35 52 E9 CF 2A B5 F3 26 90 33 68 9B 5A 04 2A 8B F5 78 13 82 FE 3C 13 C4 F9 38 39 0E 02 4C 3D 91 0A 2A 94 3F 9F A6 52 B9 14 89 C5 D9 57 0F 96 F8 0E 7D 32 81 8E 10 DB C0 CA BE C7 3F EC D0 B1 F0 9D A2 4B 9F B3 8D E0 EB 1F 42 52 EA 5E 9E 76 E2 F4 13 9D 0E 7E 6D 0A E3 56 C3 EE 8A 80 24 DE FB 08 82 FB B7 AF CE 2A 69 16 E3 C3 79 5C C7 CD 44 BA AA 08 A2 51 0B 43 31 69 A1 12 D1 AE 48 15 AE 76 E9 AB BB D2 E0 16 03 EB 2D 47 A4 61 24 65 5E CC C5 03 B3 96 3E 7A 39 90 3D DB 63 56 2B 23 85 CE 5F 9E 04 20 45 31 79 7B BF 78 33 77 34 C1 8E 83 B3 50 88 2A 01 C0 C4 E4 BF 2D 0D B9 37 32 AB E0 BB 82 36 B1 4E 51 4B F7 07 6A 12 3E 79 EA 93 3D BD 06 4E AE 1C 49 82 17 14 00 09 59 40 A6 A9 01 56 1A 23 86 A8 33 B3 9A 70 7B 3A C1 F9 31 03 FD DB 4B 5C 7B F9 BB 43 94 65 A0 1C DA 2B 85 AA AD 7B 79 42 F2 EB 25 5E F0 DA B7 E7 AD 4B 25 02 36 BB 78 5F 83 7F F7 78 F0 99 D2 B5 A3 0C 4A 7F 0E B0 A6 C4 99 F7 9E 0B C6 4D FC F5 8D 6B 5F 35 27 36 D3 DB D0 46 C7 10 76 7D 96 91 48 EA 1C B2 B7 D7 2F D2 88 A8 4C 87 D6 A9 40 33 4C 76 C5 48 3E 32 4D C1 C3 7F 5C D9 B3 22 00 88 BE 04 82 64 A9 73 AA E1 65 1A EF 49 B4 54 74 53 FF 75 B6 E9 57 1B 89 2D 6F 2A 6A CE 23 BF 41 CB 55 B3 A0 53 87 AD A0 22 EE 6B 3F 4A 97 23 36 BF 7E 08 2D 0A 9E 2E 4B F2 2E 00 59 EC F1 21 34 45 75 DB 6B F2 EC 65 24 30 69 50 CC 45 78 00 AF C8 F6 3D 8E 03 60 CF CA A1 88 14 18 82 6F 56 58 D0 BC E0 48 FD AA 86 63 CA C1 01 63 07 16 4A 79 79 17 9D 1F E2 40 4B B6 77 6E 44 84 DE BE 02 4C 33 7A F5 2F 93 21 3E 17 62 38 81 95 E6 84 8B 7C C8 7B E2 23 FB 12 4F E8 42 5F 1D 48 92 84 B1 45 FF 69 97 3C 30 C9 09 E8 84 E8 07 0E 17 D4 A1 CC 35 D6 FE 7B D2 9A 44 8B 17 BF E7 D6 98 1D 98 D7 30 BE 55 19 A9 F4 D6 0D E8 18 80 35 85 B6 AB B9 20 32 C7 ED C6 AD A7 AE 19 48 B7 17 02 B3 45 C3 A2 B9 C9 B7 58 B5 8B 4C AF 52 AD A1 E1 62 45 AB 58 26 67 20 C7 64 AA DA 7E F3 70 8B C2 92 69 E3 3E 3E 6F 39 6F 2B 35 35 0F 00 FC 52 B5 5C 5B 73 FE F6 F5 10 55 36 7C 9A 84 FC A6 23 29 4A 75 49 7C 13 1C CA 54 A2 A2 FA 2A 63 A5 4C 9A B4 27 E8 5F 9F 23 96 B2 E7 AA E6 8B E0 E2 6A 75 8A B2 F4 E4 7E 09 E8 22 70 2A 42 8B E3 DC AD E8 A8 A2 92 71 6B A2 12 78 E1 DA CC 70 57 67 F5 B4 52 F3 B4 4C 17 AB 05 33 DA 6E 47 52 C5 B2 B7 9A D2 A8 BC 44 64 D3 26 1A 6B C6 C5 36 1C 2B 8F BD B7 27 91 3E C0 C2 FC 03 41 FE 02 D3 4B B1 E5 5F 5B 50 05 29 BD 3A 64 85 E3 8C FB 11 F2 1D 94 DB D7 78 AF AD 77 A3 9C D4 39 5D 8B EA DF 9D 08 CA 92 7C 5F D5 17 49 0E FA A1 21 1C 9F C3 88 1A DC E7 D8 82 80 85 86 32 99 15 E4 89 BA 91 2B 4B FB 87 EC 44 B4 D9 83 CC 79 77 A4 A0 D0 50 E3 4F 00 E7 DA DA 79 38 1E D8 04 86 16 CD 25 BE BA 76 E4 8C F9 86 91 69 6E C7 A0 EF 6B 44 2B C9 C3 DC 8D 2D 65 60 7A F4 37 02 D4 8F 38 D0 D5 20 30 DE A5 F5 A8 75 C7 EE 0B 0F 1B 88 C2 8A CC 6F 70 1D E4 D8 4E DD 04 A5 5B B8 04 B1 29 42 08 92 19 78 E2 26 EB 6B 07 49 DE 8A AF A3 41 72 1D E2 3C 62 0F 7E 7B DE A3 0F 71 8C 5D EC E9 96 96 45 A9 39 33 8A 87 C9 93 CE 3B 6D 75 50 21 1F 4C 03 E9 A7 AD 03 0F 5E A9 EE 60 CC EA 05 4F DF E1 B1 13 A6 7D C7 B9 37 58 53 3B 06 1A AD 98 E5 06 D9 74 2A B1 96 75 DE A6 B7 89 25 53 2A A3 07 B6 70 C6 86 1F 59 EB 53 08 57 6E 86 D7 A1 5C DB 26 D7 86 3E 97 BB FD 6A 0A 4C E1 81 B9 4C C1 A0 49 89 57 29 E0 CD 79 6F 0A 46 C1 C6 62 75 49 C6 9A B9 22 75 EE 10 C7 56 E6 D5 DE 4D EC 89 5A 6F AC 60 0F B3 CC 37 9E F2 BE 49 A7 77 3C 05 AE 92 66 C8 BE 16 E5 35 17 24 18 A5 CE B8 BB AE CD 88 DE 01 53 40 84 E0 06 C6 77 96 09 DF D7 76 3B CA C9 B5 B2 91 95 07 54 6F 51 EB 12 58 16 8A AF C3 E3 B9 4A EC 25 A5 D1 19 59 72 F5 E3 4F 7C 40 B2 D0 4E 9F 50 13 FB 86 C3 6A 88 32 5B 67 EC 4F 0E 0B 31 F8 0C 02 6C CE 8D 50 55 A2 B3 57 73 7C 78 D3 43 1F 48 33 51 E7 0A D0 6D 46 71 4A AD 66 50 F9 96 11 4F A5 5B 3C A0 3E 46 D2 CB 3B A1 03 84 9C 8E 4E 2D 83 69 2E 17 9B F8 36 63 F1 93 CA F9 32 57 2B AB 4E 14 A3 5A F1 39 B0 3F 0F 99 CC 9B FB 7E BC 0A AA C9 65 3C C8 B4 B0 1F
        val flag2 = readByte().toInt()
        PacketLogger.verbose("包类型(flag2) = $flag2. (可能是 ${if (flag2 == 2) "sso" else "uni"})")

        val flag3 = readByte().toInt()

        val uinAccount = readString(readInt() - 4)//uin

        //debugPrint("remaining")

        (if (flag2 == 2) {
            //PacketLogger.verbose("SSO, 尝试使用 16 zero 解密.")
            kotlin.runCatching {
                decryptBy(DECRYPTER_16_ZERO).also { PacketLogger.verbose("成功使用 16 zero 解密") }
            }
        } else {
            //PacketLogger.verbose("Uni, 尝试使用 d2Key 解密.")
            kotlin.runCatching {
                decryptBy(D2Key).also { PacketLogger.verbose("成功使用 d2Key 解密") }
            }
        }).getOrElse {
            PacketLogger.verbose("解密失败, 尝试其他各种key")
            this.readBytes().tryDecryptOrNull()?.toReadPacket()
        }?.debugPrintThis("sso/uni body=")?.let {
            if (flag1 == 0x0A) {
                parseSsoFrame(flag3, it)
            } else {
                parseSsoFrame(flag3, it)
            }
        }?.let {
            val bytes = it.data.readBytes()
            if (flag2 == 2 && it.packetFactory != null) {
                PacketLogger.debug("Oicq Reuqest= " + bytes.toUHexString())
                try {
                    bytes.toReadPacket().parseOicqResponse {
                        debugIfFail {
                            if (it.packetFactory.commandName == "wtlogin.login") {
                                DebugLogger.info("服务器发来了 wtlogin.login. 正在解析 key")
                                try {
                                    val subCommand = readUShort().toInt()
                                    println("subCommand=$subCommand")
                                    val type = readUByte().toInt()
                                    println("type=$type")

                                    discardExact(2)
                                    val tlvMap: Map<Int, ByteArray> = this.readTLVMap()
                                    println("tlvMap: ")
                                    tlvMap.forEach {
                                        println(it.key.toShort().toUHexString() + " = " + it.value.toUHexString())
                                    }

                                    tlvMap[0x119]?.let { t119Data ->
                                        t119Data.decryptBy(tgtgtKey).toReadPacket().debugPrintThis("0x119data").apply {
                                            discardExact(2) // always discarded.  00 1C
                                            // 00 1C
                                            // 01 08 00 10 A1 73 76 98 64 E0 38 C6 C8 18 73 FA D3 85 DA D6 01 6A 00 30 1D 99 4A 28 7E B3 B8 AC 74 B9 C4 BB 6D BB 41 72 F7 5C 9F 0F 79 8A 82 4F 1F 69 34 6D 10 D6 BB E8 A3 4A 2B 5D F1 C7 05 3C F8 72 EF CF 67 E4 3C 94 01 06 00 78 B4 ED 9F 44 ED 10 18 A8 85 0A 8A 85 79 45 47 7F 25 AA EE 2C 53 83 80 0A B3 B0 47 3E 95 51 A4 AE 3E CA A0 1D B4 91 F7 BB 2E 94 76 A8 C8 97 02 C4 5B 15 02 B7 03 9A FC C2 58 6D 17 92 46 AE EB 2F 6F 65 B8 69 6C D6 9D AC 18 6F 07 53 AC FE FA BC BD CE 57 13 10 2D 5A C6 50 AA C2 AE 18 D4 FD CD F2 E0 D1 25 29 56 21 35 8F 01 9D D6 69 44 8F 06 D0 23 26 D3 0E E6 E6 B7 01 0C 00 10 73 32 61 4E 2C 72 35 58 68 28 47 3E 2B 6E 52 62 01 0A 00 48 A4 DA 48 FB B4 8D DA 7B 86 D7 A7 FE 01 1B 70 6F 54 F8 55 38 B0 AD 1B 0C 0B B9 F6 94 24 F8 9E 30 32 22 99 0C 22 CD 44 B8 B0 8A A8 65 E1 B8 F0 49 EF E1 23 D7 0D A3 F1 BB 52 B7 4B AF BD 50 EA BF 15 02 78 2B 8B 10 FB 15 01 0D 00 10 29 75 38 72 21 5D 3F 24 37 46 67 79 2B 65 6D 34 01 14 00 60 00 01 5E 19 65 8C 00 58 93 DD 4D 2C 2D 01 44 99 62 B8 7A EF 04 C5 71 0B F1 BE 4C F4 21 F2 97 B0 14 67 0E 14 9F D8 A2 0B 93 40 90 80 F3 59 7A 69 45 D7 D4 53 4C 08 3A 56 1D C9 95 36 2C 7C 5E EE 36 47 5F AE 26 72 76 FD FD 69 E6 0C 2D 3A E8 CF D4 8D 76 C9 17 C3 E3 CD 21 AB 04 6B 70 C5 EC EC 01 0E 00 10 56 48 3E 29 3A 5A 21 74 55 6A 2C 72 58 73 79 71 01 03 00 30 9B A6 5D 85 5C 40 7C 28 E7 05 A9 25 CA F5 FC C0 51 40 85 F3 2F D2 37 F9 09 A6 E6 56 7F 7A 2E 7D 9F B9 1C 00 65 55 D2 A9 60 03 77 AB 6A F5 3F CE 01 33 00 30 F4 3A A7 08 E2 04 FA C8 9D 54 49 DE 63 EA F0 A5 1C C4 03 57 51 B6 AE 0B 55 41 F8 AB 22 F1 DC A3 B0 73 08 55 14 02 BF FF 55 87 42 4C 23 70 91 6A 01 34 00 10 61 C7 02 3F 1D BE A6 27 2F 24 D4 92 95 68 71 EF 05 28 00 1A 7B 22 51 49 4D 5F 69 6E 76 69 74 61 74 69 6F 6E 5F 62 69 74 22 3A 22 31 22 7D 03 22 00 10 CE 1E 2E DC 69 24 4F 9B FF 2F 52 D8 8F 69 DD 40 01 1D 00 76 5F 5E 10 E2 34 36 79 27 23 53 4D 65 6B 6A 33 6D 7D 4E 3C 5F 00 60 00 01 5E 19 65 8C 00 58 67 00 9C 02 E4 BC DB A3 93 98 A1 ED 4C 91 08 6F 0C 06 E0 12 6A DC 14 5B 4D 20 7C 82 83 AE 94 53 A2 4A A0 35 FF 59 9D F3 EF 82 42 61 67 2A 31 E7 87 7E 74 E7 A3 E7 5C A8 3C 87 CF 40 6A 9F E5 F7 20 4E 56 C6 4F 1C 98 3A 8B A9 4F 1D 10 35 C2 3B A1 08 7A 89 0B 25 0C 63 01 1F 00 0A 00 01 51 80 00 00 03 84 00 00 01 38 00 0E 00 00 00 01 01 0A 00 27 8D 00 00 00 00 00 01 1A 00 13 02 5B 06 01 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 05 22 00 14 00 00 00 00 76 E4 B8 DD AB 53 02 9F 5E 19 65 8C 20 02 ED BD 05 37 00 17 01 01 00 00 00 00 76 E4 B8 DD 04 AB 53 02 9F 5E 19 65 8C 20 02 ED BD 01 20 00 0A 4D 39 50 57 50 6E 4C 31 65 4F 01 6D 00 2C 31 7A 50 7A 63 72 70 4D 30 43 6E 31 37 4C 32 32 6E 77 2D 36 7A 4E 71 48 48 59 41 35 48 71 77 41 37 6D 76 4F 63 2D 4A 56 77 47 51 5F 05 12 03 5D 00 0E 00 0A 74 65 6E 70 61 79 2E 63 6F 6D 00 2C 6E 4A 72 55 55 74 63 2A 34 7A 32 76 31 66 6A 75 77 6F 6A 65 73 72 76 4F 68 70 66 45 76 4A 75 55 4B 6D 34 43 2D 76 74 38 4D 77 38 5F 00 00 00 11 6F 70 65 6E 6D 6F 62 69 6C 65 2E 71 71 2E 63 6F 6D 00 2C 78 59 35 65 62 4D 74 48 44 6D 30 53 6F 68 56 71 68 33 43 79 79 34 6F 63 65 4A 46 6A 51 58 65 68 30 44 61 75 55 30 6C 78 65 52 6B 5F 00 00 00 0B 64 6F 63 73 2E 71 71 2E 63 6F 6D 00 2C 64 6A 62 79 47 57 45 4F 34 58 34 6A 36 4A 73 48 45 65 6B 73 69 74 72 78 79 62 57 69 77 49 68 46 45 70 72 4A 59 4F 2D 6B 36 47 6F 5F 00 00 00 0E 63 6F 6E 6E 65 63 74 2E 71 71 2E 63 6F 6D 00 2C 64 4C 31 41 79 32 41 31 74 33 58 36 58 58 2A 74 33 64 4E 70 2A 31 61 2D 50 7A 65 57 67 48 70 2D 65 47 78 6B 59 74 71 62 69 6C 55 5F 00 00 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 2C 75 6A 55 5A 4F 6A 4F 48 52 61 75 6B 32 55 50 38 77 33 34 68 36 69 46 38 2A 77 4E 50 35 2D 66 54 75 37 67 39 56 67 44 57 2A 6B 6F 5F 00 00 00 0A 76 69 70 2E 71 71 2E 63 6F 6D 00 2C 37 47 31 44 6F 54 2D 4D 57 50 63 2D 62 43 46 68 63 62 32 56 38 6E 77 4A 75 41 51 63 54 39 77 45 49 62 57 43 4A 4B 44 4D 6C 6D 34 5F 00 00 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 2C 7A 73 70 5A 56 43 59 45 7A 35 2A 4F 6B 4E 68 6E 74 79 61 69 6E 6F 68 4D 32 6B 41 6C 2A 74 31 63 7A 48 57 77 30 41 6A 4B 50 4B 6B 5F 00 00 00 0B 67 61 6D 65 2E 71 71 2E 63 6F 6D 00 2C 32 6F 2D 51 53 36 65 43 70 37 6A 43 4E 34 6A 74 6E 47 4F 4B 33 67 73 32 63 4A 6F 56 71 58 65 44 48 61 55 39 65 34 2D 32 34 64 30 5F 00 00 00 0C 71 71 77 65 62 2E 71 71 2E 63 6F 6D 00 2C 63 54 4D 79 64 51 43 35 50 74 43 45 51 72 6F 33 53 54 41 66 7A 56 2D 44 76 46 56 35 58 6D 56 6B 49 31 68 4C 55 48 4E 65 76 56 38 5F 00 00 00 0D 6F 66 66 69 63 65 2E 71 71 2E 63 6F 6D 00 2C 6F 73 72 54 36 32 69 37 66 76 6D 49 50 64 6F 58 4B 48 74 38 58 52 59 56 77 72 7A 6E 69 31 58 7A 57 4C 77 2A 71 36 33 44 74 73 6F 5F 00 00 00 09 74 69 2E 71 71 2E 63 6F 6D 00 2C 41 61 77 4D 78 4D 32 79 58 51 47 75 72 75 55 6C 66 53 58 79 5A 57 48 53 78 52 57 58 50 74 6B 6B 4F 78 6F 66 4A 59 47 6C 71 68 34 5F 00 00 00 0B 6D 61 69 6C 2E 71 71 2E 63 6F 6D 00 2C 67 72 57 68 58 77 34 4C 6E 4B 49 4F 67 63 78 45 71 70 33 61 45 67 37 38 46 7A 77 4E 6D 4B 48 56 6E 6F 50 4C 4F 32 6D 57 6D 6E 38 5F 00 00 00 09 71 7A 6F 6E 65 2E 63 6F 6D 00 2C 72 61 47 79 51 35 54 72 4D 55 7A 6E 74 31 4E 52 44 2D 50 72 74 72 41 55 43 35 6A 61 2D 49 47 2D 73 77 4C 6D 49 51 51 41 44 4C 41 5F 00 00 00 0A 6D 6D 61 2E 71 71 2E 63 6F 6D 00 2C 39 73 2D 4F 51 30 67 76 39 42 6A 37 58 71 52 49 4E 30 35 46 32 64 4D 47 67 47 43 58 57 4A 62 68 63 30 38 63 7A 4B 52 76 6B 78 6B 5F 00 00 03 05 00 10 77 75 6E 54 5F 7E 66 7A 72 40 3C 6E 35 50 53 46 01 43 00 40 3A AE 30 87 81 3D EE BA 31 9C EA 9D 0D D4 73 B1 81 12 E0 94 71 73 7A B0 47 3D 09 47 E5 1B E1 E2 06 1A CB A4 E3 71 9E A6 EA 2A 73 5C C8 D3 B1 2A B1 C7 DA 04 A6 6D 12 26 DF 6B 8B EC C7 12 F8 E1 01 18 00 05 00 00 00 01 00 01 63 00 10 67 6B 60 23 24 6A 55 39 4E 58 24 5E 39 2B 7A 69 01 38 00 5E 00 00 00 09 01 06 00 27 8D 00 00 00 00 00 01 0A 00 24 EA 00 00 00 00 00 01 1C 00 1A 5E 00 00 00 00 00 01 02 00 01 51 80 00 00 00 00 01 03 00 00 1C 20 00 00 00 00 01 20 00 01 51 80 00 00 00 00 01 36 00 1B AF 80 00 00 00 00 01 43 00 1B AF 80 00 00 00 00 01 64 00 1B AF 80 00 00 00 00 01 30 00 0E 00 00 5E 19 65 8C 9F 02 53 AB 00 00 00 00
                                            val tlvMap119 = this.readTLVMap()

                                            userStKey = tlvMap119.getOrEmpty(0x10e)
                                            wtSessionTicketKey = tlvMap119.getOrEmpty(0x133)
                                            D2Key = tlvMap119.getOrEmpty(0x305)
                                            DebugLogger.info("userStKey=${userStKey.toUHexString()}")
                                            DebugLogger.info("wtSessionTicketKey=${wtSessionTicketKey.toUHexString()}")
                                            DebugLogger.info("D2Key=${D2Key.toUHexString()}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else // always discarded.  00 1C
            // 00 1C
            // 01 08 00 10 A1 73 76 98 64 E0 38 C6 C8 18 73 FA D3 85 DA D6 01 6A 00 30 1D 99 4A 28 7E B3 B8 AC 74 B9 C4 BB 6D BB 41 72 F7 5C 9F 0F 79 8A 82 4F 1F 69 34 6D 10 D6 BB E8 A3 4A 2B 5D F1 C7 05 3C F8 72 EF CF 67 E4 3C 94 01 06 00 78 B4 ED 9F 44 ED 10 18 A8 85 0A 8A 85 79 45 47 7F 25 AA EE 2C 53 83 80 0A B3 B0 47 3E 95 51 A4 AE 3E CA A0 1D B4 91 F7 BB 2E 94 76 A8 C8 97 02 C4 5B 15 02 B7 03 9A FC C2 58 6D 17 92 46 AE EB 2F 6F 65 B8 69 6C D6 9D AC 18 6F 07 53 AC FE FA BC BD CE 57 13 10 2D 5A C6 50 AA C2 AE 18 D4 FD CD F2 E0 D1 25 29 56 21 35 8F 01 9D D6 69 44 8F 06 D0 23 26 D3 0E E6 E6 B7 01 0C 00 10 73 32 61 4E 2C 72 35 58 68 28 47 3E 2B 6E 52 62 01 0A 00 48 A4 DA 48 FB B4 8D DA 7B 86 D7 A7 FE 01 1B 70 6F 54 F8 55 38 B0 AD 1B 0C 0B B9 F6 94 24 F8 9E 30 32 22 99 0C 22 CD 44 B8 B0 8A A8 65 E1 B8 F0 49 EF E1 23 D7 0D A3 F1 BB 52 B7 4B AF BD 50 EA BF 15 02 78 2B 8B 10 FB 15 01 0D 00 10 29 75 38 72 21 5D 3F 24 37 46 67 79 2B 65 6D 34 01 14 00 60 00 01 5E 19 65 8C 00 58 93 DD 4D 2C 2D 01 44 99 62 B8 7A EF 04 C5 71 0B F1 BE 4C F4 21 F2 97 B0 14 67 0E 14 9F D8 A2 0B 93 40 90 80 F3 59 7A 69 45 D7 D4 53 4C 08 3A 56 1D C9 95 36 2C 7C 5E EE 36 47 5F AE 26 72 76 FD FD 69 E6 0C 2D 3A E8 CF D4 8D 76 C9 17 C3 E3 CD 21 AB 04 6B 70 C5 EC EC 01 0E 00 10 56 48 3E 29 3A 5A 21 74 55 6A 2C 72 58 73 79 71 01 03 00 30 9B A6 5D 85 5C 40 7C 28 E7 05 A9 25 CA F5 FC C0 51 40 85 F3 2F D2 37 F9 09 A6 E6 56 7F 7A 2E 7D 9F B9 1C 00 65 55 D2 A9 60 03 77 AB 6A F5 3F CE 01 33 00 30 F4 3A A7 08 E2 04 FA C8 9D 54 49 DE 63 EA F0 A5 1C C4 03 57 51 B6 AE 0B 55 41 F8 AB 22 F1 DC A3 B0 73 08 55 14 02 BF FF 55 87 42 4C 23 70 91 6A 01 34 00 10 61 C7 02 3F 1D BE A6 27 2F 24 D4 92 95 68 71 EF 05 28 00 1A 7B 22 51 49 4D 5F 69 6E 76 69 74 61 74 69 6F 6E 5F 62 69 74 22 3A 22 31 22 7D 03 22 00 10 CE 1E 2E DC 69 24 4F 9B FF 2F 52 D8 8F 69 DD 40 01 1D 00 76 5F 5E 10 E2 34 36 79 27 23 53 4D 65 6B 6A 33 6D 7D 4E 3C 5F 00 60 00 01 5E 19 65 8C 00 58 67 00 9C 02 E4 BC DB A3 93 98 A1 ED 4C 91 08 6F 0C 06 E0 12 6A DC 14 5B 4D 20 7C 82 83 AE 94 53 A2 4A A0 35 FF 59 9D F3 EF 82 42 61 67 2A 31 E7 87 7E 74 E7 A3 E7 5C A8 3C 87 CF 40 6A 9F E5 F7 20 4E 56 C6 4F 1C 98 3A 8B A9 4F 1D 10 35 C2 3B A1 08 7A 89 0B 25 0C 63 01 1F 00 0A 00 01 51 80 00 00 03 84 00 00 01 38 00 0E 00 00 00 01 01 0A 00 27 8D 00 00 00 00 00 01 1A 00 13 02 5B 06 01 0E 73 74 65 61 6D 63 68 69 6E 61 2E 66 75 6E 05 22 00 14 00 00 00 00 76 E4 B8 DD AB 53 02 9F 5E 19 65 8C 20 02 ED BD 05 37 00 17 01 01 00 00 00 00 76 E4 B8 DD 04 AB 53 02 9F 5E 19 65 8C 20 02 ED BD 01 20 00 0A 4D 39 50 57 50 6E 4C 31 65 4F 01 6D 00 2C 31 7A 50 7A 63 72 70 4D 30 43 6E 31 37 4C 32 32 6E 77 2D 36 7A 4E 71 48 48 59 41 35 48 71 77 41 37 6D 76 4F 63 2D 4A 56 77 47 51 5F 05 12 03 5D 00 0E 00 0A 74 65 6E 70 61 79 2E 63 6F 6D 00 2C 6E 4A 72 55 55 74 63 2A 34 7A 32 76 31 66 6A 75 77 6F 6A 65 73 72 76 4F 68 70 66 45 76 4A 75 55 4B 6D 34 43 2D 76 74 38 4D 77 38 5F 00 00 00 11 6F 70 65 6E 6D 6F 62 69 6C 65 2E 71 71 2E 63 6F 6D 00 2C 78 59 35 65 62 4D 74 48 44 6D 30 53 6F 68 56 71 68 33 43 79 79 34 6F 63 65 4A 46 6A 51 58 65 68 30 44 61 75 55 30 6C 78 65 52 6B 5F 00 00 00 0B 64 6F 63 73 2E 71 71 2E 63 6F 6D 00 2C 64 6A 62 79 47 57 45 4F 34 58 34 6A 36 4A 73 48 45 65 6B 73 69 74 72 78 79 62 57 69 77 49 68 46 45 70 72 4A 59 4F 2D 6B 36 47 6F 5F 00 00 00 0E 63 6F 6E 6E 65 63 74 2E 71 71 2E 63 6F 6D 00 2C 64 4C 31 41 79 32 41 31 74 33 58 36 58 58 2A 74 33 64 4E 70 2A 31 61 2D 50 7A 65 57 67 48 70 2D 65 47 78 6B 59 74 71 62 69 6C 55 5F 00 00 00 0C 71 7A 6F 6E 65 2E 71 71 2E 63 6F 6D 00 2C 75 6A 55 5A 4F 6A 4F 48 52 61 75 6B 32 55 50 38 77 33 34 68 36 69 46 38 2A 77 4E 50 35 2D 66 54 75 37 67 39 56 67 44 57 2A 6B 6F 5F 00 00 00 0A 76 69 70 2E 71 71 2E 63 6F 6D 00 2C 37 47 31 44 6F 54 2D 4D 57 50 63 2D 62 43 46 68 63 62 32 56 38 6E 77 4A 75 41 51 63 54 39 77 45 49 62 57 43 4A 4B 44 4D 6C 6D 34 5F 00 00 00 0A 71 75 6E 2E 71 71 2E 63 6F 6D 00 2C 7A 73 70 5A 56 43 59 45 7A 35 2A 4F 6B 4E 68 6E 74 79 61 69 6E 6F 68 4D 32 6B 41 6C 2A 74 31 63 7A 48 57 77 30 41 6A 4B 50 4B 6B 5F 00 00 00 0B 67 61 6D 65 2E 71 71 2E 63 6F 6D 00 2C 32 6F 2D 51 53 36 65 43 70 37 6A 43 4E 34 6A 74 6E 47 4F 4B 33 67 73 32 63 4A 6F 56 71 58 65 44 48 61 55 39 65 34 2D 32 34 64 30 5F 00 00 00 0C 71 71 77 65 62 2E 71 71 2E 63 6F 6D 00 2C 63 54 4D 79 64 51 43 35 50 74 43 45 51 72 6F 33 53 54 41 66 7A 56 2D 44 76 46 56 35 58 6D 56 6B 49 31 68 4C 55 48 4E 65 76 56 38 5F 00 00 00 0D 6F 66 66 69 63 65 2E 71 71 2E 63 6F 6D 00 2C 6F 73 72 54 36 32 69 37 66 76 6D 49 50 64 6F 58 4B 48 74 38 58 52 59 56 77 72 7A 6E 69 31 58 7A 57 4C 77 2A 71 36 33 44 74 73 6F 5F 00 00 00 09 74 69 2E 71 71 2E 63 6F 6D 00 2C 41 61 77 4D 78 4D 32 79 58 51 47 75 72 75 55 6C 66 53 58 79 5A 57 48 53 78 52 57 58 50 74 6B 6B 4F 78 6F 66 4A 59 47 6C 71 68 34 5F 00 00 00 0B 6D 61 69 6C 2E 71 71 2E 63 6F 6D 00 2C 67 72 57 68 58 77 34 4C 6E 4B 49 4F 67 63 78 45 71 70 33 61 45 67 37 38 46 7A 77 4E 6D 4B 48 56 6E 6F 50 4C 4F 32 6D 57 6D 6E 38 5F 00 00 00 09 71 7A 6F 6E 65 2E 63 6F 6D 00 2C 72 61 47 79 51 35 54 72 4D 55 7A 6E 74 31 4E 52 44 2D 50 72 74 72 41 55 43 35 6A 61 2D 49 47 2D 73 77 4C 6D 49 51 51 41 44 4C 41 5F 00 00 00 0A 6D 6D 61 2E 71 71 2E 63 6F 6D 00 2C 39 73 2D 4F 51 30 67 76 39 42 6A 37 58 71 52 49 4E 30 35 46 32 64 4D 47 67 47 43 58 57 4A 62 68 63 30 38 63 7A 4B 52 76 6B 78 6B 5F 00 00 03 05 00 10 77 75 6E 54 5F 7E 66 7A 72 40 3C 6E 35 50 53 46 01 43 00 40 3A AE 30 87 81 3D EE BA 31 9C EA 9D 0D D4 73 B1 81 12 E0 94 71 73 7A B0 47 3D 09 47 E5 1B E1 E2 06 1A CB A4 E3 71 9E A6 EA 2A 73 5C C8 D3 B1 2A B1 C7 DA 04 A6 6D 12 26 DF 6B 8B EC C7 12 F8 E1 01 18 00 05 00 00 00 01 00 01 63 00 10 67 6B 60 23 24 6A 55 39 4E 58 24 5E 39 2B 7A 69 01 38 00 5E 00 00 00 09 01 06 00 27 8D 00 00 00 00 00 01 0A 00 24 EA 00 00 00 00 00 01 1C 00 1A 5E 00 00 00 00 00 01 02 00 01 51 80 00 00 00 00 01 03 00 00 1C 20 00 00 00 00 01 20 00 01 51 80 00 00 00 00 01 36 00 1B AF 80 00 00 00 00 01 43 00 1B AF 80 00 00 00 00 01 64 00 1B AF 80 00 00 00 00 01 30 00 0E 00 00 5E 19 65 8C 9F 02 53 AB 00 00 00 00
            {
                PacketLogger.debug("不是oicq response(可能是 UNI/PB)= " + bytes.toUHexString())
            }
        } ?: inline {
            PacketLogger.error("任何key都无法解密")
            return
        }
    }
}

private fun Map<Int, ByteArray>.getOrEmpty(key: Int): ByteArray {
    return this[key] ?: byteArrayOf()
}

var randomKey: ByteArray = byteArrayOf()
private fun ByteReadPacket.parseOicqResponse(body: ByteReadPacket.() -> Unit) {
    val qq: Long
    readIoBuffer(readInt() - 4).withUse {
        check(readByte().toInt() == 2)
        this.discardExact(2) // 27 + 2 + body.size
        this.discardExact(2) // const, =8001
        this.readUShort() // commandId
        this.readShort() // const, =0x0001
        qq = this.readUInt().toLong()
        val encryptionMethod = this.readUShort().toInt()

        this.discardExact(1) // const = 0
        val packet = when (encryptionMethod) {
            4 -> { // peer public key, ECDH
                var data = this.decryptBy(shareKeyCalculatedByConstPubKey, 0, this.readRemaining - 1)
                data.read {
                    println("第一层解密: ${data.toUHexString()}")
                    val peerShareKey = ECDH.calculateShareKey(loadPrivateKey(ecdhPrivateKeyS), readUShortLVByteArray().adjustToPublicKey())
                    body(this.decryptBy(peerShareKey))
                }
            }
            0 -> {
                val data = if (0 == 0) {
                    ByteArrayPool.useInstance { byteArrayBuffer ->
                        val size = this.readRemaining - 1
                        this.readFully(byteArrayBuffer, 0, size)

                        runCatching {
                            byteArrayBuffer.decryptBy(shareKeyCalculatedByConstPubKey, size)
                        }.getOrElse {
                            byteArrayBuffer.decryptBy(randomKey, size)
                        } // 这里实际上应该用 privateKey(另一个random出来的key)
                    }
                } else {
                    this.decryptBy(randomKey, 0, this.readRemaining - 1)
                }

                PacketLogger.info("OicqRequest, Real body=" + data.toUHexString())
                body(data.toReadPacket())
            }
            else -> error("Illegal encryption method. expected 0 or 4, got $encryptionMethod")
        }
    }
}

/**
 * 解析 SSO 层包装
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
private fun parseSsoFrame(flag3: Int, input: ByteReadPacket): KnownPacketFactories.IncomingPacket {
    val commandName: String
    val ssoSequenceId: Int

    // head
    input.readIoBuffer(input.readInt() - 4).withUse {
        ssoSequenceId = readInt()
        PacketLogger.verbose("sequenceId = $ssoSequenceId")
        check(readInt() == 0)
        val extraData = readBytes(readInt() - 4)
        PacketLogger.verbose("sso(inner)extraData = ${extraData.toUHexString()}")

        commandName = readString(readInt() - 4)
        DebugLogger.warning("commandName=$commandName")
        val unknown = readBytes(readInt() - 4)
        //if (unknown.toInt() != 0x02B05B8B) DebugLogger.debug("got new unknown: ${unknown.toUHexString()}")

        check(readInt() == 0)
    }

    // body
    val packetFactory = KnownPacketFactories.findPacketFactory(commandName)

    if (packetFactory == null) {
        println("找不到包 PacketFactory")
        PacketLogger.verbose("传递给 PacketFactory 的数据 = ${input.readBytes().toUHexString()}")
    }

    var data = input.readBytes()
    if (flag3 == 1) {
        data = data.unzip(offset = 4)
    } else {

    }
    return KnownPacketFactories.IncomingPacket(packetFactory, ssoSequenceId, data.toReadPacket())
}


/**
 * 解析 Uni 层包装
 */
@UseExperimental(ExperimentalUnsignedTypes::class)
private fun parseUniFrame(input: ByteReadPacket): KnownPacketFactories.IncomingPacket {
    // 00 00 00 30 00 01 2F 7C 00 00 00 00 00 00 00 04 00 00 00 14 67 78 68 72 65 70 6F 72 74 2E 72 65 70 6F 72 74 00 00 00 08 66 82 D3 0B 00 00 00 00
    // 00 00 00 06 08 00


    //00 00 00 2D 00 01 2F 7E 00 00 00 00 00 00 00 04 00 00 00 11 4F 69 64 62 53 76 63 2E 30 78 35 39 66 00 00 00 08 66 82 D3 0B 00 00 00 00
    // 00 00 00 19 08 9F 0B 10 01 18 00 22 0C 10 00 18 00 20 00 A8 01 00 A0 06 01

    val commandName: String
    val ssoSequenceId: Int

    // head
    input.readIoBuffer(input.readInt() - 4).withUse {
        ssoSequenceId = readInt()
        PacketLogger.verbose("sequenceId = $ssoSequenceId")
        check(readInt() == 0)
        val extraData = readBytes(readInt() - 4)
        PacketLogger.verbose("sso(inner)extraData = ${extraData.toUHexString()}")

        commandName = readString(readInt() - 4)
        DebugLogger.warning("commandName=$commandName")
        val unknown = readBytes(readInt() - 4)
        //if (unknown.toInt() != 0x02B05B8B) DebugLogger.debug("got new unknown: ${unknown.toUHexString()}")

        check(readInt() == 0)
    }

    // body
    val packetFactory = KnownPacketFactories.findPacketFactory(commandName)

    if (packetFactory == null) {
        println("找不到包 PacketFactory")
        PacketLogger.verbose("传递给 PacketFactory 的数据 = ${input.readBytes().toUHexString()}")
    }
    return KnownPacketFactories.IncomingPacket(packetFactory, ssoSequenceId, input)
}

private inline fun <R> inline(block: () -> R): R = block()

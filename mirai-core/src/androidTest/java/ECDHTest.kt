import net.mamoe.mirai.timpc.network.TIMProtocol
import net.mamoe.mirai.utils.io.toUHexString


fun main() {

    println(EcdhCrypt().calShareKeyMd5ByPeerPublicKey(TIMProtocol.publicKey).toUHexString())
}
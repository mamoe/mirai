package net.mamoe.mirai.network.protocol.tim.packet

/**
 * @author Him188moe
 */
interface Packet {

}


object PacketNameFormatter {
    @JvmSynthetic
    private var longestNameLength: Int = 43

    @JvmSynthetic
    fun adjustName(name: String): String {
        if (name.length > longestNameLength) {
            longestNameLength = name.length
            return name
        }

        return StringBuilder().apply {
            repeat(longestNameLength - name.length) {
                this.append(" ")
            }
        }.toString() + name
    }
}
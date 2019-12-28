package packetdebugger

import kotlinx.io.core.ByteReadPacket
import kotlinx.io.core.discardExact
import kotlinx.io.core.readUShort
import net.mamoe.mirai.timpc.network.packet.PacketId

import net.mamoe.mirai.timpc.network.packet.matchPacketId
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@UseExperimental(ExperimentalUnsignedTypes::class)
class PacketDecoderScope(
    val packet: ByteReadPacket
) {
    @PublishedApi
    internal var _id: UShort = 0u
    @PublishedApi
    internal var _sequence: UShort = 0u

    internal val id: PacketId get() = matchPacketId(_id)
    val sequence: UShort get() = _sequence
}

@Suppress("EXPERIMENTAL_API_USAGE")
@UseExperimental(ExperimentalContracts::class)
inline fun ByteReadPacket.decodeOutgoingPacket(block: PacketDecoderScope.() -> Unit) {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }

    this.use {
        PacketDecoderScope(it).apply {
            discardExact(1) // head
            discardExact(2) // ver
            _id = readUShort()
            _sequence = readUShort()
            block()
        }
    }
}
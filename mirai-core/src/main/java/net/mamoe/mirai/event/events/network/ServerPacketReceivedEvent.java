package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.event.Cancellable;
import net.mamoe.mirai.network.packet.ServerPacket;
import net.mamoe.mirai.network.packet.ServerVerificationCodePacket;

/**
 * 服务器接到某数据包时触发这个事件.
 * 注意, 当接收到数据包的加密包(如 {@link ServerVerificationCodePacket.Encrypted})也会触发这个事件, 随后才会
 *
 * @author Him188moe
 */
public final class ServerPacketReceivedEvent extends ServerPacketEvent implements Cancellable {
    public ServerPacketReceivedEvent(ServerPacket packet) {
        super(packet);
    }
}

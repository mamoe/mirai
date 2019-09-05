package net.mamoe.mirai.event.events.network;

import net.mamoe.mirai.event.events.Cancellable;
import net.mamoe.mirai.network.packet.ServerPacket;

/**
 * @author Him188moe
 */
public class ServerPacketReceivedEvent extends ServerPacketEvent implements Cancellable {
    public ServerPacketReceivedEvent(ServerPacket packet) {
        super(packet);
    }
}

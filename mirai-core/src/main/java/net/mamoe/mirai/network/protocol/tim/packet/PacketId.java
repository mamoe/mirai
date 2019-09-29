package net.mamoe.mirai.network.protocol.tim.packet;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Him188moe
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PacketId {
    /**
     * 获取用于识别的包 ID
     */
    String value();
}

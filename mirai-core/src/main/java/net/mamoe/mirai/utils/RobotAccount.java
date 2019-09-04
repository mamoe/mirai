package net.mamoe.mirai.utils;

import lombok.Data;

/**
 * @author Him188moe
 */
@Data
public final class RobotAccount {
    public final long qqNumber;
    public final String password;

    public RobotAccount(long qqNumber, String password) {
        this.qqNumber = qqNumber;
        this.password = password;
    }
}

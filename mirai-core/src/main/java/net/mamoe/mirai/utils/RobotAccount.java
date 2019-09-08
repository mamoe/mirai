package net.mamoe.mirai.utils;

import lombok.Data;

/**
 * @author Him188moe
 */
@Data
public final class BotAccount {
    public final long qqNumber;
    public final String password;

    public BotAccount(long qqNumber, String password) {
        this.qqNumber = qqNumber;
        this.password = password;
    }
}

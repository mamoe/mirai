package net.mamoe.mirai;

import net.mamoe.mirai.utils.BotAccount;

import java.io.Closeable;

/**
 * @author Him188moe
 */
public interface Bot extends Closeable {

    BotAccount getAccount();

    // TODO: 2019/9/13 add more
}

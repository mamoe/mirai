package net.mamoe.mirai.japt;

import net.mamoe.mirai.Bot;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.japt.internal.BlockingBotImpl;
import net.mamoe.mirai.japt.internal.BlockingGroupImpl;
import net.mamoe.mirai.japt.internal.BlockingMemberImpl;
import net.mamoe.mirai.japt.internal.BlockingQQImpl;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 构造阻塞式的联系人.
 */
public final class BlockingContacts {
    @NotNull
    public static BlockingQQ createBlocking(@NotNull QQ qq) {
        return new BlockingQQImpl(Objects.requireNonNull(qq));
    }

    @NotNull
    public static BlockingGroup createBlocking(@NotNull Group group) {
        return new BlockingGroupImpl(Objects.requireNonNull(group));
    }

    @NotNull
    public static BlockingMember createBlocking(@NotNull Member member) {
        return new BlockingMemberImpl(Objects.requireNonNull(member));
    }

    @NotNull
    public static BlockingBot createBlocking(@NotNull Bot bot) {
        return new BlockingBotImpl(Objects.requireNonNull(bot));
    }
}

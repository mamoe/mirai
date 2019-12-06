package net.mamoe.mirai.japt;

import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.QQ;

/**
 * 构造阻塞式的联系人.
 */
public final class BlockingContacts {
    public static BlockingQQ createBlocking(QQ qq) {
        return new BlockingQQImpl(qq);
    }

    public static BlockingGroup createBlocking(Group group) {
        return new BlockingGroupImpl(group);
    }

    public static BlockingMember createBlocking(Member member) {
        return new BlockingMemberImpl(member);
    }
}

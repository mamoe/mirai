package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.network.RobotNetworkHandler;
import net.mamoe.mirai.utils.ContactList;
import net.mamoe.mirai.utils.config.MiraiConfigSection;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Robot implements Closeable {
    public static final List<Robot> instances = Collections.synchronizedList(new LinkedList<>());

    private final long qqNumber;
    private final String password;
    @Getter
    private final RobotNetworkHandler networkHandler;

    /**
     * Ref list
     */
    @Getter
    private final List<String> owners;

    private final ContactList<Group> groups = new ContactList<>();
    private final ContactList<QQ> qqs = new ContactList<>();

    public void close() {
        this.networkHandler.close();
        this.owners.clear();
        this.groups.values().forEach(Group::close);
        this.groups.clear();
        this.qqs.clear();
    }

    public boolean isOwnBy(String ownerName) {
        return owners.contains(ownerName);
    }

    public long getQQNumber() {
        return qqNumber;
    }

    public Robot(MiraiConfigSection<Object> data) throws Throwable {
        this(
                data.getLongOrThrow("account", () -> new IllegalArgumentException("account")),
                data.getStringOrThrow("password", () -> new IllegalArgumentException("password")),
                data.getAsOrDefault("owners", ArrayList::new)
        );

    }

    public Robot(long qqNumber, String password, List<String> owners) {
        this.qqNumber = qqNumber;
        this.password = password;
        this.owners = Collections.unmodifiableList(owners);
        this.networkHandler = new RobotNetworkHandler(this, this.qqNumber, this.password);
    }

    public QQ getQQ(long qqNumber) {
        if (!this.qqs.containsKey(qqNumber)) {
            this.qqs.put(qqNumber, new QQ(qqNumber));
        }
        return this.qqs.get(qqNumber);
    }

    public Group getGroup(long groupNumber) {
        if (!this.groups.containsKey(groupNumber)) {
            this.groups.put(groupNumber, new Group(groupNumber));
        }
        return groups.get(groupNumber);
    }

    public Group getGroupByGroupId(long groupId) {
        return getGroup(Group.Companion.groupIdToNumber(groupId));
    }
}


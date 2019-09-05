package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.network.RobotNetworkHandler;
import net.mamoe.mirai.utils.ContactList;
import net.mamoe.mirai.utils.RobotAccount;
import net.mamoe.mirai.utils.config.MiraiConfigSection;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.*;

/**
 * Robot that is the base of the whole program.
 * It contains a {@link ContactSystem}, which manage contacts such as {@link QQ} and {@link Group}.
 */
public final class Robot implements Closeable {
    public static final List<Robot> instances = Collections.synchronizedList(new LinkedList<>());

    public final RobotAccount account;

    public final ContactSystem contacts = new ContactSystem();

    public final RobotNetworkHandler network;

    /**
     * Robot 联系人管理.
     *
     * @see Robot#contacts
     */
    public final class ContactSystem {
        private final ContactList<Group> groups = new ContactList<>();
        private final ContactList<QQ> qqs = new ContactList<>();

        private ContactSystem() {

        }

        public QQ getQQ(long qqNumber) {
            if (!this.qqs.containsKey(qqNumber)) {
                this.qqs.put(qqNumber, new QQ(Robot.this, qqNumber));
            }
            return this.qqs.get(qqNumber);
        }

        public Group getGroupByNumber(long groupNumber) {
            if (!this.groups.containsKey(groupNumber)) {
                this.groups.put(groupNumber, new Group(Robot.this, groupNumber));
            }
            return groups.get(groupNumber);
        }

        public Group getGroupById(long groupId) {
            return getGroupByNumber(Group.Companion.groupIdToNumber(groupId));
        }
    }


    /**
     * Ref list
     */
    @Getter
    private final List<String> owners;

    public boolean isOwnBy(String ownerName) {
        return owners.contains(ownerName);
    }

    public Robot(MiraiConfigSection<Object> data) throws Throwable {
        this(
                new RobotAccount(
                        data.getLongOrThrow("account", () -> new IllegalArgumentException("account")),
                        data.getStringOrThrow("password", () -> new IllegalArgumentException("password"))
                ),
                data.getAsOrDefault("owners", ArrayList::new)
        );
    }

    public Robot(@NotNull RobotAccount account, @NotNull List<String> owners) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(owners);
        this.account = account;
        this.owners = Collections.unmodifiableList(owners);
        this.network = new RobotNetworkHandler(this);
    }


    public void close() {
        this.network.close();
        this.contacts.groups.values().forEach(Group::close);
        this.contacts.groups.clear();
        this.contacts.qqs.clear();
    }

}


package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.network.BotNetworkHandler;
import net.mamoe.mirai.utils.BotAccount;
import net.mamoe.mirai.utils.ContactList;
import net.mamoe.mirai.utils.config.MiraiConfigSection;
import org.jetbrains.annotations.NotNull;

import java.io.Closeable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Mirai 的机器人. 一个机器人实例登录一个 QQ 账号.
 * Mirai 为多账号设计, 可同时维护多个机器人.
 * <br>
 * {@link Bot} 由 2 个模块组成.
 * {@linkplain ContactSystem 联系人管理}: 可通过 {@link Bot#contacts} 访问
 * {@linkplain BotNetworkHandler 网络处理器}: 可通过 {@link Bot#network} 访问
 * <br>
 * 若你需要得到机器人的 QQ 账号, 请访问 {@link Bot#account}
 * 若你需要得到服务器上所有机器人列表, 请访问 {@link Bot#instances}
 *
 * @author Him188moe
 * @author NatrualHG
 * @see net.mamoe.mirai.contact.Contact
 *
 * <p>
 * Bot that is the base of the whole program.
 * It contains a {@link ContactSystem}, which manage contacts such as {@link QQ} and {@link Group}.
 */
public final class Bot implements Closeable {
    public static final List<Bot> instances = Collections.synchronizedList(new LinkedList<>());

    {
        instances.add(this);
    }

    public final int id = _id.getAndAdd(1);

    public final BotAccount account;

    public final ContactSystem contacts = new ContactSystem();

    public final BotNetworkHandler network;

    @Override
    public String toString() {
        return String.format("Bot{id=%d,qq=%d}", id, this.account.qqNumber);
    }

    /**
     * Bot 联系人管理.
     *
     * @see Bot#contacts
     */
    public final class ContactSystem {
        private final ContactList<Group> groups = new ContactList<>();
        private final ContactList<QQ> qqs = new ContactList<>();

        private ContactSystem() {

        }

        public QQ getQQ(long qqNumber) {
            if (!this.qqs.containsKey(qqNumber)) {
                this.qqs.put(qqNumber, new QQ(Bot.this, qqNumber));
            }
            return this.qqs.get(qqNumber);
        }

        public Group getGroupByNumber(long groupNumber) {
            if (!this.groups.containsKey(groupNumber)) {
                this.groups.put(groupNumber, new Group(Bot.this, groupNumber));
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

    public Bot(MiraiConfigSection<Object> data) throws Throwable {
        this(
                new BotAccount(
                        data.getLongOrThrow("account", () -> new IllegalArgumentException("account")),
                        data.getStringOrThrow("password", () -> new IllegalArgumentException("password"))
                ),
                data.getAsOrDefault("owners", ArrayList::new)
        );
    }

    public Bot(@NotNull BotAccount account, @NotNull List<String> owners) {
        Objects.requireNonNull(account);
        Objects.requireNonNull(owners);
        this.account = account;
        this.owners = Collections.unmodifiableList(owners);
        this.network = new BotNetworkHandler(this);
    }


    public void close() {
        this.network.close();
        this.contacts.groups.values().forEach(Group::close);
        this.contacts.groups.clear();
        this.contacts.qqs.clear();
    }

    public void addFriend(long qq) {

    }

    /* PRIVATE */

    private static final AtomicInteger _id = new AtomicInteger(0);
}


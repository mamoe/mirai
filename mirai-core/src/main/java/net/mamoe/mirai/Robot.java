package net.mamoe.mirai;

import kotlin.jvm.internal.MagicApiIntrinsics;
import lombok.Getter;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.QQ;
import net.mamoe.mirai.network.RobotNetworkHandler;
import net.mamoe.mirai.utils.ContactList;
import net.mamoe.mirai.utils.config.MiraiConfig;
import net.mamoe.mirai.utils.config.MiraiConfigSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Robot {

    private final int qqNumber;
    private final String password;
    @Getter
    private final RobotNetworkHandler handler;

    /**
     * Ref list
     */
    @Getter
    private final List<String> owners;

    private final ContactList<Group> groups = new ContactList<>();
    private final ContactList<QQ> qqs = new ContactList<>();

    public boolean isOwnBy(String ownerName) {
        return owners.contains(ownerName);
    }


    public Robot(MiraiConfigSection<Object> data) throws Throwable {
        this(
                data.getIntOrThrow("account", () -> new IllegalArgumentException("account")),
                data.getStringOrThrow("password", () -> new IllegalArgumentException("password")),
                data.getAsOrDefault("owners", ArrayList::new)
        );

    }

    public Robot(int qqNumber, String password, List<String> owners) {
        this.qqNumber = qqNumber;
        this.password = password;
        this.owners = Collections.unmodifiableList(owners);
        this.handler = new RobotNetworkHandler(this, this.qqNumber, this.password);
    }

    public QQ getQQ(int qqNumber) {
        if (!this.qqs.containsKey(qqNumber)) {
            this.qqs.put(qqNumber, new QQ(qqNumber));
        }
        return this.qqs.get(qqNumber);
    }

    public Group getGroup(int groupNumber) {
        if (!this.groups.containsKey(groupNumber)) {
            this.groups.put(groupNumber, new Group(groupNumber));
        }
        return groups.get(groupNumber);
    }

    public Group getGroupByGroupId(int groupId) {
        return getGroup(Group.Companion.groupIdToNumber(groupId));
    }

    /* Attribute
     * Attribute will be SAVED and LOAD automatically as long as the QQ account is same
     * {Attributes} is in the format of Map<String, Object>, keeping thread-safe
     * {Attributes} is a KEY-VALUE typed Data.
     * *
     **/

}

/*
class RobotAttribute extends MiraiConfigSection<Object>{

    static RobotAttribute load(Robot robot){

    }

    private MiraiConfigSection<Object> data;//late init

}

*/

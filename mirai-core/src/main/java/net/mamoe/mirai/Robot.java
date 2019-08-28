package net.mamoe.mirai;

import lombok.Getter;
import net.mamoe.mirai.network.RobotNetworkHandler;
import net.mamoe.mirai.utils.config.MiraiConfigSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Robot {

    private final int qq;
    private final String password;
    private final RobotNetworkHandler handler ;

    /**
     * Ref list
     */
    @Getter
    private final List<String> owners;

    public boolean isOwnBy(String ownerName){
        return owners.contains(ownerName);
    }


    public Robot(MiraiConfigSection<Object> data) throws Throwable {
        this(
                data.getIntOrThrow("account", () -> new Exception("can not parse QQ account")),
                data.getStringOrThrow("password", () -> new Exception("can not parse QQ password")),
                data.getAsOrDefault("owners",new ArrayList<>())
        );

    }


    public Robot(int qq, String password, List<String> owners){
        this.qq = qq;
        this.password = password;
        this.owners = Collections.unmodifiableList(owners);
        this.handler = new RobotNetworkHandler(this.qq,this.password);


    }


    public void connect(){

    }


    public void onPacketReceive(){

    }

}


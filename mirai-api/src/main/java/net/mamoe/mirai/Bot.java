package net.mamoe.mirai;

import java.util.ArrayList;
import java.util.List;

public class Bot {

    private final long qq;

    public Bot(long qq){
        this.qq = qq;
    }

    public String getName(){
        return "Bot";
    }

    public List<String> getOwners(){
        return new ArrayList<>();
    }

    public List<Long> getFriends(){
        return new ArrayList<>();
    }

    public void addFriend(){

    }

    public void deleteFriend(){

    }

    public void sendMessageTo(long qq, String message){

    }

    public List<Long> getGroups(){
        return new ArrayList<>();
    }

    public void addGroup(){

    }

    public void quitGroup(){

    }

    public void sendGroupMessage(long group, String message){

    }

    public List<String> getMessageHistory(){
        return new ArrayList<>();
    }



}

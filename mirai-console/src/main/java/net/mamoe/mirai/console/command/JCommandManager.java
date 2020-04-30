package net.mamoe.mirai.console.command;

// import jdk.jfr.Description;

public class JCommandManager {

    private JCommandManager() {

    }

    public static CommandManager getInstance() {
        return CommandManager.INSTANCE;
    }


}
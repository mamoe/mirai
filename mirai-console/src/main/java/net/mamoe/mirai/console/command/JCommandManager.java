package net.mamoe.mirai.console.command;

public class JCommandManager {

    private JCommandManager() {

    }

    public static CommandManager getInstance() {
        return CommandManager.INSTANCE;
    }


}

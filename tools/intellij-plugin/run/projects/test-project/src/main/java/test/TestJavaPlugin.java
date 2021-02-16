package test;

import net.mamoe.mirai.console.command.Command;
import net.mamoe.mirai.console.command.CommandOwner;
import net.mamoe.mirai.console.command.descriptor.CommandSignatureFromKFunction;
import net.mamoe.mirai.console.command.java.JCompositeCommand;
import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.console.permission.Permission;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TestJavaPlugin extends JavaPlugin {
    public TestJavaPlugin(@NotNull JvmPluginDescription description) {
        super(description);
    }
}

class TestCommand extends JSimpleCommand {

    public TestCommand(@NotNull CommandOwner owner, @NotNull String primaryName, @NotNull String[] secondaryNames, @NotNull Permission basePermission) {
        super(owner, primaryName, secondaryNames, basePermission);
    }

    @Handler
    public void test(String s) {

    }
}

class TestCommand2 extends JCompositeCommand {
    public TestCommand2(@NotNull CommandOwner owner, @NotNull String primaryName, @NotNull String[] secondaryNames, @NotNull Permission parentPermission) {
        super(owner, primaryName, secondaryNames, parentPermission);
    }

    @SubCommand("test")
    public void test() {}

    @SubCommand({})
    public void subCmd() {
    }
}

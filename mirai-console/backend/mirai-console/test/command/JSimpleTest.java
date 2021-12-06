/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package command;

import net.mamoe.mirai.console.command.java.JSimpleCommand;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;

public class JSimpleTest {
    @SuppressWarnings("PluginMainServiceNotConfiguredJava")
    private static class Main extends JavaPlugin {
        public Main(JvmPluginDescription description) {
            super(description);
        }
    }

    static class T extends JSimpleCommand {
        public T() {
            super(new Main(null), "name");
        }
    }
}

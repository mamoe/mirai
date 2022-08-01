/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

module.exports = {
    text: "mirai-console",
    link: "/",
    items: [
        {text: "Index", link: "/"},
        {text: "配置项目", link: "/ConfiguringProjects.html"},
        {text: "启动 Console", link: "/Run.html"},
        {
            text: "后端插件开发基础", items: [
                {text: "插件 - Plugin 模块", link: "/plugin/Plugins.html"},
                {text: "插件 - JVM Plugin", link: "/plugin/JVMPlugin.html"},
                {text: "指令 - Command 模块", link: "/Commands.html"},
                {text: "存储 - PluginData 模块", link: "/PluginData.html"},
                {text: "权限 - Permission 模块", link: "/Permissions.html"},
            ]
        },
        {
            text: "后端插件开发进阶", items: [
                {text: "扩展 - Extension 模块和扩展点", link: "/Extensions.html"},
            ]
        },
        {
            text: "实现前端", items: [
                {text: "FrontEnd", link: "/FrontEnd.html"},
            ]
        },
        {
            text: "Misc", items: [
                {text: 'Q & A', link: '/QA.html'},
                {text: '日志配置', link: '/Logging.html'},
                {text: '内置命令', link: '/BuiltInCommands.html'},
                {text: 'Mirai Console 附录', link: '/Appendix.html'},
                {text: "插件 - JVM Plugin - 附录", link: "/plugin/JVMPlugin-Appendix.html"},
                {text: "插件 - JVM Plugin - 多插件数据交换", link: "/plugin/JVMPlugin-DataExchange.html"},
                {text: "插件 - JVM Plugin - 排错", link: "/plugin/JVMPlugin-Debug.html"},
            ]
        },
    ]
}
/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

module.exports = {
    text: "mirai-core",
    link: "/",
    items: [
        {text: "Index", link: "/"},
        {text: "Mirai 生态概览", link: "/mirai-ecology.html"},
        {text: "从 1.x 迁移", link: "/MigrationFrom1x.html"},
        {text: '用户手册', link: '/UserManual.html'},
        {text: '用户手册 - 控制台', link: '/ConsoleTerminal.html'},
        {text: 'JVM 环境和开发准备工作', link: '/Preparations.html'},
        {text: "配置项目", link: "/ConfiguringProjects.html"},
        {text: "常见问题", link: "/Questions.html"},
        {
            text: "CoreAPI", items: [
                {text: "CoreAPI", link: "/CoreAPI.html"},
                {text: "机器人", link: "/Bots.html"},
                {text: "联系人", link: "/Contacts.html"},
                {text: "事件", link: "/Events.html"},
                {text: "消息", link: "/Messages.html"},
            ]
        },
        {
            text: "Misc", items: [
                {text: '主要API', link: '/ConciseAPI.html'},
                {text: 'Mirai - Evolution', link: '/Evolution.html'},
                {text: 'Kotlin & Java', link: '/KotlinAndJava.html'},
                {text: "事件列表", link: "/EventList.html"},
                {text: "Debugging Network", link: "/DebuggingNetwork.html"},
                {text: "Using Dev Snapshots", link: "/UsingSnapshots.html"},
                {text: "mirai 模拟测试框架", link: "/mocking/Mocking.md"},
            ]
        },
    ],
};
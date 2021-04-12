module.exports = {
    title: "mirai",
    description: "Mirai Project",
    base: "/",
    markdown: {
        lineNumbers: true,
    },
    theme: "antdocs",
    themeConfig: {
        backToTop: true,
        sidebar: "auto",
        sidebarDepth: 2,
        displayAllHeaders: true,
        repo: "mamoe/mirai",
        logo: "https://raw.githubusercontent.com/mamoe/mirai/dev/docs/mirai.png",
        docsDir: "docs",
        editLinks: false,
        smoothScroll: true,
        lastUpdated: "上次更新",
        nav: [
            {
                text: "mirai-core",
                link: "/index.html",
                items: [
                    {text: "Index", link: "/index.html"},
                    {text: "Mirai 生态概览", link: "/mirai-ecology.html"},
                    {text: "从 1.x 迁移", link: "/MigrationFrom1x.html"},
                    {
                        text: "CoreAPI", link: "/CoreAPI.html", items: [
                            {text: "机器人", link: "/Bots.html"},
                            {text: "联系人", link: "/Contacts.html"},
                            {text: "事件", link: "/Events.html"},
                            {text: "消息", link: "/Messages.html"},
                        ]
                    },
                ],
            },
            {
                text: "mirai-console",
                link: "/console/index.html",
                items: [
                    {text: "Index", link: "/console/index.html"},
                    {text: "配置项目", link: "/console/ConfiguringProjects.html"},
                    {text: "启动 Console", link: "/console/Run.html"},
                    {
                        text: "后端插件开发基础", items: [
                            {text: "插件 - Plugin 模块", link: "/console/Plugins.html"},
                            {text: "指令 - Command 模块", link: "/console/Commands.html"},
                            {text: "存储 - PluginData 模块", link: "/console/PluginData.html"},
                            {text: "权限 - Permission 模块", link: "/console/Permissions.html"},
                        ]
                    },
                    {
                        text: "后端插件开发进阶", items: [
                            {text: "扩展 - Extension 模块和扩展点", link: "/console/Extensions.html"}
                        ]
                    },
                    {
                        text: "实现前端", items: [
                            {text: "FrontEnd", link: "/console/FrontEnd.html"}
                        ]
                    },
                ]
            },
        ],
    },
    plugins: [
        "@vuepress/plugin-medium-zoom",
        "@vuepress/nprogress",
        [
            "@vuepress/pwa",
            {
                serviceWorker: true,
                updatePopup: {
                    message: "发现新内容",
                    buttonText: "刷新",
                },
            },
        ],
    ],
};

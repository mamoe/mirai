# Mirai - Question

## 用户常见问题

> 'java' 不是内部或外部命令，也不是可运行的程序

没有安装 `java`

> `Failed to fetch announcement for ...`

MCL查询更新信息失败，你可以尝试编辑 `config.json` 更换 [`mirai_repo`](https://github.com/project-mirai/mirai-repo-mirror#%E4%BB%93%E5%BA%93%E9%95%9C%E5%83%8F)

> Login failed: Error(bot=..., code=..., title=....

这些是服务器返回的信息，它表示你的 `BOT` 账号被登录风控了  
风控没有100%稳定的解决方法，你可以关注 [论坛公告帖 - 无法登录的临时处理方案](https://mirai.mamoe.net/topic/223)  
另外，密码登录中是否出现`滑块验证`或`短信验证`都是不可控的，这取决于腾讯的服务器要求你完成什么验证，没有办法自由选择。

> 登录协议如何修改

对于手动密码登录，第三个参数就是协议, 例如 `login 12345 114514 MACOS`  

对于自动密码登录，你可以使用指令修改，例如 `autoLogin setConfig 12345 protocol MACOS`  
也可以在 `Mirai Console` 关闭的情况下, 编辑 `config/Console/AutoLogin.yml` 文件  
注意，文件中有一个账号为 `12345` 的示例，请注意确认修改的配置对应的账号，不要修改错了示例

> 聊天框无法使用指令(使用指令后没效果)

1. 确认机器人收到消息  
   日志里会有消息记录，没有就是没收到，如果是群聊消息，注意是否已 `收入群助手`，这可能会导致收不到消息

2. 确认是否已经安装 [chat-command](https://github.com/project-mirai/chat-command/releases/download/v0.6.0/chat-command-0.6.0.mirai2.jar)  
   如果插件的指令是对接的 `Mirai Console` 的指令接口，那它就需要 `chat-command`

3. 确认是否已经授权给目标用户  
   默认情况下所有用户都是没有权限的，聊天框下无法使用指令

4. 确认日志中没有相关报错  
   如果插件指令执行出错，也有可能无法提供回复，请联系插件作者

> 如何确认 `Mirai 版本` 或 `插件版本` 等信息

使用指令 `/status` 

> 找不到 `http api` 的相关配置文件

你可能需要安装插件 https://github.com/project-mirai/mirai-api-http

> 如何添加 jvm 参数，例如 `-Dmirai.no-desktop=true`

编辑启动脚本 `mcl.cmd`, 在 `-jar` 前面加上 `-D...`  
例如 `-Dmirai.no-desktop=true -jar mcl...`

## 开发者常见问题

> 如何自定义登录验证处理

[覆盖登录解决器](https://github.com/mamoe/mirai/blob/dev/docs/Bots.md#%E8%A6%86%E7%9B%96%E7%99%BB%E5%BD%95%E8%A7%A3%E5%86%B3%E5%99%A8)

> IDEA 下 `import` 爆红，mirai 相关依赖全部无法解析

IDEA 太旧了，没法解析新版 kotlin, 最后升级到最新版

> 有些事件收到不到

`SignEvent` 或者 `NudgeEvent`  
总的来说 `MACOS` 和 `ANDROID_WATCH` 相对其他协议会缺少一些事件的接收  
对于这些协议来说是不会收到的，因为这在他们对应官方客户端版本里本来就没有这些功能

`GroupMessageEvent`  
群事件收不到可能是因为你将群设置为`屏蔽群`或者`收入了群助手`

另外如果消息是 `转发消息` 或 `卡片消息` 等特殊消息, 也有可能因为风控无法接收和发出

> 发送语音之后播放没有声音

你可能需要 安装插件/引入依赖 https://github.com/project-mirai/mirai-silk-converter

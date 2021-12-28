<!--
 * @Descripttion: 
 * @version: 
 * @Author: sueRimn
 * @Date: 2020-05-08 16:45:00
 * @LastEditors: sueRimn
 * @LastEditTime: 2020-05-09 12:22:15
 -->
 
 <div align="center">
   <img width="160" src="https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20200531205703.png" alt="logo"></br>

   <img width="95" src="https://cdn.jsdelivr.net/gh/mzdluo123/blog_imgs/img/20200531205726.png" alt="title">

</div>


# MiraiAndroid

<img alt="GitHub Workflow Status" src="https://img.shields.io/github/workflow/status/mzdluo123/MiraiAndroid/Android Build?style=flat-square">

<img alt="GitHub issues" src="https://img.shields.io/github/issues/mzdluo123/MiraiAndroid?style=flat-square">

<img alt="GitHub pull requests" src="https://img.shields.io/github/issues-pr/mzdluo123/MiraiAndroid?style=flat-square">

mirai-console的Android前端程序，可作为qq机器人使用，支持多种脚本接口

关于mirai项目以及mirai-console的一切请点击[这里](https://github.com/mamoe/mirai)

相比使用`Termux`或者是`Linux Deploy`等应用运行mirai的方案，该项目提供的方案具有更好的性能以及更少的资源占用，但可能存在兼容性问题

最新的构建版本你可以到release或QQ群内找到

MiraiAndroid交流群：`1131127734`但是请注意，如果您违反了群内相关规定或是有其他不当行为你可能会被无理由移出本群

图标以及形象由画师<a href = "https://github.com/DazeCake">DazeCake</a>绘制

## 声明

### 一切开发旨在学习，请勿用于非法用途
- MiraiAndroid 是完全免费且开放源代码的软件，仅供学习和娱乐用途使用
- MiraiAndroid 不会通过任何方式强制收取费用，或对使用者提出物质条件
- MiraiAndroid 由整个开源社区维护，并不是属于某个个体的作品，所有贡献者都享有其作品的著作权。

### 许可证

    Copyright (C) 2019-2020 Mamoe Technologies and contributors.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

`MiraiAndroid` 采用 `AGPLv3` 协议开源。为了整个社区的良性发展，我们**强烈建议**您做到以下几点：

- **间接接触（包括但不限于使用 `httpapi` 或 跨进程技术）到 `mirai` 的软件使用 `AGPLv3` 开源**
- **不鼓励，不支持一切商业使用**


# 已实现的功能

* 兼容mirai-console插件(实验性)
* 带验证码的登录处理
* 内置Google d8 dex编译器，可直接编译JVM的console插件在Android运行(实验性)
* lua脚本接口（测试版）
* 网络掉线提醒

# 安装脚本

目前MiraiAndroid已支持lua和JavaScript脚本，感谢[lua-mirai](https://github.com/only52607/lua-mirai)和[mirai-js](https://github.com/iTXTech/mirai-js)项目

## lua脚本

以下是一个简单的示例

```lua
Event.onLoad = function (bot)
    bot:subscribeGroupMsg(
        function(bot, msg, group, sender)
            group:sendMsg( msg )
        end
    )   
end
```

这个脚本实现了最简单的"复读机"功能，更多API请看[lua-mirai android api](https://github.com/only52607/lua-mirai/blob/master/docs/miraiandroid.md)

## JavaScript脚本

以下是一个~~简单~~复杂的示例

```JavaScript
// 插件信息
pluginInfo = {
    name: "JsPluginExample",
    version: "1.0.0",
    author: "PeratX",
    website: "https://github.com/iTXTech/mirai-js/blob/master/examples/reply.js"
};

let verbose = true;

// onLoad 事件
plugin.ev.onLoad = () => {
    logger.info("插件已加载：" + plugin.dataDir);

    // 插件数据读写
    let file = plugin.getDataFile("test.txt")
    // 第三个编码参数默认为 UTF-8，可空，同readText第二个参数
    stor.writeText(file, "真的很强。", Charset.forName("GBK"));
    logger.info("读取文件：" + file + " 内容：" + stor.readText(file, Charset.forName("GBK")));

    let config = new JsonConfig(plugin.getDataFile("test.json"));
    config.put("wow", "Hello World!");
    config.save();

    let v = 0;
    // 启动协程
    core.launch(() => {
        v++;
        logger.info("正在等待：" + v);
        if (verbose) {
            // 100ms执行一次
            return 100;
        }
        // 停止协程，返回 -1
        return -1;
    });
    // 延时1000ms执行一次
    core.launch(() => {
        verbose = false
        return -1;
    }, 1000);
    // 命令名称，描述，帮助，别名，回调
    core.registerCommand("test", "测试命令", "test", null, (sender, args) => {
        logger.info("发送者：" + sender)
        logger.info("参数：" + args)
        return true
    });
};

plugin.ev.onEnable = () => {
    logger.info("插件已启用。" + (plugin.enabled ? "是真的" : "是假的"));
    try {
        // Http 基于 OkHttp，可使用 OkHttp 的 API 自行构造
        let result = http.get("https://github.com/mamoe/mirai");
        if (result.isSuccessful()) {
            logger.info("Mirai GitHub主页长度：" + result.body().string().length());
        } else {
            logger.error("无法访问Mirai GitHub主页");
        }
        // 手动调用 OkHttp
        let client = http.newClient()
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .build()
        let response = client.newCall(
            http.newRequest()
                .url("https://im.qq.com")
                .header("User-Agent", "NMSL Browser 1.0")
                .build()
        ).execute();
        if (response.isSuccessful()) {
            logger.info("QQ主页长度：" + response.body().string().length());
        } else {
            logger.error("无法访问QQ主页");
        }
    } catch (e) {
        logger.error("无法获取网页", e)
    }
    regEv();
};

plugin.ev.onDisable = () => {
    logger.info("插件已禁用。");
};

plugin.ev.onUnload = () => {
    logger.info("插件已卸载。");
};

function regEv() {
    core.subscribeAlways(BotOnlineEvent, ev => {
        logger.info(ev);
    });
    core.subscribeAlways(GroupMessageEvent, ev => {
        logger.info(ev);
        ev.group.sendMessage(new PlainText("MiraiJs 收到消息：").plus(ev.message));
    })
}
```
你可以在[这里](https://github.com/iTXTech/mirai-js/blob/master/examples/reply.js)找到它，更多内容请查看项目介绍

在脚本管理界面点击右上角`+`可直接添加脚本到MiraiAndroid

目前该功能仍在开发中


# 安装插件

你有两个办法安装插件

## 使用app直接打开jar文件安装

这是最简单的方式。app切换到插件管理点击右上角选择即可，你也可以使用系统文件选择器直接打开jar文件

**如果你无法选择文件**，请使用第三方文件选择器选择（例如Mix）

## 使用pc转换后导入

请按照以下方法操作

* 找到`d8`编译器的运行脚本

d8工具已在新版`Android sdk`中自带，它就在`build-tools`中对应版本的文件夹下。在Windows平台他是一个bat文件

如果没有可到上面的交流群内下载

* 编译

打开终端，使用以下命令编译

```
d8.bat --output 输出文件.jar 源文件
```
输出文件扩展名必须是jar或者是zip

* 复制资源

使用压缩软件打开源jar文件，将里面的`plugin.yml`，`META-INF`和其他资源文件(除存放class文件夹的其他文件)复制到新的jar文件内

* 安装插件

将上一步的新的jar文件复制到手机的`/sdcard/Android/data/io.github.mzdluo123.mirai.android/files/plugins/`

重启即可使用插件，当然部分插件可能也会存在兼容性问题

# FAQ

Q: 后台运行被系统杀死<br>
A：请手动将应用添加到系统后台白名单

Q：应用崩溃或后台报错<br>
A：如果是后台报错一般是插件或者是mirai-core的问题，是mirai-core的问题请在菜单内找到分享日志并到群内或开启issue反馈，插件的问题请联系对应开发者；如果是应用崩溃，请重启并按照上面的方法提交日志给我们

# 兼容的Console插件列表

以下插件由群友测试未发现问题，你可以到群内下载，或是到[插件中心](https://github.com/mamoe/mirai-plugins)手动下载jvm版并导入

* mirai-api-http
* HsoSe
* keywordReply
* forward
* CQHTTPMirai

对于其他插件请自行尝试；此外，如果你的插件使用了一些Android不支持的api(例如BufferedImage)那么使用了这个api的功能将绝对不能正常工作

# 关于支持的Android版本

我们尚不清楚MiraiAndroid究竟能在哪些Android版本上正常工作，需要大家进行测试

我们已经测试无问题的Android版本：

* Android 10
* Android 8.1（无法在Android端编译插件）

其他版本还未进行测试，以下是测试要求：

* 程序不闪退，不报错，不出现无响应，通知显示正常，能正常完成登录
* 能够在Android端编译jvm插件（可选）
* 能够使用编译好的jvm插件发送消息，发送图片，处理事件和正确读写配置
* 能够正常运行两个脚本引擎的demo

从下一个release版本开始项目的minsdk版本将调整至21（Android 5.1），测试结果可以通过issue和交流群告诉我们，谢谢！（反馈时记得带上日志和Android版本，抓取日志可以在控制台右上角菜单内找到）


# 消息推送(2.9新增)

必须使用自动登录并在设置中开启才能使用该功能

你可以发送广播来快速向指定群或联系人推送信息，这里是data的URI格式

```
ma://sendGroupMsg?msg=消息&id=群号
ma://sendFriendMsg?msg=消息&id=账号
ma://sendFriendMsg?msg=消息&id=账号&at=要at的人
```

```kotlin
sendBroadcast(Intent("io.github.mzdluo123.mirai.android.PushMsg").apply {
        data = Uri.parse("ma://sendGroupMsg?msg=HelloWorld&id=655057127")
    })
```

以下是auto.js的示例

```js
app.sendBroadcast({
    action: "io.github.mzdluo123.mirai.android.PushMsg",
    data: "ma://sendGroupMsg?msg=来自autojs的消息&id=655057127"
})
```

以下是tasker的示例

```yaml
    ma (2)
    	A1: 发送意图 [ 操作:io.github.mzdluo123.mirai.android.PushMsg 类别:None Mime类型: 数据:ma://sendGroupMsg?msg=来自tasker的消息&id=655057127 额外: 额外: 额外: 包: 类: 目标:Broadcast Receiver ] 
```

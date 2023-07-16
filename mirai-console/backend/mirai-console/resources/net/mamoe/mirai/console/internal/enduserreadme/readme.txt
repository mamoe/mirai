::mirai-console.greeting

欢迎使用 mirai-console。
在您正式开始使用 mirai-console 前，您需要完整阅读此用户须知。

此用户须知包含 mirai-console 本体及其所安装的插件的用户须知。
当相关的最终用户须知更新时，mirai-console 只会显示已更新部分，而不会重新完整显示整个用户须知。

::mirai-console.usage

在使用 mirai-console 前，您需要完整阅读用户手册。
<delay>2
用户手册地址:
    GitHub:   https://github.com/mamoe/mirai/blob/dev/docs/UserManual.md
    VuePress: https://docs.mirai.mamoe.net/UserManual.html
<delay>3
当您遇到问题前，请先查阅
<delay>2
    常见问题参考: https://docs.mirai.mamoe.net/Questions.html
<delay>1
    mirai 历史问题提问: https://github.com/mamoe/mirai/issues?q=is%3Aissue
<delay>3

如果您使用的 mirai-console 来自一个单独整合包，您需要参考该整合包内的 `readme` 文件

::mirai-console.issuing

在使用 mirai-console 的过程中，您可能会遇到各种问题。
在您向他人咨询前，您需要做好以下准备。
<delay>2
无论是
<delay>2
`- 在 mirai 主仓库发起 issue
<delay>1
`- 在 mirai 论坛发起帖子
<delay>1
`- 在群聊向他人咨询
<delay>1
`- 在私聊向他人咨询
<delay>1
`- 或者更多
<delay>1
您都需要做好以下准备。
<delay>1
这不仅能让您更快解决问题，也是对被询问者的尊重。
<delay>1

1. 说明您正在使用的版本
<delay>2
版本号是确定问题的关键信息，
<delay>1
mirai-console 的版本号会在 mirai-console 运行时就打印至控制台。
其他组件版本可以通过执行 /status 命令获取

<delay>3
2. 携带报错信息 / 携带日志
<delay>3
报错信息是分析问题的关键，没有日志相当于闭眼开车。
<delay>3
当您咨询时，一定要携带当时的日志
<delay>3
「没有日志我能做的事只有帮你算一卦」
<delay>3

标准的咨询模板参考：
https://github.com/mamoe/mirai/issues/new?template=bug.yml

::mirai-core.EncryptService.alert

Reference: https://github.com/mamoe/mirai/releases/tag/v2.15.0

关于包数据加密 / 签名 （Internal）(#2716)
<delay>2
mirai 不会内置任何第三方 签名/加密 服务，而是提供 SPI 让用户自行实现。
<delay>2
mirai 已经提供了外部 EncryptService SPI 供用户对接。如果您没有能力自行对接，您可以考虑到论坛寻找社区对接。
<delay>2
在使用社区服务前，您需要了解并理解以下内容
<delay>2
<pause>

1. 确认服务来源
<delay>2
   当您安装此服务后，所有的信息都会经过此消息服务。
   <delay>2
   这其中包括
     Bot 的登录请求（包含密码，登录凭证等）
     <delay>2
     Bot 发出去的所有信息
     <delay>2
     更多.....
<delay>2
<pause>
2. 保护好网络，建立通讯防火墙
<delay>2
部分服务通讯链路是无加密的
<delay>1
如果您访问的服务位于公开网络，您的数据有被窃取、拦截的风险。

<delay>2
<pause>
3. 保护好日志。
<delay>2
并非所有日志都能直接传递给他人
<pause>

在您公开您的日志前，请先对日志中的关键信息进行抹除。
<pause>

部分相关服务使用 HTTP GET 请求传递数据体，
当远程服务出错时，服务对接可能会直接将此次请求的连接直接输出到日志中，
此日志可能包含了此次尝试 签名/加密 的内容，
而此内容可能包含关键信息。
<pause>

如果您无法分辨哪些请求需要被抹除时，您可以参考以下规则：
<pause>

    请求连接包含大量 Hex 文本，抹除 （Hex: 由 0-9 和 ABCDEF 组成的序列 ）
    <delay>2
    <pause>
    请求包含大量 Base64 文本，抹除 （如您不知道什么是 Base64 文本，您可以简单当做是超长的英文与符号组合）
    <delay>2
    <pause>
    请求连接过长，抹除（如连接日志换行了三次都还没有显示完全）
    <delay>2
    <pause>



# Mirai Console Backend - Logging

Console 的日志一共有五个级别：

|  级别（由高到低）  | 用途           |  默认启用  |
|:----------:|--------------|:------:|
|   ERROR    | 记录影响程序运行的错误  |   是    |
|  WARNING   | 记录不影响程序运行的警告 |   是    |
|    INFO    | 记录一条普通信息     |   是    |
|   DEBUG    | 记录普通调试信息     |   否    |
|  VERBOSE   | 记录详细调试信息     |   否    |

`DEBUG` 和 `VERBOSE`
作为调试信息，默认关闭。插件开发者可能会使用这两个级别来输出调试信息。如果你在使用中遇到问题，启用这个两个级别获得更多日志后再报告开发者可能更有帮助。

特别地，`ALL` 表示启用全部日志，`NONE` 表示禁用全部日志。

在终端前端（或 [MCL](https://github.com/iTXTech/mirai-console-loader)
），日志配置文件默认路径为 `config/Console/Logger.yml`。示例内容为如下。

```yaml
# 默认日志输出等级 可选值: ALL, VERBOSE, DEBUG, INFO, WARNING, ERROR, NONE
defaultPriority: INFO
# 特定日志记录器输出等级
loggers:
    example.logger: NONE
    console.debug: NONE
    Bot: ALL
```

## 调整全局默认日志等级

修改 `defaultPriority` 即可修改全局默认日志等级。

例如设置为 DEBUG，则启用上表中 DEBUG 及更高级别的日志，即
DEBUG、INFO、WARNING、ERROR。

## 调整特定日志等级

每个插件被分配的日志的 ID 为插件的显示名称。

提示：该 ID 也可以在日志中找到。如下面的日志中，`Bot 12345678` 就是其所属日志的 ID。（`V`
代表等级为 VERBOSE，以首字母识别）

```text
2022-05-02 11:09:28 V/Bot 12345678: Event: BotOnlineEvent(bot=Bot(12345678))
```

如果在日志配置这样修改：

```yaml
loggers:
    "Bot 12345678": NONE
```

那么将禁用来自该 Bot 的所有日志。

假设要启用名为 `Chat Command` 的插件的 DEBUG 及更高级别的日志：

```yaml
loggers:
    "Chat Command": DEBUG
```

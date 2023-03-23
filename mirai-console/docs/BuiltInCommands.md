# Mirai Console - BuiltIn Commands

Mirai Console 内置一些指令。

下文中 `<A|B>` 表示可以是 A 或 B。`[ ]` 表示一个必须的参数, `[ ]?` 表示一个可选的参数。

## HelpCommand

`/help`

查看指令帮助

## StopCommand

`/<stop|shutdown|exit>`

关闭 Mirai Console

## LoginCommand

`/<login|登录> [qq] [password] [protocol]?`

| 参数        | 可选值          | 描述                                                                  |
|:----       | :----           | :-------------------------------------------------------------------  |
| protocol   | ANDROID_PHONE   | Android 手机.  所有功能都支持.                                         |
| protocol   | ANDROID_PAD     | Android 平板.                                                       |
| protocol   | ANDROID_WATCH   | Android 手表.  注意: 不支持戳一戳事件解析                                 |


临时登录一个账号

## PermissionCommand

主指令: `/<permission|perm|权限>`

次指令:

| 指令                                                                                         | 描述                   |
|:--------------------------------------------------------------------------------------------|:----------------------|
| `/<permission\|perm\|权限> <permit\|grant\|add> [target] [permission]`                        | 授权一个权限            |
| `/<permission\|perm\|权限> <cancel\|deny\|remove> [target] [permission]`                      | 撤销一个权限            |
| `/<permission\|perm\|权限> <cancelAll\|denyAll\|removeAll> [target] [permission]`             | 撤销一个权限及其所有子权限 |
| `/<permission\|perm\|权限> <permittedPermissions\|pp\|grantedPermissions\|gp> [target] [all]` | 查看被授权权限列表       |
| `/<permission\|perm\|权限> <listPermissions\|lp>`                                             | 查看所有权限列表         |

### `[target]` 和 `[permission]` 示例

`[target]` 是 [*被许可人 ID*](Permissions.md#被许可人-id)，可以查看[表示方法](Permissions.md#字符串表示) 。

`[permission]` 是 [*权限 ID*](Permissions.md#权限-id)。每个指令都拥有一个权限 ID。请使用 `/perm list` 查看权限 ID 列表。

示例：`/perm permit u123456 console:command.stop`

### 授予一个用户执行所有指令的权限

示例：
- `/perm permit u123456 *:*` 允许用户 123456 执行任意指令
- `/perm permit u123456 console:*` 允许用户 123456 执行任意 Console 内置指令
- `/perm permit u123456 org.example.my-plugin:*` 允许用户 123456 执行插件 `org.example.my-plugin` 的任意指令

### 授予所有用户执行某个指令的权限

示例：
- `/perm permit u* console:command.help` 允许所有用户执行 `console:command.help`（即 `/help`）
- `/perm permit u* console:*` 允许所有用户执行任意 Console 内置指令
- `/perm permit u* org.example.my-plugin:*` 允许所有用户执行插件 `org.example.my-plugin` 的任意指令

## AutoLoginCommand

主指令: `/autoLogin`

次指令:

| 指令                                                           | 描述                 |
|:--------------------------------------------------------------|:---------------------|
| `/<autoLogin\|自动登录> list`                                    | 查看自动登录账号列表    |
| `/<autoLogin\|自动登录> add [account] [password] [passwordKind]` | 添加自动登录           |
| `/<autoLogin\|自动登录> clear`                                   | 清除所有配置           |
| `/<autoLogin\|自动登录> remove [account]`                        | 删除一个账号           |
| `/<autoLogin\|自动登录> setConfig [account] [configKey] [value]` | 设置一个账号的一个配置项 |
| `/<autoLogin\|自动登录> removeConfig [account] [configKey]`      | 删除一个账号的一个配置项 |


| `configKey` |                       可选值                       |
|:-----------:|:-------------------------------------------------:|
| `protocol`  | `ANDROID_PHONE` / `ANDROID_PAD` / `ANDROID_WATCH` |

示例：`/autoLogin setConfig 123456 protocol ANDROID_PHONE`

## StatusCommand

`/status`

获取 Mirai Console 运行状态

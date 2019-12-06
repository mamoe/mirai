# UpdateLog

## Major version 0

开发版本. 频繁更新, 不保证高稳定性
### 0.7.2  2019/12/07
- 使所有协议相关类 `internal`
- 去掉一些 `close` 的不应该有的 `suspend`
- `QQ`, `Member`, `Group` 现在继承接口 `CoroutineScope`
- 将 `LoginResult` 由 `inline class` 修改为 `enum class`
- 添加和修改了 `BotAccount` 和 `Bot` 的构造器

### 0.7.1  2019/12/05  
- 修复禁言时间范围错误的问题
- 禁言的扩展函数现在会传递实际函数的返回值

### 0.7.0  2019/12/04  
协议  
- 重新分析验证码包, 解决一些无法解析的情况. (这可能会产生新的问题, 遇到后请提交 issue)
- 重新分析提交密码包
- *提交验证码仍可能出现问题*

功能  
- XML 消息 DSL 构造支持 (实验性) (暂不支持发送)
- 群成员列表现在包含群主 (原本就应该包含)
- 在消息事件处理中添加获取 `.qq()` 和 `.group()` 的扩展函数. 
- 现在处理群消息时 sender 为 Member (以前为 QQ)
- 修改 `Message.concat` 为 `Message.followedBy`
- 修改成员权限 `OPERATOR` 为 `ADMINISTRATOR`
- **bot.subscribeAll<> 等函数的 handler lambda 的 receiver 由 Bot 改变为 BotSession**; 此变动不会造成现有代码的修改, 但并不兼容旧版本编译的代码

性能优化  
- 内联 ContactList
-  2 个 Contact.sendMessage 重载改为内联扩展函数 **(需要添加 import)**
- 其他小优化

### 0.6.1  2019/12/03
- 新增: 无法解析密码包/验证码包时的调试输出. 以兼容更多的设备情况
- 新增: `MessagePacket` 下 `At.qq()` 捷径获取 QQ

### 0.6.0  2019/12/02
- 新增: 禁言群成员 (`Member.mute(TimeSpan|Duration|MonthsSpan|Int|UInt)`)
- 新增: 解禁群成员 (`Member.unmute()`)
- 修复: ContactList key 无法匹配 (Kotlin 内联类型泛型投影错误)

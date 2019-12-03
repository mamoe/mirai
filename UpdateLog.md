# UpdateLog

## Main version 0

开发版本. 频繁更新, 不保证高稳定性

### 0.6.1
2019/12/3
- 新增: 无法解析密码包/验证码包时的调试输出. 以兼容更多的设备情况
- 新增: `MessagePacket` 下 `At.qq()` 捷径获取 QQ

### 0.6.0 
2019/12/02

- 新增: 禁言群成员 (`Member.mute(TimeSpan|Duration|MonthsSpan|Int|UInt)`)
- 新增: 解禁群成员 (`Member.unmute()`)
- 修复: ContactList key 无法匹配 (Kotlin 内联类型泛型投影错误)
# mirai-console-intellij

IntelliJ 平台的 Mirai Console 开发插件

## 功能

### 诊断

#### ILLEGAL_PLUGIN_DESCRIPTION

- 使用 [ResolveContext](../../backend/mirai-console/src/common/ResolveContext.kt)
- 检测 Plugin Id, Plugin Name, Plugin Version 的合法性. 并在非法时提示正确的语法.
- 支持编译期常量

![ILLEGAL_PLUGIN_DESCRIPTION](.images/ILLEGAL_PLUGIN_DESCRIPTION.png)

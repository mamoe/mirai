# Mirai Console - Contributing

感谢你来到这里，感谢你对 Mirai Console 做的一切贡献。

## 开发 Mirai Console

### 模块

Mirai Console 项目由四个模块组成：后端，前端，Gradle 插件，Intellij 插件。

```
/
|--- backend                     后端
|  |--- codegen                     后端代码生成工具
|  `--- mirai-console               后端主模块, 发布为 net.mamoe:mirai-console
|--- buildSrc                    项目构建
|--- frontend                    前端
|  `--- mirai-console-terminal      终端前端，发布为 net.mamoe:mirai-console-terminal
`--- tools                       开发工具
   |--- compiler-common             编译器通用模块
   |--- gradle-plugin               Gradle 插件，发布为 net.mamoe.mirai-console
   `--- intellij-plugin             IntelliJ 平台 IDE 插件，发布为 Mirai Console
```

请前往各模块内的 README.md 查看详细说明。
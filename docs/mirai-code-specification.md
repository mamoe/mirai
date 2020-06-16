# mirai Code Specification - mirai 码规范

> 此文档最后更新于 2020/06/12, 基于 mirai `1.1.0`

## mirai 码
mirai 的部分 [消息](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Message.kt) 可以表示为形如 `[mirai:atall]` 的字符串.  
模块 `mirai-core` 包含消息到 mirai 码的单向转换; 模块 `mirai-serialization` 提供 mirai 码的解析.

运行时: [mirai-serialization](../mirai-serialization/)

## 变更记录
- `1.1.0`: 引入 mirai 码于 `mirai-serialization` 模块

## 格式

格式分为有参数和无参数两种.

### 无参数

字符串格式: \[mirai:*typename*\]

```regex
(?:\[mirai:([^:]+)\])
```

| Message Type                                                                       | mirai Code Typename | Example         |
|:-----------------------------------------------------------------------------------|:--------------------|:----------------|
| [AtAll](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/AtAll.kt) | atall               | `[mirai:atall]` |

### 有参数
字符串格式:  \[mirai:*名称*:*参数列表*\]  
多个参数之间使用逗号分隔, 如 `[mirai:at:123456,test]`

```regex
(?:\[mirai:([^\]]*)?:(.*?)?\])
```

| Message Type                                                                                         | mirai Code Typename | Params                | Example                                               | Note                                                                                                                                                          |
|:-----------------------------------------------------------------------------------------------------|:--------------------|:----------------------|:------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [At](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/At.kt#L29)                     | at                  | `target`, `display` | `[mirai:at:123456,test]`                              | `target` 为 at 对象的 QQ 账号;<br /> `display` 为官方客户端中 at 显示的内容                                                                                                   |
| [Face](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Face.kt#L20)                 | face                | `id`                  | `[mirai:face:123]`                                    | `id` 见 [Face.IdList](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Face.kt#L36-L237)                          |
| [PokeMessage](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/HummerMessage.kt#L40) | poke                | `name`, `type` , `id` | `[mirai:poke:戳一戳,1,-1]`                               | 详见 [PokeMessage.Types](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/HummerMessage.kt#L55-L138)  |
| [VipFace](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/HummerMessage.kt#L149)    | vipface             | `id`, `name`, `count` | `[mirai:vipface:9,榴莲,5]`                              | 详见 [VipFace.Companion](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/HummerMessage.kt#L174-L225) |
| [Image](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Image.kt#L35)               | image               | `imageId`             | `[mirai:image:/f8f1ab55-bf8e-4236-b55e-955848d7069f]` | `imageId` 见 [Image.imageId](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Image.kt#L82)                                                    |
| [FlashImage](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/HummerMessage.kt#L234) | flash               | `imageId`             | `[mirai:flash:/f8f1ab55-bf8e-4236-b55e-955848d7069f]` | `imageId` 见 [Image.imageId](../mirai-core/src/commonMain/kotlin/net.mamoe.mirai/message/data/Image.kt#L82)                                                    |

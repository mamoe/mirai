# mirai-api-http

<b>Mirai-API-http 提供HTTP API供所有语言使用mirai</b>

### 快速开始

```kotlin
fun main() {
    val bot = Bot(123456789, "password")

    bot.login()

    MiraiHttpAPIServer.start()
    
    bot.network.awaitDisconnection()
}
```

### 开始会话-认证(Authorize)

```
[POST] /auth
```
使用此方法验证你的身份，并返回一个会话

#### 请求:

```json5
{
    "authKey": "U9HSaDXl39ksd918273hU"
}
```

|  名字    | 类型 | 可选 | 举例 | 说明 |
| --- | --- | --- | --- | --- |
| authKey |  String |false|"U9HSaDXl39ksd918273hU"|创建Mirai-Http-Server时生成的key，可在启动时指定或随机生成|

#### 响应: 返回(成功):

```json5
{
    "code": 0,
    "session": "UnVerifiedSession"
}
```

|  名字    | 类型 | 举例 | 说明|
| --- | --- | ---  | --- |
| code |Int |0|返回状态码|
| session |String |"UnVerifiedSession"|你的session key|

#### 状态码:

|  代码    | 原因|
| --- | --- |
| 0 | 正常 |
| 1 | 错误的MIRAI API HTTP auth key|

 session key 是使用以下方法必须携带的</br>
 session key 使用前必须进行校验和绑定指定的Bot，**每个Session只能绑定一个Bot，但一个Bot可有多个Session**



### 校验Session

```
[post] /verify
```

使用此方法校验并激活你的Session，同时将Session与一个**已登录**的Bot绑定

#### 请求:

```json5
{
    "sessionKey": "UnVerifiedSession",
    "qq": 123456789
}
```

| 名字       | 类型   | 可选  | 举例                | 说明                       |
| ---------- | ------ | ----- | ------------------- | -------------------------- |
| sessionKey | String | false | "UnVerifiedSession" | 你的session key            |
| qq         | Long   | false | 123456789           | Session将要绑定的Bot的qq号 |

#### 响应: 返回统一状态码（后续不再赘述）

```json5
{
    "code": 0,
    "msg": "success"
}
```

| 状态码 | 原因                               |
| ------ | ---------------------------------- |
| 0      | 正常                               |
| 1      | 错误的auth key                     |
| 2      | 绑定的Bot不存在                    |
| 3      | Session失效或不存在                |
| 4      | Session未认证(未激活)              |
| 5      | 发送消息目标不存在(指定对象不存在) |
| 400    | 错误的访问，如参数错误等           |

 

### 发送好友消息

```
[POST] /sendFriendMessage
```

使用此方法向指定好友发送消息

#### 请求

```json5
{
    "sessionKey": "YourSession",
    "target": 987654321,
    "messageChain": [
        { "type": "Plain", "text":"hello\n" },
        { "type": "Plain", "text":"world" }
    ]
}
```

| 名字         | 类型   | 可选  | 举例        | 说明                             |
| ------------ | ------ | ----- | ----------- | -------------------------------- |
| sessionKey   | String | false | YourSession | 已经激活的Session                |
| target       | Long   | false | 987654321   | 发送消息目标好友的QQ号           |
| messageChain | Array  | false | []          | 消息链，是一个消息对象构成的数组 |

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 发送群消息

```
[POST] /sendGroupMessage
```

使用此方法向指定群发送消息

#### 请求

```json5
{
    "sessionKey": "YourSession",
    "target": 987654321,
    "messageChain": [
        { "type": "Plain", "text":"hello\n" },
        { "type": "Plain", "text":"world" }
    ]
}
```

| 名字         | 类型   | 可选  | 举例        | 说明                             |
| ------------ | ------ | ----- | ----------- | -------------------------------- |
| sessionKey   | String | false | YourSession | 已经激活的Session                |
| target       | Long   | false | 987654321   | 发送消息目标群的群号             |
| messageChain | Array  | false | []          | 消息链，是一个消息对象构成的数组 |

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 获取Bot收到的消息

```
[GET] /fetchMessage?sessionKey=YourSessionKey&count=10
```

#### 请求:

| 名字       | 可选  | 举例           | 说明            |
| ---------- | ----- | -------------- | --------------- |
| sessionKey | false | YourSessionKey | 你的session key |
| count      | false | 10             | 获取消息的数量  |

#### 响应: 返回JSON对象

```json5
[{
    "type": "GroupMessage",        // 消息类型：GroupMessage或FriendMessage
	"messageChain": [{             // 消息链，是一个消息对象构成的数组
        "type": "Plain",
        "text": "Miral牛逼"
    }],
    "sender": {                      // 发送者信息
        "id": 123456789,             // 发送者的QQ号码
        "memberName": "化腾",        // 发送者的群名片
        "permission": "MEMBER",      // 发送者的群限权：OWNER、ADMINISTRATOR或MEMBER
        "group": {                   // 消息发送群的信息
            "id": 1234567890,        // 发送群的群号
            "name": "Miral Technology" // 发送群的群名称
        }
    }
 },
 {
    "type": "FriendMessage",         // 消息类型：GroupMessage或FriendMessage
        "messageChain": [{           // 消息链，是一个消息对象构成的数组
        "type": "Plain",
        "text": "Miral牛逼"
    }],
    "sender": {                      // 发送者信息
        "id": 1234567890,            // 发送者的QQ号码
        "nickName": "",              // 发送者的昵称
        "remark": ""                 // 发送者的备注
    }
}]
```



### 消息类型一览

#### 消息是构成消息链的基本对象，目前支持的消息类型有

+ [x] At，@消息
+ [x] Face，表情消息
+ [x] Plain，文字消息
+ [ ] Image，图片消息
+ [ ] Xml，Xml卡片消息
+ [ ] 敬请期待

#### At

```json5
{
    "type": "At",
    "target": 123456,
    "display": "@Mirai"
}
```

| 名字    | 类型   | 说明                      |
| ------- | ------ | ------------------------- |
| target  | Long   | 群员QQ号                  |
| display | String | @时显示的文本如："@Mirai" |

#### Face

```json5
{
    "type": "Face",
    "faceID": 123
}
```

| 名字   | 类型 | 说明       |
| ------ | ---- | ---------- |
| faceID | Int  | QQ表情编号 |

#### Plain

```json5
{
    "type": "Plain",
    "text": "Mirai牛逼"
}
```

| 名字 | 类型   | 说明     |
| ---- | ------ | -------- |
| text | String | 文字消息 |

#### Image

```json5
{
    "type": "Image"
    // 暂时不支持Image
}
```

| 名字 | 类型 | 说明 |
| ---- | ---- | ---- |
|      |      |      |

#### Xml

```json5
{
    "type": "Xml",
    "xml": "XML"
}
```

| 名字 | 类型   | 说明    |
| ---- | ------ | ------- |
| xml  | String | XML文本 |
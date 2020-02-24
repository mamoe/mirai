# mirai-api-http

<b>Mirai-API-http 提供HTTP API供所有语言使用mirai</b>

## 快速开始

```kotlin
fun main() {
    val bot = Bot(123456789, "password")

    bot.login()

    MiraiHttpAPIServer.start()
    
    bot.join()
}
```



## 认证相关

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

| 名字    | 类型   | 可选  | 举例                    | 说明                                                       |
| ------- | ------ | ----- | ----------------------- | ---------------------------------------------------------- |
| authKey | String | false | "U9HSaDXl39ksd918273hU" | 创建Mirai-Http-Server时生成的key，可在启动时指定或随机生成 |

#### 响应: 返回(成功):

```json5
{
    "code": 0,
    "session": "UnVerifiedSession"
}
```

| 名字    | 类型   | 举例                | 说明            |
| ------- | ------ | ------------------- | --------------- |
| code    | Int    | 0                   | 返回状态码      |
| session | String | "UnVerifiedSession" | 你的session key |

#### 状态码:

| 代码 | 原因                          |
| ---- | ----------------------------- |
| 0    | 正常                          |
| 1    | 错误的MIRAI API HTTP auth key |

 session key 是使用以下方法必须携带的
 session key 使用前必须进行校验和绑定指定的Bot，**每个Session只能绑定一个Bot，但一个Bot可有多个Session**
 session Key 在未进行校验的情况下，一定时间后将会被自动释放


### 校验Session

```
[POST] /verify
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

| 状态码 | 原因                                |
| ------ | ----------------------------------- |
| 0      | 正常                                |
| 1      | 错误的auth key                      |
| 2      | 指定的Bot不存在                     |
| 3      | Session失效或不存在                 |
| 4      | Session未认证(未激活)               |
| 5      | 发送消息目标不存在(指定对象不存在)  |
| 10     | 无操作权限，指Bot没有对应操作的限权 |
| 400    | 错误的访问，如参数错误等            |



### 释放Session

```
[POST] /release
```

使用此方式释放session及其相关资源（Bot不会被释放）
**不使用的Session应当被释放，否则Session持续保存Bot收到的消息，将会导致内存泄露**

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "qq": 123456789
}
```

| 名字       | 类型   | 可选  | 举例             | 说明                       |
| ---------- | ------ | ----- | -----------------| -------------------------- |
| sessionKey | String | false | "YourSessionKey" | 你的session key            |
| qq         | Long   | false | 123456789        | 与该Session绑定Bot的QQ号码 |

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```
> SessionKey与Bot 对应错误时将会返回状态码5：指定对象不存在




## 消息相关


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
| quote        | Long   | true  | 135798642   | 引用一条消息的messageId进行回复  |
| messageChain | Array  | false | []          | 消息链，是一个消息对象构成的数组 |

#### 响应: 返回统一状态码（并携带messageId）

```json5
{
    "code": 0,
    "msg": "success",
    "messageId": 1234567890 // 一个Long类型属性，标识本条消息，用于撤回和引用回复
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
| quote        | Long   | true  | 135798642   | 引用一条消息的messageId进行回复  |
| messageChain | Array  | false | []          | 消息链，是一个消息对象构成的数组 |

#### 响应: 返回统一状态码（并携带messageId）

```json5
{
    "code": 0,
    "msg": "success",
    "messageId": 1234567890 // 一个Long类型属性，标识本条消息，用于撤回和引用回复
}
```



### 发送图片消息（通过URL）

```
[POST] /sendImageMessage
```

使用此方法向指定对象（群或好友）发送图片消息

#### 请求

```json5
{
    "sessionKey": "YourSession",
    "target": 987654321,
    "qq": 1234567890,
    "group": 987654321,
    "urls": [
        "https://xxx.yyy.zzz/",
        "https://aaa.bbb.ccc/"
    ]
}
```

| 名字         | 类型   | 可选  | 举例        | 说明                               |
| ------------ | ------ | ----- | ----------- | ---------------------------------- |
| sessionKey   | String | false | YourSession | 已经激活的Session                  |
| target       | Long   | true  | 987654321   | 发送对象的QQ号或群号，可能存在歧义 |
| qq           | Long   | true  | 123456789   | 发送对象的QQ号                     |
| group        | Long   | true  | 987654321   | 发送对象的群号                     |
| urls         | Array  | false | []          | 是一个url字符串构成的数组          |

#### 响应: 图片的imageId数组

```json5
[
    "{XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}.jpg",
    "{YYYYYYYY-YYYY-YYYY-YYYY-YYYYYYYYYYYY}.jpg"
]
```



### 图片文件上传

```
[POST] /uploadImage
```

使用此方法上传图片文件至服务器并返回ImageId

#### 请求

Content-Type：multipart/form-data

| 名字         | 类型   | 可选  | 举例        | 说明                               |
| ------------ | ------ | ----- | ----------- | ---------------------------------- |
| sessionKey   | String | false | YourSession | 已经激活的Session                  |
| type         | String | false | "friend "   | "friend" 或 "group"                |
| img          | File   | false | -           | 图片文件                           |


#### 响应: 图片的imageId（好友图片与群聊图片Id不同）

```
{XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX}.jpg
```



### 撤回消息

```
[POST] /recall
```

使用此方法撤回指定消息。对于bot发送的消息，又2分钟时间限制。对于撤回群聊中群员的消息，需要有相应权限

#### 请求

```json5
{
    "sessionKey": "YourSession",
    "target": 987654321
}
```

| 名字         | 类型   | 可选  | 举例        | 说明                             |
| ------------ | ------ | ----- | ----------- | -------------------------------- |
| sessionKey   | String | false | YourSession | 已经激活的Session                |
| target       | Long   | false | 987654321   | 需要撤回的消息的messageId        |

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 获取Bot收到的消息和事件

```
[GET] /fetchMessage?sessionKey=YourSessionKey&count=10
```

使用此方法获取bot接收到的消息和各类事件

#### 请求:

| 名字       | 可选  | 举例           | 说明                 |
| ---------- | ----- | -------------- | -------------------- |
| sessionKey | false | YourSessionKey | 你的session key      |
| count      | false | 10             | 获取消息和事件的数量 |

#### 响应: 返回JSON对象

```json5
[{
    "type": "GroupMessage",        // 消息类型：GroupMessage或FriendMessage或各类Event
	"messageChain": [{             // 消息链，是一个消息对象构成的数组
	    "type": "Source",
	    "uid": 123456
	},{
        "type": "Plain",
        "text": "Miral牛逼"
    }],
    "sender": {                      // 发送者信息
        "id": 123456789,             // 发送者的QQ号码
        "memberName": "化腾",        // 发送者的群名片
        "permission": "MEMBER",      // 发送者的群限权：OWNER、ADMINISTRATOR或MEMBER
        "group": {                   // 消息发送群的信息
            "id": 1234567890,        // 发送群的群号
            "name": "Miral Technology", // 发送群的群名称
            "permission": "MEMBER"      // 发送群中，Bot的群限权
        }
    }
 },{
    "type": "FriendMessage",         // 消息类型：GroupMessage或FriendMessage或各类Event
    "messageChain": [{             // 消息链，是一个消息对象构成的数组
        "type": "Source",
        "uid": 123456
    },{
        "type": "Plain",
        "text": "Miral牛逼"
    }],
    "sender": {                      // 发送者信息
        "id": 1234567890,            // 发送者的QQ号码
        "nickName": "",              // 发送者的昵称
        "remark": ""                 // 发送者的备注
    }
 },{
    "type": "MemberMuteEvent",       // 消息类型：GroupMessage或FriendMessage或各类Event
    "durationSeconds": 600,
    "member":{
        "id": 123456789,
        "memberName": "禁言对象",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    },
    "operator":{
        "id": 987654321, 
        "memberName": "群主大人", 
        "permission": "OWNER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}]
```



### 事件类型一览
[事件类型一览](EventType_CH.md)

> 事件为Bot被动接收的信息，无法主动构建


### 消息类型一览

#### 消息是构成消息链的基本对象，目前支持的消息类型有

+ [x] At，@消息
+ [x] AtAll，@全体成员
+ [x] Face，表情消息
+ [x] Plain，文字消息
+ [x] Image，图片消息
+ [ ] Xml，Xml卡片消息
+ [ ] 敬请期待

#### Source

```json5
{
    "type": "Source",
    "id": 123456
}
```

| 名字 | 类型 | 说明                                                         |
| ---- | ---- | ------------------------------------------------------------ |
| id   | Long | 消息的识别号，用于引用回复（Source类型只在群消息中返回，且永远为chain的第一个元素） |

#### At

```json5
{
    "type": "At",
    "target": 123456,
    "display": "@Mirai"
}
```

| 名字    | 类型   | 说明                                           |
| ------- | ------ | ---------------------------------------------- |
| target  | Long   | 群员QQ号                                       |
| dispaly | String | At时显示的文字，发送消息时无效，自动使用群名片 |

#### AtAll

```json5
{
    "type": "AtAll"
}
```

| 名字    | 类型   | 说明                      |
| ------- | ------ | ------------------------- |
| -       | -      | -                         |

#### Face

```json5
{
    "type": "Face",
    "faceId": 123
}
```

| 名字   | 类型 | 说明       |
| ------ | ---- | ---------- |
| faceId | Int  | QQ表情编号 |

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
    "type": "Image",
    "imageId": "{01E9451B-70ED-EAE3-B37C-101F1EEBF5B5}.png" //群图片格式
    //"imageId": "/f8f1ab55-bf8e-4236-b55e-955848d7069f"      //好友图片格式
}
```

| 名字    | 类型   | 说明                                    |
| ------- | ------ | --------------------------------------- |
| imageId | String | 图片的imageId，群图片与好友图片格式不同 |

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



## 管理相关

### 获取好友列表

使用此方法获取bot的好友列表

```
[GET] /friendList?sessionKey=YourSessionKey
```

#### 请求:

| 名字       | 可选  | 举例           | 说明            |
| ---------- | ----- | -------------- | --------------- |
| sessionKey | false | YourSessionKey | 你的session key |

#### 响应: 返回JSON对象

```json5
[{
    "id":123456789,
    "nickName":"",
    "remark":""
  },{
    "id":987654321,
    "nickName":"",
    "remark":""
}]
```



### 获取群列表

使用此方法获取bot的群列表

```
[GET] /groupList?sessionKey=YourSessionKey
```

#### 请求:

| 名字       | 可选  | 举例           | 说明            |
| ---------- | ----- | -------------- | --------------- |
| sessionKey | false | YourSessionKey | 你的session key |

#### 响应: 返回JSON对象

```json5
[{
    "id":123456789,
    "name":"群名1",
    "permission": "MEMBER"
  },{
    "id":987654321,
    "name":"群名2",
    "permission": "MEMBER"
}]
```



### 获取群成员列表

使用此方法获取bot指定群种的成员列表

```
[GET] /memberList?sessionKey=YourSessionKey
```

#### 请求:

| 名字       | 可选  | 举例           | 说明            |
| ---------- | ----- | -------------- | --------------- |
| sessionKey | false | YourSessionKey | 你的session key |
| target     | false | 123456789      | 指定群的群号    |

#### 响应: 返回JSON对象

```json5
[{
    "id":1234567890,
    "memberName":"",
    "permission":"MEMBER",
    "group":{
        "id":12345,
        "name":"群名1",
        "permission": "MEMBER"
    }
  },{
    "id":9876543210,
    "memberName":"",
    "permission":"OWNER",
    "group":{
        "id":54321,
        "name":"群名2",
        "permission": "MEMBER"
    }
}]
```



### 群全体禁言

使用此方法令指定群进行全体禁言（需要有相关限权）

```
[POST] /muteAll
```

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "target": 123456789,
}
```

| 名字       | 可选  | 类型   | 举例             | 说明            |
| ---------- | ----- | ------ | ---------------- | --------------- |
| sessionKey | false | String | "YourSessionKey" | 你的session key |
| target     | false | Long   | 123456789        | 指定群的群号    |


#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 群解除全体禁言

使用此方法令指定群解除全体禁言（需要有相关限权）

```
[POST] /unmuteAll
```

#### 请求:

同全体禁言

#### 响应

同全体禁言



### 群禁言群成员

使用此方法指定群禁言指定群员（需要有相关限权）

```
[POST] /mute
```

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "target": 123456789,
    "memberId": 987654321,
    "time": 1800
}
```

| 名字       | 可选  | 类型   | 举例             | 说明                                  |
| ---------- | ----- | ------ | ---------------- | ------------------------------------- |
| sessionKey | false | String | "YourSessionKey" | 你的session key                       |
| target     | false | Long   | 123456789        | 指定群的群号                          |
| memberId   | false | Long   | 987654321        | 指定群员QQ号                          |
| time       | true  | Int    | 1800             | 禁言时长，单位为秒，最多30天，默认为0 |

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 群解除群成员禁言

使用此方法令指定群解除全体禁言（需要有相关限权）

```
[POST] /unmute
```

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "target": 123456789,
    "memberId": 987654321
}
```

#### 响应

同群禁言群成员



### 移除群成员

使用此方法移除指定群成员（需要有相关限权）

```
[POST] /kick
```

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "target": 123456789,
    "memberId": 987654321,
    "msg": "您已被移出群聊"
}
```

| 名字       | 可选  | 类型   | 举例             | 说明            |
| ---------- | ----- | ------ | ---------------- | --------------- |
| sessionKey | false | String | "YourSessionKey" | 你的session key |
| target     | false | Long   | 123456789        | 指定群的群号    |
| memberId   | false | Long   | 987654321        | 指定群员QQ号    |
| msg        | true  | String | ""               | 信息            |

#### 响应

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 群设置

使用此方法修改群设置（需要有相关限权）

```
[POST] /groupConfig
```

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "target": 123456789,
    "config": {
        "name": "群名称",
        "announcement": "群公告",
        "confessTalk": true,
        "allowMemberInvite": true,
        "autoApprove": true,
        "anonymousChat": true
    }
}
```

| 名字              | 可选  | 类型    | 举例             | 说明                 |
| ----------------- | ----- | ------- | ---------------- | -------------------- |
| sessionKey        | false | String  | "YourSessionKey" | 你的session key      |
| target            | false | Long    | 123456789        | 指定群的群号         |
| config            | false | Object  | {}               | 群设置               |
| name              | true  | String  | "Name"           | 群名                 |
| announcement      | true  | Boolean | true             | 群公告               |
| confessTalk       | true  | Boolean | true             | 是否开启坦白说       |
| allowMemberInvite | true  | Boolean | true             | 是否运行群员邀请     |
| autoApprove       | true  | Boolean | true             | 是否开启自动审批入群 |
| anonymousChat     | true  | Boolean | true             | 是否允许匿名聊天     |

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 获取群设置

使用此方法获取群设置

```
[Get] /groupConfig?sessionKey=YourSessionKey&target=123456789
```

#### 请求:

| 名字              | 可选  | 类型    | 举例             | 说明                 |
| ----------------- | ----- | ------- | ---------------- | -------------------- |
| sessionKey        | false | String  | YourSessionKey   | 你的session key      |
| target            | false | Long    | 123456789        | 指定群的群号         |


#### 响应

```json5
{
    "name": "群名称",
    "announcement": "群公告",
    "confessTalk": true,
    "allowMemberInvite": true,
    "autoApprove": true,
    "anonymousChat": true
}
```



### 修改群员资料

使用此方法修改群员资料（需要有相关限权）

```
[POST] /memberInfo
```

#### 请求:

```json5
{
    "sessionKey": "YourSessionKey",
    "target": 123456789,
    "memberId": 987654321,
    "info": {
        "name": "群名片",
        "specialTitle": "群头衔"
    }
}
```

| 名字              | 可选  | 类型    | 举例             | 说明                 |
| ----------------- | ----- | ------- | ---------------- | -------------------- |
| sessionKey        | false | String  | "YourSessionKey" | 你的session key      |
| target            | false | Long    | 123456789        | 指定群的群号         |
| memberId          | false | Long    | 987654321        | 群员QQ号             |
| info              | false | Object  | {}               | 群员资料             |
| name              | true  | String  | "Name"           | 群名片，即群昵称     |
| specialTitle      | true  | String  | "Title"          | 群头衔               |

#### 响应: 返回统一状态码

```json5
{
    "code": 0,
    "msg": "success"
}
```



### 获取群员资料

使用此方法获取群员资料

```
[Get] /groupConfig?sessionKey=YourSessionKey&target=123456789
```

#### 请求:

| 名字              | 可选  | 类型    | 举例             | 说明                 |
| ----------------- | ----- | ------- | ---------------- | -------------------- |
| sessionKey        | false | String  | YourSessionKey   | 你的session key      |
| target            | false | Long    | 123456789        | 指定群的群号         |
| memberId          | false | Long    | 987654321        | 群员QQ号             |


#### 响应

```json5
{
    "name": "群名片",
    "announcement": "群头衔"
}
```
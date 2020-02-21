
#### Bot登录成功

```json5
{
    "type": "BotOnlineEvent",
    "qq": 123456
}
```

| 名字 | 类型 | 说明                |
| ---- | ---- | ------------------- |
| qq   | Long | 登录成功的Bot的QQ号 |



#### Bot主动离线

```json5
{
    "type": "BotOfflineEventActive",
    "qq": 123456
}
```

| 名字 | 类型 | 说明                |
| ---- | ---- | ------------------- |
| qq   | Long | 主动离线的Bot的QQ号 |



#### Bot被挤下线

```json5
{
    "type": "BotOfflineEventForce",
    "qq": 123456
}
```

| 名字 | 类型 | 说明                |
| ---- | ---- | ------------------- |
| qq   | Long | 被挤下线的Bot的QQ号 |



#### Bot被服务器断开或因网络问题而掉线

```json5
{
    "type": "BotOfflineEventDropped",
    "qq": 123456
}
```

| 名字 | 类型 | 说明                                      |
| ---- | ---- | ----------------------------------------- |
| qq   | Long | 被服务器断开或因网络问题而掉线的Bot的QQ号 |



#### Bot主动重新登录.

```json5
{
    "type": "BotReloginEvent",
    "qq": 123456
}
```

| 名字 | 类型 | 说明                    |
| ---- | ---- | ----------------------- |
| qq   | Long | 主动重新登录的Bot的QQ号 |



#### Bot在群里的权限被改变. 操作人一定是群主

```json5
{
    "type": "BotGroupPermissionChangeEvent",
    "origin": "MEMBER",
    "new": "ADMINISTRATOR",
    "group": {
        "id": 123456789,
        "name": "Miral Technology",
        "permission": "ADMINISTRATOR"
    }
}
```

| 名字             | 类型   | 说明                                          |
| ---------------- | ------ | --------------------------------------------- |
| origin           | String | Bot的原权限，OWNER、ADMINISTRATOR或MEMBER     |
| new              | String | Bot的新权限，OWNER、ADMINISTRATOR或MEMBER     |
| group            | Object | 权限改变所在的群信息                          |
| group.id         | Long   | 群号                                          |
| group.name       | String | 群名                                          |
| group.permission | String | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |



#### Bot被禁言

```json5
{
    "type": "BotMuteEvent",
    "durationSeconds": 600,
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                      | 类型   | 说明                                             |
| ------------------------- | ------ | ------------------------------------------------ |
| durationSeconds           | Int    | 禁言时长，单位为秒                               |
| operator                  | Object | 操作的管理员或群主信息                           |
| operator.id               | Long   | 操作者的QQ号                                     |
| operator.memberName       | String | 操作者的群名片                                   |
| operator.permission       | String | 操作者在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| operator.group            | Object | Bot被禁言所在群的信息                            |
| operator.group.id         | Long   | 群号                                             |
| operator.group.name       | String | 群名                                             |
| operator.group.permission | String | Bot在群中的权限，OWNER或ADMINISTRATOR            |



#### Bot被取消禁言

```json5
{
    "type": "BotUnmuteEvent",
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                      | 类型   | 说明                                             |
| ------------------------- | ------ | ------------------------------------------------ |
| operator                  | Object | 操作的管理员或群主信息                           |
| operator.id               | Long   | 操作者的QQ号                                     |
| operator.memberName       | String | 操作者的群名片                                   |
| operator.permission       | String | 操作者在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| operator.group            | Object | Bot被取消禁言所在群的信息                        |
| operator.group.id         | Long   | 群号                                             |
| operator.group.name       | String | 群名                                             |
| operator.group.permission | String | Bot在群中的权限，OWNER或ADMINISTRATOR            |



#### Bot加入了一个新群

```json5
{
    "type": "BotJoinGroupEvent",
    "group": {
        "id": 123456789,
        "name": "Miral Technology",
        "permission": "MEMBER"
    }
}
```

| 名字             | 类型   | 说明                                                         |
| ---------------- | ------ | ------------------------------------------------------------ |
| group            | Object | Bot新加入群的信息                                            |
| group.id         | Long   | 群号                                                         |
| group.name       | String | 群名                                                         |
| group.permission | String | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER（新加入群通常是Member） |



#### 某个群名改变

```json5
{
    "type": "GroupNameChangeEvent",
    "origin": "miral technology",
    "new": "MIRAI TECHNOLOGY",
    "group": {
        "id": 123456789,
        "name": "MIRAI TECHNOLOGY",
        "permission": "MEMBER"
    },
    "isByBot": false
}
```

| 名字             | 类型    | 说明                                          |
| ---------------- | ------- | --------------------------------------------- |
| origin           | String  | 原群名                                        |
| new              | String  | 新群名                                        |
| group            | Object  | 群名改名的群信息                              |
| group.id         | Long    | 群号                                          |
| group.name       | String  | 群名                                          |
| group.permission | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| isByBot          | Boolean | 是否Bot进行该操作                             |



#### 某群入群公告改变

```json5
{
    "type": "GroupEntranceAnnouncementChangeEvent",
    "origin": "abc",
    "new": "cba",
    "group": {
        "id": 123456789,
        "name": "Miral Technology",
        "permission": "MEMBER"
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                | 类型    | 说明                                          |
| ------------------- | ------- | --------------------------------------------- |
| origin              | String  | 原公告                                        |
| new                 | String  | 新公告                                        |
| group               | Object  | 公告改变的群信息                              |
| group.id            | Long    | 群号                                          |
| group.name          | String  | 群名                                          |
| group.permission    | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| operator            | Object? | 操作的管理员或群主信息，当null时为Bot操作     |
| operator.id         | Long    | 操作者的QQ号                                  |
| operator.memberName | String  | 操作者的群名片                                |
| operator.permission | String  | 操作者在群中的权限，OWNER或ADMINISTRATOR      |
| operator.group      | Object  | 同group                                       |



#### 全员禁言

```json5
{
    "type": "GroupMuteAllEvent",
    "origin": false,
    "new": true,
    "group": {
        "id": 123456789,
        "name": "Miral Technology",
        "permission": "MEMBER"
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                | 类型    | 说明                                          |
| ------------------- | ------- | --------------------------------------------- |
| origin              | Boolean | 原本是否处于全员禁言                          |
| new                 | Boolean | 现在是否处于全员禁言                          |
| group               | Object  | 全员禁言的群信息                              |
| group.id            | Long    | 群号                                          |
| group.name          | String  | 群名                                          |
| group.permission    | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| operator            | Object? | 操作的管理员或群主信息，当null时为Bot操作     |
| operator.id         | Long    | 操作者的QQ号                                  |
| operator.memberName | String  | 操作者的群名片                                |
| operator.permission | String  | 操作者在群中的权限，OWNER或ADMINISTRATOR      |
| operator.group      | Object  | 同group                                       |



#### 匿名聊天

```json5
{
    "type": "GroupAllowAnonymousChatEvent",
    "origin": false,
    "new": true,
    "group": {
        "id": 123456789,
        "name": "Miral Technology",
        "permission": "MEMBER"
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                | 类型    | 说明                                          |
| ------------------- | ------- | --------------------------------------------- |
| origin              | Boolean | 原本匿名聊天是否开启                          |
| new                 | Boolean | 现在匿名聊天是否开启                          |
| group               | Object  | 匿名聊天状态改变的群信息                      |
| group.id            | Long    | 群号                                          |
| group.name          | String  | 群名                                          |
| group.permission    | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| operator            | Object? | 操作的管理员或群主信息，当null时为Bot操作     |
| operator.id         | Long    | 操作者的QQ号                                  |
| operator.memberName | String  | 操作者的群名片                                |
| operator.permission | String  | 操作者在群中的权限，OWNER或ADMINISTRATOR      |
| operator.group      | Object  | 同group                                       |



#### 坦白说

```json5
{
    "type": "GroupAllowConfessTalkEvent",
    "origin": false,
    "new": true,
    "group": {
        "id": 123456789,
        "name": "Miral Technology",
        "permission": "MEMBER"
    },
    "isByBot": false
}
```

| 名字             | 类型    | 说明                                          |
| ---------------- | ------- | --------------------------------------------- |
| origin           | Boolean | 原本坦白说是否开启                            |
| new              | Boolean | 现在坦白说是否开启                            |
| group            | Object  | 坦白说状态改变的群信息                        |
| group.id         | Long    | 群号                                          |
| group.name       | String  | 群名                                          |
| group.permission | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| isByBot          | Boolean | 是否Bot进行该操作                             |



#### 允许群员邀请好友加群

```json5
{
    "type": "GroupAllowMemberInviteEvent",
    "origin": false,
    "new": true,
    "group": {
        "id": 123456789,
        "name": "Miral Technology",
        "permission": "MEMBER"
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                | 类型    | 说明                                          |
| ------------------- | ------- | --------------------------------------------- |
| origin              | Boolean | 原本是否允许群员邀请好友加群                  |
| new                 | Boolean | 现在是否允许群员邀请好友加群                  |
| group               | Object  | 允许群员邀请好友加群状态改变的群信息          |
| group.id            | Long    | 群号                                          |
| group.name          | String  | 群名                                          |
| group.permission    | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| operator            | Object? | 操作的管理员或群主信息，当null时为Bot操作     |
| operator.id         | Long    | 操作者的QQ号                                  |
| operator.memberName | String  | 操作者的群名片                                |
| operator.permission | String  | 操作者在群中的权限，OWNER或ADMINISTRATOR      |
| operator.group      | Object  | 同group                                       |



#### 新人入群的事件

```json5
{
    "type": "MemberJoinEvent",
    "member": {
        "id": 123456789,
        "memberName": "我是新人",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型   | 说明                                                         |
| ----------------------- | ------ | ------------------------------------------------------------ |
| member                  | Object | 新人信息                                                     |
| member.id               | Long   | 新人的QQ号                                                   |
| member.memberName       | String | 新人的群名片                                                 |
| member.permission       | String | 新人在群中的权限，OWNER、ADMINISTRATOR或MEMBER（新入群通常是MEMBER） |
| member.group            | Object | 新人入群的群信息                                             |
| member.group.id         | Long   | 群号                                                         |
| member.group.name       | String | 群名                                                         |
| member.group.permission | String | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER                |



#### 成员被踢出群（该成员不是Bot）

```json5
{
    "type": "MemberLeaveEventKick",
    "member": {
        "id": 123456789,
        "memberName": "我是被踢的",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型    | 说明                                          |
| ----------------------- | ------- | --------------------------------------------- |
| member                  | Object  | 被踢者的信息                                  |
| member.id               | Long    | 被踢者的QQ号                                  |
| member.memberName       | String  | 被踢者的群名片                                |
| member.permission       | String  | 被踢者在群中的权限，ADMINISTRATOR或MEMBER     |
| member.group            | Object  | 被踢者所在的群                                |
| member.group.id         | Long    | 群号                                          |
| member.group.name       | String  | 群名                                          |
| member.group.permission | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| operator                | Object? | 操作的管理员或群主信息，当null时为Bot操作     |
| operator.id             | Long    | 操作者的QQ号                                  |
| operator.memberName     | String  | 操作者的群名片                                |
| operator.permission     | String  | 操作者在群中的权限，OWNER或ADMINISTRATOR      |
| operator.group          | Object  | 同member.group                                |



#### 成员主动离群（该成员不是Bot）

```json5
{
    "type": "MemberLeaveEventQuit",
    "member": {
        "id": 123456789,
        "memberName": "我是被踢的",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型   | 说明                                          |
| ----------------------- | ------ | --------------------------------------------- |
| member                  | Object | 退群群员的信息                                |
| member.id               | Long   | 退群群员的QQ号                                |
| member.memberName       | String | 退群群员的群名片                              |
| member.permission       | String | 退群群员在群中的权限，ADMINISTRATOR或MEMBER   |
| member.group            | Object | 退群群员所在的群信息                          |
| member.group.id         | Long   | 群号                                          |
| member.group.name       | String | 群名                                          |
| member.group.permission | String | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER |



#### 群名片改动

```json5
{
    "type": "MemberCardChangeEvent",
    "origin": "origin name",
    "new": "我是被改名的",
    "member": {
        "id": 123456789,
        "memberName": "我是被改名的",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员，也可能是我自己",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型    | 说明                                                     |
| ----------------------- | ------- | -------------------------------------------------------- |
| member                  | Object  | 名片改动的群员的信息                                     |
| member.id               | Long    | 名片改动的群员的QQ号                                     |
| member.memberName       | String  | 名片改动的群员的群名片                                   |
| member.permission       | String  | 名片改动的群员在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| member.group            | Object  | 名片改动的群员所在群的信息                               |
| member.group.id         | Long    | 群号                                                     |
| member.group.name       | String  | 群名                                                     |
| member.group.permission | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER            |
| operator                | Object? | 操作者的信息，可能为该群员自己，当null时为Bot操作        |
| operator.id             | Long    | 操作者的QQ号                                             |
| operator.memberName     | String  | 操作者的群名片                                           |
| operator.permission     | String  | 操作者在群中的权限，OWNER、ADMINISTRATOR或MEMBER         |
| operator.group          | Object  | 同member.group                                           |



#### 群头衔改动（只有群主有操作限权）

```json5
{
    "type": "MemberSpecialTitleChangeEvent",
    "origin": "origin title",
    "new": "new title",
    "member": {
        "id": 123456789,
        "memberName": "我是被改头衔的",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型   | 说明                                                     |
| ----------------------- | ------ | -------------------------------------------------------- |
| origin                  | String | 原头衔                                                   |
| new                     | String | 现头衔                                                   |
| member                  | Object | 头衔改动的群员的信息                                     |
| member.id               | Long   | 头衔改动的群员的QQ号                                     |
| member.memberName       | String | 头衔改动的群员的群名片                                   |
| member.permission       | String | 头衔改动的群员在群中的权限，OWNER、ADMINISTRATOR或MEMBER |
| member.group            | Object | 头衔改动的群员所在群的信息                               |
| member.group.id         | Long   | 群号                                                     |
| member.group.name       | String | 群名                                                     |
| member.group.permission | String | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER            |



#### 成员权限改变的事件（该成员不可能是Bot，见BotGroupPermissionChangeEvent）

```json5
{
    "type": "MemberPermissionChangeEvent",
    "origin": "MEMBER",
    "new": "ADMINISTRATOR",
    "member": {
        "id": 123456789,
        "memberName": "我是被改权限的",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型   | 说明                                              |
| ----------------------- | ------ | ------------------------------------------------- |
| origin                  | String | 原权限                                            |
| new                     | String | 现权限                                            |
| member                  | Object | 权限改动的群员的信息                              |
| member.id               | Long   | 权限改动的群员的QQ号                              |
| member.memberName       | String | 权限改动的群员的群名片                            |
| member.permission       | String | 权限改动的群员在群中的权限，ADMINISTRATOR或MEMBER |
| member.group            | Object | 权限改动的群员所在群的信息                        |
| member.group.id         | Long   | 群号                                              |
| member.group.name       | String | 群名                                              |
| member.group.permission | String | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER     |



#### 群成员被禁言事件（该成员不可能是Bot，见BotMuteEvent）

```json5
{
    "type": "MemberMuteEvent",
    "durationSeconds": 600,
    "member": {
        "id": 123456789,
        "memberName": "我是被禁言的",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型    | 说明                                            |
| ----------------------- | ------- | ----------------------------------------------- |
| durationSeconds         | Long    | 禁言时长，单位为秒                              |
| member                  | Object  | 被禁言的群员的信息                              |
| member.id               | Long    | 被禁言的群员的QQ号                              |
| member.memberName       | String  | 被禁言的群员的群名片                            |
| member.permission       | String  | 被禁言的群员在群中的权限，ADMINISTRATOR或MEMBER |
| member.group            | Object  | 被禁言的群员所在群的信息                        |
| member.group.id         | Long    | 群号                                            |
| member.group.name       | String  | 群名                                            |
| member.group.permission | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER   |
| operator                | Object? | 操作者的信息，当null时为Bot操作                 |
| operator.id             | Long    | 操作者的QQ号                                    |
| operator.memberName     | String  | 操作者的群名片                                  |
| operator.permission     | String  | 操作者在群中的权限，OWNER、ADMINISTRATOR        |
| operator.group          | Object  | 同member.group                                  |



#### 群成员被取消禁言事件（该成员不可能是Bot，见BotUnmuteEvent）

```json5
{
    "type": "MemberUnmuteEvent",
    "member": {
        "id": 123456789,
        "memberName": "我是被取消禁言的",
        "permission": "MEMBER",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    },
    "operator": {
        "id": 123456789,
        "memberName": "我是管理员",
        "permission": "ADMINISTRATOR",
        "group": {
            "id": 123456789,
            "name": "Miral Technology",
            "permission": "MEMBER"
        }
    }
}
```

| 名字                    | 类型    | 说明                                                |
| ----------------------- | ------- | --------------------------------------------------- |
| member                  | Object  | 被取消禁言的群员的信息                              |
| member.id               | Long    | 被取消禁言的群员的QQ号                              |
| member.memberName       | String  | 被取消禁言的群员的群名片                            |
| member.permission       | String  | 被取消禁言的群员在群中的权限，ADMINISTRATOR或MEMBER |
| member.group            | Object  | 被取消禁言的群员所在群的信息                        |
| member.group.id         | Long    | 群号                                                |
| member.group.name       | String  | 群名                                                |
| member.group.permission | String  | Bot在群中的权限，OWNER、ADMINISTRATOR或MEMBER       |
| operator                | Object? | 操作者的信息，当null时为Bot操作                     |
| operator.id             | Long    | 操作者的QQ号                                        |
| operator.memberName     | String  | 操作者的群名片                                      |
| operator.permission     | String  | 操作者在群中的权限，OWNER、ADMINISTRATOR            |
| operator.group          | Object  | 同member.group                                      |



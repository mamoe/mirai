# 事件

## 事件列表一览

提示:
- 在 IntelliJ 平台双击 shift 可输入类名进行全局搜索
- 在 IntelliJ 平台, 按 alt + 7 可打开文件的结构, [效果图](/.github/EZSLAB`K@YFFOW47{090W8B.png)

### [Bot](bot.kt)
- Bot 登录完成: BotOnlineEvent
- Bot 离线: BotOfflineEvent
  - 主动: Active
  - 被挤下线: Force
  - 被服务器断开或因网络问题而掉线: Dropped
  - 服务器主动要求更换另一个服务器: RequireReconnect
- Bot 重新登录: BotReloginEvent
- Bot 头像改变: BotAvatarChangedEvent

### [消息](message.kt)
- 主动发送消息: MessageSendEvent
  - 群消息: GroupMessageSendEvent
  - 好友消息: FriendMessageSendEvent
- 消息撤回: MessageRecallEvent
  - 好友撤回: FriendRecall
  - 群撤回: GroupRecall
- 图片上传前: BeforeImageUploadEvent
- 图片上传完成: ImageUploadEvent
  - 图片上传成功: Succeed
  - 图片上传失败: Failed

### [群](group.kt)
- 机器人被踢出群或在其他客户端主动退出一个群: BotLeaveEvent
  - 机器人主动退出一个群: Active
  - 机器人被管理员或群主踢出群: Kick
- 机器人在群里的权限被改变: BotGroupPermissionChangeEvent
- 机器人被禁言: BotMuteEvent
- 机器人被取消禁言: BotUnmuteEvent
- 机器人成功加入了一个新群: BotJoinGroupEvent

#### 群设置
- 群设置改变: GroupSettingChangeEvent
  - 群名改变: GroupNameChangeEvent
  - 入群公告改变: GroupEntranceAnnouncementChangeEvent
  - 全员禁言状态改变: GroupMuteAllEvent
  - 匿名聊天状态改变: GroupAllowAnonymousChatEvent
  - 坦白说状态改变: GroupAllowConfessTalkEvent
  - 允许群员邀请好友加群状态改变: GroupAllowMemberInviteEvent

#### 群成员
##### 成员列表变更
- 成员已经加入群: MemberJoinEvent
  - 成员被邀请加入群: Invite
  - 成员主动加入群: Active

- 成员已经离开群: MemberLeaveEvent
  - 成员被踢出群: Kick
  - 成员主动离开群: Quit

- 一个账号请求加入群: MemberJoinRequestEvent
- 机器人被邀请加入群: BotInvitedJoinGroupRequestEvent

##### 名片和头衔
- 成员群名片改动: MemberCardChangeEvent
- 成员群头衔改动: MemberSpecialTitleChangeEvent

##### 成员权限
- 成员权限改变: MemberPermissionChangeEvent

##### 禁言
- 群成员被禁言: MemberMuteEvent
- 群成员被取消禁言: MemberUnmuteEvent

### [好友](friend.kt)
- 好友昵称改变: FriendRemarkChangeEvent
- 成功添加了一个新好友: FriendAddEvent
- 好友已被删除: FriendDeleteEvent
- 一个账号请求添加机器人为好友: NewFriendRequestEvent
- 好友头像改变: FriendAvatarChangedEvent
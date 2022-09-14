/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.data

import net.mamoe.mirai.LowLevelApi

@LowLevelApi
public interface  ChannelInfo {
    /**
     * 子频道名称
     */
    public val name: String

    /**
     * 子频道ID
     */
    public val id: Long

    /**
     * 创建时间
     */
    public val createTime: Long

    /**
     * 子频道类型
     *
     * 0 文字子频道 | 1 保留不可用 | 2 语音子频道 | 3 保留不可用 | 4 子频道分组 | 10005 直播子频道 | 10006 应用子频道 | 10007 论坛子频道
     */
    public val channelType: Short

    /**
     * 只有文字子频道具有二级分类
     *
     * 0 闲聊 | 1 公告 | 2 攻略 | 3 开黑
     */
//    public val channelSubType: Int

    /**
     * 说话权限
     *
     * 0 无效类型 | 1 所有人 | 2 频道主 + 指定成员
     */
    public val talkPermission: Short

    /**
     * maybe PrivateType?
     *
     * [privateType](https://bot.q.qq.com/wiki/develop/api/openapi/channel/model.html#privatetype)
     */
    public val finalNotifyType: Short

    /**
     * 创建者Id
     */
    public val creatorTinyId: Long

    public val topMsg: TopMsg

    public val slowModeInfos: List<SlowModeInfosItem>

    public interface TopMsg {
        public val topMsgSeq: Long
        public val topMsgTime: Long
        public val topMsgOperatorTinyId: Long
    }

    /**
     * 慢速模式
     */
    public interface SlowModeInfosItem {
        public val slowModeKey: Long
        public val speakFrequency: Long
        public val slowModeCircle: Long
        public val slowModeText: String
    }
}
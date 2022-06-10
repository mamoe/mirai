/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package event;

import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Objects;

/**
 * 仅测试可以使用, 不会被编译运行
 */
public class EventChannelJavaTest {

    public static void main(String[] args) {
        GlobalEventChannel.INSTANCE
                .filter(event -> Objects.equals(event.toString(), "test"))
                .filterIsInstance(MessageEvent.class)
                .subscribeAlways(GroupMessageEvent.class, groupMessageEvent -> {

                });
    }
}

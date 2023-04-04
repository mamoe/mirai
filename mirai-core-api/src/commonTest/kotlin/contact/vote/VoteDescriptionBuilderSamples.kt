/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.contact.vote

import net.mamoe.mirai.contact.Group
import kotlin.time.Duration.Companion.days

class VoteDescriptionBuilderSamples {
    private val group: Group = magic()

    suspend fun simple() {
        val description = buildVoteDescription {
            title = "世界上最好的编程语言是什么?"
            isAnonymous = true // 设置为匿名
            duration(2.days) // 2 天后结束
            remind(1.days) // 1 天后提醒
            availableVotes(1) // 每人可投 1 票

            option("Java") // 添加一个选项
            option("Kotlin") // 添加一个选项
            options("C", "C++") // 添加多个选项
        }

        description.publishTo(group)
    }

    suspend fun simple2() {
        val desc = VoteDescriptionBuilder()
            .title("世界上最好的编程语言是什么?")
            .isAnonymous(true) // 设置为匿名
            .duration(2 * 24 * 3600) // 2 天后结束
            .remind(1 * 24 * 3600) // 1 天后提醒
            .availableVotes(1) // 每人可投 1 票
            .options("Java", "Kotlin", "C", "C++") // 添加多个选项
            .option("都不是") // 添加一个选项
            .build();

        desc.publishTo(group); // 发布到群
    }
}

private fun <T> magic(): T = throw NotImplementedError()
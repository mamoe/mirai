/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.utils

public class Symbol private constructor(name: String) {
    private val str = "Symbol($name)"
    override fun toString(): String = str

    public companion object {
        @Suppress("RedundantNullableReturnType")
        @JvmName("create")
        public operator fun invoke(name: String): Any? = Symbol(name) // calls constructor
    }
}
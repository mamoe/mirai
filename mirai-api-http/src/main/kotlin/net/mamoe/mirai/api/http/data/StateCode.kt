/*
 * Copyright 2020 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/master/LICENSE
 */

package net.mamoe.mirai.api.http.data

import kotlinx.serialization.Serializable
import net.mamoe.mirai.api.http.data.common.DTO

@Serializable
open class StateCode(val code: Int, var msg: String) : DTO {
    object Success : StateCode(0, "success") // 成功
    object NoBot : StateCode(2, "指定Bot不存在")
    object IllegalSession : StateCode(3, "Session失效或不存在")
    object NotVerifySession : StateCode(4, "Session未认证")
    object NoElement : StateCode(5, "指定对象不存在")
    object PermissionDenied : StateCode(10, "无操作权限")

    // KS bug: 主构造器中不能有非字段参数 https://github.com/Kotlin/kotlinx.serialization/issues/575
    @Serializable
    class IllegalAccess() : StateCode(400, "") { // 非法访问
        constructor(msg: String) : this() {
            this.msg = msg
        }
    }
}
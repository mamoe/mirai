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
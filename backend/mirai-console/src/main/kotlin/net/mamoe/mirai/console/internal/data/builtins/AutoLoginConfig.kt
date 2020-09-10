package net.mamoe.mirai.console.internal.data.builtins

import net.mamoe.mirai.console.data.AutoSavePluginConfig
import net.mamoe.mirai.console.data.ValueDescription
import net.mamoe.mirai.console.data.value
import net.mamoe.mirai.console.internal.util.md5
import net.mamoe.mirai.console.internal.util.toUHexString

internal object AutoLoginConfig : AutoSavePluginConfig() {
    override val saveName: String
        get() = "AutoLogin"

    @ValueDescription(
        """
        账号和明文密码列表
    """
    )
    val plainPasswords: MutableMap<Long, String> by value(mutableMapOf(123456654321L to "example"))


    @ValueDescription(
        """
        账号和 MD5 密码列表
    """
    )
    val md5Passwords: MutableMap<Long, String> by value(
        mutableMapOf(
            123456654321L to "example".toByteArray().md5().toUHexString()
        )
    )
}
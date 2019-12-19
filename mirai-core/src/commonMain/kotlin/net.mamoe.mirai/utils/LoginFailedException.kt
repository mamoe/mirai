package net.mamoe.mirai.utils

import net.mamoe.mirai.network.data.LoginResult

class LoginFailedException(
    val result: LoginResult,
    message: String = "Login failed with reason $result"
) : Exception(message)
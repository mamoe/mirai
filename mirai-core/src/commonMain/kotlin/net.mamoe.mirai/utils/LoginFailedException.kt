package net.mamoe.mirai.utils

import net.mamoe.mirai.data.LoginResult

class LoginFailedException(
    val result: LoginResult,
    message: String = "Login failed with reason $result"
) : Exception(message)
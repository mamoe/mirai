package net.mamoe.mirai.api.http.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthDTO(val authKey: String) : DTO

@Serializable
data class AuthResDTO(val code: Int, val session: String) : DTO

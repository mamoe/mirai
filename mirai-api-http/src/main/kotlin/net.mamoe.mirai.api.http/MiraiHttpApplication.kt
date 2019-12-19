package net.mamoe.mirai.api.http

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.routing.routing
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext
import io.ktor.util.pipeline.PipelineInterceptor
import net.mamoe.mirai.Bot
import net.mamoe.mirai.contact.sendMessage
import net.mamoe.mirai.utils.io.hexToBytes
import net.mamoe.mirai.utils.io.hexToUBytes

fun main() {
    Application(applicationEngineEnvironment {}).apply { mirai() }
}

@UseExperimental(ExperimentalUnsignedTypes::class)
fun Application.mirai() {
    install(DefaultHeaders)
    install(CallLogging)

    routing {
        mirai("/sendFriendMessage") {
            // TODO: 2019/11/21 解析图片消息等为 Message
            Bot.instanceWhose(qq = param("bot")).getQQ(param("qq")).sendMessage(param<String>("message"))
            call.ok()
        }

        mirai("/sendGroupMessage") {
            Bot.instanceWhose(qq = param("bot")).getGroup(param<Long>("group")).sendMessage(param<String>("message"))
            call.ok()
        }

        mirai("/event/message") {
            // TODO: 2019/11/21
            Bot.instanceWhose(qq = param("bot"))
        }

        mirai("/addFriend") {
            Bot.instanceWhose(qq = param("bot")).addFriend(
                id = param("qq"),
                message = paramOrNull<String?>("message")?.let { it },
                remark = paramOrNull<String?>("remark")?.let { it }
            )

            call.ok()
        }
    }
}


@ContextDsl
private fun Route.mirai(path: String, body: PipelineInterceptor<Unit, ApplicationCall>): Route {
    return route(path, HttpMethod.Get) {
        handle {
            try {
                this.body(this.subject)
            } catch (e: IllegalAccessException) {
                call.respond(HttpStatusCode.BadRequest, e.message)
            }
        }
    }
}

suspend inline fun ApplicationCall.ok() = this.respond(HttpStatusCode.OK, "OK")

/**
 * 错误请求. 抛出这个异常后将会返回错误给一个请求
 */
@Suppress("unused")
open class IllegalAccessException : Exception {
    override val message: String get() = super.message!!

    constructor(message: String) : super(message, null)
    constructor(cause: Throwable) : super(cause.toString(), cause)
    constructor(message: String, cause: Throwable?) : super(message, cause)
}

/**
 * 错误参数
 */
class IllegalParamException(message: String) : IllegalAccessException(message)

fun PipelineContext<Unit, ApplicationCall>.illegalParam(
    expectingType: String?,
    paramName: String,
    actualValue: String? = call.parameters[paramName]
): Nothing = throw IllegalParamException("Illegal param. A $expectingType is required for `$paramName` while `$actualValue` is given")

@Suppress("IMPLICIT_CAST_TO_ANY")
@UseExperimental(ExperimentalUnsignedTypes::class)
private inline fun <reified R> PipelineContext<Unit, ApplicationCall>.param(name: String): R = this.paramOrNull(name) ?: illegalParam(R::class.simpleName, name)

@Suppress("IMPLICIT_CAST_TO_ANY")
@UseExperimental(ExperimentalUnsignedTypes::class)
private inline fun <reified R> PipelineContext<Unit, ApplicationCall>.paramOrNull(name: String): R? =
    when (R::class) {
        Byte::class -> call.parameters[name]?.toByte()
        Int::class -> call.parameters[name]?.toInt()
        Short::class -> call.parameters[name]?.toShort()
        Float::class -> call.parameters[name]?.toFloat()
        Long::class -> call.parameters[name]?.toLong()
        Double::class -> call.parameters[name]?.toDouble()
        Boolean::class -> when (call.parameters[name]) {
            "true" -> true
            "false" -> false
            "0" -> false
            "1" -> true
            null -> null
            else -> illegalParam("boolean", name)
        }

        String::class -> call.parameters[name]

        UByte::class -> call.parameters[name]?.toUByte()
        UInt::class -> call.parameters[name]?.toUInt()
        UShort::class -> call.parameters[name]?.toUShort()

        UByteArray::class -> call.parameters[name]?.hexToUBytes()
        ByteArray::class -> call.parameters[name]?.hexToBytes()
        else -> error(name::class.simpleName + " is not supported")
    } as R?

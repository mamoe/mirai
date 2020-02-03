package net.mamoe.mirai.api.http.route

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.defaultTextContentType
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.route
import io.ktor.util.pipeline.ContextDsl
import io.ktor.util.pipeline.PipelineContext
import net.mamoe.mirai.api.http.AuthedSession
import net.mamoe.mirai.api.http.SessionManager
import net.mamoe.mirai.api.http.TempSession
import net.mamoe.mirai.api.http.dto.*

fun Application.mirai() {
    install(DefaultHeaders)
    install(CallLogging)

    authModule()
    messageModule()
}

/**
 * Auth，处理http server的验证
 * 为闭包传入一个AuthDTO对象
 */
@ContextDsl
internal fun Route.miraiAuth(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthDTO) -> Unit
): Route {
    return route(path, HttpMethod.Post) {
        intercept {
            val dto = context.receiveDTO<AuthDTO>() ?: throw IllegalParamException("参数格式错误")
            this.body(dto)
        }
    }
}

/**
 * Get，用于获取bot的属性
 * 验证请求参数中sessionKey参数的有效性
 */
@ContextDsl
internal fun Route.miraiGet(
    path: String,
    body: suspend PipelineContext<Unit, ApplicationCall>.(AuthedSession) -> Unit
): Route {
    return route(path, HttpMethod.Get) {
        intercept {
            val sessionKey = call.parameters["sessionKey"] ?: throw IllegalParamException("参数格式错误")
            if (!SessionManager.containSession(sessionKey)) throw IllegalSessionException

            when(val session = SessionManager[sessionKey]) {
                is TempSession -> throw NotVerifiedSessionException
                is AuthedSession -> this.body(session)
            }
        }
    }
}

/**
 * Verify，用于处理bot的行为请求
 * 验证数据传输对象(DTO)中是否包含sessionKey字段
 * 且验证sessionKey的有效性
 *
 * @param verifiedSessionKey 是否验证sessionKey是否被激活
 *
 * it 为json解析出的DTO对象
 */
@ContextDsl
internal inline fun <reified T : VerifyDTO> Route.miraiVerify(
    path: String,
    verifiedSessionKey: Boolean = true,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(T) -> Unit
): Route {
    return route(path, HttpMethod.Post) {
        intercept {
            val dto = context.receiveDTO<T>() ?: throw IllegalParamException("参数格式错误")
            SessionManager[dto.sessionKey]?.let {
                when {
                    it is TempSession && verifiedSessionKey -> throw NotVerifiedSessionException
                    it is AuthedSession -> dto.session = it
                }
            } ?: throw IllegalSessionException

            this.body(dto)
        }
    }
}

/**
 * 统一捕获并处理异常
 */
internal inline fun Route.intercept(crossinline blk: suspend PipelineContext<Unit, ApplicationCall>.() -> Unit) = handle {
    try {
       blk(this)
    } catch (e: IllegalSessionException) {
        call.respondDTO(StateCodeDTO.IllegalSession)
    } catch (e: NotVerifiedSessionException) {
        call.respondDTO(StateCodeDTO.NotVerifySession)
    } catch (e: NoSuchElementException) {
        call.respondDTO(StateCodeDTO.NoElement)
    } catch (e: IllegalAccessException) {
        call.respondDTO(StateCodeDTO.IllegalAccess(e.message), HttpStatusCode.BadRequest)
    }
}

/*
    extend function
 */
internal suspend inline fun <reified T : DTO> ApplicationCall.respondDTO(dto: T, status: HttpStatusCode = HttpStatusCode.OK)
        = respondJson(dto.toJson(), status)

internal suspend fun ApplicationCall.respondJson(json: String, status: HttpStatusCode = HttpStatusCode.OK) =
    respondText(json, defaultTextContentType(ContentType("application", "json")), status)

internal suspend inline fun <reified T : DTO> ApplicationCall.receiveDTO(): T? = receive<String>().jsonParseOrNull()




fun PipelineContext<Unit, ApplicationCall>.illegalParam(
    expectingType: String?,
    paramName: String,
    actualValue: String? = call.parameters[paramName]
): Nothing = throw IllegalParamException("Illegal param. A $expectingType is required for `$paramName` while `$actualValue` is given")

@Suppress("IMPLICIT_CAST_TO_ANY")
@UseExperimental(ExperimentalUnsignedTypes::class)
internal inline fun <reified R> PipelineContext<Unit, ApplicationCall>.paramOrNull(name: String): R =
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

        else -> error(name::class.simpleName + " is not supported")
    } as R ?: illegalParam(R::class.simpleName, name)


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
 * Session失效或不存在
 */
object IllegalSessionException : IllegalAccessException("Session失效或不存在")

/**
 * Session未激活
 */
object NotVerifiedSessionException : IllegalAccessException("Session未激活")

/**
 * 错误参数
 */
class IllegalParamException(message: String) : IllegalAccessException(message)


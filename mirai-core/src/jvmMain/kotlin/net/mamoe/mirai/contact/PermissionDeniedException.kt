package net.mamoe.mirai.contact

/**
 * 权限不足
 */ // 不要删除多平台结构
actual class PermissionDeniedException : IllegalStateException {
    actual constructor() : super("Permission denied")
    actual constructor(message: String?) : super(message)
}
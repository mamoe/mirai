package net.mamoe.mirai.contact

/**
 * 权限不足
 */
actual class PermissionDeniedException : IllegalStateException {
    actual constructor() : super("Permission denied")
    actual constructor(message: String?) : super(message)
}
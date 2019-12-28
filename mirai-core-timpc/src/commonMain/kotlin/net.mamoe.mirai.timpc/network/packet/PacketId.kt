@file:Suppress("EXPERIMENTAL_API_USAGE", "EXPERIMENTAL_UNSIGNED_LITERALS")

package net.mamoe.mirai.timpc.network.packet


import net.mamoe.mirai.utils.io.toUHexString


/**
 * 包 ID.
 */
interface PacketId {
    val value: UShort
    val factory: PacketFactory<*, *>
}

/**
 * 通过 [value] 匹配一个 [IgnoredPacketId] 或 [KnownPacketId], 无匹配则返回一个 [UnknownPacketId].
 */
fun matchPacketId(value: UShort): PacketId =
    IgnoredPacketIds.firstOrNull { it.value == value }
        ?: KnownPacketId.entries.firstOrNull { it.value.value == value }?.value
        ?: UnknownPacketId(value)


/**
 * 用于代表 `null`. 调用任何属性时都将会得到一个 [error]
 */
@Suppress("unused")
object NullPacketId : PacketId {
    override val factory: PacketFactory<*, *> get() = error("uninitialized")
    override val value: UShort get() = error("uninitialized")
    override fun toString(): String = "NullPacketId"
}

/**
 * 未知的 [PacketId]
 */
inline class UnknownPacketId(override inline val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = UnknownPacketFactory
    override fun toString(): String = "UnknownPacketId(${value.toUHexString()})"
}

object IgnoredPacketIds : List<IgnoredPacketId> by {
    listOf<UShort>(
    ).map { IgnoredPacketId(it.toUShort()) }
}()

inline class IgnoredPacketId constructor(override val value: UShort) : PacketId {
    override val factory: PacketFactory<*, *> get() = IgnoredPacketFactory
    override fun toString(): String = "IgnoredPacketId(${value.toUHexString()})"
}

class KnownPacketId(override val value: UShort, override val factory: PacketFactory<*, *>) :
    PacketId {
    companion object : MutableMap<UShort, KnownPacketId> by mutableMapOf() {
        operator fun set(key: UShort, factory: PacketFactory<*, *>) {
            this[key] = KnownPacketId(key, factory)
        }

        inline fun <reified PF : PacketFactory<*, *>> getOrNull(): KnownPacketId? {
            val clazz = PF::class
            this.forEach {
                if (clazz.isInstance(it.value)) {
                    return it.value
                }
            }
            return null
        }

        inline fun <reified PF : PacketFactory<*, *>> get(): KnownPacketId = getOrNull<PF>()
            ?: throw NoSuchElementException()
    }

    override fun toString(): String = (factory::class.simpleName ?: factory::class.simpleName) + "(${value.toUHexString()})"

    init {
        factory._id = this
    }
}
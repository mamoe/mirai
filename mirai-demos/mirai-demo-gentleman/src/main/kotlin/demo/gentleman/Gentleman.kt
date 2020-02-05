package demo.gentleman

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import net.mamoe.mirai.contact.Contact


/**
 * 最少缓存的图片数量
 */
private const val IMAGE_BUFFER_CAPACITY: Int = 5

/**
 * 为不同的联系人提供图片
 */
@ExperimentalUnsignedTypes
@ExperimentalCoroutinesApi
object Gentlemen : MutableMap<Long, Gentleman> by mutableMapOf() {
    fun provide(key: Contact, keyword: String = ""): Gentleman = this.getOrPut(key.id) { Gentleman(key, keyword) }
}

/**
 * 工作是缓存图片
 */
@ExperimentalCoroutinesApi
class Gentleman(private val contact: Contact, private val keyword: String) : Channel<GentleImage> by Channel(IMAGE_BUFFER_CAPACITY) {
    init {

        GlobalScope.launch {
            while (!isClosedForSend) {
                send(GentleImage(contact, keyword).apply {
                    seImage// start downloading
                })
            }
        }
    }
}
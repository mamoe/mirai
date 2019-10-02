package event

import net.mamoe.mirai.event.Event
import net.mamoe.mirai.event.broadcast
import net.mamoe.mirai.event.subscribeAll
import kotlin.reflect.KClass

/**
 * @author Him188moe
 */
open class SuperEvent : Event() {
    companion object : KClass<SuperEvent> by SuperEvent::class//方便 subscribe
}

open class ChildEvent : SuperEvent()

open class ChildChildEvent : ChildEvent()

class ChildChildChildEvent : ChildChildEvent()


suspend fun main() {
    SuperEvent.subscribeAll {
        always {
            println(it.javaClass.simpleName)//ChildChildChildEvent
        }
    }

    ChildChildChildEvent().broadcast()
}
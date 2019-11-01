package mirai.test

import kotlin.reflect.KProperty


class A {

    val valProp: Any = Any()
}

fun main() {
    A::class.members.filterIsInstance<KProperty<*>>().forEach {
        println(it.getter.call(A()))
    }
}
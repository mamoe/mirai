package test

import kotlin.reflect.KProperty


data class Info(
    var value: Int
) {
    operator fun getValue(c: C, property: KProperty<*>): Int {
        return value
    }
}

class C(var info: Info) {
    val value by info
}

fun main() {
    val info = Info(1)
    val c = C(info)
    println(c.value) //1
    info.value = 2
    println(c.value) //2
    c.info = Info(3)
    println(c.value) //2
}
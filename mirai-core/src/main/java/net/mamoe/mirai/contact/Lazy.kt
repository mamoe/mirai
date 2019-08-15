package net.mamoe.mirai.contact


fun Long.asQQ(): QQ = Instances.qqs.stream().filter { t: QQ? -> t?.number?.equals(this)!! }.findAny().orElse(QQ(this))!!

fun Long.asGroup(): Group = Instances.groups.stream().filter { t: Group? -> t?.number?.equals(this)!! }.findAny().orElse(Group(this))!!

fun String.withImage(id: String, type: String) = "{$id}.$type"

object Instances {
    var qqs = arrayListOf<QQ>()
    var groups = arrayListOf<Group>()
}

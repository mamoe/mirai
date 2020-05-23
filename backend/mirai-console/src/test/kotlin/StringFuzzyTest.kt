import net.mamoe.mirai.console.command.fuzzySearch
import net.mamoe.mirai.console.command.fuzzySearchOnly

class Him188(val name:String){
    override fun toString(): String {
        return name
    }
}

fun main(){
    val list = listOf(
        Him188("111122"),
        Him188("H1hsncm"),
        Him188("Ahsndb1"),
        Him188("Him188"),
        Him188("aisb11j2"),
        Him188("aisndnme"),
        Him188("a9su102"),
        Him188("nmsl"),
        Him188("Him1888"),
        Him188("Him18887")
    )
    val s1 = list.fuzzySearch("Him1888"){
        it.name
    }
    val s2 = list.fuzzySearchOnly("Him1888"){
        it.name
    }
    println("S1: $s1")
    println("S2: $s2")
}
import java.io.File

fun main() {


    val file = File("C:\\Users\\Him18\\Desktop\\lemon.png")
    println(file.inputStream().readAllBytes().size)
    println(file.length())
}
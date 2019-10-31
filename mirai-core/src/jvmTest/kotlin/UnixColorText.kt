fun main() {
    repeat(100) {
        println("\u001b[1;${it}m" + it)
    }
}
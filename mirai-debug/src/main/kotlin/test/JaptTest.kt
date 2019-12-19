package test

@Suppress("RedundantSuspendModifier")
suspend fun suspendPrintln(arg: String) = println(arg)

suspend fun main() {
    suspendPrintln("Hello")
    suspendPrintln(" World!")
}
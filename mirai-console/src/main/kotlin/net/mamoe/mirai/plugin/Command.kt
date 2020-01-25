package net.mamoe.mirai.plugin

object CommandManager {
    private val registeredCommand: MutableMap<String, Command> = mutableMapOf()


    fun register(command: Command) {
        val allNames = mutableListOf<String>(command.name).also { it.addAll(command.alias) }
        allNames.forEach {
            if (registeredCommand.containsKey(it)) {
                error("Command Name(or Alias) $it is already registered, consider if same function plugin was installed")
            }
        }
        allNames.forEach {
            registeredCommand[it] = command
        }
    }


}

abstract class Command(
    val name: String,
    val alias: List<String> = listOf()
) {
    /**
     * 最高优先级监听器
     * 如果return [false] 这次指令不会被[PluginBase]的全局onCommand监听器监听
     * */
    fun onCommand(args: List<String>): Boolean {
        return true
    }
}

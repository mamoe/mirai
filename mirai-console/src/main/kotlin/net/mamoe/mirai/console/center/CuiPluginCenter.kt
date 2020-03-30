package net.mamoe.mirai.console.center

object CuiPluginCenter: PluginCenter{
    override suspend fun fetchPlugin(page: Int): Map<String, PluginCenter.PluginInsight> {
        TODO("Not yet implemented")
    }

    override suspend fun findPlugin(name: String): PluginCenter.PluginInfo? {
        TODO("Not yet implemented")
    }

}
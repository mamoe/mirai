# Mirai Console
你可以在全平台运行Mirai高效率机器人框架
### Mirai Console提供了5个版本以满足各种需要
#### 所有版本的Mirai Console API相同 插件系统相同

|  名字    | 介绍 |
| --- | --- |
| Mirai-Console-Pure  |  最纯净版, CLI环境, 通过标准输入与标准输出 交互 |
| Mirai-Console-Terminal  |  (UNIX)Terminal环境 提供简介好用的富文本控制台 |
| Mirai-Console-Android   |  安卓APP (TODO) |
| Mirai-Console-Graphical  |  JavaFX的图形化界面, 有Native版本(.jar/.exe/.dmg) |
| Mirai-Console-WebPanel  |   Web Panel操作(TODO) |



### 如何选择版本
1:  Mirai-Console-Pure 兼容性最高, 在其他都表现不佳的时候请使用</br>
2:  以系统区分
```kotlin
    return when(operatingSystem){
        WINDOWS -> listOf("Graphical","WebPanel","Pure")
        MAC_OS  -> listOf("Graphical","Terminal","WebPanel","Pure") 
        LINUX   -> listOf("Terminal","Pure")
        ANDROID -> listOf("Android","Pure","WebPanel") 
        else    -> listOf("Pure") 
    }      
```
3: 以策略区分
```kotlin
    return when(task){
        体验         -> listOf("Graphical","Terminal","WebPanel","Android","Pure")
        测试插件      -> listOf("Pure") 
        调试插件      -> byOperatingSystem() 
        UNIX稳定挂机 -> listOf("Terminal","Pure") 
        else         -> listOf("Pure") 
    }      
```


#### More Importantly, Mirai Console support <b>Plugins</b>, tells the bot what to do
#### Mirai Console 支持插件系统, 你可以自己开发或使用公开的插件来逻辑化机器人, 如群管
<br>

#### download 下载
#### how to get/write plugins 如何获取/写插件
<br>
<br>

### how to use(如何使用)
#### how to run Mirai Console
<ul>
    <li>download mirai-console.jar</li>
    <li>open command line/terminal</li>
    <li>create a folder and put mirai-console.jar in</li>
    <li>cd that folder</li>
    <li>"java -jar mirai-console.jar"</li>
</ul>

<ul>
    <li>下载mirai-console.jar</li>
    <li>打开终端</li>
    <li>在任何地方创建一个文件夹, 并放入mirai-console.jar</li>
    <li>在终端中打开该文件夹"cd"</li>
    <li>输入"java -jar mirai-console.jar"</li>
</ul>

#### how to add plugins
<ul>
    <li>After first time of running mirai console</li>
    <li>/plugins/folder will be created next to mirai-console.jar</li>
    <li>put plugin(.jar) into /plugins/</li>
    <li>restart mirai console</li>
    <li>checking logger and check if the plugin is loaded successfully</li>
    <li>if the plugin has it own Config file, it normally appears in /plugins/{pluginName}/</li>
</ul>

<ul>
    <li>在首次运行mirai console后</li>
    <li>mirai-console.jar 的同级会出现/plugins/文件夹</li>
    <li>将插件(.jar)放入/plugins/文件夹</li>
    <li>重启mirai console</li>
    <li>在开启后检查日志, 是否成功加载</li>
    <li>如该插件有配置文件, 配置文件一般会创建在/plugins/插件名字/ 文件夹下</li>
</ul>



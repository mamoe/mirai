# 插件结构

请注意, 如果你有IDEA，推荐使用idea插件, 可以全自动配置环境, 插件结构, debug运行<br>
本文是为没有IDEA, 也想开发mirai-console插件的人准备的, 除去插件结构外, 您还需要自己配置运行环境<br>


### Plugin.yml
你应当有一个plugin.yml, 放置在resources文件夹下<br>
```
name: "插件名字"
author: "作者名字"
version: "0.1.0"
main: "my_package_name.ExamplePluginBase"
info: "插件介绍"
depends: []
```
其中main指向 你的PluginBase

### PluginBase
pluginBase为你插件的启动点, 生命管理周期, 他应该被放到src/main/java或src/main/kotlin下<br>
一般来说, 他要被放到一个package下, 假如它叫MyPluginBase, 放到package为my.package下, 那么plugin.yml的main则要写my.package.MyPluginBase<br>
你可以在下面的DEMO中找到PluginBase的一些简单例子
   
<br><br>    
## DEMO
[插件结构例子](https://github.com/mamoe/mirai-console/tree/master/PluginDocs/demo)
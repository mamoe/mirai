# Mirai - Questions

## 用户常见问题

> 'java' 不是内部或外部命令，也不是可运行的程序

没有安装 Java。

> `Failed to fetch announcement for ...`

[MCL](https://github.com/iTXTech/mirai-console-loader) 查询更新信息失败，可以尝试编辑 `config.json`，
更换 [mirai repo](https://github.com/project-mirai/mirai-repo-mirror#%E4%BB%93%E5%BA%93%E9%95%9C%E5%83%8F)。

> Login failed: Error(bot=..., code=..., title=....

这些是服务器返回的信息，它表示你的账号被登录风控了。  
风控没有 100% 稳定的解决方法，你可以关注 [论坛公告帖 - 无法登录的临时处理方案](https://mirai.mamoe.net/topic/223)。  
密码登录中是否出现滑块验证或短信验证都是不可控的，这取决于腾讯的服务器要求你完成什么验证，没有办法自由选择。

> 登录协议如何修改

对于手动密码登录，第三个参数就是协议, 例如 `login 12345 114514 MACOS`  

对于自动密码登录，可以使用指令修改，例如 `autoLogin setConfig 12345 protocol MACOS`，  
也可以在 Mirai Console 关闭的情况下, 编辑 `config/Console/AutoLogin.yml` 文件。

注意，文件中有一个账号为 `12345` 的示例，请注意确认修改的配置对应的账号，不要修改错了示例。

> 聊天框无法使用指令(使用指令后没效果)

1. 确认机器人收到消息  
   日志里会有消息记录，没有就是没收到，如果是群聊消息，注意是否已 `收入群助手`，这可能会导致收不到消息

2. 确认是否已经安装 [chat-command](https://github.com/project-mirai/chat-command/releases/latest)  
   如果插件的指令是对接的 `Mirai Console` 的指令接口，那它就需要 `chat-command`

3. 确认是否已经授权给目标用户  
   默认情况下所有用户都是没有权限的，聊天框下无法使用指令  
   备注：如果使用了 LuckPerms-Mirai，可通过在控制台执行 /lp verbose on 查看权限检测情况

4. 确认日志中没有相关报错  
   如果插件指令执行出错，也有可能无法提供回复，请联系插件作者

> 如何确认 `Mirai 版本` 或 `插件版本` 等信息

可以启动 Mirai Console 的情况下:  
使用指令 `/status` 。

无法启动的情况下:  
Mirai Console 的组件在 `libs` 文件夹下, 文件名包含 `版本信息`。  

> 找不到 `http api` 的相关配置文件

可能需要安装插件 <https://github.com/project-mirai/mirai-api-http>。

> 如何添加 jvm 参数，例如 `-Dmirai.no-desktop=true`

编辑启动脚本 `mcl.cmd`，在 `-jar` 前面加上 `-D...`，例如 `-Dmirai.no-desktop=true -jar mcl...`。

Linux 和 macOS 的启动脚本是 `mcl` (没有后缀的那个文件)。

> MCL 不能更新到最新版 Mirai

编辑 `config.json`  
修改 `maven_repo` 为 `https://repo.huaweicloud.com/repository/maven` (默认是阿里云，更新很不及时)  
将 `packages` 中 `net.mamoe:` 开头的项的 `channel` 改为 `maven` (最新版) 或 `maven-stable` (最新稳定版)  

## 开发者常见问题

> 如何自定义登录验证处理

扫码登陆，滑块处理等  

[覆盖登录解决器](Bots.md#覆盖登录解决器)

> IDEA 下 `import` 爆红，mirai 相关依赖全部无法解析

IDEA 版本过于老旧，无法分析新版本的 Kotlin 依赖，请尝试升级 IDEA 后重试。

> 有些事件收到不到

`SignEvent` 或者 `NudgeEvent`：

总的来说 `MACOS` 和 `ANDROID_WATCH` 相对其他协议会缺少一些事件的接收。  
对于这些协议来说是不会收到的，因为这在他们对应官方客户端版本里本来就没有这些功能。

`GroupMessageEvent`：

群事件收不到可能是因为你将群设置为 `屏蔽群` 或者 `收入了群助手`。  
另外如果消息是 `转发消息` 或 `卡片消息` 等特殊消息, 也有可能因为风控无法接收和发出。

> 发送语音之后播放没有声音

你可能需要安装插件或引入依赖 <https://github.com/project-mirai/mirai-silk-converter>。

> Mirai Console 插件找不到类

1. 使用了私有的第三方库  
   你需要使用 `shadowLink` 
   参照 [打包依赖](../mirai-console/tools/gradle-plugin/README.md#打包依赖)

2. 使用 数据库框架/反射框架 但找不到 驱动类/实体类  
   你需要将**上下文**切换到插件中，或者指定 `ClassLoader`，  
   相关内容请参考 [JVM Plugins - Debug](../mirai-console/docs/plugin/JVMPlugin-Debug.md)  
   下面只列出部分例子，请根据你所使用的框架的文档进行修整。  
   切换上下文：  
   ```java
   import java.util.ServiceLoader;
   
   public class SQL {
       void load() {
           Thread current = Thread.currentThread();
           ClassLoader context = current.getContextClassLoader();
           ClassLoader plugin = this.getClass().getClassLoader();
           try {
               current.setContextClassLoader(plugin);
               ServiceLoader.load(java.sql.Driver.class).reload();
   
               // database load ...
   
           } finally {
               current.setContextClassLoader(context);
           }
       }
   }
   ```
   指定 `ClassLoader`  
   ```java
   import jakarta.persistence.Embeddable;
   import jakarta.persistence.Entity;
   import jakarta.persistence.MappedSuperclass;
   import org.reflections.Reflections;
   import org.reflections.util.ConfigurationBuilder;

   import java.util.Set;
   
   public class SQL {
       Set<Class<?>> load() {
           ClassLoader plugin = this.getClass().getClassLoader();
           ConfigurationBuilder builder = new ConfigurationBuilder()
                   .forPackage("org.example", plugin)
                   .addClassLoaders(plugin); // 指定从插件的类加载器中检索类
           Reflections reflections = new Reflections(builder);
           Set<Class<?>> query = org.reflections.scanners.Scanners.TypesAnnotated
                   .of(Entity.class, Embeddable.class, MappedSuperclass.class)
                   .asClass(plugin) // 指定从插件的类加载器中提取类
                   .apply(reflections.getStore());
           return query;
       }
   }
   ```
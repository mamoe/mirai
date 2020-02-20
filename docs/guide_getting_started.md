# Mirai Guide - Getting Started

由于Mirai项目在快速推进中，因此内容时有变动，本文档的最后更新日期为```2020-02-20```，对应版本```0.16.0```

假如仅仅使用Mirai，不需要对整个项目进行Clone，只需在项目内添加Gradle Dependency或使用即可。

下面介绍详细的入门步骤。

## With console

使用mirai-console，以插件形式对服务器功能进行管理，启动无需任何IDE。

**由于mirai-console还没有开发完成，暂时不提供入门**

## With loader

通过编写Kotlin程序启动mirai-core，并定义你的Mirai Bot行为。

假如已经对Gradle有一定了解，可跳过1，2

### 1 安装IDEA与JDK

JDK要求8以上

### 2 新建Gradle项目

- 在```File->new project```中选择```Gradle```
- 在面板中的```Additional Libraries and Frameworks```中勾选```Java```以及```Kotlin/JVM```
- 点击```next```，填入```GroupId```与```ArtifactId```(对于测试项目来说，可随意填写)
- 点击```next```，点击```Use default gradle wrapper(recommended)```
- 创建项目完成

### 3 添加依赖

- 打开项目的```Project```面板，点击编辑```build.gradle```

- 首先添加repositories

  ```
  //添加jcenter仓库
  /*
  repositories {
      mavenCentral()
  }
  原文内容，更新为下文
  */
  
  repositories {
      mavenCentral()
      jcenter()
  }
  ```

- 添加依赖，将dependencies部分覆盖为

  ```
  dependencies {
      implementation 'net.mamoe:mirai-core:0.16.0'
      implementation 'net.mamoe:mirai-core-qqandroid-jvm:0.16.0'
      implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
      testCompile group: 'junit', name: 'junit', version: '4.12'
  }
  ```

- 打开右侧Gradle面板，点击刷新按钮
- 至此，依赖添加完成

### 4 Try Bot

- 在src/main文件夹下新建文件夹，命名为```kotlin```
- 在```kotlin```下新建包(在```kotlin```文件夹上右键-```New```-```Packages```) 包名为```net.mamoe.mirai.simpleloader```

- 在包下新建kotlin文件```MyLoader.kt```

  ```
  package net.mamoe.mirai.simpleloader
  
  import kotlinx.coroutines.*
  import net.mamoe.mirai.Bot
  import net.mamoe.mirai.alsoLogin
  import net.mamoe.mirai.event.subscribeMessages
  
  fun main(args: Array<String>) {
      runBlocking {//阻塞，直到Mirai退出
          coroutineScope() {
              launch {
                  val qqId = 10000L//Bot的QQ号，需为Long类型，在结尾处添加大写L
                  val password = "your_password"//Bot的密码
                  val miraiBot = Bot(qqId, password).alsoLogin()//新建Bot并登陆
                  miraiBot.subscribeMessages {
                      "你好" reply "你好!"
                      "profile" reply { sender.queryProfile() }
                      case("at me") {
                          reply(sender.at() + " 给爷爬 ")
                      }
  
                      (contains("舔") or contains("刘老板")) {
                          "刘老板太强了".reply()
                      }
                  }
                  miraiBot.join()
              }
          }
      }
  }
  ```

- 单击编辑器内第8行(```fun main```)左侧的run按钮(绿色三角)，等待，MiraiBot成功登录。
- 本例的功能中，在任意群内任意成员发送包含“舔”字或“刘老板”字样的消息，MiraiBot会回复“刘老板太强了”



至此，简单的入门已经结束，下面可根据不同的需求参阅文档进行功能的添加。

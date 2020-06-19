# Mirai Guide - Getting Started

由于Mirai项目在快速推进中，因此内容时有变动，本文档的最后更新日期为```2020-04-01```，对应版本```0.31.4```

假如仅仅使用Mirai，不需要对整个项目进行Clone，只需在项目内添加Gradle Dependency或使用即可。

下面介绍详细的入门步骤。

本页采用Kotlin作为开发语言，**若你希望使用 Java 开发**, 请参阅: [mirai-japt](https://github.com/mamoe/mirai-japt)



## 起步步骤
通过编写Kotlin程序，以第三方库的形式调用```mirai-core```，并定义你的Mirai Bot行为。

假如已经对Gradle有一定了解，可跳过1，2

### 1 安装IDEA与JDK

- JDK 要求6以上

### 2 新建Gradle项目

*使用gradle项目可能需要代理，在IDEA的```settings```->```proxy settings```中可以设置

- 在```File->new project```中选择```Gradle```
- 在面板中的```Additional Libraries and Frameworks```中勾选```Java```以及```Kotlin/JVM```
- 点击```next```，填入```GroupId```与```ArtifactId```(对于测试项目来说，可随意填写)
- 点击```next```，点击```Use default gradle wrapper(recommended)```
- 创建项目完成

### 3 添加依赖

- 打开项目的```Project```面板，点击编辑```build.gradle```

- 首先添加repositories

  ```groovy
  //添加jcenter仓库
  /*
  repositories {
      maven { url 'https://dl.bintray.com/kotlin/kotlin-eap' }
      mavenCentral()
  }
  原文内容，更新为下文
  */
  
  repositories {
      maven { url 'https://dl.bintray.com/kotlin/kotlin-eap' }
      mavenCentral()
      jcenter()
  }
  ```

- 添加依赖，将dependencies部分覆盖。 `mirai-core` 的最新版本为: [![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  

  ```groovy
  dependencies {
      implementation 'net.mamoe:mirai-core-qqandroid:1.1-EA'//此处版本应替换为当前最新
      implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
      testCompile group: 'junit', name: 'junit', version: '4.12'
  }
  ```

- 打开右侧Gradle面板，点击刷新按钮
- 至此，依赖添加完成

### 4 Try Bot

- 在src/main文件夹下新建文件夹，命名为```kotlin```
- 在```kotlin```下新建包(在```kotlin```文件夹上右键-```New```-```Package```) 包名为```net.mamoe.mirai.simpleloader```

- 在包下新建kotlin文件```MyLoader.kt```

```kotlin
package net.mamoe.mirai.simpleloader

import kotlinx.coroutines.*
import net.mamoe.mirai.Bot
import net.mamoe.mirai.alsoLogin
import net.mamoe.mirai.join
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.subscribeMessages

suspend fun main() {
    val qqId = 10000L//Bot的QQ号，需为Long类型，在结尾处添加大写L
    val password = "your_password"//Bot的密码
    val miraiBot = Bot(qqId, password).alsoLogin()//新建Bot并登录
    miraiBot.subscribeMessages {
        "你好" reply "你好!"
        case("at me") {
            reply(At(sender as Member) + " 给爷爬 ")
        }

        (contains("舔") or contains("刘老板")) {
            reply("刘老板太强了")
        }
    }
    miraiBot.join() // 等待 Bot 离线, 避免主线程退出
}
```

- 单击编辑器内第8行(```suspend fun main```)左侧的run按钮(绿色三角)，等待，MiraiBot成功登录。
- 本例的功能中，在任意群内任意成员发送包含“舔”字或“刘老板”字样的消息，MiraiBot会回复“刘老板太强了”


至此，简单的入门已经结束，下面可根据不同的需求参阅wiki进行功能的添加。

下面，可以尝试对不同事件进行监听[Mirai Guide - Subscribe Events](/docs/guide_subscribe_events.md)

### 此外，还可以使用Maven作为包管理工具
本项目推荐使用gradle，因此不提供详细入门指导

```xml
<repositories>
    <repository>
        <id>jcenter</id>
        <url>https://jcenter.bintray.com/</url>
    </repository>
</repositories>
```
```xml
<dependencies>
    <dependency>
        <groupId>net.mamoe</groupId>
        <artifactId>mirai-core-qqandroid</artifactId>
        <version>0.23.0</version> <!-- 替换版本为最新版本 -->
    </dependency>
</dependencies>
```

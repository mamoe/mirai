# Mirai
[![Gitter](https://badges.gitter.im/mamoe/mirai.svg)](https://gitter.im/mamoe/mirai?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge)
[![Actions Status](https://github.com/mamoe/mirai/workflows/CI/badge.svg)](https://github.com/mamoe/mirai/actions)
[![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)  

Coroutine-based open-source multiplatform library of QQ protocol.  
Some of the protocol came from the other open-source projects.  

**The development is only for learning, DO NOT use it for illegal purposes.**

## Changelog

You can inspect supported protocols at [Project](https://github.com/mamoe/mirai/projects/1)  
and logs of updates at [CHANGELOG](https://github.com/mamoe/mirai/blob/master/CHANGELOG.md)

## Use as a library
You can install mirai as a library into your project.

Mirai is only published on `jcenter`, therefore please ensure you have the `jcenter()` repository in your `build.gradle`.

```kotlin
repositories{
  jcenter()
}
```

If your project is a multiplatform project, you should add dependencies for each platform respectively.  
If your project is not a multiplatform project, you just need to add the platform-specific dependency.  

`VERSION` should be replaced with the newest version, say [![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/) 

Mirai is still under experimental stage, it is suggested to keep the version newest.

**common**
```kotlin
implementation("net.mamoe:mirai-core-common:VERSION")
```
**jvm**
```kotlin
implementation("net.mamoe:mirai-core-jvm:VERSION")
```
**android**
```kotlin
implementation("net.mamoe:mirai-core-android:VERSION")
```

## Try

### On JVM or Android

Mirai is now available to work.

```kotlin
val bot = Bot(qqId, password).alsoLogin()
bot.subscribeMessages {
  "Hello" reply "World!"
  "profile" reply { sender.queryProfile() }
  contains("img"){ File(imagePath).send() }
}
bot.subscribeAlways<MemberPermissionChangedEvent> {
  if (it.kind == BECOME_OPERATOR)
    reply("${it.member.id} has become a operator")
}
```

1. Clone this GitHub project
2. Import as Gradle project
3. Run demo main functions: [mirai-demo](#mirai-demo)

## Contribution
Any kinds of contribution is welcomed. If you hold a interest in helping us implementing Mirai on JS, iOS or Native platforms, please email me `Him188@mamoe.net`
If you meet any problem or have any questions, be free to open a issue. Our goal is to make Mirai easy to use.

## Requirements

Kotlin 1.3.61  

On JVM: Java 6  

On Android: SDK 15

### Using java
Q: Can I use Mirai without Kotlin?  

A: Calling from java is not yet supported. Coroutines, extensions and inlines, which are difficult to use from Java, are generally used in Mirai. Therefore you should have the skill of Kotlin before you use Mirai.

## Acknowledgements

Thanks to [JetBrains](https://www.jetbrains.com/?from=mirai) for allocating free open-source licences for IDEs such as [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai).  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai)

### Third Party Libraries

- [kotlin-stdlib](https://github.com/JetBrains/kotlin)
- [kotlinx-coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [kotlinx-io](https://github.com/Kotlin/kotlinx-io)
- [kotlin-reflect](https://github.com/JetBrains/kotlin)
- [pcap4j](https://github.com/kaitoy/pcap4j)
- [atomicfu](https://github.com/Kotlin/kotlinx.atomicfu)
- [ktor](https://github.com/ktorio/ktor)
- [klock](https://github.com/korlibs/klock)
- [tornadofx](https://github.com/edvin/tornadofx)
- [javafx](https://github.com/openjdk/jfx)
- [kotlinx-serialization](https://github.com/Kotlin/kotlinx.serialization)

## License

    Copyright (C) 2019-2020 mamoe and Mirai contributors

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

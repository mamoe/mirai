# Mirai
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/7d0ec3ea244b424f93a6f59038a9deeb)](https://www.codacy.com/manual/Him188/mirai?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=mamoe/mirai&amp;utm_campaign=Badge_Grade)  

Mirai is a high-performance multi-platform library, as well as a framework, providing protocol support for Tencent QQ.

Mirai is designed to handle all sorts of messaging works that can be automatically done by bots **in a perfect way**.

> Tencent QQ: A modern messaging software used by all Chinese netizens.

## Start
**Development document**： [docs](docs)

### Use as a framework

Mirai is able to run as a plugin-supported framework.  
The community, (with `mirai-console`) that allows developers to share their plugins, and for users to install plugins quickly, is building in progress.

- JVM languages like `Java` or `Kotlin`： Make Jar plugin for [mirai-console](https://github.com/mamoe/mirai-console) directly and share with other developers through the plugin center.
- `Kotlin Script`： [mirai-kts](https://github.com/iTXTech/mirai-kts) supports plugins using Kotlin Scripts (`kts`)（**OpenJDK 8+ only，except Android**）
- Native languages like `C`, `C++`： [mirai-native](https://github.com/iTXTech/mirai-native) supports plugins from CoolQ **(`Windows JREx86` only / with `Wine`)**
- `JavaScript`： [mirai-js](https://github.com/iTXTech/mirai-js) supports plugins using `JavaScript` and inter-operate with **mirai** on JVM directly.
- Any language：Use HTTP API from [mirai-api-http](https://github.com/mamoe/mirai-api-http)

**Though only Jar plugins are supported officially, language bridges that are maintained by the community can connect with your knowledge.**:

- `Python`: [python-mirai](https://github.com/NatriumLab/python-mirai) A Bot framework based on `mirai-api-http`.
- `JavaScript`(`Node.js`): [node-mirai](https://github.com/RedBeanN/node-mirai) The Node.js SDK for mirai.
- `Go`: [gomirai](https://github.com/Logiase/gomirai) The GoLang SDK for mirai.
- `Mozilla Rhino`: [mirai-rhinojs-sdk](https://github.com/StageGuard/mirai-rhinojs-sdk) The Mozilla Rhino (JavaScript) SDK for mirai.
- `Lua`: [lua-mirai](https://github.com/only52607/lua-mirai) The Lua SDK for mirai-core, supporting Java extensions that act as a bridge between Java and native Lua.
- `C++`: [mirai-cpp](https://github.com/cyanray/mirai-cpp) A simple C++ SDK using `mirai-api-http` for ALL platforms.
- `C++`: [miraipp](https://github.com/Chlorie/miraipp-template) A sophisticated, modern mapping for `mirai-http-api` to C++, providing development documents.
- `Rust`: [mirai-rs](https://github.com/HoshinoTented/mirai-rs) The Rust mapping for `mirai-http-api`.
- `TypeScript`: [mirai-ts](https://github.com/YunYouJun/mirai-ts) TypeScript SDK comes with a declaration file, has good code hints, and can also be used as a JavaScript SDK.

### Use as a library
You can install mirai as a library into your project.

#### Import with Gradle

Mirai is only published on `jcenter`, therefore please ensure you have the `jcenter()` repository added in your `build.gradle`.

```kotlin
repositories{
  jcenter()
}
```

Then add dependency to `dependencies` block, following:  
If your project is a multiplatform project, you need to add dependencies for each platform respectively.  
If your project is not a multiplatform project, add the platform-specific dependency only.

Replace `VERSION` with the newest version, say [![Download](https://api.bintray.com/packages/him188moe/mirai/mirai-core/images/download.svg)](https://bintray.com/him188moe/mirai/mirai-core/)

**jvm**
```kotlin
implementation("net.mamoe:mirai-core:VERSION")
```
**common**
```kotlin
implementation("net.mamoe:mirai-core-common:VERSION")
```
**android**
```kotlin
implementation("net.mamoe:mirai-core-android:VERSION")
```

#### Import with Maven

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
        <version>0.23.0</version> <!-- Replace to the newest -->
    </dependency>
</dependencies>
```

## Contribution
**All kinds of contributions are welcomed.**  
If you hold an interest in helping us implementing Mirai on JS, iOS or Native platforms, please email us `support@mamoe.net`.  
If you meet any problem or have any questions, feel free to file an issue. Our goal is to make Mirai easy to use.

## Acknowledgements

Thanks to [JetBrains](https://www.jetbrains.com/?from=mirai) for allocating free open-source licences for IDEs such as [IntelliJ IDEA](https://www.jetbrains.com/idea/?from=mirai).  
[<img src=".github/jetbrains-variant-3.png" width="200"/>](https://www.jetbrains.com/?from=mirai)

## License

    Copyright (C) 2019-2020 Mamoe Technologies and mirai contributors

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

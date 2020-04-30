# Mirai Guide - Build For Mirai

由于Mirai项目在快速推进中，因此内容时有变动，本文档的最后更新日期为```2020-02-29```，对应版本```0.23.0```

本页面采用Kotlin作为开发语言，**若你希望使用 Java 开发**, 请参阅: [mirai-japt](https://github.com/mamoe/mirai-japt)

本页面是[Mirai Guide - Subscribe Events](/docs/guide_subscribe_events.md)的后续Guide

## build.gradle

我们首先来看一下完整的```build.gradle```文件

```groovy
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.3.61'
}

group 'test'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation 'net.mamoe:mirai-core-qqandroid:0.19.1'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}

compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
```

使用gradle直接打包，不会将依赖也打包进去

因此，我们引入一些插件进行打包

### ShadowJar

shadowJar支持将依赖也打包到Jar内，下面介绍用法。

#### 1.buildscript

首先声明buildScript

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:5.2.0'
    }
}
```

在plugin前加入以上语句



#### 2.在plugins中进行插件的使用

将原本的plugins

```groovy
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.3.61'
}
```

覆盖为

```groovy
plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.3.61'
    id 'com.github.johnrengelman.shadow' version '5.2.0'//使用shadow对依赖进行打包
}

apply plugin: 'com.github.johnrengelman.shadow'
apply plugin: 'java'
```



#### 3.添加shadowJar

在文件底部添加

```groovy
shadowJar {
    // 生成包的命名规则： baseName-version-classifier.jar
    manifest {
        attributes(
                'Main-Class': 'net.mamoe.mirai.simpleloader.MyLoaderKt'//入口点
        )
    }

    // 将 build.gradle 打入到 jar 中, 方便查看依赖包版本
    from("./"){
        include 'build.gradle'
    }
}
```



#### 4.运行build

在IDEA中点击```ShadowJar```左侧的run按钮(绿色小三角)，build完成后在```build\libs```中找到jar



至此，build.gradle内的内容是

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:5.2.0'
    }
}


plugins {
    id 'java'
    id 'org.jetbrains.kotlin.jvm' version '1.3.61'
    id 'com.github.johnrengelman.shadow' version '5.2.0'//使用shadow对依赖进行打包
}

apply plugin: 'com.github.johnrengelman.shadow'
apply plugin: 'java'

group 'test'
version '1.0-SNAPSHOT'

sourceCompatibility = 1.8


repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    implementation 'net.mamoe:mirai-core-qqandroid:0.23.0'
    implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8"
    testCompile group: 'junit', name: 'junit', version: '4.12'
}


compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
}
compileTestKotlin {
    kotlinOptions.jvmTarget = "1.8"
}

shadowJar {
    // 生成包的命名规则： baseName-version-classifier.jar
    manifest {
        attributes(
                'Main-Class': 'net.mamoe.mirai.simpleloader.MyLoaderKt'
        )
    }

    // 将 build.gradle 打入到 jar 中, 方便查看依赖包版本
    from("./"){
        include 'build.gradle'
    }
}

```


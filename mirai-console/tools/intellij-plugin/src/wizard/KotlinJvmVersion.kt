/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.console.intellij.wizard

import org.jetbrains.kotlin.tools.projectWizard.Versions
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

typealias KotlinStdlibVersion = String

object KotlinStdlibVersionFetcher {
    @Throws(IOException::class)
    fun getKotlinStdlibVersion(miraiVersion: MiraiVersion): KotlinStdlibVersion {
        fun download(url: String): Document {
            return Jsoup.connect(url)
                .followRedirects(true)
                .ignoreContentType(true)
                .ignoreHttpErrors(true)
                .get()
        }

        val path = "net/mamoe/mirai-core/$miraiVersion/mirai-core-$miraiVersion.pom"
        return kotlin.runCatching {
            download("https://maven.aliyun.com/repository/central/$path")
        }.recoverCatching {
            download("https://repo.maven.apache.org/maven2/$path")
        }.map { document ->
            val xml = document.toString()

            Regex("""<artifactid>\s*kotlin-stdlib-[A-Za-z0-9-]+\s*</artifactid>\s*<version>\s*(.*?)\s*</version>""")
                .findAll(xml)
                .mapNotNull {
                    it.groupValues.getOrNull(1)
                }
                .firstOrNull() ?: Versions.KOTLIN.text
        }.getOrThrow()
    }
}

/*

<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <!-- This module was also published with a richer model, Gradle metadata,  -->
  <!-- which should be used instead. Do not delete the following line which  -->
  <!-- is to indicate to Gradle or any Gradle module metadata file consumer  -->
  <!-- that they should prefer consuming it instead. -->
  <!-- do_not_remove: published-with-gradle-metadata -->
  <modelVersion>4.0.0</modelVersion>
  <groupId>net.mamoe</groupId>
  <artifactId>mirai-core</artifactId>
  <version>2.12.2</version>
  <packaging>pom</packaging>
  <licenses>
    <license>
      <name>GNU AGPLv3</name>
      <url>https://github.com/mamoe/mirai/blob/master/LICENSE</url>
    </license>
  </licenses>
  <developers>
    <developer>
      <id>mamoe</id>
      <name>Mamoe Technologies</name>
      <email>support@mamoe.net</email>
    </developer>
  </developers>
  <scm>
    <connection>scm:https://github.com/mamoe/mirai.git</connection>
    <developerConnection>scm:git://github.com/mamoe/mirai.git</developerConnection>
    <url>https://github.com/mamoe/mirai</url>
  </scm>
  <dependencies>
    <dependency>
      <groupId>net.mamoe</groupId>
      <artifactId>mirai-core-api</artifactId>
      <version>2.12.2</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.jetbrains.kotlinx</groupId>
      <artifactId>kotlinx-serialization-core-jvm</artifactId>
      <version>1.3.2</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.jetbrains.kotlinx</groupId>
      <artifactId>kotlinx-serialization-json-jvm</artifactId>
      <version>1.3.2</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.jetbrains.kotlinx</groupId>
      <artifactId>kotlinx-coroutines-core-jvm</artifactId>
      <version>1.6.1</version>
      <scope>runtime</scope>
    </dependency>
    <dependency>
      <groupId>org.jetbrains.kotlin</groupId>
      <artifactId>kotlin-stdlib-jdk8</artifactId>
      <version>1.6.21</version>
      <scope>runtime</scope>
    </dependency>
  </dependencies>
  <description>Mirai Protocol implementation for QQ Android</description>
  <name>mirai-core</name>
  <url>https://github.com/mamoe/mirai</url>
</project>

 */

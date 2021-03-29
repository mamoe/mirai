/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


package net.mamoe.mirai.console.intellij.creator

import kotlinx.coroutines.*
import org.jsoup.Jsoup

typealias MiraiVersion = String

enum class MiraiVersionKind {
    Stable {
        override fun isThatKind(version: String): Boolean = version matches REGEX_STABLE
    },
    Prerelease {
        override fun isThatKind(version: String): Boolean = !version.contains("-dev") // && (version.contains("-M") || version.contains("-RC"))
    },
    Nightly {
        override fun isThatKind(version: String): Boolean = true // version.contains("-dev")
    }, ;

    abstract fun isThatKind(version: String): Boolean

    companion object {
        val DEFAULT = Stable

        private val REGEX_STABLE = Regex("""^\d+\.\d+(?:\.\d+)?$""")

        private suspend fun getMiraiVersionList(): Set<MiraiVersion>? {
            val xml = runInterruptible {
                // https://maven.aliyun.com/repository/central/net/mamoe/mirai-core/maven-metadata.xml
                // https://repo.maven.apache.org/maven2/net/mamoe/mirai-core/maven-metadata.xml
                kotlin.runCatching {
                    Jsoup.connect("https://maven.aliyun.com/repository/central/net/mamoe/mirai-core/maven-metadata.xml").get()
                }.recoverCatching {
                    Jsoup.connect("https://repo.maven.apache.org/maven2/net/mamoe/mirai-core/maven-metadata.xml").get()
                }.getOrNull()
            }?.body()?.toString() ?: return null

            return Regex("""<version>\s*(.*?)\s*</version>""").findAll(xml).mapNotNull { it.groupValues[1] }.toSet()
        }

        fun CoroutineScope.getMiraiVersionListAsync(): Deferred<Set<MiraiVersion>> {
            return async(CoroutineName("getMiraiVersionListAsync")) {
               getMiraiVersionList()?: setOf("+")
            }
        }
    }
}


/*

<?xml version="1.0" encoding="UTF-8"?>

<metadata>
  <groupId>net.mamoe</groupId>
  <artifactId>mirai-core</artifactId>
  <versioning>
    <latest>2.5.0-dev-2</latest>
    <release>2.5.0-dev-2</release>
    <versions>
      <version>2.4-RC</version>
      <version>2.4-M1-dev-publish-3</version>
      <version>2.4.0-dev-publish-2</version>
      <version>2.4.0</version>
      <version>2.4.1</version>
      <version>2.4.2</version>
      <version>2.5-RC-dev-1</version>
      <version>2.5-M1</version>
      <version>2.5-M2-dev-2</version>
      <version>2.5-M2</version>
      <version>2.5.0-dev-android-1</version>
      <version>2.5.0-dev-1</version>
      <version>2.5.0-dev-2</version>
    </versions>
    <lastUpdated>20210319014025</lastUpdated>
  </versioning>
</metadata>

 */
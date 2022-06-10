/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */


package net.mamoe.mirai.console.intellij.wizard

import com.intellij.openapi.diagnostic.Logger
import com.intellij.util.text.SemVer
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException

typealias MiraiVersion = String

enum class MiraiVersionKind {
    Stable {
        override fun isThatKind(version: String): Boolean = version matches REGEX_STABLE
    },
    Prerelease {
        override fun isThatKind(version: String): Boolean =
            Stable.isThatKind(version) || version.contains("-M") || version.contains("-RC")
    },
    Nightly {
        override fun isThatKind(version: String): Boolean = true // version.contains("-dev")
    }, ;

    abstract fun isThatKind(version: String): Boolean

    companion object {
        val DEFAULT = Stable

        private val REGEX_STABLE = Regex("""^\d+\.\d+(?:\.\d+)?$""")
        private val LOG = Logger.getInstance(MiraiVersionKind::class.java)

        @Throws(IOException::class)
        fun getMiraiVersionList(): Set<MiraiVersion> {
            fun download(url: String): Document {
                return Jsoup.connect(url)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get()
            }

            return kotlin.runCatching {
                download("https://maven.aliyun.com/repository/central/net/mamoe/mirai-core/maven-metadata.xml")
            }.recoverCatching {
                download("https://repo.maven.apache.org/maven2/net/mamoe/mirai-core/maven-metadata.xml")
            }.map { document ->
                val xml = document.toString()

                Regex("""<version>\s*(.*?)\s*</version>""")
                    .findAll(xml)
                    .mapNotNull { it.groupValues.getOrNull(1) }
                    .sortVersionsDescending()
                    .toSet()
            }.getOrThrow()
        }

        // Kotlin version: not working because
        // Caused by: java.util.ServiceConfigurationError: kotlinx.coroutines.CoroutineExceptionHandler: com.intellij.openapi.application.impl.CoroutineExceptionHandlerImpl not a subtype
//
//        private suspend fun getMiraiVersionList(): Set<MiraiVersion> {
//            suspend fun download(url: String): Document {
//                return Jsoup.connect(url)
//                    .followRedirects(true)
//                    .ignoreContentType(true)
//                    .ignoreHttpErrors(true)
//                    .run { runInterruptible(Dispatchers.IO) { get() } }
//            }
//
//            val document = supervisorScope {
//                val jobs = mutableListOf<Deferred<Document>>()
//                jobs += async {
//                    download("https://maven.aliyun.com/repository/central/net/mamoe/mirai-core/maven-metadata.xml")
//                }
//                jobs += async {
//                    download("https://repo.maven.apache.org/maven2/net/mamoe/mirai-core/maven-metadata.xml")
//                }
//                val timeout = launch {
//                    delay(10_000)
//                }
//                // select the faster one
//                select<Document> {
//                    jobs.forEach { job -> job.onAwait { it } }
//                    timeout.onJoin {
//                        throw IllegalStateException("Timeout getMiraiVersionList").apply {
//                            jobs.forEach {
//                                if (it.isCompleted) {
//                                    try {
//                                        it.await()
//                                    } catch (e: Throwable) {
//                                        addSuppressed(e)
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                jobs.forEach { it.cancel() }
//                timeout.cancel()
//            }
//
//            val xml = document.toString()
//
//            return Regex("""<version>\s*(.*?)\s*</version>""").findAll(xml).mapNotNull { it.groupValues.getOrNull(1) }.toSet()
//        }

//        fun CoroutineScope.getMiraiVersionListAsync(): Deferred<Set<MiraiVersion>> {
//            return async(CoroutineName("getMiraiVersionListAsync")) {
//                getMiraiVersionList()
//            }
//        }
    }
}

internal fun Sequence<String>.sortVersionsDescending(): Sequence<String> {
    return this
        .mapNotNull { SemVer.parseFromText(it) }
        .sortedWith { o1, o2 ->
            o2.compareTo(o1)
        }
        .map { it.toString() }
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
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

typealias KotlinJvmVersion = String

enum class KotlinJvmVersionKind {
    Stable {
        override fun isThatKind(version: String): Boolean = version matches REGEX_STABLE
    },
    Prerelease {
        override fun isThatKind(version: String): Boolean =
            Stable.isThatKind(version) || version.contains("-Beta") || version.contains("-M") || version.contains("-RC")
    },
    Nightly {
        override fun isThatKind(version: String): Boolean = true // version.contains("-dev")
    }, ;

    abstract fun isThatKind(version: String): Boolean

    companion object {
        val DEFAULT = Stable

        private val REGEX_STABLE = Regex("""^\d+\.\d+(?:\.\d+)?$""")
        private val LOG = Logger.getInstance(KotlinJvmVersionKind::class.java)

        @Throws(IOException::class)
        fun getKotlinJvmVersionList(): Set<KotlinJvmVersion> {
            fun download(url: String): Document {
                return Jsoup.connect(url)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .ignoreHttpErrors(true)
                    .get()
            }

            return kotlin.runCatching {
                download("https://plugins.gradle.org/m2/org/jetbrains/kotlin/jvm/org.jetbrains.kotlin.jvm.gradle.plugin/maven-metadata.xml")
            }.map { document ->
                val xml = document.toString()

                Regex("""<version>\s*(.*?)\s*</version>""")
                    .findAll(xml)
                    .mapNotNull { it.groupValues.getOrNull(1) }
                    .sortVersionsDescending()
                    .toSet()
            }.getOrThrow()
        }
    }
}

/*

<?xml version='1.0' encoding='US-ASCII'?>
<metadata>
  <groupId>org.jetbrains.kotlin.jvm</groupId>
  <artifactId>org.jetbrains.kotlin.jvm.gradle.plugin</artifactId>
  <version>1.7.20-RC</version>
  <versioning>
    <latest>1.7.20-RC</latest>
    <release>1.7.20-RC</release>
    <versions>
      <version>0.0.1-test-1</version>
      <version>0.0.1-test-2</version>
      <version>0.1.1.2-5-test-2</version>
      <version>0.1.1.2-5-test-4</version>
      <version>0.1.1.2-5-test-5</version>
      <version>1.1.1</version>
      <version>1.1.2</version>
      <version>1.1.2-2</version>
      <version>1.1.2-5</version>
      <version>1.1.3</version>
      <version>1.1.3-2</version>
      <version>1.1.4</version>
      <version>1.1.4-2</version>
      <version>1.1.4-3</version>
      <version>1.1.50</version>
      <version>1.1.51</version>
      <version>1.1.60</version>
      <version>1.1.61</version>
      <version>1.2.0</version>
      <version>1.2.10</version>
      <version>1.2.20</version>
      <version>1.2.21</version>
      <version>1.2.30</version>
      <version>1.2.31</version>
      <version>1.2.40</version>
      <version>1.2.41</version>
      <version>1.2.50</version>
      <version>1.2.51</version>
      <version>1.2.60</version>
      <version>1.2.61</version>
      <version>1.2.70</version>
      <version>1.2.71</version>
      <version>1.3.0</version>
      <version>1.3.0-rc-190</version>
      <version>1.3.0-rc-198</version>
      <version>1.3.10</version>
      <version>1.3.11</version>
      <version>1.3.20</version>
      <version>1.3.21</version>
      <version>1.3.30</version>
      <version>1.3.31</version>
      <version>1.3.40</version>
      <version>1.3.41</version>
      <version>1.3.50</version>
      <version>1.3.60</version>
      <version>1.3.61</version>
      <version>1.3.70</version>
      <version>1.3.71</version>
      <version>1.3.72</version>
      <version>1.4.0</version>
      <version>1.4.0-rc</version>
      <version>1.4.10</version>
      <version>1.4.20</version>
      <version>1.4.20-M1</version>
      <version>1.4.20-M2</version>
      <version>1.4.20-RC</version>
      <version>1.4.21</version>
      <version>1.4.21-2</version>
      <version>1.4.30</version>
      <version>1.4.30-M1</version>
      <version>1.4.30-RC</version>
      <version>1.4.31</version>
      <version>1.4.32</version>
      <version>1.5.0</version>
      <version>1.5.0-M1</version>
      <version>1.5.0-M2</version>
      <version>1.5.0-RC</version>
      <version>1.5.10</version>
      <version>1.5.20</version>
      <version>1.5.20-M1</version>
      <version>1.5.20-RC</version>
      <version>1.5.21</version>
      <version>1.5.30</version>
      <version>1.5.30-M1</version>
      <version>1.5.30-RC</version>
      <version>1.5.31</version>
      <version>1.5.32</version>
      <version>1.6.0</version>
      <version>1.6.0-M1</version>
      <version>1.6.0-RC</version>
      <version>1.6.0-RC2</version>
      <version>1.6.10</version>
      <version>1.6.10-RC</version>
      <version>1.6.20</version>
      <version>1.6.20-M1</version>
      <version>1.6.20-RC</version>
      <version>1.6.20-RC2</version>
      <version>1.6.21</version>
      <version>1.7.0</version>
      <version>1.7.0-Beta</version>
      <version>1.7.0-RC</version>
      <version>1.7.0-RC2</version>
      <version>1.7.10</version>
      <version>1.7.20-Beta</version>
      <version>1.7.20-RC</version>
    </versions>
    <lastUpdated>20220907205901</lastUpdated>
  </versioning>
</metadata>

 */

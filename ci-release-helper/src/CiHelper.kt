/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */
@file:JvmName("CiHelperKt")

package cihelper

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.io.OutputStream
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.Charset
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.security.MessageDigest
import java.util.*
import java.util.stream.Collectors
import kotlin.io.path.*

private val hexTemplate: CharArray = "0123456789abcdef".toCharArray()
private const val useragent = "Gradle/7.3.1 (Windows 10;10.0;amd64) (Azul Systems, Inc.;18.0.2.1;18.0.2.1+1)"

fun ByteArray.hexToString(): String {
    val sb = StringBuilder(this.size * 2)
    forEach { sbyte ->
        sb.append(hexTemplate[sbyte.toInt().shr(4).and(0xF)])
        sb.append(hexTemplate[sbyte.toInt().and(0xF)])
    }
    return sb.toString()
}

private fun getAuth(): String {

    val cert_username =
        System.getenv("CERT_USERNAME") ?: System.getProperty("cihelper.cert.username") ?: error("CERT_USERNAME")
    val cert_password =
        System.getenv("CERT_PASSWORD") ?: System.getProperty("cihelper.cert.password") ?: error("CERT_PASSWORD")

    return "Basic " + Base64.getEncoder().encodeToString(
        ("$cert_username:$cert_password").toByteArray()
    )
}

@Suppress("Since15")
fun main(args: Array<String>) {
    val projVer = System.getenv("PROJ_VERSION") ?: error("Please use `./gradlew runcihelper --args XXXX`")
    val projArtifacts = System.getenv("PROJ_ARTIFACTS")!!.split("|")
    val repoLoc = System.getenv("PROJ_MiraiStageRepo")!!.let { Paths.get(URI.create(it)) }

    if (args.isEmpty()) error("no action")

    val relatedRepoLoc = repoLoc.resolve("net/mamoe")

    val httpc = HttpClient.newBuilder().build()

    when (args[0]) {

        "sync-maven-metadata" -> {
            // https://repo1.maven.org/maven2/net/mamoe/mirai-core-all/maven-metadata.xml

            projArtifacts.forEach { projArtifact ->
                val savedLoc = relatedRepoLoc.resolve(projArtifact)
                    .createDirectories()
                    .resolve("maven-metadata.xml")

                println("[metadata.xml] Syncing $projArtifact")

                val verPath = relatedRepoLoc.resolve(projArtifact).resolve(projVer)

                val isNotEmpty = if (verPath.exists()) {
                    Files.newDirectoryStream(verPath).use { it.iterator().hasNext() }
                } else false

                if (isNotEmpty) {
                    println("[metadata.xml] Skipped $projArtifact because it was published to stage.")
                    return@forEach
                }


                val rsp = httpc.send(
                    HttpRequest.newBuilder(
                        URI.create("https://repo1.maven.org/maven2/net/mamoe/$projArtifact/maven-metadata.xml")
                    ).GET().build(),
                    HttpResponse.BodyHandlers.ofFile(savedLoc)
                )
                if (rsp.statusCode() != 200) {
                    if (rsp.statusCode() == 404) {
                        savedLoc.deleteIfExists()
                        return@forEach
                    }
                    error("$rsp -> " + savedLoc.takeIf { it.isRegularFile() }?.readText())
                }
            }
        }

        "create-stage-repo" -> {
            val rsp = httpc.send(
                HttpRequest.newBuilder(
                    URI.create(
                        "https://oss.sonatype.org/service/local/staging/profiles/${
                            System.getProperty(
                                "cihelper.cert.profileid"
                            )
                        }/start"
                    )
                )
                    .header("User-Agent", useragent)
                    .header("Authorization", getAuth())
                    .header("Content-Type", "application/json;charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString("{\"data\":{\"description\": \"mamoe/mirai release $projVer\"}}"))
                    .build(),
                HttpResponse.BodyHandlers.ofString()
            )
            if (rsp.statusCode() != 201) {
                error(rsp.toString())
            }
            val rspx = Json.decodeFromString(JsonObject.serializer(), rsp.body())
            val stagedRepositoryId = rspx["data"]!!.jsonObject["stagedRepositoryId"]!!.jsonPrimitive.content

            File("ci-release-helper").also { it.mkdirs() }
                .resolve("repoid").writeText(stagedRepositoryId)
        }

        "publish-to-maven-central" -> {
            // https://oss.sonatype.org/service/local/staging/deploy/maven2
            relatedRepoLoc.listDirectoryEntries().forEach { subdir ->
                val verpath = subdir.resolve(projVer)
                val doDelete = if (!verpath.isDirectory()) {
                    true
                } else {
                    verpath.listDirectoryEntries().isEmpty()
                }
                if (doDelete) {
                    subdir.toFile().deleteRecursively()
                }
            }
            val pendingFiles = Files.walk(relatedRepoLoc)
                .filter { it.isRegularFile() }
                .filter { !it.name.endsWith(".md5") && !it.name.endsWith(".sha1") }
                .filter { !it.name.endsWith(".asc") }
                .use { stream -> stream.collect(Collectors.toList()) }

            run `sign artifacts`@{
                // build-gpg-sign/keys.gpg
                // build-gpg-sign/keys.gpg.pub
                val bgs = Paths.get("build-gpg-sign").toAbsolutePath()
                if (!bgs.isDirectory()) return@`sign artifacts`
                val gpgHomeDir = bgs.resolve("homedir")
                val bgsFile = bgs.toFile()

                fun execGpg(vararg cmd: String) {
                    println("::group::${cmd.joinToString(" ")}")
                    try {
                        val exitcode = ProcessBuilder("gpg", "--homedir", "homedir", "--batch", "--no-tty", *cmd)
                            .directory(bgsFile)
                            .inheritIO()
                            .start()
                            .waitFor()
                        if (exitcode != 0) {
                            error("Exit code $exitcode != 0")
                        }
                    } finally {
                        println("::endgroup::")
                    }
                }


                if (!gpgHomeDir.resolve("pubring.kbx").exists()) {

                    val keys = arrayOf("keys.gpg", "keys.gpg.pub")
                    if (!keys.all { bgs.resolve(it).isRegularFile() }) return@`sign artifacts`

                    val isPosix = FileSystems.getDefault().supportedFileAttributeViews().contains("posix")
                    val dirPermissions = PosixFilePermissions.asFileAttribute(
                        EnumSet.of(
                            PosixFilePermission.OWNER_READ,
                            PosixFilePermission.OWNER_WRITE,
                            PosixFilePermission.OWNER_EXECUTE
                        )
                    )

                    Files.createDirectories(
                        gpgHomeDir,
                        *if (isPosix) arrayOf(dirPermissions) else arrayOf(),
                    )

                    keys.forEach { execGpg("--import", it) }
                }


                println("::group::Signing artifacts")
                pendingFiles.toList().asSequence().filterNot { it.name == "maven-metadata.xml" }
                    .forEach { pendingFile ->
                        val pt = pendingFile.absolutePathString()
                        val ascFile = pendingFile.resolveSibling(pendingFile.name + ".asc")
                        ascFile.deleteIfExists()
                        execGpg("-a", "--detach-sig", "--sign", pt)

                        pendingFiles.add(ascFile)
                    }
                println("::endgroup::")
            }

            run `calc msg digest`@{
                pendingFiles.toList().forEach { pendingFile ->
                    val sha1MD = MessageDigest.getInstance("SHA-1")
                    val md5MD = MessageDigest.getInstance("MD5")

                    pendingFile.inputStream().use { content ->
                        content.copyTo(object : OutputStream() {
                            override fun write(b: Int) {
                                sha1MD.update(b.toByte())
                                md5MD.update(b.toByte())
                            }

                            override fun write(b: ByteArray, off: Int, len: Int) {
                                sha1MD.update(b, off, len)
                                md5MD.update(b, off, len)
                            }
                        })
                    }

                    val sha1 = sha1MD.digest().hexToString()
                    val mg5 = md5MD.digest().hexToString()

                    val pfname = pendingFile.name
                    val sha1File = pendingFile.resolveSibling("$pfname.sha1")
                    val md5File = pendingFile.resolveSibling("$pfname.md5")

                    sha1File.writeText(sha1)
                    md5File.writeText(mg5)

                    pendingFiles.add(sha1File)
                    pendingFiles.add(md5File)
                }
            }

            pendingFiles.sort()

            println("::group::Publishing to Maven Central")

            val authorization = getAuth()
            val errors = mutableListOf<String>()

            fun resolveSonatypeRepoLoc(): String {
                val repoIdPath = Paths.get("ci-release-helper/repoid")

                var repoId = ""
                if (repoIdPath.isRegularFile()) {
                    repoId = repoIdPath.readText().trim()
                } else if (repoIdPath.isDirectory()) {
                    val files = repoIdPath.listDirectoryEntries().filter { it.isRegularFile() }
                    if (files.size == 1) {
                        repoId = files.first().readText().trim()
                    }
                }
                if (repoId.isNotBlank()) {
                    return "https://oss.sonatype.org/service/local/staging/deployByRepositoryId/$repoId/"
                }

                return "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
            }

            val repoServerLocation = resolveSonatypeRepoLoc()

            pendingFiles.forEach { pending ->
                val netpath = repoLoc.relativize(pending)

                val uri = repoServerLocation + (netpath.toString().replace("\\", "/"))

                println("Processing $uri")

                val rsp = httpc.send(
                    HttpRequest.newBuilder(
                        URI.create(uri)
                    ).PUT(HttpRequest.BodyPublishers.ofFile(pending))
                        .header("Authorization", authorization)
                        .header("User-Agent", useragent)
                        .build(),
                    HttpResponse.BodyHandlers.ofByteArray()
                )
                if (rsp.statusCode() / 100 != 2) {
                    val errmsg = "$rsp -> " + String(rsp.body(), Charset.defaultCharset())
                    errors.add(errmsg)
                    println(errmsg)
                }
            }

            println("::endgroup::")
            if (errors.isNotEmpty()) {
                error(errors.joinToString("\n\n", prefix = "\n"))
            }
        }

        else -> error("Unknown command: " + args.joinToString(" "))
    }
}

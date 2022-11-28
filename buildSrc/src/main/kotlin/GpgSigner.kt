/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 *  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 *  https://github.com/mamoe/mirai/blob/master/LICENSE
 */


import org.gradle.api.Project
import java.io.File

open class GpgSigner(private val workdir: File) {
    private val workdirParent by lazy { workdir.parentFile ?: error("Assertion error: No parent file of $workdir") }
    private val workdirName by lazy { workdir.name }

    fun verbose(msg: String) {
        println("[GPG SIGN] [Verbose] $msg")
    }

    @Suppress("RemoveExplicitTypeArguments")
    private val verbosePrintOnce by lazy<Unit> {
        verbose("GPG Signer  working dir: $workdir")
        verbose("GPG command working dir: $workdirParent")
    }

    constructor(workdir: String) : this(File(workdir))

    object NoopSigner : GpgSigner("build/gpg-noop") {
        override fun processGpg(vararg cmds: String) {
        }

        override fun importKey(file: File) {
        }

        override fun doSign(file: File) {
        }
    }

    companion object {
        private var initialized: Boolean = false
        var signer: GpgSigner = NoopSigner
        fun setup(project: Project) {
        }
    }

    open fun processGpg(
        vararg cmds: String
    ) {
        workdir.mkdirs()
        verbosePrintOnce

        val response = ProcessBuilder().command(ArrayList<String>().apply {
            add("gpg")
            add("--homedir"); add(workdirName)
            addAll(cmds)
        }.also {
            verbose("Processing " + it.joinToString(" "))
        }).directory(workdirParent)
            .inheritIO()
            .start()
            .waitFor()
        if (response != 0) {
            error("Exit Response $response")
        }
    }

    open fun importKey(file: File) {
        processGpg("--batch", "--import", file.toString())
    }

    open fun doSign(file: File) {
        if (!file.isFile) {
            println("[GPG SIGN] $file not a file")
            return
        }
        println("[GPG SIGN] Signing $file")
        File("${file.path}.asc").delete()
        processGpg("-a", "--batch", "--no-tty", "--detach-sig", "--sign", file.toString())
        processGpg("--verify", "$file.asc", file.toString())
    }

}

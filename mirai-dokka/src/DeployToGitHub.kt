/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.dokka

fun main() {
    val token = System.getenv("gh_token") ?: error("Token not found")
    val currentVersion = System.getenv("mirai_ver") ?: error("version not found")

    repoexec("git", "config", "--local", "http.https://github.com/.extraheader", "")
    runCatching {
        repoexec("git", "remote", "remove", "token")
    }
    repoexec(
        "git", "remote", "add", "token",
        "https://x-access-token:$token@github.com/project-mirai/mirai-doc.git"
    )

    repoexec("git", "config", "--local", "user.email", "mamoebot@users.noreply.github.com")
    repoexec("git", "config", "--local", "user.name", "mamoebot")
    repoexec("git", "add", "-A")
    repoexec(
        "git", "commit", "-m", currentVersion,
        nooutput = true,
    )
    repoexec("git", "push", "token", "HEAD:master")
}

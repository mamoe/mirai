/*
 * Copyright 2019-2021 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

import org.w3c.dom.Node

data class SemanticVersion( // 2.7-RC-dev-1
    val major: Int,  // 2
    val minor: Int,  // 7
    val patch: Int?, // null
    val meta: String?, // RC
    val branch: String?, // dev
    val snapshotNumber: Int? // 1
) : Comparable<SemanticVersion> {
    override fun toString(): String = buildString {
        append(major)
        append(".")
        append(minor)
        if (patch != null) {
            append(".")
            append(patch)
        }
        if (meta != null) {
            append("-")
            append(meta)
        }
        if (branch != null) {
            append("-")
            append(branch)
        }
        if (snapshotNumber != null) {
            append("-")
            append(snapshotNumber)
        }
    }

    override fun compareTo(other: SemanticVersion): Int {
        // assuming branches are the same.

        this.major.compareTo(other.major).let { if (it != 0) return it }
        this.minor.compareTo(other.minor).let { if (it != 0) return it }
        (this.patch ?: 0).compareTo(other.patch ?: 0).let { if (it != 0) return it }

        when {
            this.meta != null && other.meta == null -> return -1
            this.meta == null && other.meta != null -> return 1
            this.meta != null && other.meta != null -> return this.meta.compareTo(other.meta)
            this.meta == null && other.meta == null -> {
                if (this.snapshotNumber == null) return if (other.snapshotNumber == null) 0 else 1
                return this.snapshotNumber.compareTo(other.snapshotNumber ?: return -1)
            }
        }

        return 0
    }

    fun nextSnapshotVersion(branch: String): SemanticVersion {
        return SemanticVersion(major, minor, patch, meta, branch, (snapshotNumber ?: 0) + 1)
    }
}

fun analyzeMiraiVersion(content: String): SemanticVersion? {
    return Regex(
        """([0-9]+)\.([0-9]+)(?:\.([0-9]+)-((?:M|RC)[0-9]*)|\.([0-9]+)|-(M[0-9]+|RC[0-9]*))?(?:-(\w+)-([0-9]+))?"""
    ).matchEntire(
        content
    )?.destructured?.let { (major, minor, patch, meta, patchOnly, metaOnly, branch, snapshotNumber) ->
        when {
            patchOnly.isNotEmpty() -> {
                SemanticVersion(
                    major = major.toInt(),
                    minor = minor.toInt(),
                    patch = patchOnly.toInt(),
                    meta = null,
                    branch = branch.takeIf { it.isNotBlank() },
                    snapshotNumber = snapshotNumber.toIntOrNull()
                )
            }
            metaOnly.isNotEmpty() -> {
                SemanticVersion(
                    major = major.toInt(),
                    minor = minor.toInt(),
                    patch = null,
                    meta = metaOnly,
                    branch = branch.takeIf { it.isNotBlank() },
                    snapshotNumber = snapshotNumber.toIntOrNull()
                )
            }
            else -> {
                SemanticVersion(
                    major = major.toInt(),
                    minor = minor.toInt(),
                    patch = patch.toInt(),
                    meta = meta,
                    branch = branch.takeIf { it.isNotBlank() },
                    snapshotNumber = snapshotNumber.toIntOrNull()
                )
            }
        }
    }
}

fun getLatestMiraiVersionForBranch(content: String, branch: String): SemanticVersion {
    val versions = Regex("""<version>\s*(.*?)\s*</version>""").findAll(content)
        .map { it.groupValues[1] }
        .map { analyzeMiraiVersion(it) }
        .filterNotNull()
        .toSet()
    return versions.maxByOrNull { it } ?: error("Could not fetch version list for branch '$branch'")
}

fun org.w3c.dom.NodeList.toList(): List<Node> {
    val list = ArrayList<Node>(this.length)
    repeat(this.length) {
        list.add(item(it))
    }
    return list
}
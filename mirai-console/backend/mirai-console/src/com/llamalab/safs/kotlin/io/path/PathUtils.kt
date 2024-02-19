package com.llamalab.safs.kotlin.io.path
import com.llamalab.safs.LinkOption
import com.llamalab.safs.Path
import com.llamalab.safs.Files
import com.llamalab.safs.OpenOption
import com.llamalab.safs.attribute.FileAttribute
import java.io.InputStream
import java.nio.charset.Charset

public inline fun Path.isRegularFile(vararg options: LinkOption): Boolean = Files.isRegularFile(this, *options)
public inline fun Path.readBytes(): ByteArray {
    return Files.readAllBytes(this)
}
public inline fun Path.writeBytes(array: ByteArray, vararg options: OpenOption): Unit {
    Files.write(this, array, *options)
}
public inline fun Path.createDirectories(vararg attributes: FileAttribute<*>): Path =
    Files.createDirectories(this, *attributes)

public fun createTempDirectory(directory: Path?, prefix: String? = null, vararg attributes: FileAttribute<*>): Path =
    if (directory != null)
        Files.createTempDirectory(directory, prefix, *attributes)
    else
        Files.createTempDirectory(prefix, *attributes)
public inline fun createTempDirectory(prefix: String? = null, vararg attributes: FileAttribute<*>): Path = Files.createTempDirectory(prefix, *attributes)

public fun Path.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption) {
    Files.newOutputStream(this, *options).writer(charset).use { it.append(text) }
}

public inline fun Path.inputStream(vararg options: OpenOption): InputStream {
    return Files.newInputStream(this, *options)
}

public val Path.name: String
    get() = fileName?.toString().orEmpty()
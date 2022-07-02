/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import kotlinx.cinterop.*
import net.mamoe.mirai.utils.toUHexString
import platform.posix.memcpy
import platform.windows.*
import kotlin.native.internal.createCleaner


private const val BCRYPT_ECCPUBLIC_BLOB = "ECCPUBLICBLOB"
private const val BCRYPT_ECCPRIVATE_BLOB = "ECCPRIVATEBLOB"

/*
 * Note that raw secret is not visible to users until Win8.1 / Win10.
 * As OICQ uses a special (non-standard) algorithm for key derivation,
 * we must get raw secret.
 */

/**
 * [BCRYPT_KDF_RAW_SECRET] is available from Windows 8.1 and onwards.
 */
private const val BCRYPT_KDF_RAW_SECRET = "TRUNCATE"

// shared
private val hAlgorithm by lazy {
    memScoped {
        val hAlgorithmVar = alloc<BCRYPT_ALG_HANDLEVar>()
        BCryptOpenAlgorithmProvider(hAlgorithmVar.ptr, "ECDH_P256", null, 0).checkNTStatus()
        hAlgorithmVar.value
    } ?: error("Failed to open ECDH_P256 algorithm provider")
}

internal class BCryptECPublicKey private constructor(val hKey: BCRYPT_KEY_HANDLE) {
    @Suppress("unused")
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(hKey) {
        BCryptDestroyKey(it)
    }

    fun export(): ByteArray {
        memScoped {
            val sizeVar = alloc<ULONGVar>()
            BCryptExportKey(hKey, null, BCRYPT_ECCPUBLIC_BLOB, null, 0, sizeVar.ptr, 0).checkNTStatus()
            val buffer = allocArray<ByteVar>(sizeVar.value.convert())
            BCryptExportKey(
                hKey,
                null,
                BCRYPT_ECCPUBLIC_BLOB,
                buffer.reinterpret(),
                sizeVar.value,
                sizeVar.ptr,
                0
            ).checkNTStatus()
            val header = buffer.reinterpret<BCRYPT_ECCKEY_BLOB>().pointed
            check(header.dwMagic == BCRYPT_ECDH_PUBLIC_P256_MAGIC.convert<ULONG>()) {
                "Failed to export ecdh p256 public key, improper key got (magic = ${header.dwMagic})"
            }
            val src = buffer + sizeOf<BCRYPT_ECCKEY_BLOB>()

            val encoded = ByteArray(header.cbKey.convert<Int>() * 2 + 1)
            encoded[0] = 0x04 // uncompressed
            encoded.usePinned {
                memcpy(it.addressOf(1), src, (header.cbKey * 2.convert()).convert())
            }
            return encoded
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? BCryptECPublicKey)?.let {
            export().contentEquals(other.export())
        } ?: false
    }

    override fun hashCode(): Int {
        return export().hashCode()
    }

    companion object {
        fun import(encoded: ByteArray): BCryptECPublicKey {
            val keyLength = 32
            require(encoded[0] == 0x04.toByte()) { "Only uncompressed format is supported" }
            require(encoded.size == keyLength * 2 + 1) { "Invalid ECDH_P256 public key" }
            memScoped {
                val sizeOfBuffer = encoded.size - 1 + sizeOf<BCRYPT_ECCKEY_BLOB>()
                val buffer = allocArray<ByteVar>(sizeOfBuffer)
                buffer.reinterpret<BCRYPT_ECCKEY_BLOB>().pointed.apply {
                    dwMagic = BCRYPT_ECDH_PUBLIC_P256_MAGIC.convert()
                    cbKey = keyLength.convert()
                }
                val dst = buffer + sizeOf<BCRYPT_ECCKEY_BLOB>()
                encoded.usePinned {
                    memcpy(dst, it.addressOf(1), (encoded.size - 1).convert())
                }

                val hKeyVar = alloc<BCRYPT_KEY_HANDLEVar>()
                BCryptImportKeyPair(
                    hAlgorithm,
                    null,
                    BCRYPT_ECCPUBLIC_BLOB,
                    hKeyVar.ptr,
                    buffer.reinterpret(),
                    sizeOfBuffer.convert(),
                    BCRYPT_NO_KEY_VALIDATION
                ).checkNTStatus()
                return BCryptECPublicKey(hKeyVar.value ?: error("Failed to import a Bcrypt EC public key"))
            }
        }

        fun copyFrom(source: BCRYPT_KEY_HANDLE): BCryptECPublicKey {
            val hKey = memScoped {
                val sizeVar = alloc<ULONGVar>()
                BCryptExportKey(
                    source,
                    null,
                    BCRYPT_ECCPUBLIC_BLOB,
                    null,
                    0,
                    sizeVar.ptr,
                    0
                ).checkNTStatus()
                val buffer = allocArray<ByteVar>(sizeVar.value.convert())
                BCryptExportKey(
                    source,
                    null,
                    BCRYPT_ECCPUBLIC_BLOB,
                    buffer.reinterpret(),
                    sizeVar.value,
                    sizeVar.ptr,
                    0
                ).checkNTStatus()

                val hKeyVar = alloc<BCRYPT_KEY_HANDLEVar>()
                BCryptImportKeyPair(
                    hAlgorithm,
                    null,
                    BCRYPT_ECCPUBLIC_BLOB,
                    hKeyVar.ptr,
                    buffer.reinterpret(),
                    sizeVar.value,
                    BCRYPT_NO_KEY_VALIDATION
                ).checkNTStatus()
                hKeyVar.value
            } ?: error("Failed to copy a Bcrypt EC public key")
            return BCryptECPublicKey(hKey)
        }
    }
}

internal class BCryptECPrivateKey private constructor(val hKey: BCRYPT_KEY_HANDLE) {
    @Suppress("unused")
    @OptIn(ExperimentalStdlibApi::class)
    private val cleaner = createCleaner(hKey) {
        BCryptDestroyKey(it)
    }

    private fun blob(): ByteArray {
        memScoped {
            val sizeVar = alloc<ULONGVar>()
            BCryptExportKey(hKey, null, BCRYPT_ECCPRIVATE_BLOB, null, 0, sizeVar.ptr, 0).checkNTStatus()
            val buffer = ByteArray(sizeVar.value.convert())
            buffer.usePinned {
                BCryptExportKey(
                    hKey,
                    null,
                    BCRYPT_ECCPRIVATE_BLOB,
                    it.addressOf(0).reinterpret(),
                    sizeVar.value,
                    sizeVar.ptr,
                    0
                ).checkNTStatus()
            }
            return buffer
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        return (other as? BCryptECPrivateKey)?.let {
            blob().contentEquals(other.blob())
        } ?: false
    }

    override fun hashCode(): Int {
        return blob().hashCode()
    }

    companion object {
        fun createInPlace(hKey: BCRYPT_KEY_HANDLE): BCryptECPrivateKey {
            return BCryptECPrivateKey(hKey)
        }
    }
}

/**
 * Not available until Windows 8.1 or onwards.
 */
internal class BCryptEcdh : Ecdh<BCryptECPublicKey, BCryptECPrivateKey> {
    init {
        try {
            val keyPair = generateKeyPair()
            calculateShareKey(keyPair.public, keyPair.private)
        } catch (e: Throwable) {
            throw IllegalStateException("BcryptECDH is not supported on this platform")
        }
    }

    override fun generateKeyPair(): EcdhKeyPair<BCryptECPublicKey, BCryptECPrivateKey> {
        val hKey = memScoped {
            val hKeyVar = alloc<BCRYPT_KEY_HANDLEVar>()
            BCryptGenerateKeyPair(hAlgorithm, hKeyVar.ptr, 256, 0).checkNTStatus()
            BCryptFinalizeKeyPair(hKeyVar.value, 0).checkNTStatus()
            hKeyVar.value
        } ?: error("Failed to generate key pair")
        return EcdhKeyPair(BCryptECPublicKey.copyFrom(hKey), BCryptECPrivateKey.createInPlace(hKey))
    }

    override fun calculateShareKey(peerKey: BCryptECPublicKey, privateKey: BCryptECPrivateKey): ByteArray {
        // Only Win8.1/Win10 support to get raw secret
        memScoped {
            val hAgreedSecretVar = alloc<BCRYPT_SECRET_HANDLEVar>()
            BCryptSecretAgreement(privateKey.hKey, peerKey.hKey, hAgreedSecretVar.ptr, 0).checkNTStatus()
            val sizeVar = alloc<ULONGVar>()
            BCryptDeriveKey(
                hAgreedSecretVar.value,
                BCRYPT_KDF_RAW_SECRET,
                null,
                null,
                0,
                sizeVar.ptr,
                0
            ).checkNTStatus()
            val buffer = ByteArray(sizeVar.value.convert())
            buffer.usePinned {
                BCryptDeriveKey(
                    hAgreedSecretVar.value,
                    BCRYPT_KDF_RAW_SECRET,
                    null,
                    it.addressOf(0).reinterpret(),
                    sizeVar.value,
                    sizeVar.ptr,
                    0
                ).checkNTStatus()
            }
            BCryptDestroySecret(hAgreedSecretVar.value).checkNTStatus()
            buffer.reverse()
            return buffer
        }
    }

    override fun importPublicKey(encoded: ByteArray): BCryptECPublicKey {
        return BCryptECPublicKey.import(encoded)
    }

    override fun exportPublicKey(key: BCryptECPublicKey): ByteArray {
        return key.export()
    }
}

private fun NTSTATUS.checkNTStatus() {
    // https://docs.microsoft.com/en-us/windows-hardware/drivers/kernel/using-ntstatus-values
    if (this >= 0) {
        // a success type (0 − 0x3FFFFFFF) or an informational type (0x40000000 − 0x7FFFFFFF)
        return
    }
    throw IllegalStateException("NtError: ${this.toUHexString()}")
}
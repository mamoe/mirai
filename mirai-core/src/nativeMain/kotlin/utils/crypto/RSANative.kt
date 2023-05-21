/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import kotlinx.cinterop.*
import net.mamoe.mirai.internal.utils.getOpenSSLError
import net.mamoe.mirai.internal.utils.ref
import openssl.*

/**
 * reference:
 * - https://stackoverflow.com/questions/70535625/openssl-rsa-encryption-decryption-with-evp-methods
 * - https://www.openssl.org/docs/man3.1/man3/
 */

/**
 * Generate RSA key pair with size of [keySize].
 * The public key pair is encoded with x.509, and the private key pair is encoded with PKCS8
 */
internal actual fun generateRSAKeyPair(keySize: Int): RSAKeyPair {
    memScoped {
        val evpPkeyCtx = EVP_PKEY_CTX_new_id(EVP_PKEY_RSA, null)
            ?: error("Failed to create evp pkey context: ${getOpenSSLError()}")

        if (EVP_PKEY_keygen_init(evpPkeyCtx) <= 0) {
            error("Failed to init evp pkey context: ${getOpenSSLError()}")
        }

        // libcrypto 3 move EVP_PKEY_CTX_set_rsa_keygen_bits from macro to function
        if (_evpPkeyCtxSetRSAKeygenBits(evpPkeyCtx, keySize) <= 0) {
            EVP_PKEY_CTX_free(evpPkeyCtx)
            error("Failed to set key bit for rsa evp pkey: ${getOpenSSLError()}")
        }

        val evpPKey = EVP_PKEY_new() ?: kotlin.run {
            EVP_PKEY_CTX_free(evpPkeyCtx)
            error("Failed to create evp pkey: ${getOpenSSLError()}")
        }

        if (EVP_PKEY_keygen(evpPkeyCtx, ref(evpPKey)) <= 0) {
            EVP_PKEY_free(evpPKey)
            EVP_PKEY_CTX_free(evpPkeyCtx)
            error("Failed to generate rsa key pair: ${getOpenSSLError()}")
        }

        val publicPemKey = dumpPKey(evpPKey) { b, k -> PEM_write_bio_PUBKEY(b, k) }
            ?: kotlin.run {
                EVP_PKEY_free(evpPKey)
                EVP_PKEY_CTX_free(evpPkeyCtx)
                error("Failed to dump rsa public key: ${getOpenSSLError()}")
            }
        val privatePemKey = dumpPKey(evpPKey) { b, k ->
            PEM_write_bio_PKCS8PrivateKey(b, k, null, null, 0, null, null)
        } ?: kotlin.run {
            EVP_PKEY_free(evpPKey)
            EVP_PKEY_CTX_free(evpPkeyCtx)
            error("Failed to dump rsa public key: ${getOpenSSLError()}")
        }

        EVP_PKEY_free(evpPKey)
        EVP_PKEY_CTX_free(evpPkeyCtx)

        return RSAKeyPair(publicPemKey, privatePemKey)
    }
}

@OptIn(UnsafeNumber::class)
private inline fun MemScope.dumpPKey(
    evpPKey: CPointer<EVP_PKEY>,
    dumper: (CPointer<BIO>, CPointer<EVP_PKEY>) -> Unit
): String? {
    val bio = BIO_new(BIO_s_mem()) ?: error("Failed to init mem BIO: ${getOpenSSLError()}")

    dumper(bio, evpPKey)
    BIO_ctrl(bio, BIO_CTRL_FLUSH, 0, null)

    val pKeyBuf = allocPointerTo<ByteVar>()
    BIO_ctrl(bio, BIO_CTRL_INFO, 0, pKeyBuf.ptr)

    return pKeyBuf.value?.toKString().also { BIO_free(bio) }
}

private fun MemScope.loadPKey(
    plainPemKey: String,
    reader: (CPointer<BIO>) -> CPointer<RSA>?
): CPointer<RSA>? {
    val bio = BIO_new(BIO_s_mem()) ?: error("Failed to init mem BIO: ${getOpenSSLError()}")

    return plainPemKey.encodeToByteArray().usePinned {
        BIO_write(bio, it.addressOf(0), it.get().size)
        reader(bio)
    }
}

internal actual fun rsaEncryptWithX509PubKey(input: ByteArray, plainPubPemKey: String, seed: Long): ByteArray {
    memScoped {
        val pubPKey = loadPKey(plainPubPemKey) {
            PEM_read_bio_RSA_PUBKEY(it, null, null, null)
        } ?: error("Failed to read pem key from BIO: ${getOpenSSLError()}")

        val pinnedInput = input.pin()
        val encMsg = ByteArray(4096).pin()

        val encMsgLen = RSA_public_encrypt(
            flen = pinnedInput.get().size,
            from = pinnedInput.addressOf(0).reinterpret(),
            to = encMsg.addressOf(0).reinterpret(),
            rsa = pubPKey,
            padding = RSA_PKCS1_PADDING
        )
        if (encMsgLen <= 0) {
            pinnedInput.unpin()
            encMsg.unpin()
            RSA_free(pubPKey)
            error("Failed to do rsa decrypt: ${getOpenSSLError()}")
        }

        return encMsg.get().copyOf(encMsgLen).also {
            pinnedInput.unpin()
            encMsg.unpin()
            RSA_free(pubPKey)
        }

        /*if (1 != EVP_SealInit(
                ctx = evpCipherCtx,
                type = aes256CBC,
                ek = encKey.ptr,
                ekl = encKeyLen.ptr,
                pubk = ref(pubPKey),
                iv = iv.ptr,
                npubk = 1,
            )
        ) {
            free(encKey.ptr, encKeyLen.ptr, iv.ptr)
            EVP_CIPHER_CTX_free(evpCipherCtx)
            error("Failed to init evp seal: ${getOpenSSLError()}")
        }
        println("total size: ${pinnedInput.get().size + 1 + EVP_MAX_IV_LENGTH}")
        val encMsgLen = alloc<size_tVar>().apply { value = 0u }
        val blockSize = alloc<size_tVar>().apply { value = 0u }

        if (1 != EVP_EncryptUpdate(
                ctx = evpCipherCtx,
                out = encMsg.addressOf(encMsgLen.value.convert()).reinterpret(),
                outl = blockSize.ptr.reinterpret(),
                `in` = pinnedInput.addressOf(0).reinterpret(),
                inl = pinnedInput.get().size
            )
        ) {
            pinnedInput.unpin()
            encMsg.unpin()
            free(encMsgLen.ptr, blockSize.ptr, encKey.ptr, encKeyLen.ptr, iv.ptr)
            EVP_CIPHER_CTX_free(evpCipherCtx)
            error("Failed to update evp seal: ${getOpenSSLError()}")
        }
        println("${encMsgLen.value}, ${blockSize.value}")
        encMsgLen.value += blockSize.value
        println("${encMsg.addressOf(0)}, ${encMsg.addressOf(encMsgLen.value.convert())}")

        if (1 != EVP_SealFinal(
                ctx = evpCipherCtx,
                out = encMsg.addressOf(encMsgLen.value.convert()).reinterpret(),
                outl = blockSize.ptr.reinterpret()
            )
        ) {
            pinnedInput.unpin()
            encMsg.unpin()
            free(encMsgLen.ptr, blockSize.ptr, encKey.ptr, encKeyLen.ptr, iv.ptr)
            EVP_CIPHER_CTX_free(evpCipherCtx)
            error("Failed to do final evp seal: ${getOpenSSLError()}")
        }
        println("${encMsgLen.value}, ${blockSize.value}")
        encMsgLen.value += blockSize.value

        return encMsg.get().copyOf(encMsgLen.value.convert()).also {
            encMsg.unpin()
            pinnedInput.unpin()
            EVP_CIPHER_CTX_free(evpCipherCtx)
        }.toByteArray()*/
    }
}

internal actual fun rsaDecryptWithPKCS8PrivKey(input: ByteArray, plainPrivPemKey: String, seed: Long): ByteArray {
    memScoped {
        val evpCipherCtx = EVP_CIPHER_CTX_new()
            ?: error("Failed to create evp cipher context: ${getOpenSSLError()}")

        val privKey = loadPKey(plainPrivPemKey) {
            PEM_read_bio_RSAPrivateKey(it, null, null, null)
        } ?: kotlin.run {
            EVP_CIPHER_CTX_free(evpCipherCtx)
            error("Failed to read pem key from BIO: ${getOpenSSLError()}")
        }

        val pinnedInput = input.pin()
        val encMsg = UByteArray(4096).pin()

        val encMsgLen = RSA_private_decrypt(
            flen = pinnedInput.get().size,
            from = pinnedInput.addressOf(0).reinterpret(),
            to = encMsg.addressOf(0).reinterpret(),
            rsa = privKey,
            padding = RSA_PKCS1_PADDING
        )
        if (encMsgLen <= 0) {
            pinnedInput.unpin()
            encMsg.unpin()
            RSA_free(privKey)
            error("Failed to do rsa decrypt: ${getOpenSSLError()}")
        }

        return encMsg.get().copyOf(encMsgLen).toByteArray().also {
            pinnedInput.unpin()
            encMsg.unpin()
            RSA_free(privKey)
        }

        /*println(dumpPKey(privKey) { b, k -> PEM_write_bio_PKCS8PrivateKey(b, k, null, null, 0, null, null) })

        val decKeyLen = EVP_PKEY_get_size(privKey)
        println("evp_pkey_size: $decKeyLen")
        val decKey = ByteArray(decKeyLen).pin()
        val pinnedIv = ByteArray(16).pin()

        if (1 != EVP_OpenInit(
                ctx = evpCipherCtx,
                type = aes256CBC,
                ek = decKey.addressOf(0).reinterpret(),
                ekl = decKeyLen,
                iv = pinnedIv.addressOf(0).reinterpret(),
                priv = privKey
            )
        ) {
            pinnedIv.unpin()
            decKey.unpin()
            EVP_CIPHER_CTX_free(evpCipherCtx)
            error("Failed to init evp open: ${getOpenSSLError()}")
        }
        println("init")

        val pinnedInput = input.pin()
        val decMsg = ByteArray(
            pinnedInput.get().size + EVP_CIPHER_CTX_get_block_size(evpCipherCtx)
        ).pin()
        val decMsgLen = alloc<size_tVar>().apply { value = 0u }
        val blockSize = alloc<size_tVar>().apply { value = 0u }

        if (1 != EVP_DecryptUpdate(
                ctx = evpCipherCtx,
                out = decMsg.addressOf(0).reinterpret(),
                outl = blockSize.ptr.reinterpret(),
                `in` = pinnedInput.addressOf(0).reinterpret(),
                inl = pinnedInput.get().size
            )
        ) {
            pinnedInput.unpin()
            decMsg.unpin()
            pinnedIv.unpin()
            decKey.unpin()
            free(decMsgLen.ptr, blockSize.ptr)
            EVP_CIPHER_CTX_free(evpCipherCtx)
            error("Failed to update evp open: ${getOpenSSLError()}")
        }
        decMsgLen.value += blockSize.value
        println("update")

        if (1 != EVP_OpenFinal(
                ctx = evpCipherCtx,
                out = decMsg.addressOf(decMsgLen.value.convert()).reinterpret(),
                outl = blockSize.ptr.reinterpret()
            )
        ) {
            pinnedInput.unpin()
            decMsg.unpin()
            pinnedIv.unpin()
            decKey.unpin()
            free(decMsgLen.ptr, blockSize.ptr)
            EVP_CIPHER_CTX_free(evpCipherCtx)
            error("Failed to do final evp open: ${getOpenSSLError()}")
        }
        decMsgLen.value += blockSize.value
        println("final")

        return decMsg.get().copyOf(decMsgLen.value.convert()).also {
            decMsg.unpin()
            pinnedInput.unpin()
            pinnedIv.unpin()
            decKey.unpin()
            EVP_CIPHER_CTX_free(evpCipherCtx)
        }*/
    }
}
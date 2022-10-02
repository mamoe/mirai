/*
 * Copyright 2019-2022 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import net.mamoe.mirai.internal.test.AbstractTest
import net.mamoe.mirai.utils.toUHexString
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

internal class EcdhTest : AbstractTest() {

    @Test
    fun `can generate key pair`() {
        val alice = Ecdh.Instance.generateKeyPair()
        val bob = Ecdh.Instance.generateKeyPair()

        val aliceSecret = Ecdh.Instance.calculateShareKey(bob.public, alice.private)
        val bobSecret = Ecdh.Instance.calculateShareKey(alice.public, bob.private)

        println(aliceSecret.toUHexString())
        assertContentEquals(aliceSecret, bobSecret)
    }

    @Test
    fun `can export and import public keys`() {
        val alice = Ecdh.Instance.generateKeyPair()

        println(alice)
        val publicKey = Ecdh.Instance.exportPublicKey(alice.public)
        println(publicKey.toUHexString())
        assertEquals(0x04, publicKey.first())

        val importedAlicePubKey = Ecdh.Instance.importPublicKey(publicKey)
        assertEquals(alice.public, importedAlicePubKey)
    }

    /*

	EC_KEY *alice = create_key();
	EC_KEY *bob = create_key();
	assert(alice != NULL && bob != NULL);

	const EC_POINT *alice_public = EC_KEY_get0_public_key(alice);
	const EC_POINT *bob_public = EC_KEY_get0_public_key(bob);

	size_t alice_secret_len;
	size_t bob_secret_len;

	unsigned char *alice_secret = get_secret(alice, bob_public, &alice_secret_len);
	unsigned char *bob_secret = get_secret(bob, alice_public, &bob_secret_len);
	assert(alice_secret != NULL && bob_secret != NULL
		&& alice_secret_len == bob_secret_len);

	for (int i = 0; i < alice_secret_len; i++)
		assert(alice_secret[i] == bob_secret[i]);

	EC_KEY_free(alice);
	EC_KEY_free(bob);
	OPENSSL_free(alice_secret);
	OPENSSL_free(bob_secret);

	return 0;
     */
}
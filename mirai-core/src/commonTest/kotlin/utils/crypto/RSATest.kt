/*
 * Copyright 2019-2023 Mamoe Technologies and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/mamoe/mirai/blob/dev/LICENSE
 */

package net.mamoe.mirai.internal.utils.crypto

import net.mamoe.mirai.internal.testFramework.*
import net.mamoe.mirai.internal.testFramework.rules.DisabledOnJvmLikePlatform
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertEquals

@DisabledOnJvmLikePlatform(Platform.AndroidUnitTest::class)
class RSATest {
    @TestFactory
    fun `can generate key pair`(): DynamicTestsResult {
        return runDynamicTests(buildList {
            repeat(4) { exp ->
                val keySize = 2.0.pow(9 + exp).toInt()
                add(dynamicTest("RSAKeyGenLength$keySize") {
                    val rsaKeyPair = generateRSAKeyPair(keySize)
                    println("RSA keygen test #${exp + 1}: keySize = $keySize")
                    println(rsaKeyPair.plainPubPemKey)
                    println(rsaKeyPair.plainPrivPemKey)
                })
            }
        })
    }

    @Test
    fun `can do crypto with generated key`() {
        val keyPair = generateRSAKeyPair(2048)

        val plainText = buildString {
            append("Use of this source code is governed by the GNU AGPLv3 license ")
            append("that can be found through the following link. ")
        }

        println(
            "RSA crypto test: plainTextLength: ${plainText.length}, " +
                    "pubKey = ${keyPair.plainPubPemKey}, " +
                    "privKey = ${keyPair.plainPrivPemKey}"
        )
        val enc = rsaEncryptWithX509PubKey(plainText.encodeToByteArray(), keyPair.plainPubPemKey, 0)
        println("rsa encrypt: data size=${enc.size}")
        val decrypted = rsaDecryptWithPKCS8PrivKey(enc, keyPair.plainPrivPemKey, 0)

        assertEquals(plainText, decrypted.decodeToString())
    }

    @Test
    fun `can do crypto with specific key`() {
        val pubKey = """
-----BEGIN PUBLIC KEY-----
MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAr0KIpsWPW2JA7ShJI18o
wPv3Ip3Y6a0OkJOozfVlQDOjjUG6niDcrPIm+OpL7pCAzwc+h8d9sFH5c/7/bY4i
wKK6CpSaOYgQQ03P31KhzmXGJ4LVSxUIV0bhuDYQr+sU5Gu97onF8Ko8MELtWTPw
KP1dfqZ3PrK8QBH6su0GlB8onYFtzDUckr2wCrrJ1cR4L1Dg5f2egE75l1cliAIM
4FH1WFU2musfdEuCo5oPgl8ZPPLrQwp8qm9w7xBvbgbmfPTjPBC0N4gcelVzvdfC
eU8vpIlLP/9W5nkdqF6CWzjE3dIx2btOH4QDDyogDSLRAvcKN5/1EIZeu2FTbw9k
3QIDAQAB
-----END PUBLIC KEY-----
        """.trimIndent()

        val privKey = """
-----BEGIN PRIVATE KEY-----
MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCvQoimxY9bYkDt
KEkjXyjA+/cindjprQ6Qk6jN9WVAM6ONQbqeINys8ib46kvukIDPBz6Hx32wUflz
/v9tjiLAoroKlJo5iBBDTc/fUqHOZcYngtVLFQhXRuG4NhCv6xTka73uicXwqjww
Qu1ZM/Ao/V1+pnc+srxAEfqy7QaUHyidgW3MNRySvbAKusnVxHgvUODl/Z6ATvmX
VyWIAgzgUfVYVTaa6x90S4Kjmg+CXxk88utDCnyqb3DvEG9uBuZ89OM8ELQ3iBx6
VXO918J5Ty+kiUs//1bmeR2oXoJbOMTd0jHZu04fhAMPKiANItEC9wo3n/UQhl67
YVNvD2TdAgMBAAECggEAExREqRcfvJyNIeQ9Vg7xclTbuhaB+ypeSAnzGfzJeXxF
pUaPCNDeBSvVZ0qmWoG7rA4HViO3AJ9j7ydG6kfLa7orU6SKx5GS56jMZOzrdXsp
37pD+wj+n/W08+da2LPYUeeSxSmVdVYq+DwI96mKTwQKDhQULiyqBrWOW7Um/q/Z
JC8kJWKEmlNideDQHJxZViRyOdKvJtiwvoBLe1Jvbx7oMbpZnf20gV8C7UU7U38R
e0BKT6HBUHXuOOp2tFFpX6dySkJqW7Jijv04B/KnDYaSWD8TtaQfPfAhkiEVA17E
Ret04PnPMiYCkSVakO0MEeFpwb01vPca4Z64zgf7EQKBgQDyU6wsO3v8L1OlV7tx
7+T0PuOqeo7MWESSn18LeyOP2Y+fDtHKMUFULeYH1UZaGsZJvW+P+c8Mvyitbcvv
SZPTR0Dg+1HueXWkNTejs8Z2BKpPmIPaVLz5FxhV7hV2hKgII/yhyRoiWrTiawLg
ocOnYSostg+tt6kT8U2QPKhg9QKBgQC5Jho1nZ3pFPVu2rV/o9VvN7bGfn1M2o8k
9PQjLZQYiXJvPP0tNlvAOHk9cAqYecHJ3wDVacZWmLicU8xmNSFmmN6Vs9jj6km4
CWq0/wuTUO/fiH+oHZb6+JM63RXbASWyNK+WwmZtDryNBGRB5zbeCAK8tFsRCJDw
19WQUzljSQKBgCWKkuzTVlTuXA4MdmyjVpwENi8OB5tevVjdudLEg/DgKqDgod2q
Hc3VwoJKJzkEVt3LrEHo2IvH/ZxIm0R56J3dtw5jwQCp7nC/EdyZmFBmTqBAJ4Um
hZQtYMbHOKoAySthr9y8lADofodpPqvgQ7hllCwTFIC8KER/qJ2E2C0VAoGATLUM
hsoWckrMpHDYYVlvQ/TBNNuS7hRe2eDihPCNOt03G/8YpXKv8KN1F48j1KgdMZXC
sqhwE9CSK7JMLMw2WltbXIp2gXa/tA+yteo00YPm3aWfvfcEZlY2KV0PgPyosXxC
gyNnbCd+1q3LG8K/aJ3JBIV0dUonQqEpSfIxBIECgYArO3Iw+LvoePjq4yHheyEM
rz6d6RB+i1Q7ExBK7lbZxN17HmKiOewwI772zEo28IY9sIHugV7rW1vQVs3bnzgk
ExDGjYWZSKHfs+3mvrLNRIx/IsVqqwlXt5oO9TspSh68ASvmXN51dmduxRrSuScq
8a49uOr675SyFCJTIdF/Ag==
-----END PRIVATE KEY-----
""".trimIndent()

        val plainText = buildString {
            append("Use of this source code is governed by the GNU AGPLv3 license ")
            append("that can be found through the following link. ")
        }

        val enc = rsaEncryptWithX509PubKey(plainText.encodeToByteArray(), pubKey, 0)
        val dec = rsaDecryptWithPKCS8PrivKey(enc, privKey, 0)

        assertEquals(plainText, dec.decodeToString())

    }
}
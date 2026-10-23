package com.ftsinan.myvault.data.backup

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.crypto.AEADBadTagException

class BackupCryptoTest {

    @Test
    fun `password backup round trip verifies at EOF`() {
        val plain = "My Vault backup payload".toByteArray(Charsets.UTF_8)
        val encrypted = encrypt(plain, "correct horse battery staple".toCharArray())

        val input = ByteArrayInputStream(encrypted)
        val header = BackupCrypto.readHeader(input)
        val decrypted = BackupCrypto.decryptingStream(
            input = input,
            header = header,
            password = "correct horse battery staple".toCharArray()
        ).readBytes()

        assertEquals(BackupProtection.PASSWORD, header.protection)
        assertArrayEquals(plain, decrypted)
    }

    @Test
    fun `wrong password fails authentication`() {
        val encrypted = encrypt(
            "secret".toByteArray(Charsets.UTF_8),
            "right password".toCharArray()
        )
        val input = ByteArrayInputStream(encrypted)
        val header = BackupCrypto.readHeader(input)

        try {
            BackupCrypto.decryptingStream(
                input = input,
                header = header,
                password = "wrong password".toCharArray()
            ).readBytes()
            fail("Wrong password should not decrypt")
        } catch (_: AEADBadTagException) {
            // Expected.
        }
    }

    @Test
    fun `tampered password backup fails authentication`() {
        val encrypted = encrypt(
            "secret".toByteArray(Charsets.UTF_8),
            "right password".toCharArray()
        )
        encrypted[encrypted.lastIndex] = (encrypted.last().toInt() xor 1).toByte()
        val input = ByteArrayInputStream(encrypted)
        val header = BackupCrypto.readHeader(input)

        try {
            BackupCrypto.decryptingStream(
                input = input,
                header = header,
                password = "right password".toCharArray()
            ).readBytes()
            fail("Tampered backup should not decrypt")
        } catch (_: AEADBadTagException) {
            // Expected.
        }
    }

    @Test
    fun `unprotected backup keeps payload readable after header`() {
        val plain = byteArrayOf(1, 2, 3, 4, 5)
        val output = ByteArrayOutputStream()
        BackupCrypto.encryptingStream(
            output = output,
            protection = BackupProtection.NONE,
            password = null,
            iterations = 100_000
        ).use { it.write(plain) }

        val input = ByteArrayInputStream(output.toByteArray())
        val header = BackupCrypto.readHeader(input)
        val restored = BackupCrypto.decryptingStream(input, header, null).readBytes()

        assertEquals(BackupProtection.NONE, header.protection)
        assertArrayEquals(plain, restored)
    }

    private fun encrypt(plain: ByteArray, password: CharArray): ByteArray {
        val output = ByteArrayOutputStream()
        BackupCrypto.encryptingStream(
            output = output,
            protection = BackupProtection.PASSWORD,
            password = password,
            iterations = 100_000
        ).use { it.write(plain) }
        return output.toByteArray()
    }
}

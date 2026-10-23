package com.ftsinan.myvault.data.backup

import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

enum class BackupProtection {
    NONE,
    PASSWORD
}

data class BackupHeader(
    val version: Int,
    val protection: BackupProtection,
    val salt: ByteArray? = null,
    val iterations: Int = 0,
    val iv: ByteArray? = null
)

/**
 * Portable backup encryption. This is intentionally separate from the
 * device-bound Android Keystore file encryption: a password backup can be
 * restored on another phone, while an unprotected backup is a plain ZIP
 * payload behind the same My Vault header.
 */
object BackupCrypto {
    const val FORMAT_VERSION = 1
    const val DEFAULT_ITERATIONS = 600_000

    private val MAGIC = "MVBK".toByteArray(Charsets.US_ASCII)
    private const val MIN_ITERATIONS = 100_000
    private const val MAX_ITERATIONS = 2_000_000
    private const val SALT_BYTES = 16
    private const val IV_BYTES = 12
    private const val KEY_BITS = 256
    private const val GCM_TAG_BITS = 128
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KDF = "PBKDF2WithHmacSHA256"

    /**
     * Writes the My Vault header to [output] and returns the stream that ZIP
     * data should be written to. The caller must close the returned stream.
     */
    fun encryptingStream(
        output: OutputStream,
        protection: BackupProtection,
        password: CharArray?
    ): OutputStream = encryptingStream(output, protection, password, DEFAULT_ITERATIONS)

    internal fun encryptingStream(
        output: OutputStream,
        protection: BackupProtection,
        password: CharArray?,
        iterations: Int
    ): OutputStream {
        return when (protection) {
            BackupProtection.NONE -> {
                require(password == null || password.isEmpty()) {
                    "Password supplied for an unprotected backup"
                }
                val header = BackupHeader(FORMAT_VERSION, BackupProtection.NONE)
                output.write(headerBytes(header))
                output.flush()
                output
            }
            BackupProtection.PASSWORD -> {
                require(password != null && password.isNotEmpty()) {
                    "Backup password is required"
                }
                require(iterations in MIN_ITERATIONS..MAX_ITERATIONS) {
                    "Unsafe backup KDF iteration count"
                }
                val random = SecureRandom()
                val salt = ByteArray(SALT_BYTES).also { random.nextBytes(it) }
                val iv = ByteArray(IV_BYTES).also { random.nextBytes(it) }
                val key = deriveKey(password, salt, iterations)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.ENCRYPT_MODE,
                    key,
                    GCMParameterSpec(GCM_TAG_BITS, iv)
                )
                val header = BackupHeader(
                    version = FORMAT_VERSION,
                    protection = BackupProtection.PASSWORD,
                    salt = salt,
                    iterations = iterations,
                    iv = iv.copyOf()
                )
                val encodedHeader = headerBytes(header)
                cipher.updateAAD(encodedHeader)
                output.write(encodedHeader)
                output.flush()
                CipherOutputStream(output, cipher)
            }
        }
    }

    fun readHeader(input: InputStream): BackupHeader {
        val data = DataInputStream(input)
        val magic = ByteArray(MAGIC.size)
        data.readFully(magic)
        require(magic.contentEquals(MAGIC)) { "Not a My Vault backup file" }
        val version = data.readUnsignedByte()
        require(version == FORMAT_VERSION) { "Unsupported backup version: $version" }
        val protection = when (data.readUnsignedByte()) {
            0 -> BackupProtection.NONE
            1 -> BackupProtection.PASSWORD
            else -> error("Unknown backup protection mode")
        }
        if (protection == BackupProtection.NONE) {
            return BackupHeader(version, protection)
        }
        val saltLength = data.readUnsignedByte()
        require(saltLength in 12..32) { "Invalid backup salt" }
        val salt = ByteArray(saltLength).also { data.readFully(it) }
        val iterations = data.readInt()
        require(iterations in MIN_ITERATIONS..MAX_ITERATIONS) {
            "Invalid backup KDF iteration count"
        }
        val ivLength = data.readUnsignedByte()
        require(ivLength in 12..16) { "Invalid backup IV" }
        val iv = ByteArray(ivLength).also { data.readFully(it) }
        return BackupHeader(
            version = version,
            protection = protection,
            salt = salt,
            iterations = iterations,
            iv = iv
        )
    }

    /**
     * Returns the ZIP payload stream. For password backups the caller must
     * read this stream to EOF before trusting staged data: that final read is
     * what verifies the AES-GCM authentication tag.
     */
    fun decryptingStream(
        input: InputStream,
        header: BackupHeader,
        password: CharArray?
    ): InputStream {
        return when (header.protection) {
            BackupProtection.NONE -> BufferedInputStream(input)
            BackupProtection.PASSWORD -> {
                require(password != null && password.isNotEmpty()) {
                    "Backup password is required"
                }
                val salt = requireNotNull(header.salt) { "Backup salt is missing" }
                val iv = requireNotNull(header.iv) { "Backup IV is missing" }
                val key = deriveKey(password, salt, header.iterations)
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    key,
                    GCMParameterSpec(GCM_TAG_BITS, iv)
                )
                cipher.updateAAD(headerBytes(header))
                GcmDecryptInputStream(input, cipher)
            }
        }
    }

    private fun headerBytes(header: BackupHeader): ByteArray {
        val bytes = ByteArrayOutputStream()
        DataOutputStream(bytes).use { out ->
            out.write(MAGIC)
            out.writeByte(header.version)
            out.writeByte(if (header.protection == BackupProtection.PASSWORD) 1 else 0)
            if (header.protection == BackupProtection.PASSWORD) {
                val salt = requireNotNull(header.salt)
                val iv = requireNotNull(header.iv)
                out.writeByte(salt.size)
                out.write(salt)
                out.writeInt(header.iterations)
                out.writeByte(iv.size)
                out.write(iv)
            }
            out.flush()
        }
        return bytes.toByteArray()
    }

    private fun deriveKey(
        password: CharArray,
        salt: ByteArray,
        iterations: Int
    ): SecretKeySpec {
        val spec = PBEKeySpec(password, salt, iterations, KEY_BITS)
        try {
            val keyBytes = SecretKeyFactory.getInstance(KDF)
                .generateSecret(spec)
                .encoded
            return SecretKeySpec(keyBytes, "AES")
        } finally {
            spec.clearPassword()
            password.fill('\u0000')
        }
    }

    /**
     * CipherInputStream can hide authentication failures on some runtimes.
     * This small stream calls [Cipher.doFinal] itself at physical EOF, so a
     * wrong password or modified backup reliably throws before restore commit.
     */
    private class GcmDecryptInputStream(
        private val input: InputStream,
        private val cipher: Cipher
    ) : InputStream() {
        private val cipherBuffer = ByteArray(DEFAULT_BUFFER_SIZE)
        private var plainBuffer = ByteArray(0)
        private var plainPosition = 0
        private var inputFinished = false
        private var finalDone = false
        private var closed = false

        override fun read(): Int {
            val one = ByteArray(1)
            val count = read(one, 0, 1)
            return if (count == -1) -1 else one[0].toInt() and 0xFF
        }

        override fun read(target: ByteArray, offset: Int, length: Int): Int {
            if (offset < 0 || length < 0 || length > target.size - offset) {
                throw IndexOutOfBoundsException()
            }
            if (length == 0) return 0
            if (closed) return -1
            while (plainPosition >= plainBuffer.size) {
                if (finalDone) return -1
                refill()
            }
            val count = minOf(length, plainBuffer.size - plainPosition)
            plainBuffer.copyInto(target, offset, plainPosition, plainPosition + count)
            plainPosition += count
            return count
        }

        private fun refill() {
            if (!inputFinished) {
                val count = input.read(cipherBuffer)
                if (count == -1) {
                    inputFinished = true
                    plainBuffer = cipher.doFinal()
                    plainPosition = 0
                    finalDone = true
                } else if (count > 0) {
                    plainBuffer = cipher.update(cipherBuffer, 0, count) ?: ByteArray(0)
                    plainPosition = 0
                } else {
                    plainBuffer = ByteArray(0)
                    plainPosition = 0
                }
            } else if (!finalDone) {
                plainBuffer = cipher.doFinal()
                plainPosition = 0
                finalDone = true
            }
        }

        override fun close() {
            if (!closed) {
                closed = true
                input.close()
            }
        }
    }
}

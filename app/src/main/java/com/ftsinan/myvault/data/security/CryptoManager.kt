package com.ftsinan.myvault.data.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.io.File
import java.io.InputStream
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Encrypts vault files with AES-256/GCM using a key held in the
 * Android Keystore (hardware-backed where available).
 *
 * Encrypted file format:
 *   byte 0      : IV length
 *   bytes 1..n  : IV
 *   rest        : ciphertext (includes GCM auth tag)
 *
 * NOTE: files are loaded fully into memory — fine for ID photos and
 * typical certificate PDFs (a few MB). Switch to streaming cipher I/O
 * if you later allow very large files.
 */
class CryptoManager {

    private val keyStore: KeyStore =
        KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }

    private fun getOrCreateKey(): SecretKey {
        (keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry)?.let {
            return it.secretKey
        }
        val generator =
            KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        val spec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .build()
        generator.init(spec)
        return generator.generateKey()
    }

    /** Reads [input] fully, encrypts it and writes the result to [outFile]. */
    fun encrypt(input: InputStream, outFile: File) {
        val plainBytes = input.readBytes()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val iv = cipher.iv
        val cipherBytes = cipher.doFinal(plainBytes)

        outFile.parentFile?.mkdirs()
        outFile.outputStream().use { out ->
            out.write(iv.size)
            out.write(iv)
            out.write(cipherBytes)
        }
    }

    /** Decrypts [encryptedFile] and returns the original bytes. */
    fun decryptToBytes(encryptedFile: File): ByteArray {
        val allBytes = encryptedFile.readBytes()
        require(allBytes.isNotEmpty()) { "Encrypted file is empty" }
        val ivSize = allBytes[0].toInt()
        val iv = allBytes.copyOfRange(1, 1 + ivSize)
        val cipherBytes = allBytes.copyOfRange(1 + ivSize, allBytes.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(
            Cipher.DECRYPT_MODE,
            getOrCreateKey(),
            GCMParameterSpec(GCM_TAG_BITS, iv)
        )
        return cipher.doFinal(cipherBytes)
    }

    /** Decrypts [encryptedFile] into [outFile] (used for PDF rendering / sharing). */
    fun decryptToFile(encryptedFile: File, outFile: File): File {
        outFile.parentFile?.mkdirs()
        outFile.writeBytes(decryptToBytes(encryptedFile))
        return outFile
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "myvault_file_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_BITS = 128
    }
}

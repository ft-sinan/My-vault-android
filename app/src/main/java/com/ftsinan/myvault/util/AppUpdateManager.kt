package com.ftsinan.myvault.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

/** Pure version helpers, kept Android-free so they can be unit tested. */
object UpdateVersion {
    fun normalize(version: String): String = version
        .trim()
        .removePrefix("v")
        .removePrefix("V")
        .substringBefore('-')
        .substringBefore('+')

    fun isNewer(latest: String, current: String): Boolean {
        val latestParts = normalize(latest).split('.').map { it.toIntOrNull() ?: 0 }
        val currentParts = normalize(current).split('.').map { it.toIntOrNull() ?: 0 }
        val max = maxOf(latestParts.size, currentParts.size)
        for (index in 0 until max) {
            val newest = latestParts.getOrElse(index) { 0 }
            val installed = currentParts.getOrElse(index) { 0 }
            if (newest != installed) return newest > installed
        }
        return false
    }
}

/** SHA-256 sidecar parsing, supporting both bare hashes and sha256sum output. */
object ChecksumParser {
    private val sha256Regex = Regex("\\b[a-fA-F0-9]{64}\\b")

    fun expectedHash(checksumText: String, apkName: String): String? {
        checksumText.lineSequence()
            .firstOrNull { line -> line.contains(apkName) && sha256Regex.containsMatchIn(line) }
            ?.let { line -> return sha256Regex.find(line)?.value?.lowercase() }
        return sha256Regex.find(checksumText)?.value?.lowercase()
    }
}

data class UpdateRelease(
    val versionName: String,
    val tagName: String,
    val releaseNotes: String?,
    val apkName: String,
    val apkDownloadUrl: String,
    val apkSizeBytes: Long,
    val checksumDownloadUrl: String?
)

/**
 * GitHub Releases update support. Settings may run a lightweight foreground
 * check when it opens (at most once a day), and users can always check
 * manually. Downloads and installs still require an explicit user action.
 */
object AppUpdateManager {
    private const val LATEST_RELEASE_URL =
        "https://api.github.com/repos/ft-sinan/My-vault-android/releases/latest"

    suspend fun fetchLatestRelease(): UpdateRelease = withContext(Dispatchers.IO) {
        val json = JSONObject(fetchText(LATEST_RELEASE_URL, githubApi = true))
        val tag = json.optString("tag_name").trim()
        require(tag.isNotBlank()) { "Release tag missing" }

        val assets = json.optJSONArray("assets")
        var apkName: String? = null
        var apkUrl: String? = null
        var apkSize = 0L
        var checksumUrl: String? = null
        if (assets != null) {
            for (index in 0 until assets.length()) {
                val asset = assets.getJSONObject(index)
                val name = asset.optString("name")
                val url = asset.optString("browser_download_url")
                val lowerName = name.lowercase()
                when {
                    lowerName.endsWith(".apk") && apkName == null -> {
                        apkName = name
                        apkUrl = url
                        apkSize = asset.optLong("size", 0L)
                    }
                    (lowerName.endsWith(".sha256") || lowerName.contains("checksum")) &&
                        checksumUrl == null -> {
                        checksumUrl = url
                    }
                }
            }
        }
        require(!apkName.isNullOrBlank() && !apkUrl.isNullOrBlank()) {
            "No APK attached to the latest release"
        }
        UpdateRelease(
            versionName = UpdateVersion.normalize(tag),
            tagName = tag,
            releaseNotes = json.optString("body").takeIf { it.isNotBlank() && it != "null" },
            apkName = apkName,
            apkDownloadUrl = apkUrl,
            apkSizeBytes = apkSize,
            checksumDownloadUrl = checksumUrl
        )
    }

    suspend fun downloadReleaseApk(
        context: Context,
        release: UpdateRelease,
        onProgress: (Int?) -> Unit
    ): File = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val safeName = release.apkName.replace(Regex("[^A-Za-z0-9._-]"), "_")
        val destination = File(updatesDir, safeName)
        if (destination.exists()) destination.delete()

        val connection = openConnection(release.apkDownloadUrl, githubApi = false)
        try {
            val responseCode = connection.responseCode
            check(responseCode in 200..299) { "APK download failed: HTTP $responseCode" }
            val total = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connection.contentLengthLong
            } else {
                connection.contentLength.toLong()
            }.takeIf { it > 0 } ?: release.apkSizeBytes.takeIf { it > 0 } ?: -1L

            connection.inputStream.buffered().use { input ->
                destination.outputStream().buffered().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    var lastPercent = -1
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        downloaded += read
                        if (total > 0) {
                            val percent = ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                onProgress(percent)
                            }
                        } else {
                            onProgress(null)
                        }
                    }
                }
            }
            check(destination.length() > 0L) { "Downloaded APK is empty" }
            destination
        } finally {
            connection.disconnect()
        }
    }

    /** Returns null when no checksum was published or it cannot be interpreted. */
    suspend fun verifyChecksum(release: UpdateRelease, apk: File): Boolean? =
        withContext(Dispatchers.IO) {
            val checksumUrl = release.checksumDownloadUrl ?: return@withContext null
            val expected = ChecksumParser.expectedHash(
                checksumText = fetchText(checksumUrl, githubApi = false),
                apkName = release.apkName
            ) ?: return@withContext null
            expected.equals(sha256(apk), ignoreCase = true)
        }

    fun canInstallPackages(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
            context.packageManager.canRequestPackageInstalls()

    fun openInstallPermissionSettings(context: Context) {
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Intent(
                Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")
            )
        } else {
            Intent(Settings.ACTION_SECURITY_SETTINGS)
        }.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun openInstaller(context: Context, apk: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apk
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun fetchText(url: String, githubApi: Boolean): String {
        val connection = openConnection(url, githubApi)
        return try {
            val responseCode = connection.responseCode
            check(responseCode in 200..299) { "Request failed: HTTP $responseCode" }
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }

    private fun openConnection(url: String, githubApi: Boolean): HttpURLConnection =
        (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 20_000
            readTimeout = 30_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "MyVault-Android")
            if (githubApi) {
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }
        }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val read = input.read(buffer)
                if (read < 0) break
                digest.update(buffer, 0, read)
            }
        }
        return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }
}

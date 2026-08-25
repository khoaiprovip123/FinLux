package com.finlux.app.core.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.FileProvider
import com.finlux.app.BuildConfig
import com.finlux.app.core.common.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

data class AppUpdateInfo(
    val latestVersionName: String,
    val latestVersionCode: Int = 0,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
    val sha256Checksum: String = "",
    val isUpdateAvailable: Boolean,
)

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val GITHUB_API_LATEST_RELEASE =
            "https://api.github.com/repos/khoaiprovip123/FinLux/releases/latest"
    }

    suspend fun checkForUpdates(): AppResult<AppUpdateInfo> = withContext(Dispatchers.IO) {
        runCatching {
            val url = URL(GITHUB_API_LATEST_RELEASE)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json")
            connection.setRequestProperty("User-Agent", "FinLux-Android-App")
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return@runCatching AppUpdateInfo(
                    latestVersionName = BuildConfig.VERSION_NAME,
                    latestVersionCode = BuildConfig.VERSION_CODE,
                    releaseTitle = "",
                    releaseNotes = "",
                    downloadUrl = "",
                    sha256Checksum = "",
                    isUpdateAvailable = false,
                )
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseBody)
            val tagName = json.optString("tag_name", "").removePrefix("v").trim()
            val releaseTitle = json.optString("name", "FinLux v$tagName")
            val releaseNotes = json.optString("body", "Bản cập nhật mới của FinLux.")

            var downloadUrl = ""
            var sha256Checksum = ""
            var remoteVersionCode = 0

            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                    }
                    if (name.endsWith(".sha256", ignoreCase = true)) {
                        val shaUrl = asset.optString("browser_download_url", "")
                        if (shaUrl.isNotBlank()) {
                            runCatching {
                                val shaConn = URL(shaUrl).openConnection() as HttpURLConnection
                                shaConn.connectTimeout = 5000
                                shaConn.readTimeout = 5000
                                val text = shaConn.inputStream.bufferedReader().use { it.readText() }
                                sha256Checksum = text.trim().split("\\s+".toRegex()).firstOrNull().orEmpty()
                            }
                        }
                    }
                    if (name.equals("update.json", ignoreCase = true)) {
                        val jsonUrl = asset.optString("browser_download_url", "")
                        if (jsonUrl.isNotBlank()) {
                            runCatching {
                                val jsonConn = URL(jsonUrl).openConnection() as HttpURLConnection
                                jsonConn.connectTimeout = 5000
                                jsonConn.readTimeout = 5000
                                val text = jsonConn.inputStream.bufferedReader().use { it.readText() }
                                val updateJson = JSONObject(text)
                                remoteVersionCode = updateJson.optInt("versionCode", 0)
                                if (sha256Checksum.isBlank()) {
                                    sha256Checksum = updateJson.optString("sha256", "")
                                }
                            }
                        }
                    }
                }
            }

            val currentVersionName = BuildConfig.VERSION_NAME
            val currentVersionCode = BuildConfig.VERSION_CODE
            val hasUpdate = if (remoteVersionCode > 0) {
                remoteVersionCode > currentVersionCode && downloadUrl.isNotBlank()
            } else {
                isNewerVersion(remoteVersion = tagName, currentVersion = currentVersionName) && downloadUrl.isNotBlank()
            }

            AppUpdateInfo(
                latestVersionName = tagName,
                latestVersionCode = remoteVersionCode,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                sha256Checksum = sha256Checksum,
                isUpdateAvailable = hasUpdate,
            )
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.localizedMessage ?: "Không thể kiểm tra bản cập nhật", it) },
        )
    }

    suspend fun downloadApk(
        downloadUrl: String,
        expectedSha256: String = "",
        onProgress: (Float) -> Unit,
    ): AppResult<File> = withContext(Dispatchers.IO) {
        val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
        val apkFile = File(updatesDir, "FinLux-update.apk")
        if (apkFile.exists()) apkFile.delete()

        try {
            val url = URL(downloadUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.instanceFollowRedirects = true
            connection.connectTimeout = 15000
            connection.readTimeout = 30000
            connection.connect()

            val responseCode = connection.responseCode
            val finalConnection = if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP ||
                responseCode == HttpURLConnection.HTTP_MOVED_PERM ||
                responseCode == HttpURLConnection.HTTP_SEE_OTHER
            ) {
                val redirectUrl = connection.getHeaderField("Location")
                val redirect = URL(redirectUrl).openConnection() as HttpURLConnection
                redirect.connectTimeout = 15000
                redirect.readTimeout = 30000
                redirect.connect()
                redirect
            } else {
                connection
            }

            val fileLength = finalConnection.contentLengthLong
            finalConnection.inputStream.use { input ->
                FileOutputStream(apkFile).use { output ->
                    val buffer = ByteArray(8192)
                    var totalRead = 0L
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        if (fileLength > 0) {
                            val progress = (totalRead.toFloat() / fileLength.toFloat()).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                    }
                }
            }

            // Verify integrity of downloaded APK
            val verificationResult = verifyApkIntegrity(apkFile, expectedSha256)
            if (verificationResult is AppResult.Error) {
                if (apkFile.exists()) apkFile.delete()
                return@withContext verificationResult
            }

            AppResult.Success(apkFile)
        } catch (e: Exception) {
            if (apkFile.exists()) apkFile.delete()
            AppResult.Error(e.localizedMessage ?: "Lỗi tải bản cập nhật", e)
        }
    }

    fun verifyApkIntegrity(apkFile: File, expectedSha256: String = ""): AppResult<Unit> {
        if (!apkFile.exists() || apkFile.length() <= 0) {
            return AppResult.Error("File cài đặt không tồn tại hoặc bị lỗi.")
        }

        // 1. Check SHA-256 checksum if provided
        if (expectedSha256.isNotBlank()) {
            val calculatedSha256 = calculateFileSha256(apkFile)
            if (!calculatedSha256.equals(expectedSha256.trim(), ignoreCase = true)) {
                return AppResult.Error("Xác thực SHA-256 thất bại! File APK cập nhật có thể bị hỏng hoặc bị can thiệp.")
            }
        }

        // 2. Verify Package Archive & Package Name
        val pm = context.packageManager
        val archiveInfo: PackageInfo? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.PackageInfoFlags.of(PackageManager.GET_ACTIVITIES.toLong()))
        } else {
            @Suppress("DEPRECATION")
            pm.getPackageArchiveInfo(apkFile.absolutePath, PackageManager.GET_ACTIVITIES)
        }

        if (archiveInfo == null) {
            return AppResult.Error("File APK không đúng định dạng hoặc bị lỗi cấu trúc.")
        }

        if (archiveInfo.packageName != context.packageName) {
            return AppResult.Error("Gói ứng dụng không khớp với FinLux (${archiveInfo.packageName} != ${context.packageName}).")
        }

        // 3. Verify Signing Certificate Digest (Fail-Closed)
        val currentSignatures = getAppSignatures(context.packageName)
        val apkSignatures = getApkSignatures(apkFile.absolutePath)
        if (currentSignatures.isEmpty() || apkSignatures.isEmpty()) {
            return AppResult.Error("Không thể xác minh chứng chỉ số của ứng dụng cập nhật (Chữ ký rỗng).")
        }
        val matches = currentSignatures.any { currentSig ->
            apkSignatures.any { apkSig -> currentSig.equals(apkSig, ignoreCase = true) }
        }
        if (!matches) {
            return AppResult.Error("Chữ ký bảo mật của bản cập nhật không khớp với ứng dụng FinLux đang cài đặt.")
        }

        return AppResult.Success(Unit)
    }

    fun installApk(apkFile: File): AppResult<Unit> = runCatching {
        val verification = verifyApkIntegrity(apkFile)
        if (verification is AppResult.Error) {
            if (apkFile.exists()) apkFile.delete()
            error(verification.message)
        }

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            apkFile,
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }.fold(
        onSuccess = { AppResult.Success(Unit) },
        onFailure = { AppResult.Error(it.localizedMessage ?: "Không thể mở trình cài đặt", it) },
    )

    fun calculateFileSha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        FileInputStream(file).use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }

    private fun getAppSignatures(packageName: String): List<String> = runCatching {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = info.signingInfo
            val certs = if (signingInfo?.hasMultipleSigners() == true) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo?.signingCertificateHistory
            }
            certs?.map { sha256Digest(it.toByteArray()) }.orEmpty()
        } else {
            @Suppress("DEPRECATION")
            val info = pm.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            info.signatures?.map { sha256Digest(it.toByteArray()) }.orEmpty()
        }
    }.getOrDefault(emptyList())

    private fun getApkSignatures(apkPath: String): List<String> = runCatching {
        val pm = context.packageManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNING_CERTIFICATES)
            val signingInfo = info?.signingInfo
            val certs = if (signingInfo?.hasMultipleSigners() == true) {
                signingInfo.apkContentsSigners
            } else {
                signingInfo?.signingCertificateHistory
            }
            certs?.map { sha256Digest(it.toByteArray()) }.orEmpty()
        } else {
            @Suppress("DEPRECATION")
            val info = pm.getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES)
            @Suppress("DEPRECATION")
            info?.signatures?.map { sha256Digest(it.toByteArray()) }.orEmpty()
        }
    }.getOrDefault(emptyList())

    private fun sha256Digest(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }

    fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        if (remoteVersion.isBlank() || currentVersion.isBlank()) return false
        val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
        val maxLength = maxOf(remoteParts.size, currentParts.size)

        for (i in 0 until maxLength) {
            val remote = remoteParts.getOrElse(i) { 0 }
            val current = currentParts.getOrElse(i) { 0 }
            if (remote > current) return true
            if (remote < current) return false
        }
        return false
    }
}

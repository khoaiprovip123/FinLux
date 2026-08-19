package com.finlux.app.core.updater

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.finlux.app.BuildConfig
import com.finlux.app.core.common.AppResult
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

data class AppUpdateInfo(
    val latestVersionName: String,
    val releaseTitle: String,
    val releaseNotes: String,
    val downloadUrl: String,
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
                    releaseTitle = "",
                    releaseNotes = "",
                    downloadUrl = "",
                    isUpdateAvailable = false,
                )
            }

            val responseBody = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseBody)
            val tagName = json.optString("tag_name", "").removePrefix("v").trim()
            val releaseTitle = json.optString("name", "FinLux v$tagName")
            val releaseNotes = json.optString("body", "Bản cập nhật mới của FinLux.")

            var downloadUrl = ""
            val assets = json.optJSONArray("assets")
            if (assets != null) {
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.optString("name", "")
                    if (name.endsWith(".apk", ignoreCase = true)) {
                        downloadUrl = asset.optString("browser_download_url", "")
                        break
                    }
                }
            }

            val currentVersion = BuildConfig.VERSION_NAME
            val hasUpdate = isNewerVersion(remoteVersion = tagName, currentVersion = currentVersion) && downloadUrl.isNotBlank()

            AppUpdateInfo(
                latestVersionName = tagName,
                releaseTitle = releaseTitle,
                releaseNotes = releaseNotes,
                downloadUrl = downloadUrl,
                isUpdateAvailable = hasUpdate,
            )
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.localizedMessage ?: "Không thể kiểm tra bản cập nhật", it) },
        )
    }

    suspend fun downloadApk(
        downloadUrl: String,
        onProgress: (Float) -> Unit,
    ): AppResult<File> = withContext(Dispatchers.IO) {
        runCatching {
            val updatesDir = File(context.cacheDir, "updates").apply { mkdirs() }
            val apkFile = File(updatesDir, "FinLux-update.apk")
            if (apkFile.exists()) apkFile.delete()

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
            apkFile
        }.fold(
            onSuccess = { AppResult.Success(it) },
            onFailure = { AppResult.Error(it.localizedMessage ?: "Lỗi tải bản cập nhật", it) },
        )
    }

    fun installApk(apkFile: File): AppResult<Unit> = runCatching {
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

    fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
        if (remoteVersion.isBlank() || currentVersion.isBlank()) return false
        val remoteParts = remoteVersion.split(".").mapNotNull { it.toIntOrNull() }
        val currentParts = currentVersion.split(".").mapNotNull { it.toIntOrNull() }
        val maxLen = maxOf(remoteParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}

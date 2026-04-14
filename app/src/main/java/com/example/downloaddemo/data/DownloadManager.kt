package com.example.downloaddemo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DownloadManager {
    suspend fun downloadApk(
        url: String,
        destination: File,
        onProgress: (Float) -> Unit
    ) = withContext(Dispatchers.IO) {

        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()

        client.newCall(request).execute().use { response ->

            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}")
            }

            val body = response.body ?: throw IOException("Empty body")
            val contentLength = body.contentLength()

            destination.outputStream().use { output ->
                body.byteStream().use { input ->

                    val buffer = ByteArray(8_192)
                    var totalRead = 0L
                    var read: Int

                    while (true) {

                        ensureActive()

                        read = input.read(buffer)
                        if (read == -1) break

                        output.write(buffer, 0, read)
                        totalRead += read

                        if (contentLength > 0) {
                            onProgress(totalRead.toFloat() / contentLength)
                        }
                    }
                }
            }
        }
    }
}
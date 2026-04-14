package com.example.downloaddemo.ui.download_ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.downloaddemo.data.DownloadManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

class DownloadViewModel(
    val downloadManager: DownloadManager = DownloadManager()
): ViewModel() {
    private val _uiState = MutableStateFlow(DownloadUiState())
    val uiState: StateFlow<DownloadUiState> = _uiState

    private var downloadJob: Job? = null

    fun startDownload(url: String, apkFile: File) {

        downloadJob = viewModelScope.launch {
            try {
                if (apkFile.exists()) {
                    apkFile.delete()
                }
                _uiState.value = _uiState.value.copy(
                    downloadStatus = DownloadStatus.Downloading,
                    progress = 0f
                )

                downloadManager.downloadApk(
                    url = url,
                    destination = apkFile,
                ) { progress ->
                    Log.d("DownloadViewModel", "Download progress: $progress")
                    _uiState.value = _uiState.value.copy(progress = progress)
                }

                Log.d("DownloadViewModel", "Download completed")
                _uiState.value = _uiState.value.copy(
                    progress = 1f,
                    downloadStatus = DownloadStatus.Downloaded
                )
            } catch (e: CancellationException) {
                Log.d("DownloadViewModel", "Download failed: $e")
                apkFile.delete()
                _uiState.value = _uiState.value.copy(downloadStatus = DownloadStatus.Error(e.message ?: "Download cancelled"))
            } catch (e: Exception) {
                Log.e("DownloadViewModel", "Download failed: $e")
                apkFile.delete()
                _uiState.value = _uiState.value.copy(downloadStatus = DownloadStatus.Error(e.message ?: "Unknown error"))
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
    }
}

sealed interface DownloadStatus {
    object Idle: DownloadStatus
    object Downloading: DownloadStatus
    object Downloaded: DownloadStatus
    class Error(val message: String): DownloadStatus
}

data class DownloadUiState(
    val progress: Float = 0f,
    val downloadStatus: DownloadStatus = DownloadStatus.Idle,
)
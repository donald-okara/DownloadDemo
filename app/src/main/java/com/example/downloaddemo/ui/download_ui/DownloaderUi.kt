package com.example.downloaddemo.ui.download_ui

import android.content.Intent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.downloaddemo.ui.theme.DownloadDemoTheme
import java.io.File

@Composable
fun DownloaderUi(
    modifier: Modifier = Modifier,
    viewModel: DownloadViewModel = remember { DownloadViewModel() }
) {
    val appContext = LocalContext.current
    val apkFile = File(appContext.filesDir, "app-prod-debug.apk")


    val uiState by viewModel.uiState.collectAsState()

    DownloaderUiContent(
        modifier = modifier,
        uiState = uiState,
        startDownLoad = {
            viewModel.startDownload(
                url = "https://waasbeneficiaryapp.ams3.cdn.digitaloceanspaces.com/app-prod-debug.apk",
                apkFile = apkFile
            )
        },
        cancelDownload = {
            viewModel.cancelDownload()
        }
    )
}

@Composable
fun DownloaderUiContent(
    uiState: DownloadUiState,
    startDownLoad: () -> Unit,
    cancelDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = "Download App",
            style = MaterialTheme.typography.titleLarge
        )

        if (uiState.downloadStatus == DownloadStatus.Downloading){
            ProgressBar(progress = uiState.progress)
        }
        when (uiState.downloadStatus) {
            DownloadStatus.Downloaded -> InstallApkButton()
            DownloadStatus.Downloading -> {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = cancelDownload
                ) {
                    Text(text = "Cancel")
                }
            }
            is DownloadStatus.Error -> Text(text = uiState.downloadStatus.message)
            DownloadStatus.Idle -> Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = startDownLoad
            ) {
                Text(text = "Download")
            }
        }
    }
}

@Composable
fun ProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 4.dp,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 300),
        label = "progressAnimation",
    )

    val animatedProgressColor by animateColorAsState(
        targetValue = progressColor,
        label = "animatedColor",
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp),
    ) {
        val barWidth = size.width
        val barHeight = height.toPx()
        val radius = barHeight / 2
        val progressWidth = barWidth * animatedProgress

        // Draw background bar
        drawRoundRect(
            color = backgroundColor,
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(radius, radius),
            topLeft = Offset(0f, (size.height - barHeight) / 2),
        )

        // Draw progress with rounded ends
        if (animatedProgress > 0) {
            drawRoundRect(
                color = animatedProgressColor,
                size = Size(progressWidth, barHeight),
                cornerRadius = CornerRadius(radius, radius),
                topLeft = Offset(0f, (size.height - barHeight) / 2),
            )
        }
    }
}

@PreviewLightDark
@Composable
fun DownloaderUiPreview() {
    DownloadDemoTheme {
        Surface{
            DownloaderUiContent(
                uiState = DownloadUiState(progress = 0.5f),
                startDownLoad = {},
                cancelDownload = {}
            )
        }
    }
}

@Composable
fun InstallApkButton(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val apkFile = File(context.filesDir, "app-prod-debug.apk")

    Button(
        modifier = modifier,
        onClick = {
            if (apkFile.exists()) {
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
                }

                // Launch install wizard
                context.startActivity(intent)
            }
        }
    ) {
        Text(text = "Install")
    }
}
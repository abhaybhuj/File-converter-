package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Transform
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.filee.data.model.FileFormat
import com.example.filee.ui.screens.BatchQueueScreen
import com.example.filee.ui.screens.ConvertScreen
import com.example.filee.ui.screens.HistoryScreen
import com.example.filee.ui.screens.RouteExplorerScreen
import com.example.filee.ui.screens.SettingsScreen
import com.example.ui.theme.FileeCyan
import com.example.ui.theme.FileeNavyDark
import com.example.ui.theme.FileeNavySurface
import com.example.ui.theme.FileeNavySurfaceVariant
import com.example.ui.theme.MyApplicationTheme
import com.example.filee.ui.viewmodel.FileeViewModel
import com.example.filee.ui.viewmodel.SelectedFile
import com.example.filee.util.FileUtils

enum class FileeTab(val title: String, val icon: ImageVector, val tag: String) {
    CONVERT("Convert", Icons.Default.Transform, "tab_convert"),
    BATCH("Batch", Icons.Default.Layers, "tab_batch"),
    ROUTES("Routes", Icons.Default.AltRoute, "tab_routes"),
    HISTORY("History", Icons.Default.History, "tab_history"),
    SETTINGS("Settings", Icons.Default.Settings, "tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: FileeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                var currentTab by rememberSaveable { mutableStateOf(FileeTab.CONVERT) }

                // Predictive back handler: Navigate back to Convert tab if on subtab
                if (currentTab != FileeTab.CONVERT) {
                    BackHandler {
                        currentTab = FileeTab.CONVERT
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = FileeNavyDark,
                    contentWindowInsets = WindowInsets.safeDrawing,
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .windowInsetsPadding(WindowInsets.navigationBars)
                                .testTag("main_bottom_nav"),
                            containerColor = FileeNavySurface,
                            tonalElevation = 8.dp
                        ) {
                            FileeTab.entries.forEach { tab ->
                                val selected = currentTab == tab
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = tab.title,
                                            fontSize = 11.sp
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = FileeNavyDark,
                                        selectedTextColor = FileeCyan,
                                        indicatorColor = FileeCyan,
                                        unselectedIconColor = Color(0xFF64748B),
                                        unselectedTextColor = Color(0xFF64748B)
                                    ),
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Crossfade(
                        targetState = currentTab,
                        label = "tab_transition",
                        modifier = Modifier.padding(innerPadding)
                    ) { tab ->
                        when (tab) {
                            FileeTab.CONVERT -> ConvertScreen(
                                viewModel = viewModel,
                                onNavigateToBatch = { currentTab = FileeTab.BATCH }
                            )
                            FileeTab.BATCH -> BatchQueueScreen(
                                viewModel = viewModel
                            )
                            FileeTab.ROUTES -> RouteExplorerScreen(
                                viewModel = viewModel
                            )
                            FileeTab.HISTORY -> HistoryScreen(
                                viewModel = viewModel
                            )
                            FileeTab.SETTINGS -> SettingsScreen(
                                viewModel = viewModel
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent == null) return

        when (intent.action) {
            Intent.ACTION_SEND -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM) ?: intent.data
                if (uri != null) {
                    val name = FileUtils.getFileNameFromUri(this, uri)
                    val bytes = FileUtils.readBytesFromUri(this, uri)
                    if (bytes != null) {
                        val format = FileFormat.fromFileName(name) ?: FileFormat.TXT
                        viewModel.selectFile(name, bytes, format, uri)
                    }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val uris = intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                if (!uris.isNullOrEmpty()) {
                    val list = uris.mapNotNull { uri ->
                        val name = FileUtils.getFileNameFromUri(this, uri)
                        val bytes = FileUtils.readBytesFromUri(this, uri)
                        if (bytes != null) {
                            val format = FileFormat.fromFileName(name) ?: FileFormat.TXT
                            SelectedFile(name, format, bytes, uri)
                        } else null
                    }
                    if (list.isNotEmpty()) {
                        viewModel.addFilesToBatch(list)
                    }
                }
            }
        }
    }
}

package com.ktar.ui.screens.sftp

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktar.core.sftp.SFTPFile
import com.ktar.ui.theme.TerminalGreen
import com.ktar.ui.theme.TerminalRed
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * SFTP Manager Screen.
 * Displays remote files and allows upload/download operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SFTPScreen(
    viewModel: SFTPViewModel,
    sessionId: String,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    val currentPath by viewModel.currentPath.collectAsState()
    val files by viewModel.files.collectAsState()

    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedFileForDownload by remember { mutableStateOf<SFTPFile?>(null) }

    // Initialize SFTP
    LaunchedEffect(sessionId) {
        viewModel.initialize(sessionId)
    }

    // File picker for upload
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val localPath = getPathFromUri(context, it)
            if (localPath != null) {
                val fileName = File(localPath).name
                val remotePath = if (currentPath.endsWith("/")) {
                    "$currentPath$fileName"
                } else {
                    "$currentPath/$fileName"
                }
                viewModel.uploadFile(localPath, remotePath)
            }
        }
    }

    // Storage permission launcher for Android <= 12
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            selectedFileForDownload?.let { file ->
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val localPath = File(downloadsDir, file.name).absolutePath
                viewModel.downloadFile(file.path, localPath)
            }
        }
        selectedFileForDownload = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "SFTP Manager",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            currentPath,
                            fontSize = 12.sp,
                            color = TerminalGreen,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentPath != "/") {
                        IconButton(onClick = { viewModel.navigateUp() }) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Up")
                        }
                    }
                    IconButton(onClick = { viewModel.listRemoteFiles(currentPath) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = TerminalGreen,
                contentColor = Color.Black
            ) {
                Icon(Icons.Default.Upload, contentDescription = "Upload File")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Status message
                when (val currentState = state) {
                    is SFTPState.Loading -> {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth(),
                            color = TerminalGreen
                        )
                    }
                    is SFTPState.Progress -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface)
                                .padding(16.dp)
                        ) {
                            Text(
                                "Transfer Progress: ${currentState.percent}%",
                                color = TerminalGreen,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = currentState.percent / 100f,
                                modifier = Modifier.fillMaxWidth(),
                                color = TerminalGreen
                            )
                            Text(
                                "${formatBytes(currentState.bytesTransferred)} / ${formatBytes(currentState.totalBytes)}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                    is SFTPState.Success -> {
                        MessageCard(
                            message = currentState.message,
                            isError = false,
                            onDismiss = { viewModel.clearState() }
                        )
                    }
                    is SFTPState.Error -> {
                        MessageCard(
                            message = currentState.error,
                            isError = true,
                            onDismiss = { viewModel.clearState() }
                        )
                    }
                    else -> {}
                }

                // File list
                if (files.isEmpty() && state is SFTPState.Listing) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No files found",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp)
                    ) {
                        items(files) { file ->
                            FileListItem(
                                file = file,
                                onClick = {
                                    if (file.isDirectory) {
                                        viewModel.listRemoteFiles(file.path)
                                    } else {
                                        selectedFileForDownload = file
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    // Download confirmation dialog
    selectedFileForDownload?.let { file ->
        AlertDialog(
            onDismissRequest = { selectedFileForDownload = null },
            title = { Text("Download File") },
            text = { Text("Download ${file.name} to Downloads folder?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(
                            Environment.DIRECTORY_DOWNLOADS
                        )
                        val localPath = File(downloadsDir, file.name).absolutePath
                        
                        // Check permission for Android <= 12
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                            permissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        } else {
                            viewModel.downloadFile(file.path, localPath)
                            selectedFileForDownload = null
                        }
                    }
                ) {
                    Text("Download", color = TerminalGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedFileForDownload = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FileListItem(
    file: SFTPFile,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (file.isDirectory) Icons.Default.Folder else Icons.Default.InsertDriveFile,
                contentDescription = if (file.isDirectory) "Directory" else "File",
                tint = if (file.isDirectory) TerminalGreen else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (file.isDirectory) "Directory" else formatBytes(file.size),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    
                    Text(
                        text = formatDate(file.lastModified),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Text(
                    text = file.permissions,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                )
            }
            
            if (!file.isDirectory) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = TerminalGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MessageCard(
    message: String,
    isError: Boolean,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isError) TerminalRed.copy(alpha = 0.2f) else TerminalGreen.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (isError) TerminalRed else TerminalGreen
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = message,
                    color = if (isError) TerminalRed else TerminalGreen,
                    fontSize = 14.sp
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Helper functions
private fun formatBytes(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        bytes < 1024 * 1024 * 1024 -> "${bytes / (1024 * 1024)} MB"
        else -> "${bytes / (1024 * 1024 * 1024)} GB"
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun getPathFromUri(context: Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.cacheDir, "upload_${System.currentTimeMillis()}")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        file.absolutePath
    } catch (e: Exception) {
        null
    }
}

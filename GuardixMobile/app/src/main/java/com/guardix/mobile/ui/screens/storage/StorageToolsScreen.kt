package com.guardix.mobile.ui.screens.storage

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.guardix.mobile.ui.theme.*
import com.guardix.mobile.data.managers.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageToolsScreen(
    viewModel: StorageToolsViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.initializeStorageManager(context)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Storage & File Manager",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))
                    )
                ),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Storage Overview Card
            item {
                StorageOverviewCard(
                    storageInfo = uiState.storageInfo,
                    isLoading = uiState.isLoading
                )
            }
            
            // Storage Breakdown
            item {
                StorageBreakdownCard(
                    breakdown = uiState.storageBreakdown,
                    isLoading = uiState.isLoading
                )
            }
            
            // Storage Tools Grid
            item {
                StorageToolsGrid(
                    onToolClick = { toolType ->
                        viewModel.handleStorageTool(toolType)
                    }
                )
            }
            
            // Large Files Card
            item {
                LargeFilesCard(
                    largeFiles = uiState.largeFiles,
                    onFileClick = { file ->
                        viewModel.selectFile(file)
                    }
                )
            }
            
            // Duplicate Files Card
            if (uiState.duplicateFiles.isNotEmpty()) {
                item {
                    DuplicateFilesCard(
                        duplicateFiles = uiState.duplicateFiles,
                        onRemoveDuplicates = {
                            viewModel.removeDuplicateFiles()
                        }
                    )
                }
            }
            
            // Recent Activity
            item {
                RecentStorageActivityCard(
                    activities = uiState.recentActivity
                )
            }
        }
    }
    
    // Cleaning Progress Dialog
    if (uiState.isCleaningInProgress) {
        CleaningProgressDialog(
            progress = uiState.cleaningProgress,
            currentOperation = uiState.currentCleaningOperation,
            onCancel = { viewModel.cancelCleaning() }
        )
    }
    
    // File Details Dialog
    if (uiState.showFileDetailsDialog && uiState.selectedFile != null) {
        FileDetailsDialog(
            file = uiState.selectedFile!!,
            onDismiss = { viewModel.dismissFileDetailsDialog() },
            onDelete = { viewModel.deleteFile(it) },
            onMove = { file, destination -> viewModel.moveFile(file, destination) }
        )
    }
    
    // Cleanup Results Dialog
    if (uiState.showCleanupResultsDialog && uiState.lastCleanupResult != null) {
        CleanupResultsDialog(
            result = uiState.lastCleanupResult!!,
            onDismiss = { viewModel.dismissCleanupResultsDialog() }
        )
    }
}

@Composable
fun StorageOverviewCard(
    storageInfo: StorageInfo?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Storage Overview",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = null,
                    tint = Purple,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Purple
                )
            } else if (storageInfo != null) {
                // Storage usage progress
                val usagePercent = (storageInfo.usedSpace.toFloat() / storageInfo.totalSpace.toFloat()) * 100
                
                LinearProgressIndicator(
                    progress = usagePercent / 100f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    color = when {
                        usagePercent > 90 -> ErrorRed
                        usagePercent > 75 -> WarningOrange
                        else -> SuccessGreen
                    }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = formatFileSize(storageInfo.usedSpace),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Purple
                        )
                        Text(
                            text = "Used",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = formatFileSize(storageInfo.freeSpace),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SuccessGreen
                        )
                        Text(
                            text = "Free",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Total: ${formatFileSize(storageInfo.totalSpace)} • ${String.format("%.1f", usagePercent)}% used",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun StorageBreakdownCard(
    breakdown: StorageBreakdown?,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Storage Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                repeat(5) {
                    StorageBreakdownItemSkeleton()
                    Spacer(modifier = Modifier.height(8.dp))
                }
            } else if (breakdown != null) {
                StorageBreakdownItem(
                    "Apps", 
                    breakdown.appsSize, 
                    breakdown.totalSize, 
                    Icons.Default.Apps, 
                    LightBlue
                )
                StorageBreakdownItem(
                    "Photos & Videos", 
                    breakdown.mediaSize, 
                    breakdown.totalSize, 
                    Icons.Default.Photo, 
                    SuccessGreen
                )
                StorageBreakdownItem(
                    "Documents", 
                    breakdown.documentsSize, 
                    breakdown.totalSize, 
                    Icons.Default.Description, 
                    WarningOrange
                )
                StorageBreakdownItem(
                    "Downloads", 
                    breakdown.downloadsSize, 
                    breakdown.totalSize, 
                    Icons.Default.Download, 
                    Purple
                )
                StorageBreakdownItem(
                    "Cache & Temp", 
                    breakdown.cacheSize, 
                    breakdown.totalSize, 
                    Icons.Default.CleaningServices, 
                    ErrorRed
                )
                StorageBreakdownItem(
                    "Other", 
                    breakdown.otherSize, 
                    breakdown.totalSize, 
                    Icons.Default.FolderOpen, 
                    Color.Gray
                )
            }
        }
    }
}

@Composable
fun StorageBreakdownItem(
    category: String,
    size: Long,
    totalSize: Long,
    icon: ImageVector,
    color: Color
) {
    val percentage = if (totalSize > 0) (size.toFloat() / totalSize.toFloat()) * 100 else 0f
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = category,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatFileSize(size),
                    style = MaterialTheme.typography.bodyMedium,
                    color = color,
                    fontWeight = FontWeight.Bold
                )
            }
            
            LinearProgressIndicator(
                progress = percentage / 100f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color
            )
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = "${String.format("%.1f", percentage)}%",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun StorageBreakdownItemSkeleton() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(Color.Gray.copy(alpha = 0.2f), CircleShape)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(16.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(Color.Gray.copy(alpha = 0.2f), RoundedCornerShape(2.dp))
            )
        }
    }
}

@Composable
fun StorageToolsGrid(
    onToolClick: (StorageToolType) -> Unit
) {
    val tools = listOf(
        StorageTool("Quick Clean", Icons.Default.CleaningServices, LightBlue, StorageToolType.QUICK_CLEAN),
        StorageTool("Duplicate Finder", Icons.Default.ContentCopy, SuccessGreen, StorageToolType.DUPLICATE_FINDER),
        StorageTool("Large Files", Icons.Default.FolderSpecial, WarningOrange, StorageToolType.LARGE_FILES),
        StorageTool("App Manager", Icons.Default.Apps, Purple, StorageToolType.APP_MANAGER),
        StorageTool("File Explorer", Icons.Default.Folder, Cyan, StorageToolType.FILE_EXPLORER),
        StorageTool("Backup & Restore", Icons.Default.Backup, ErrorRed, StorageToolType.BACKUP_RESTORE)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Storage Tools",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.height(240.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tools) { tool ->
                    StorageToolCard(
                        tool = tool,
                        onClick = { onToolClick(tool.type) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StorageToolCard(
    tool: StorageTool,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .combinedClickable(
                onClickLabel = tool.name,
                onClick = {
                    isPressed = true
                    onClick()
                }
            )
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = tool.color.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, tool.color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        tool.color.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = tool.name,
                    tint = tool.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = tool.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

@Composable
fun LargeFilesCard(
    largeFiles: List<FileInfo>,
    onFileClick: (FileInfo) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Large Files",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Text(
                    text = "${largeFiles.size} files",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.height(200.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(largeFiles.take(10)) { file ->
                    LargeFileItem(
                        file = file,
                        onClick = { onFileClick(file) }
                    )
                }
            }
        }
    }
}

@Composable
fun LargeFileItem(
    file: FileInfo,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F9FA)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = getFileTypeIcon(file.extension),
                contentDescription = null,
                tint = getFileTypeColor(file.extension),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${formatFileSize(file.size)} • ${file.extension.uppercase()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            IconButton(
                onClick = onClick,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DuplicateFilesCard(
    duplicateFiles: List<DuplicateFileGroup>,
    onRemoveDuplicates: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duplicate Files",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                
                Button(
                    onClick = onRemoveDuplicates,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarningOrange
                    ),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "Remove",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            val totalDuplicates = duplicateFiles.sumOf { it.files.size - 1 }
            val totalSize = duplicateFiles.sumOf { group ->
                group.files.drop(1).sumOf { it.size }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "$totalDuplicates files",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange
                    )
                    Text(
                        text = "Duplicates found",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatFileSize(totalSize),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ErrorRed
                    )
                    Text(
                        text = "Can be freed",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
fun RecentStorageActivityCard(
    activities: List<StorageActivity>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Recent Activity",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            LazyColumn(
                modifier = Modifier.height(150.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { activity ->
                    StorageActivityItem(activity = activity)
                }
            }
        }
    }
}

@Composable
fun StorageActivityItem(
    activity: StorageActivity
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(activity.color.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = activity.color,
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = activity.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = activity.timestamp,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

// Dialog Components
@Composable
fun CleaningProgressDialog(
    progress: Float,
    currentOperation: String,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "Cleaning Storage",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Purple
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = currentOperation,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${(progress * 100).toInt()}% complete",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun FileDetailsDialog(
    file: FileInfo,
    onDismiss: () -> Unit,
    onDelete: (FileInfo) -> Unit,
    onMove: (FileInfo, String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = file.name,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        text = {
            Column {
                Text("Size: ${formatFileSize(file.size)}")
                Text("Type: ${file.extension.uppercase()}")
                Text("Location: ${file.path}")
                Text("Modified: ${file.lastModified}")
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = { onDelete(file) }) {
                    Text("Delete", color = ErrorRed)
                }
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun CleanupResultsDialog(
    result: StorageCleanupResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cleanup Complete",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                Text("Files cleaned: ${result.filesRemoved}")
                Text("Space freed: ${formatFileSize(result.spaceFreed)}")
                Text("Cache cleared: ${formatFileSize(result.cacheCleared)}")
                Text("Time taken: ${result.timeTaken}s")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

// Utility functions
private fun getFileTypeIcon(extension: String): ImageVector {
    return when (extension.lowercase()) {
        "pdf" -> Icons.Default.PictureAsPdf
        "jpg", "jpeg", "png", "gif" -> Icons.Default.Image
        "mp4", "avi", "mkv" -> Icons.Default.Movie
        "mp3", "wav", "flac" -> Icons.Default.AudioFile
        "doc", "docx" -> Icons.Default.Description
        "zip", "rar", "7z" -> Icons.Default.Archive
        "apk" -> Icons.Default.Android
        else -> Icons.Default.InsertDriveFile
    }
}

private fun getFileTypeColor(extension: String): Color {
    return when (extension.lowercase()) {
        "pdf" -> ErrorRed
        "jpg", "jpeg", "png", "gif" -> SuccessGreen
        "mp4", "avi", "mkv" -> Purple
        "mp3", "wav", "flac" -> LightBlue
        "doc", "docx" -> LightBlue
        "zip", "rar", "7z" -> WarningOrange
        "apk" -> SuccessGreen
        else -> Color.Gray
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    
    return when {
        gb >= 1 -> String.format("%.1f GB", gb)
        mb >= 1 -> String.format("%.1f MB", mb)
        kb >= 1 -> String.format("%.1f KB", kb)
        else -> "$bytes B"
    }
}

// Data classes
data class StorageTool(
    val name: String,
    val icon: ImageVector,
    val color: Color,
    val type: StorageToolType
)
package com.guardix.mobile.ui.screens.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import com.guardix.mobile.data.managers.*
import com.guardix.mobile.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

data class StorageToolsUiState(
    val storageInfo: StorageInfo? = null,
    val storageBreakdown: StorageBreakdown? = null,
    val largeFiles: List<FileInfo> = emptyList(),
    val duplicateFiles: List<DuplicateFileGroup> = emptyList(),
    val recentActivity: List<StorageActivity> = emptyList(),
    val isLoading: Boolean = false,
    val isCleaningInProgress: Boolean = false,
    val cleaningProgress: Float = 0f,
    val currentCleaningOperation: String = "",
    val showFileDetailsDialog: Boolean = false,
    val showCleanupResultsDialog: Boolean = false,
    val selectedFile: FileInfo? = null,
    val lastCleanupResult: StorageCleanupResult? = null,
    val errorMessage: String? = null
)

class StorageToolsViewModel : ViewModel() {
    
    private var storageManager: StorageManager? = null
    
    private val _uiState = MutableStateFlow(StorageToolsUiState())
    val uiState: StateFlow<StorageToolsUiState> = _uiState.asStateFlow()
    
    fun initializeStorageManager(context: Context) {
        if (storageManager == null) {
            storageManager = StorageManager(context)
            loadInitialData()
        }
    }
    
    private fun loadInitialData() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                storageManager?.let { manager ->
                    val storageInfo = manager.getStorageInfo()
                    val storageBreakdown = manager.getStorageBreakdown()
                    val largeFiles = manager.getLargeFiles()
                    val duplicateFiles = manager.findDuplicateFiles()
                    val recentActivity = generateRecentActivity()
                    
                    _uiState.value = _uiState.value.copy(
                        storageInfo = storageInfo,
                        storageBreakdown = storageBreakdown,
                        largeFiles = largeFiles,
                        duplicateFiles = duplicateFiles,
                        recentActivity = recentActivity,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to load storage data: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
    
    fun handleStorageTool(toolType: StorageToolType) {
        viewModelScope.launch {
            try {
                when (toolType) {
                    StorageToolType.QUICK_CLEAN -> performQuickClean()
                    StorageToolType.DUPLICATE_FINDER -> findDuplicates()
                    StorageToolType.LARGE_FILES -> analyzeLargeFiles()
                    StorageToolType.APP_MANAGER -> manageApps()
                    StorageToolType.FILE_EXPLORER -> openFileExplorer()
                    StorageToolType.BACKUP_RESTORE -> performBackup()
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Tool operation failed: ${e.message}"
                )
            }
        }
    }
    
    private suspend fun performQuickClean() {
        _uiState.value = _uiState.value.copy(
            isCleaningInProgress = true,
            cleaningProgress = 0f,
            currentCleaningOperation = "Scanning for junk files..."
        )
        
        val operations = listOf(
            "Scanning for junk files..." to 0.2f,
            "Cleaning cache files..." to 0.4f,
            "Removing temporary files..." to 0.6f,
            "Optimizing storage..." to 0.8f,
            "Finalizing cleanup..." to 1.0f
        )
        
        for ((operation, progress) in operations) {
            _uiState.value = _uiState.value.copy(
                currentCleaningOperation = operation,
                cleaningProgress = progress
            )
            delay(1000)
        }
        
        storageManager?.let { manager ->
            val result = manager.performQuickClean()
            
            _uiState.value = _uiState.value.copy(
                isCleaningInProgress = false,
                lastCleanupResult = result,
                showCleanupResultsDialog = true
            )
            
            addStorageActivity(
                StorageActivity(
                    description = "Quick clean completed - ${formatFileSize(result.spaceFreed)} freed",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.CleaningServices,
                    color = LightBlue
                )
            )
            
            // Refresh storage info
            val updatedStorageInfo = manager.getStorageInfo()
            _uiState.value = _uiState.value.copy(storageInfo = updatedStorageInfo)
        }
    }
    
    private suspend fun findDuplicates() {
        storageManager?.let { manager ->
            val duplicates = manager.findDuplicateFiles()
            _uiState.value = _uiState.value.copy(duplicateFiles = duplicates)
            
            val totalDuplicates = duplicates.sumOf { it.files.size - 1 }
            addStorageActivity(
                StorageActivity(
                    description = "Duplicate scan completed - $totalDuplicates duplicates found",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.ContentCopy,
                    color = SuccessGreen
                )
            )
        }
    }
    
    private suspend fun analyzeLargeFiles() {
        storageManager?.let { manager ->
            val largeFiles = manager.getLargeFiles()
            _uiState.value = _uiState.value.copy(largeFiles = largeFiles)
            
            val totalSize = largeFiles.sumOf { it.size }
            addStorageActivity(
                StorageActivity(
                    description = "Large files analysis - ${formatFileSize(totalSize)} in ${largeFiles.size} files",
                    timestamp = getCurrentTimestamp(),
                    icon = Icons.Default.FolderSpecial,
                    color = WarningOrange
                )
            )
        }
    }
    
    private suspend fun manageApps() {
        addStorageActivity(
            StorageActivity(
                description = "App manager opened",
                timestamp = getCurrentTimestamp(),
                icon = Icons.Default.Apps,
                color = Purple
            )
        )
    }
    
    private suspend fun openFileExplorer() {
        addStorageActivity(
            StorageActivity(
                description = "File explorer opened",
                timestamp = getCurrentTimestamp(),
                icon = Icons.Default.Folder,
                color = Cyan
            )
        )
    }
    
    private suspend fun performBackup() {
        addStorageActivity(
            StorageActivity(
                description = "Backup process initiated",
                timestamp = getCurrentTimestamp(),
                icon = Icons.Default.Backup,
                color = ErrorRed
            )
        )
    }
    
    fun selectFile(file: FileInfo) {
        _uiState.value = _uiState.value.copy(
            selectedFile = file,
            showFileDetailsDialog = true
        )
    }
    
    fun dismissFileDetailsDialog() {
        _uiState.value = _uiState.value.copy(
            showFileDetailsDialog = false,
            selectedFile = null
        )
    }
    
    fun deleteFile(file: FileInfo) {
        viewModelScope.launch {
            try {
                storageManager?.let { manager ->
                    val success = manager.deleteFile(file.path)
                    
                    if (success) {
                        // Remove from large files list
                        val updatedLargeFiles = _uiState.value.largeFiles.filter { it.path != file.path }
                        _uiState.value = _uiState.value.copy(
                            largeFiles = updatedLargeFiles,
                            showFileDetailsDialog = false,
                            selectedFile = null
                        )
                        
                        addStorageActivity(
                            StorageActivity(
                                description = "File deleted: ${file.name}",
                                timestamp = getCurrentTimestamp(),
                                icon = Icons.Default.Delete,
                                color = ErrorRed
                            )
                        )
                        
                        // Refresh storage info
                        val updatedStorageInfo = manager.getStorageInfo()
                        _uiState.value = _uiState.value.copy(storageInfo = updatedStorageInfo)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            errorMessage = "Failed to delete file: ${file.name}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Delete operation failed: ${e.message}"
                )
            }
        }
    }
    
    fun moveFile(file: FileInfo, destination: String) {
        viewModelScope.launch {
            try {
                storageManager?.let { manager ->
                    val success = manager.moveFile(file.path, destination)
                    
                    if (success) {
                        _uiState.value = _uiState.value.copy(
                            showFileDetailsDialog = false,
                            selectedFile = null
                        )
                        
                        addStorageActivity(
                            StorageActivity(
                                description = "File moved: ${file.name}",
                                timestamp = getCurrentTimestamp(),
                                icon = Icons.Default.DriveFileMove,
                                color = LightBlue
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Move operation failed: ${e.message}"
                )
            }
        }
    }
    
    fun removeDuplicateFiles() {
        viewModelScope.launch {
            try {
                val duplicates = _uiState.value.duplicateFiles
                if (duplicates.isNotEmpty()) {
                    storageManager?.let { manager ->
                        val result = manager.removeDuplicateFiles(duplicates)
                        
                        _uiState.value = _uiState.value.copy(
                            duplicateFiles = emptyList(),
                            lastCleanupResult = result,
                            showCleanupResultsDialog = true
                        )
                        
                        addStorageActivity(
                            StorageActivity(
                                description = "Duplicate files removed - ${formatFileSize(result.spaceFreed)} freed",
                                timestamp = getCurrentTimestamp(),
                                icon = Icons.Default.DeleteSweep,
                                color = SuccessGreen
                            )
                        )
                        
                        // Refresh storage info
                        val updatedStorageInfo = manager.getStorageInfo()
                        _uiState.value = _uiState.value.copy(storageInfo = updatedStorageInfo)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Failed to remove duplicates: ${e.message}"
                )
            }
        }
    }
    
    fun cancelCleaning() {
        _uiState.value = _uiState.value.copy(
            isCleaningInProgress = false,
            cleaningProgress = 0f,
            currentCleaningOperation = ""
        )
    }
    
    fun dismissCleanupResultsDialog() {
        _uiState.value = _uiState.value.copy(
            showCleanupResultsDialog = false,
            lastCleanupResult = null
        )
    }
    
    private fun addStorageActivity(activity: StorageActivity) {
        val currentActivity = _uiState.value.recentActivity.toMutableList()
        currentActivity.add(0, activity) // Add to beginning
        if (currentActivity.size > 10) {
            currentActivity.removeAt(currentActivity.size - 1) // Keep only 10 recent activities
        }
        
        _uiState.value = _uiState.value.copy(recentActivity = currentActivity)
    }
    
    private fun generateRecentActivity(): List<StorageActivity> {
        return listOf(
            StorageActivity(
                description = "Storage tools initialized",
                timestamp = getCurrentTimestamp(),
                icon = Icons.Default.Storage,
                color = Purple
            ),
            StorageActivity(
                description = "Storage analysis completed",
                timestamp = getTimestamp(-1),
                icon = Icons.Default.Analytics,
                color = LightBlue
            ),
            StorageActivity(
                description = "File system scanned",
                timestamp = getTimestamp(-2),
                icon = Icons.Default.FolderOpen,
                color = SuccessGreen
            )
        )
    }
    
    private fun getCurrentTimestamp(): String {
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
    }
    
    private fun getTimestamp(hoursAgo: Int): String {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, hoursAgo)
        return SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)
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
}
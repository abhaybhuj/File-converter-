package com.example.filee.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.filee.data.engine.ConversionEngine
import com.example.filee.data.engine.RoutePlanner
import com.example.filee.data.local.AppDatabase
import com.example.filee.data.local.ConversionRecordEntity
import com.example.filee.data.model.ConversionJob
import com.example.filee.data.model.ConversionOptions
import com.example.filee.data.model.ConversionRoute
import com.example.filee.data.model.FileFormat
import com.example.filee.data.model.FormatCategory
import com.example.filee.data.model.JobStatus
import com.example.filee.data.repository.ConversionRepository
import com.example.filee.util.FileUtils
import com.example.filee.util.SampleFilesUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

data class SelectedFile(
    val name: String,
    val format: FileFormat,
    val bytes: ByteArray,
    val uri: Uri? = null
)

class FileeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ConversionRepository
    val historyRecords: StateFlow<List<ConversionRecordEntity>>
    val totalRecordsCount: StateFlow<Int>
    val totalConvertedBytes: StateFlow<Long?>

    init {
        val database = AppDatabase.getDatabase(application)
        val engine = ConversionEngine(application)
        repository = ConversionRepository(database.conversionRecordDao(), engine)

        historyRecords = repository.allRecords
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        totalRecordsCount = repository.recordCount
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
        totalConvertedBytes = repository.totalConvertedBytes
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    }

    // Single conversion state
    private val _selectedFile = MutableStateFlow<SelectedFile?>(null)
    val selectedFile: StateFlow<SelectedFile?> = _selectedFile.asStateFlow()

    private val _selectedCategory = MutableStateFlow(FormatCategory.DOCUMENT)
    val selectedCategory: StateFlow<FormatCategory> = _selectedCategory.asStateFlow()

    private val _availableTargetFormats = MutableStateFlow<List<FileFormat>>(emptyList())
    val availableTargetFormats: StateFlow<List<FileFormat>> = _availableTargetFormats.asStateFlow()

    private val _selectedTargetFormat = MutableStateFlow<FileFormat?>(null)
    val selectedTargetFormat: StateFlow<FileFormat?> = _selectedTargetFormat.asStateFlow()

    private val _currentRoute = MutableStateFlow<ConversionRoute?>(null)
    val currentRoute: StateFlow<ConversionRoute?> = _currentRoute.asStateFlow()

    private val _activeJob = MutableStateFlow<ConversionJob?>(null)
    val activeJob: StateFlow<ConversionJob?> = _activeJob.asStateFlow()

    // Global Options
    private val _conversionOptions = MutableStateFlow(ConversionOptions())
    val conversionOptions: StateFlow<ConversionOptions> = _conversionOptions.asStateFlow()

    // Batch Queue State
    private val _batchQueue = MutableStateFlow<List<ConversionJob>>(emptyList())
    val batchQueue: StateFlow<List<ConversionJob>> = _batchQueue.asStateFlow()

    private val _isBatchConverting = MutableStateFlow(false)
    val isBatchConverting: StateFlow<Boolean> = _isBatchConverting.asStateFlow()

    // Route Explorer State
    private val _explorerSource = MutableStateFlow(FileFormat.CSV)
    val explorerSource: StateFlow<FileFormat> = _explorerSource.asStateFlow()

    private val _explorerTarget = MutableStateFlow(FileFormat.PDF)
    val explorerTarget: StateFlow<FileFormat> = _explorerTarget.asStateFlow()

    private val _exploredRoute = MutableStateFlow<ConversionRoute?>(
        RoutePlanner.findRoute(FileFormat.CSV, FileFormat.PDF)
    )
    val exploredRoute: StateFlow<ConversionRoute?> = _exploredRoute.asStateFlow()

    init {
        // Preload default sample file for quick testing
        val defaultSample = SampleFilesUtil.getSampleFiles().first()
        selectFile(defaultSample.name, defaultSample.bytes, defaultSample.format)
    }

    fun selectFile(name: String, bytes: ByteArray, format: FileFormat, uri: Uri? = null) {
        _selectedFile.value = SelectedFile(name, format, bytes, uri)
        val targets = RoutePlanner.getAvailableTargetFormats(format)
        _availableTargetFormats.value = targets

        // Pick a smart target
        val preferredTarget = targets.firstOrNull { it != format && it.category != format.category }
            ?: targets.firstOrNull { it != format }
            ?: FileFormat.TXT

        _selectedCategory.value = preferredTarget.category
        selectTargetFormat(preferredTarget)
    }

    fun selectCategory(category: FormatCategory) {
        _selectedCategory.value = category
        val targetsInCat = _availableTargetFormats.value.filter { it.category == category }
        if (targetsInCat.isNotEmpty() && (_selectedTargetFormat.value == null || _selectedTargetFormat.value?.category != category)) {
            selectTargetFormat(targetsInCat.first())
        }
    }

    fun selectTargetFormat(format: FileFormat) {
        _selectedTargetFormat.value = format
        _selectedCategory.value = format.category
        val currentFile = _selectedFile.value
        if (currentFile != null) {
            _currentRoute.value = RoutePlanner.findRoute(currentFile.format, format)
        }
    }

    fun updateConversionOptions(options: ConversionOptions) {
        _conversionOptions.value = options
    }

    fun startSingleConversion() {
        val file = _selectedFile.value ?: return
        val target = _selectedTargetFormat.value ?: return
        val route = _currentRoute.value ?: return

        val job = ConversionJob(
            id = UUID.randomUUID().toString(),
            originalFileName = file.name,
            sourceUri = file.uri,
            rawBytes = file.bytes,
            sourceSizeBytes = file.bytes.size.toLong(),
            inputFormat = file.format,
            targetFormat = target,
            route = route,
            options = _conversionOptions.value,
            status = JobStatus.CONVERTING,
            currentStepText = "Preparing route..."
        )

        _activeJob.value = job

        viewModelScope.launch {
            val completed = repository.executeConversion(job) { progress, stepText ->
                _activeJob.value = _activeJob.value?.copy(
                    progress = progress,
                    currentStepText = stepText
                )
            }
            _activeJob.value = completed
        }
    }

    fun addFilesToBatch(files: List<SelectedFile>, defaultTarget: FileFormat? = null) {
        val newJobs = files.mapNotNull { file ->
            val target = defaultTarget ?: RoutePlanner.getAvailableTargetFormats(file.format).firstOrNull { it != file.format } ?: return@mapNotNull null
            val route = RoutePlanner.findRoute(file.format, target) ?: return@mapNotNull null

            ConversionJob(
                id = UUID.randomUUID().toString(),
                originalFileName = file.name,
                sourceUri = file.uri,
                rawBytes = file.bytes,
                sourceSizeBytes = file.bytes.size.toLong(),
                inputFormat = file.format,
                targetFormat = target,
                route = route,
                options = _conversionOptions.value,
                status = JobStatus.PENDING
            )
        }

        _batchQueue.value = _batchQueue.value + newJobs
    }

    fun startBatchConversion() {
        if (_isBatchConverting.value) return
        _isBatchConverting.value = true

        viewModelScope.launch {
            val queue = _batchQueue.value.toMutableList()
            for (i in queue.indices) {
                val job = queue[i]
                if (job.status == JobStatus.SUCCESS) continue

                queue[i] = job.copy(status = JobStatus.CONVERTING, progress = 0.1f)
                _batchQueue.value = queue.toList()

                val completed = repository.executeConversion(job) { progress, stepText ->
                    val cur = _batchQueue.value.toMutableList()
                    cur[i] = cur[i].copy(progress = progress, currentStepText = stepText)
                    _batchQueue.value = cur
                }

                queue[i] = completed
                _batchQueue.value = queue.toList()
            }
            _isBatchConverting.value = false
        }
    }

    fun removeBatchJob(id: String) {
        _batchQueue.value = _batchQueue.value.filter { it.id != id }
    }

    fun clearBatch() {
        _batchQueue.value = emptyList()
    }

    // Route Explorer methods
    fun setExplorerSource(format: FileFormat) {
        _explorerSource.value = format
        _exploredRoute.value = RoutePlanner.findRoute(format, _explorerTarget.value)
    }

    fun setExplorerTarget(format: FileFormat) {
        _explorerTarget.value = format
        _exploredRoute.value = RoutePlanner.findRoute(_explorerSource.value, format)
    }

    fun toggleFavorite(record: ConversionRecordEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(record)
        }
    }

    fun deleteHistoryRecord(record: ConversionRecordEntity) {
        viewModelScope.launch {
            repository.deleteRecord(record)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllHistory()
        }
    }
}

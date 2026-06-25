package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface SyncState {
    object Idle : SyncState
    object Loading : SyncState
    data class Success(val count: Int) : SyncState
    data class Error(val message: String) : SyncState
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = HouseRepository(db.houseDao())

    // Source flow
    val allRecords: StateFlow<List<HouseRecord>> = repository.allRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Sync state
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState.asStateFlow()

    // Filter states
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedBlock = MutableStateFlow("All")
    val selectedBlock: StateFlow<String> = _selectedBlock.asStateFlow()

    private val _selectedFeeder = MutableStateFlow("All")
    val selectedFeeder: StateFlow<String> = _selectedFeeder.asStateFlow()

    private val _selectedLoadRange = MutableStateFlow("All") // "All", "0-2 kW", "2-5 kW", "5-10 kW", ">10 kW"
    val selectedLoadRange: StateFlow<String> = _selectedLoadRange.asStateFlow()

    // Appliance-specific filters
    private val _selectedApplianceFilter = MutableStateFlow<ApplianceType?>(null)
    val selectedApplianceFilter: StateFlow<ApplianceType?> = _selectedApplianceFilter.asStateFlow()

    private val _applianceMinQuantity = MutableStateFlow(0)
    val applianceMinQuantity: StateFlow<Int> = _applianceMinQuantity.asStateFlow()

    // Selected house for Detail view
    private val _selectedHouseId = MutableStateFlow<Int?>(null)
    val selectedHouseId: StateFlow<Int?> = _selectedHouseId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val selectedHouse: StateFlow<HouseRecord?> = _selectedHouseId
        .flatMapLatest { id ->
            if (id == null) flowOf(null)
            else allRecords.map { list -> list.find { it.id == id } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Automatically fetch live electrical load data from the Google Sheets API endpoint on start
        viewModelScope.launch {
            repository.allRecords.first().let { current ->
                if (current.isEmpty()) {
                    val defaultSheetUrl = "https://docs.google.com/spreadsheets/d/1kYndPjWpIlPpEEyCXp_ZuKnA_RoX84u_IEyjlIie7QY/edit?usp=drivesdk"
                    Log.d("DashboardViewModel", "Prepopulating app with real-time data from sheet: $defaultSheetUrl")
                    try {
                        val count = repository.syncGoogleSheet(defaultSheetUrl)
                        _syncState.value = SyncState.Success(count)
                        kotlinx.coroutines.delay(2000)
                        _syncState.value = SyncState.Idle
                    } catch (e: Exception) {
                        Log.e("DashboardViewModel", "Initial Google Sheet sync failed, falling back to sample data", e)
                        loadSampleDataset()
                    }
                }
            }
        }
    }

    // Combined filtered records flow
    val filteredRecords: StateFlow<List<HouseRecord>> = combine(
        allRecords,
        searchQuery,
        selectedBlock,
        selectedFeeder,
        selectedLoadRange,
        selectedApplianceFilter,
        applianceMinQuantity
    ) { flowsArray ->
        @Suppress("UNCHECKED_CAST")
        val records = flowsArray[0] as List<HouseRecord>
        val query = flowsArray[1] as String
        val block = flowsArray[2] as String
        val feeder = flowsArray[3] as String
        val range = flowsArray[4] as String
        val appType = flowsArray[5] as ApplianceType?
        val minQty = flowsArray[6] as Int

        records.filter { record ->
            // 1. Search filter (House No or Resident Name)
            val matchesQuery = query.isBlank() || 
                record.houseNo.contains(query, ignoreCase = true) || 
                record.residentName.contains(query, ignoreCase = true)
            
            // 2. Block filter
            val matchesBlock = block == "All" || record.getBlock().equals(block, ignoreCase = true)

            // 3. Feeder filter
            val matchesFeeder = feeder == "All" || record.gridFeeder.equals(feeder, ignoreCase = true)

            // 4. Load range filter
            val totalLoadKw = record.calculateTotalLoadKw()
            val matchesRange = when (range) {
                "All" -> true
                "0-2 kW" -> totalLoadKw in 0.0..2.0
                "2-5 kW" -> totalLoadKw > 2.0 && totalLoadKw <= 5.0
                "5-10 kW" -> totalLoadKw > 5.0 && totalLoadKw <= 10.0
                ">10 kW" -> totalLoadKw > 10.0
                else -> true
            }

            // 5. Appliance filter
            val matchesAppliance = if (appType == null) {
                true
            } else {
                record.getQuantity(appType) >= minQty
            }

            matchesQuery && matchesBlock && matchesFeeder && matchesRange && matchesAppliance
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Unique blocks and feeders derived dynamically from current records
    val availableBlocks: StateFlow<List<String>> = allRecords
        .map { list -> listOf("All") + list.map { it.getBlock() }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    val availableFeeders: StateFlow<List<String>> = allRecords
        .map { list -> listOf("All") + list.map { it.gridFeeder }.distinct().sorted() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf("All"))

    // Dynamic stats computations based on current filtered list
    val totalTownshipLoadKw: StateFlow<Double> = filteredRecords
        .map { list -> list.sumOf { it.calculateTotalLoad() } / 1000.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val averageHouseLoadKw: StateFlow<Double> = filteredRecords
        .map { list -> 
            if (list.isEmpty()) 0.0 
            else (list.sumOf { it.calculateTotalLoad() }.toDouble() / list.size) / 1000.0 
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val peakLoadHouse: StateFlow<HouseRecord?> = filteredRecords
        .map { list -> list.maxByOrNull { it.calculateTotalLoad() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Appliance Analytics aggregates across all filtered records
    val applianceAnalytics: StateFlow<List<ApplianceAnalyticRow>> = filteredRecords
        .map { list ->
            ApplianceType.values().map { type ->
                var totalQty = 0
                var totalW = 0
                var houseCount = 0
                list.forEach { record ->
                    val qty = record.getQuantity(type)
                    if (qty > 0) {
                        totalQty += qty
                        totalW += qty * type.ratedWattage
                        houseCount++
                    }
                }
                ApplianceAnalyticRow(
                    applianceType = type,
                    totalQuantity = totalQty,
                    totalLoadKw = totalW / 1000.0,
                    activeHousesCount = houseCount
                )
            }.sortedByDescending { it.totalLoadKw }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Actions
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedBlock(block: String) {
        _selectedBlock.value = block
    }

    fun setSelectedFeeder(feeder: String) {
        _selectedFeeder.value = feeder
    }

    fun setSelectedLoadRange(range: String) {
        _selectedLoadRange.value = range
    }

    fun setApplianceFilter(type: ApplianceType?, minQty: Int) {
        _selectedApplianceFilter.value = type
        _applianceMinQuantity.value = minQty
    }

    fun setSelectedHouseId(id: Int?) {
        _selectedHouseId.value = id
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedBlock.value = "All"
        _selectedFeeder.value = "All"
        _selectedLoadRange.value = "All"
        _selectedApplianceFilter.value = null
        _applianceMinQuantity.value = 0
    }

    fun syncWithGoogleSheet(url: String) {
        if (url.isBlank()) {
            _syncState.value = SyncState.Error("Please enter a valid Google Sheets URL")
            return
        }
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            try {
                val count = repository.syncGoogleSheet(url)
                _syncState.value = SyncState.Success(count)
                // Reset sync state to Idle after a short delay
                kotlinx.coroutines.delay(3000)
                _syncState.value = SyncState.Idle
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Sync failed", e)
                _syncState.value = SyncState.Error(e.localizedMessage ?: "Unknown network error")
            }
        }
    }

    fun loadSampleDataset() {
        viewModelScope.launch {
            _syncState.value = SyncState.Loading
            try {
                repository.clearAllRecords()
                val sample = SampleData.generate600Houses()
                repository.insertRecords(sample)
                _syncState.value = SyncState.Success(sample.size)
                kotlinx.coroutines.delay(2000)
                _syncState.value = SyncState.Idle
            } catch (e: Exception) {
                _syncState.value = SyncState.Error("Failed to load sample dataset: ${e.message}")
            }
        }
    }

    fun resetDatabase() {
        viewModelScope.launch {
            repository.clearAllRecords()
            _syncState.value = SyncState.Idle
        }
    }
}

data class ApplianceAnalyticRow(
    val applianceType: ApplianceType,
    val totalQuantity: Int,
    val totalLoadKw: Double,
    val activeHousesCount: Int
)

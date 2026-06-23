package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ApplianceType
import com.example.data.HouseRecord
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onHouseClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val syncState by viewModel.syncState.collectAsStateWithLifecycle()

    // Filter selectors
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedBlock by viewModel.selectedBlock.collectAsStateWithLifecycle()
    val selectedFeeder by viewModel.selectedFeeder.collectAsStateWithLifecycle()
    val selectedLoadRange by viewModel.selectedLoadRange.collectAsStateWithLifecycle()
    val selectedApplianceFilter by viewModel.selectedApplianceFilter.collectAsStateWithLifecycle()
    val applianceMinQty by viewModel.applianceMinQuantity.collectAsStateWithLifecycle()

    // Dynamic stats
    val totalLoadKw by viewModel.totalTownshipLoadKw.collectAsStateWithLifecycle()
    val avgLoadKw by viewModel.averageHouseLoadKw.collectAsStateWithLifecycle()
    val peakHouse by viewModel.peakLoadHouse.collectAsStateWithLifecycle()

    // UI state
    var showSyncPanel by remember { mutableStateOf(false) }
    var showFilterPanel by remember { mutableStateOf(false) }
    var sheetInputUrl by remember { mutableStateOf("") }

    val blocksList by viewModel.availableBlocks.collectAsStateWithLifecycle()
    val feedersList by viewModel.availableFeeders.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // App Header Bar styled precisely to match Geometric Balance specifications
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Geometric Balance: Blue rounded-xl bolt icon box with shadow-lg
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Bolt",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "PFL Dashboard",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "TOWNSHIP LOAD MONITORING",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { showSyncPanel = !showSyncPanel }) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync Options",
                        tint = if (showSyncPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(
                    onClick = {
                        // Profile engineering identity button
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Engineer Profile",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Sync & Sheet URL Configuration Panel (Collapsible)
        AnimatedVisibility(
            visible = showSyncPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Google Sheets Read-Only Integration",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Connect to any public Google Sheet by sharing 'Anyone with link can view' and pasting the URL.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sheetInputUrl,
                        onValueChange = { sheetInputUrl = it },
                        label = { Text("Paste Google Sheet URL") },
                        placeholder = { Text("https://docs.google.com/spreadsheets/...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sheet_url_input"),
                        leadingIcon = { Icon(Icons.Default.Link, "Link") },
                        trailingIcon = {
                            if (sheetInputUrl.isNotEmpty()) {
                                IconButton(onClick = { sheetInputUrl = "" }) {
                                    Icon(Icons.Default.Close, "Clear")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Uri,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.syncWithGoogleSheet(sheetInputUrl)
                                focusManager.clearFocus()
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("sync_button"),
                            enabled = syncState !is SyncState.Loading && sheetInputUrl.isNotBlank()
                        ) {
                            if (syncState is SyncState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Icon(Icons.Default.Download, "Download", modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Sync Live")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                viewModel.loadSampleDataset()
                                showSyncPanel = false
                            },
                            modifier = Modifier.weight(1f),
                            enabled = syncState !is SyncState.Loading
                        ) {
                            Icon(Icons.Default.Dataset, "Dataset", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Load Sample DB")
                        }
                    }

                    // Sync Status Indicator Messages
                    Spacer(modifier = Modifier.height(8.dp))
                    when (val state = syncState) {
                        is SyncState.Loading -> {
                            Text(
                                "Downloading township electrical sheet...",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        is SyncState.Success -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, "Success", tint = Color(0xFF4CAF50), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "Successfully loaded ${state.count} houses!",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF4CAF50),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                        is SyncState.Error -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Error, "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    state.message,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        else -> {}
                    }
                }
            }
        }

        // Geometric Balance: Quick Stats row
        val highDemandCount = remember(filteredRecords) {
            filteredRecords.count { house -> house.calculateTotalLoadKw() > 5.0 }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Card 1: Total Load
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "TOTAL LOAD",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%,.0f kW", totalLoadKw),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Card 2: High Demand
            Card(
                modifier = Modifier
                    .weight(1f)
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(24.dp)
                    ),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "HIGH DEMAND",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$highDemandCount Units",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Sub-Banner for Peak House Alert
        peakHouse?.let { peak ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHouseClick(peak.id) }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peak Load Alert",
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Peak House: ${peak.houseNo} (${peak.residentName})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f kW", peak.calculateTotalLoadKw()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Details",
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Live Filtering Panel (Collapsible)
        AnimatedVisibility(
            visible = showFilterPanel,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "Advanced Dashboard Filters",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Block & Feeder dropdown rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Block Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Block", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            FilterSpinner(
                                selected = selectedBlock,
                                options = blocksList,
                                onSelected = { viewModel.setSelectedBlock(it) }
                            )
                        }

                        // Feeder Selector
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Grid Feeder", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(4.dp))
                            FilterSpinner(
                                selected = selectedFeeder,
                                options = feedersList,
                                onSelected = { viewModel.setSelectedFeeder(it) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Connected Load Range Filter
                    Column {
                        Text("Connected Load Range", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        val loadRanges = listOf("All", "0-2 kW", "2-5 kW", "5-10 kW", ">10 kW")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            loadRanges.forEach { range ->
                                val active = selectedLoadRange == range
                                FilterChip(
                                    selected = active,
                                    onClick = { viewModel.setSelectedLoadRange(range) },
                                    label = { Text(range, fontSize = 10.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Appliance Presence Filter (e.g., AC or Ceiling Fans)
                    Column {
                        Text("Appliance-Specific Audit", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filterOptions = listOf(
                                "No Appliance Filter" to null,
                                "AC Units" to ApplianceType.AC,
                                "Ceiling Fans" to ApplianceType.CEILING_FAN,
                                "15A High Power Sockets" to ApplianceType.SOCKET_15A,
                                "20A Heavy Sockets" to ApplianceType.SOCKET_20A
                            )
                            
                            var expandedAppMenu by remember { mutableStateOf(false) }
                            Box(modifier = Modifier.weight(1.3f)) {
                                OutlinedCard(
                                    onClick = { expandedAppMenu = true },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = filterOptions.find { it.second == selectedApplianceFilter }?.first ?: "Appliance Type",
                                            style = MaterialTheme.typography.bodySmall,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Icon(Icons.Default.ArrowDropDown, "Dropdown", modifier = Modifier.size(16.dp))
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedAppMenu,
                                    onDismissRequest = { expandedAppMenu = false }
                                ) {
                                    filterOptions.forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt.first, style = MaterialTheme.typography.bodySmall) },
                                            onClick = {
                                                viewModel.setApplianceFilter(opt.second, if (opt.second == null) 0 else 1)
                                                expandedAppMenu = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (selectedApplianceFilter != null) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    Text("Qty ≥", style = MaterialTheme.typography.bodySmall)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { if (applianceMinQty > 1) viewModel.setApplianceFilter(selectedApplianceFilter, applianceMinQty - 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Remove, "Less", modifier = Modifier.size(14.dp))
                                    }
                                    Text(
                                        text = applianceMinQty.toString(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(horizontal = 6.dp)
                                    )
                                    IconButton(
                                        onClick = { viewModel.setApplianceFilter(selectedApplianceFilter, applianceMinQty + 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(Icons.Default.Add, "More", modifier = Modifier.size(14.dp))
                                    }
                                }
                            }
                        }
                    }

                    // Reset Filters Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                viewModel.clearAllFilters()
                            }
                        ) {
                            Icon(Icons.Default.ClearAll, "Clear Filters", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset All", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Active Search & Total Matches Banner with Geometric Balance styling
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "Search ${allRecords.size} houses or blocks...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Close, "Clear search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        IconButton(onClick = { showFilterPanel = !showFilterPanel }) {
                            val filtersActive = selectedBlock != "All" || selectedFeeder != "All" || selectedLoadRange != "All" || selectedApplianceFilter != null
                            BadgedBox(badge = {
                                if (filtersActive) {
                                    Badge { Text("!") }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter List",
                                    tint = if (showFilterPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("house_search_bar"),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                shape = CircleShape
            )
        }

        // Display Count Summary
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Showing ${filteredRecords.size} of ${allRecords.size} Houses",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                fontWeight = FontWeight.Bold
            )

            val filtersActive = selectedBlock != "All" || selectedFeeder != "All" || selectedLoadRange != "All" || selectedApplianceFilter != null || searchQuery.isNotBlank()
            if (filtersActive) {
                Text(
                    text = "Filters Active",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // High-performance LazyColumn of House items
        if (filteredRecords.isEmpty()) {
            EmptyHousesState(
                isDatabaseEmpty = allRecords.isEmpty(),
                onLoadSample = { viewModel.loadSampleDataset() }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredRecords, key = { it.id }) { house ->
                    HouseRecordCard(
                        house = house,
                        onClick = { onHouseClick(house.id) }
                    )
                }
            }
        }
    }
}

/**
 * Filter spinner representation inside the custom filtering card.
 */
@Composable
fun FilterSpinner(
    selected: String,
    options: List<String>,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selected,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(Icons.Default.ArrowDropDown, "Expand", modifier = Modifier.size(16.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodySmall) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Single House Card with detailed visual telemetry.
 */
@Composable
fun HouseRecordCard(
    house: HouseRecord,
    onClick: () -> Unit
) {
    val totalLoadKw = house.calculateTotalLoadKw()
    
    // Status badges styled using Tailwind semantic equivalents (Stable, Heavy, Critical)
    val (loadColor, loadBg, loadLabel) = when {
        totalLoadKw <= 2.0 -> Triple(Color(0xFF15803D), Color(0xFFDCFCE7), "Stable")
        totalLoadKw <= 5.0 -> Triple(Color(0xFF15803D), Color(0xFFDCFCE7), "Stable")
        totalLoadKw <= 10.0 -> Triple(Color(0xFFB45309), Color(0xFFFEF3C7), "Heavy")
        else -> Triple(Color(0xFFB91C1C), Color(0xFFFEE2E2), "Critical")
    }

    val totalApp = ApplianceType.values().sumOf { house.getQuantity(it) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("house_card_${house.houseNo}")
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp), // 3xl roundness matching theme
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp), // flat styling
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Geometric Balance: Custom BLK block badge container (w-12 h-12 bg-slate-50 border border-slate-200)
            Column(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFF8FAFC), shape = RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), shape = RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "BLK",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    lineHeight = 10.sp
                )
                Text(
                    text = house.getBlock(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Unit ${house.houseNo}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${house.residentName} • $totalApp App.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Short preview icons (highly polished, elegant sizing)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (house.acQuantity > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AcUnit, "AC", modifier = Modifier.size(10.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${house.acQuantity} AC", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    if (house.ceilingFanQuantity > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Toys, "Fans", modifier = Modifier.size(10.dp), tint = Color(0xFF0D9488))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${house.ceilingFanQuantity} Fan", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    if (house.socket20AQuantity > 0 || house.socket15AQuantity > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Power, "Power", modifier = Modifier.size(10.dp), tint = Color(0xFFD97706))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("${house.socket20AQuantity + house.socket15AQuantity} H-Socket", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Load metric and dynamic status pill
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = String.format(Locale.getDefault(), "%.2f kW", totalLoadKw),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (totalLoadKw > 10.0) Color(0xFFB91C1C) else MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Rounded-full badge
                Box(
                    modifier = Modifier
                        .background(loadBg, shape = CircleShape)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = loadLabel,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = loadColor
                    )
                }
            }
        }
    }
}

/**
 * Clean Empty State Illustration and Helpers.
 */
@Composable
fun EmptyHousesState(
    isDatabaseEmpty: Boolean,
    onLoadSample: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isDatabaseEmpty) Icons.Default.CloudOff else Icons.Default.SearchOff,
            contentDescription = "No data",
            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isDatabaseEmpty) "Township Database Empty" else "No Matching Houses Found",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = if (isDatabaseEmpty) {
                "Synchronize your public Google Sheet or load the preloaded 600-house township dataset for instant audit, engineering load calculation, and analytics."
            } else {
                "Adjust your filters or clear the search query to view other electrical records in the township."
            },
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(20.dp))
        if (isDatabaseEmpty) {
            Button(onClick = onLoadSample) {
                Icon(Icons.Default.Dataset, "Sample")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Load PFL Township Sample (600 Houses)")
            }
        }
    }
}

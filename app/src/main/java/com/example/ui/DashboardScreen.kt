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
import androidx.compose.ui.graphics.vector.ImageVector
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
    var sheetInputUrl by remember { mutableStateOf("https://docs.google.com/spreadsheets/d/1kYndPjWpIlPpEEyCXp_ZuKnA_RoX84u_IEyjlIie7QY/edit?usp=drivesdk") }

    val blocksList by viewModel.availableBlocks.collectAsStateWithLifecycle()
    val feedersList by viewModel.availableFeeders.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Minimalist Masthead
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "GRID REGISTRY",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF10B981), shape = CircleShape)
                    )
                    Text(
                        text = "GRID STATUS: SECURE",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { showSyncPanel = !showSyncPanel },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            if (showSyncPanel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = "Sync Options",
                        tint = if (showSyncPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(
                    onClick = { /* Profile */ },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Engineer Profile",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

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

        // Asymmetric Metric Dashboard
        val highDemandCount = remember(filteredRecords) {
            filteredRecords.count { house -> house.calculateTotalLoadKw() > 5.0 }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp))
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(20.dp))
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.2f)) {
                Text(
                    text = "TOTAL DEMAND LOAD",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = String.format(Locale.getDefault(), "%,.1f kW", totalLoadKw),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Active across all feeders",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            
            // Vertical hairline divider
            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(60.dp)
                    .background(MaterialTheme.colorScheme.outline)
            )
            
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 20.dp)
            ) {
                Text(
                    text = "HEAVY DEMAND",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$highDemandCount",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (highDemandCount > 0) Color(0xFFE11D48) else MaterialTheme.colorScheme.primary,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = " units",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = ">5.0 kW active load",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Sub-Banner for Peak House Alert
        peakHouse?.let { peak ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp)
                    .background(Color(0xFFFEF2F2), shape = RoundedCornerShape(14.dp))
                    .border(BorderStroke(1.dp, Color(0xFFFCA5A5)), shape = RoundedCornerShape(14.dp))
                    .clickable { onHouseClick(peak.id) }
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Peak Load Alert",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Peak House: Unit ${peak.houseNo} (${peak.residentName})",
                            fontSize = 11.sp,
                            color = Color(0xFF991B1B),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f kW", peak.calculateTotalLoadKw()),
                            fontSize = 12.sp,
                            color = Color(0xFFDC2626),
                            fontWeight = FontWeight.Black
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "View Details",
                            tint = Color(0xFFDC2626),
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

        // Active Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = {
                    Text(
                        text = "Search units, blocks, or residents...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
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
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(6.dp)
                                    )
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter List",
                                    tint = if (showFilterPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("house_search_bar"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = RoundedCornerShape(16.dp)
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
    val totalApp = ApplianceType.values().sumOf { house.getQuantity(it) }
    
    // Modern neutral/slate colors for status tags
    val (statusColor, statusBg, statusLabel) = when {
        totalLoadKw <= 2.0 -> Triple(Color(0xFF0F766E), Color(0xFFF0FDFA), "OPTIONAL")
        totalLoadKw <= 5.0 -> Triple(Color(0xFF0369A1), Color(0xFFF0F9FF), "STANDARD")
        totalLoadKw <= 10.0 -> Triple(Color(0xFFB45309), Color(0xFFFFFBEB), "HEAVY LOAD")
        else -> Triple(Color(0xFFE11D48), Color(0xFFFFF1F2), "CRITICAL")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("house_card_${house.houseNo}")
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Architectural Block Label
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "BLK",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = house.getBlock(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Unit Info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Unit ${house.houseNo}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = house.residentName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Kilowatt load indicator
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.2f kW", totalLoadKw),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Box(
                        modifier = Modifier
                            .background(statusBg, shape = RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Progress capacity bar
            Column(modifier = Modifier.fillMaxWidth()) {
                val progressFraction = (totalLoadKw / 12.0).coerceIn(0.0, 1.0).toFloat()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CAPACITY LOAD DEMAND",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${(progressFraction * 100).toInt()}% of 12kW Max",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Minimal horizontal bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(MaterialTheme.colorScheme.outline, shape = CircleShape)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progressFraction)
                            .fillMaxHeight()
                            .background(
                                if (totalLoadKw > 10.0) Color(0xFFE11D48) else MaterialTheme.colorScheme.primary,
                                shape = CircleShape
                            )
                    )
                }
            }

            // Minimalist appliance categories row
            if (totalApp > 0) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (house.acQuantity > 0) {
                        AppliancePill(label = "${house.acQuantity} AC", icon = Icons.Default.AcUnit)
                    }
                    if (house.ceilingFanQuantity > 0) {
                        AppliancePill(label = "${house.ceilingFanQuantity} FAN", icon = Icons.Default.Toys)
                    }
                    if (house.socket20AQuantity > 0 || house.socket15AQuantity > 0) {
                        AppliancePill(label = "${house.socket20AQuantity + house.socket15AQuantity} HIGH-PWR", icon = Icons.Default.Power)
                    }
                }
            }
        }
    }
}

@Composable
fun AppliancePill(label: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(10.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 8.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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

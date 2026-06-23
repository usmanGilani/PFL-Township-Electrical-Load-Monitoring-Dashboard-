package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
fun AnalyticsScreen(
    viewModel: DashboardViewModel,
    onHouseClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
    
    // Dropdown selection state
    var selectedAppliance by remember { mutableStateOf(ApplianceType.AC) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    // Dynamic aggregates based on selected appliance type and currently filtered list
    val housesWithAppliance = filteredRecords.filter { it.getQuantity(selectedAppliance) > 0 }
    val totalQty = housesWithAppliance.sumOf { it.getQuantity(selectedAppliance) }
    val totalLoadW = totalQty * selectedAppliance.ratedWattage
    val totalLoadKw = totalLoadW / 1000.0
    
    // Penetration percentage
    val penetrationRate = if (filteredRecords.isEmpty()) 0.0 else {
        (housesWithAppliance.size.toDouble() / filteredRecords.size) * 100
    }

    // Block-wise distribution
    val blockDistribution = remember(filteredRecords, selectedAppliance) {
        val blocksMap = mutableMapOf<String, Int>()
        filteredRecords.forEach { record ->
            val block = record.getBlock()
            val qty = record.getQuantity(selectedAppliance)
            if (qty > 0) {
                blocksMap[block] = (blocksMap[block] ?: 0) + qty
            }
        }
        blocksMap.entries.sortedByDescending { it.value }
    }

    // Sorted list of houses possessing the selected appliance
    val sortedHousesList = remember(housesWithAppliance, selectedAppliance) {
        housesWithAppliance.sortedByDescending { it.getQuantity(selectedAppliance) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Toolbar
        TopAppBar(
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Geometric Balance: Blue rounded-xl icon box
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = "Analytics",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Appliance Analytics",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = "FIELD SURVEY & POWER CONTRIBUTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Selected Appliance Selector Dropdown Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "SELECT APPLIANCE TYPE FOR FIELD AUDIT",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
 
                Box(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { dropdownExpanded = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appliance_selector_dropdown"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Devices, "Appliance", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${selectedAppliance.displayName} (${selectedAppliance.ratedWattage}W)",
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ArrowDropDown, "Open Dropdown")
                        }
                    }
 
                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .heightIn(max = 400.dp)
                    ) {
                        ApplianceType.values().forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(type.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                        Text("${type.ratedWattage}W", color = MaterialTheme.colorScheme.outline, fontSize = 12.sp)
                                    }
                                },
                                onClick = {
                                    selectedAppliance = type
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // LazyColumn containing stats blocks, chart representation, and lists of audited houses
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // 1. KPI Stats Summary row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // KPI: Total Quantity
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Total Installed", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("$totalQty units", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Across township", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                        }
                    }

                    // KPI: Connected Load contribution
                    Card(
                        modifier = Modifier
                            .weight(1.2f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Township Load", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (totalLoadKw >= 100.0) String.format(Locale.getDefault(), "%.1f kW", totalLoadKw) else String.format(Locale.getDefault(), "%.2f kW", totalLoadKw),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Total rated power", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                        }
                    }

                    // KPI: Penetration rate
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(20.dp)),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("Household Pen.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(String.format(Locale.getDefault(), "%.1f%%", penetrationRate), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Of filtered homes", fontSize = 9.sp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            // 2. Block-Wise Proportional Distribution Bar Chart (Natively Styled with Material 3)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(28.dp)),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "Block-wise Device Distribution",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (blockDistribution.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No devices of this type installed in current filters.", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        } else {
                            val maxBlockQty = blockDistribution.maxOfOrNull { it.value }?.toFloat() ?: 1.0f

                            blockDistribution.forEach { entry ->
                                val blockName = entry.key
                                val qty = entry.value
                                val loadKw = (qty * selectedAppliance.ratedWattage) / 1000.0
                                val fraction = qty.toFloat() / maxBlockQty

                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(blockName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                        Text("$qty units (${String.format(Locale.getDefault(), "%.1f kW", loadKw)})", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    
                                    // Simulated Horizontal Bar Chart
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(4.dp))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth(fraction)
                                                .fillMaxHeight()
                                                .background(MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Header title for Household Audit Listing
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HOUSEHOLDS WITH ${selectedAppliance.displayName.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = "${sortedHousesList.size} households",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // 4. Listing houses owning this appliance
            if (sortedHousesList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(24.dp)),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Inbox, "Empty", tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No households own this appliance.", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            } else {
                items(sortedHousesList, key = { it.id }) { house ->
                    val qty = house.getQuantity(selectedAppliance)
                    val contributionKw = (qty * selectedAppliance.ratedWattage) / 1000.0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onHouseClick(house.id) },
                        shape = RoundedCornerShape(28.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Geometric Balance: Custom BLK block badge container
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

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Unit ${house.houseNo}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = house.residentName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$qty units",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.2f kW", contributionKw),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, "Navigate", tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

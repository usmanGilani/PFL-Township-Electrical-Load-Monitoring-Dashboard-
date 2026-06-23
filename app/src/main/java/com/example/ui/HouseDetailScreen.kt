package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ApplianceCategory
import com.example.data.ApplianceType
import com.example.data.HouseRecord
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseDetailScreen(
    houseId: Int,
    viewModel: DashboardViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val house by viewModel.selectedHouse.collectAsStateWithLifecycle()
    var showZeroQuantity by remember { mutableStateOf(false) }

    // Make sure we have selected the correct house ID inside the ViewModel
    LaunchedEffect(houseId) {
        viewModel.setSelectedHouseId(houseId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = house?.let { "Unit ${it.houseNo}" } ?: "Loading...",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium,
                            lineHeight = 16.sp
                        )
                        Text(
                            text = house?.residentName ?: "Audit Details",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = MaterialTheme.colorScheme.onSurface)
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Show All", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                        Switch(
                            checked = showZeroQuantity,
                            onCheckedChange = { showZeroQuantity = it },
                            modifier = Modifier
                                .scale(0.75f)
                                .testTag("toggle_show_all_switch")
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        val record = house
        if (record == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            val scrollState = rememberScrollState()
            val totalLoadKw = record.calculateTotalLoadKw()

            // Safety risk color styling
            val loadColor = when {
                totalLoadKw <= 2.0 -> Color(0xFF4CAF50)
                totalLoadKw <= 5.0 -> Color(0xFFFBC02D)
                totalLoadKw <= 10.0 -> Color(0xFFFF9800)
                else -> Color(0xFFF44336)
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
            ) {
                // House Hero Dashboard Summary Header
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(28.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(28.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "RESIDENT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    record.residentName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.LocationOn, "Location", modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "${record.getBlock()} • House ${record.houseNo}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }

                            // Dynamic Feeders Badge
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = record.gridFeeder,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "CALCULATED PEAK LOAD",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "%.2f kW", totalLoadKw),
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Black,
                                    color = loadColor
                                )
                            }

                            // Total active devices
                            val activeCount = ApplianceType.values().count { record.getQuantity(it) > 0 }
                            val totalQtyCount = ApplianceType.values().sumOf { record.getQuantity(it) }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "ELECTRICAL AUDIT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.outline,
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    "$totalQtyCount units",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "of $activeCount appliance types",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }

                // Title Section
                Text(
                    text = "APPLIANCE BREAKDOWN BY ENGINEERING CATEGORY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 6.dp)
                )

                // Render categories inside M3 Cards
                val categories = ApplianceCategory.values()
                categories.forEach { category ->
                    val appliancesInCategory = ApplianceType.values().filter { it.category == category }
                    val itemsToRender = if (showZeroQuantity) {
                        appliancesInCategory
                    } else {
                        appliancesInCategory.filter { record.getQuantity(it) > 0 }
                    }

                    // Check if there are items to show in this category
                    if (itemsToRender.isNotEmpty() || category == ApplianceCategory.OTHERS) {
                        CategorySectionCard(
                            category = category,
                            items = itemsToRender,
                            record = record,
                            loadColor = loadColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}


/**
 * Category section Card displaying its audit manifest.
 */
@Composable
fun CategorySectionCard(
    category: ApplianceCategory,
    items: List<ApplianceType>,
    record: HouseRecord,
    loadColor: Color
) {
    val totalCategoryLoadW = record.calculateCategoryLoad(category)
    val categoryIcon = when (category) {
        ApplianceCategory.MAJOR_LOADS -> Icons.Default.AcUnit
        ApplianceCategory.FANS -> Icons.Default.Toys
        ApplianceCategory.LIGHTING -> Icons.Default.Lightbulb
        ApplianceCategory.SOCKETS -> Icons.Default.Power
        ApplianceCategory.OTHERS -> Icons.Default.DevicesOther
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Category Header with Subtotals
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = categoryIcon,
                        contentDescription = category.displayName,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = category.displayName,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Text(
                    text = if (totalCategoryLoadW >= 1000) String.format(Locale.getDefault(), "%.2f kW", totalCategoryLoadW / 1000.0) else "$totalCategoryLoadW W",
                    fontWeight = FontWeight.Bold,
                    color = if (category == ApplianceCategory.MAJOR_LOADS && totalCategoryLoadW > 0) loadColor else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

            // Check if there are any devices
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No appliances active in this category.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // Table header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Appliance Description", modifier = Modifier.weight(2f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text("Qty", modifier = Modifier.weight(0.5f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.Center)
                    Text("Rating", modifier = Modifier.weight(0.7f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.End)
                    Text("Connected", modifier = Modifier.weight(0.9f), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, textAlign = TextAlign.End)
                }
                
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))

                // Items list
                items.forEach { type ->
                    val qty = record.getQuantity(type)
                    val contribution = record.getLoadContribution(type)
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Title Column
                        Column(modifier = Modifier.weight(2f)) {
                            Text(
                                text = type.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Header: ${type.csvHeaderName}",
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.outline,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Quantity Column
                        Text(
                            text = qty.toString(),
                            modifier = Modifier.weight(0.5f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            color = if (qty > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )

                        // Rating Column
                        Text(
                            text = "${type.ratedWattage} W",
                            modifier = Modifier.weight(0.7f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = TextAlign.End
                        )

                        // Connected Load Column
                        Text(
                            text = if (contribution >= 1000) String.format(Locale.getDefault(), "%.1f kW", contribution / 1000.0) else "$contribution W",
                            modifier = Modifier.weight(0.9f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.End,
                            color = if (contribution > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.05f))
                }
            }
        }
    }
}

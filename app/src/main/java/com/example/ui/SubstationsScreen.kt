package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.HouseRecord
import com.example.data.ApplianceCategory
import com.example.data.ApplianceType
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubstationsScreen(
    viewModel: DashboardViewModel,
    onHouseClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val filteredRecords by viewModel.filteredRecords.collectAsStateWithLifecycle()
    val customWattages by viewModel.customWattages.collectAsStateWithLifecycle()

    // Tab state (0 = Grids / Transformers, 1 = Blocks Telemetry, 2 = Appliances Loading)
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    // Simulation states
    var powerFactor by remember { mutableFloatStateOf(0.90f) }
    var useFilteredScope by remember { mutableStateOf(false) }
    var selectedSubstationId by remember { mutableStateOf<Int?>(null) }

    // Selected dataset
    val activeDataset = if (useFilteredScope) filteredRecords else allRecords

    // Substation configuration (physically isolated, separate transformers)
    val substationRatings = listOf(1250.0, 1000.0, 1600.0) // kVA ratings
    val substationNames = listOf("Substation 1", "Substation 2", "Substation 3")
    val substationSectors = listOf("North Sector", "South Sector", "East/West Sector")

    // Helper: Map feeder to substation index
    fun getSubstationIndex(feeder: String): Int {
        val f = feeder.trim().uppercase()
        return when {
            f.endsWith("A") || f.contains("FEEDER A") || f.contains("SUBSTATION 1") || f.contains("SUB 1") -> 0
            f.endsWith("B") || f.contains("FEEDER B") || f.contains("SUBSTATION 2") || f.contains("SUB 2") -> 1
            f.endsWith("C") || f.endsWith("D") || f.contains("FEEDER C") || f.contains("FEEDER D") || f.contains("SUBSTATION 3") || f.contains("SUB 3") -> 2
            else -> {
                val hash = Math.abs(feeder.hashCode())
                hash % 3
            }
        }
    }

    // Calculations: Individual Substation load calculations (isolated, no sharing)
    val substationLoads = remember(activeDataset, customWattages) {
        val loads = doubleArrayOf(0.0, 0.0, 0.0)
        activeDataset.forEach { record ->
            val idx = getSubstationIndex(record.gridFeeder)
            loads[idx] += record.calculateTotalLoadKw(customWattages)
        }
        loads
    }

    // Group houses by Substation for drill-downs
    val housesBySubstation = remember(activeDataset) {
        val lists = listOf(mutableListOf<HouseRecord>(), mutableListOf<HouseRecord>(), mutableListOf<HouseRecord>())
        activeDataset.forEach { record ->
            val idx = getSubstationIndex(record.gridFeeder)
            lists[idx].add(record)
        }
        lists
    }

    // Group feeders by Substation
    val feedersBySubstation = remember(activeDataset, customWattages) {
        val maps = listOf(mutableMapOf<String, Pair<Int, Double>>(), mutableMapOf<String, Pair<Int, Double>>(), mutableMapOf<String, Pair<Int, Double>>())
        activeDataset.forEach { record ->
            val idx = getSubstationIndex(record.gridFeeder)
            val current = maps[idx][record.gridFeeder] ?: Pair(0, 0.0)
            maps[idx][record.gridFeeder] = Pair(
                current.first + 1,
                current.second + record.calculateTotalLoadKw(customWattages)
            )
        }
        maps
    }

    // Design Colors for independent transformers
    val subColors = listOf(
        Color(0xFF0D9488), // Teal
        Color(0xFF8B5CF6), // Purple
        Color(0xFFF59E0B)  // Amber
    )
    val subColorGradients = listOf(
        listOf(Color(0xFF0D9488), Color(0xFF0F766E)),
        listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9)),
        listOf(Color(0xFFF59E0B), Color(0xFFD97706))
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Redesigned Masthead Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "TOWNSHIP POWER ANALYTICS",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Substation & Load Telemetry",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "Analytics",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Custom M3 Primary Scrollable or standard TabRow for switching analysis profiles
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = { Text("Grid Stations", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.ElectricMeter, "Substations", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = { Text("Blocks Loading", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.GridOn, "Blocks", modifier = Modifier.size(16.dp)) }
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = { Text("Appliance Loads", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                icon = { Icon(Icons.Default.Bolt, "Appliances", modifier = Modifier.size(16.dp)) }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Shared Configuration & Simulation Header for all tabs
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (useFilteredScope) Icons.Default.FilterAlt else Icons.Default.Public,
                            contentDescription = "Scope Indicator",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (useFilteredScope) "Filtered Scope (${activeDataset.size} Units)" else "Township Full Scope (${activeDataset.size} Units)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .clickable { useFilteredScope = !useFilteredScope }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = if (useFilteredScope) "Use Full Township" else "Use Filters",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Content Switching based on Tab index
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> GridsTabContent(
                    activeDataset = activeDataset,
                    substationLoads = substationLoads,
                    substationRatings = substationRatings,
                    substationNames = substationNames,
                    substationSectors = substationSectors,
                    feedersBySubstation = feedersBySubstation,
                    housesBySubstation = housesBySubstation,
                    subColors = subColors,
                    subColorGradients = subColorGradients,
                    powerFactor = powerFactor,
                    onPowerFactorChange = { powerFactor = it },
                    customWattages = customWattages,
                    selectedSubstationId = selectedSubstationId,
                    onSubstationIdChange = { selectedSubstationId = it },
                    onHouseClick = onHouseClick
                )
                1 -> BlocksTabContent(
                    activeDataset = activeDataset,
                    customWattages = customWattages,
                    onHouseClick = onHouseClick
                )
                2 -> AppliancesTabContent(
                    activeDataset = activeDataset,
                    customWattages = customWattages
                )
            }
        }
    }
}

// ==========================================
// TAB 1: GRIDS TAB CONTENT (INDEPENDENT WORK)
// ==========================================
@Composable
fun GridsTabContent(
    activeDataset: List<HouseRecord>,
    substationLoads: DoubleArray,
    substationRatings: List<Double>,
    substationNames: List<String>,
    substationSectors: List<String>,
    feedersBySubstation: List<Map<String, Pair<Int, Double>>>,
    housesBySubstation: List<List<HouseRecord>>,
    subColors: List<Color>,
    subColorGradients: List<List<Color>>,
    powerFactor: Float,
    onPowerFactorChange: (Float) -> Unit,
    customWattages: Map<ApplianceType, Int>,
    selectedSubstationId: Int?,
    onSubstationIdChange: (Int?) -> Unit,
    onHouseClick: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Independent system note / alert banner
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)), // Amber tint
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Independent Status Warning",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "INDEPENDENT ISOLATED TRANSFORMERS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309),
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "Grid stations operate completely independently on physically separated transformers. Load is never shared across grids. Managing overload spikes relies entirely on managing demand inside each isolated sector.",
                            fontSize = 11.sp,
                            color = Color(0xFF78350F),
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // 2. Power Factor slider block
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.SettingsSuggest, "Power Factor", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Target Power Factor (PF)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = String.format(Locale.getDefault(), "%.2f", powerFactor),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Slider(
                        value = powerFactor,
                        onValueChange = { onPowerFactorChange(Math.round(it * 100f) / 100f) },
                        valueRange = 0.80f..1.00f,
                        steps = 19,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0.80 (Inductive Spike)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("0.90 (Nominal)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("1.00 (Ideal Unity)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 3. Three Independent Circular Gauges Side-by-Side
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "INDEPENDENT TRANSFORMER GAUGES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0..2) {
                            val activeLoadKw = substationLoads[i]
                            val ratingKva = substationRatings[i]
                            val capacityKw = ratingKva * powerFactor
                            val loadingPct = if (capacityKw > 0) (activeLoadKw / capacityKw) * 100.0 else 0.0

                            val gaugeColor = when {
                                loadingPct <= 75.0 -> subColors[i]
                                loadingPct <= 95.0 -> Color(0xFFD97706) // Warning Amber
                                else -> Color(0xFFDC2626) // Danger Red
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier.size(86.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        // Gray background track
                                        drawCircle(
                                            color = Color.LightGray.copy(alpha = 0.25f),
                                            style = Stroke(width = 8.dp.toPx())
                                        )

                                        // Colored loading sweep
                                        val sweepAngle = (loadingPct / 100.0 * 360.0).coerceAtMost(360.0).toFloat()
                                        drawArc(
                                            color = gaugeColor,
                                            startAngle = -90f,
                                            sweepAngle = sweepAngle,
                                            useCenter = false,
                                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.1f%%", loadingPct),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = gaugeColor
                                        )
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.0f kW", activeLoadKw),
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = substationNames[i],
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format(Locale.getDefault(), "Max %.0fkW", capacityKw),
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Comparative Active vs Capacity Bar Charts for Independent Substation Loads
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ISOLATED LOAD VS RATED PHYSICAL CAPACITY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    substationNames.forEachIndexed { i, name ->
                        val load = substationLoads[i]
                        val capacityKva = substationRatings[i]
                        val capacityKw = capacityKva * powerFactor
                        val loadingPercent = if (capacityKw > 0) (load / capacityKw) * 100 else 0.0
                        val barFraction = Math.min(1.0, load / capacityKw).toFloat()

                        val barColor = when {
                            loadingPercent <= 75.0 -> subColors[i]
                            loadingPercent <= 95.0 -> Color(0xFFD97706) // Warning Orange
                            else -> Color(0xFFDC2626) // Danger Red
                        }

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f kW", load),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = barColor
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), " / %.0f kW Limit", capacityKw),
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Capacity Visual Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape = CircleShape)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(barFraction)
                                        .fillMaxHeight()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(subColors[i], barColor)
                                            ),
                                            shape = CircleShape
                                        )
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(substationSectors[i], fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = String.format(Locale.getDefault(), "Loading: %.1f%%", loadingPercent),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = barColor
                                )
                            }
                        }
                        if (i < 2) {
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        // 5. Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SUBSTATION & TRANSFORMER PROFILES",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "3 STATIONS ACTIVE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )
            }
        }

        // 6. Individual Substation Expandable Cards
        items(substationNames.indices.toList()) { i ->
            val name = substationNames[i]
            val sector = substationSectors[i]
            val ratingKva = substationRatings[i]
            val activeKw = substationLoads[i]
            val apparentKva = activeKw / powerFactor
            val capacityKw = ratingKva * powerFactor
            val loadingPercent = if (capacityKw > 0) (activeKw / capacityKw) * 100 else 0.0
            val isExpanded = selectedSubstationId == i

            val (statusLabel, statusColor, statusBg, glowColor) = when {
                loadingPercent <= 75.0 -> Quadruple("SECURE - HEALTHY", Color(0xFF0F766E), Color(0xFFE6F4F1), Color(0xFF14B8A6))
                loadingPercent <= 95.0 -> Quadruple("CAUTION - ELEVATED", Color(0xFFB45309), Color(0xFFFEF3C7), Color(0xFFF59E0B))
                else -> Quadruple("CRITICAL - OVERLOADED", Color(0xFFE11D48), Color(0xFFFFE4E6), Color(0xFFF43F5E))
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSubstationIdChange(if (isExpanded) null else i) }
                    .border(
                        BorderStroke(
                            width = if (isExpanded) 2.dp else 1.dp,
                            color = if (isExpanded) subColors[i] else MaterialTheme.colorScheme.outline
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Substation Colored Badge
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .background(
                                    brush = Brush.verticalGradient(subColorGradients[i]),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ElectricMeter,
                                contentDescription = "Sub",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$sector • ${ratingKva.toInt()} kVA Transformer",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                              )
                        }

                        // Glowing State Indicator Light & Percentage
                        Column(horizontalAlignment = Alignment.End) {
                            Box(
                                modifier = Modifier
                                    .background(statusBg, shape = RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 3.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(glowColor, shape = CircleShape)
                                    )
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f%%", loadingPercent),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = statusColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Inline Loading bar
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), shape = CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(Math.min(1.0f, loadingPercent.toFloat() / 100f))
                                .fillMaxHeight()
                                .background(statusColor, shape = CircleShape)
                        )
                    }

                    // Expanded metrics & list of connected feeders
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Technical Substation parameters
                            Text(
                                text = "TRANSFORMER PARAMETERS",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricBox(label = "Nominal Rating", value = "${ratingKva.toInt()} kVA", modifier = Modifier.weight(1f))
                                MetricBox(label = "Active Power", value = String.format(Locale.getDefault(), "%.1f kW", activeKw), modifier = Modifier.weight(1f))
                                MetricBox(label = "Apparent Power", value = String.format(Locale.getDefault(), "%.1f kVA", apparentKva), modifier = Modifier.weight(1f))
                                MetricBox(label = "Status", value = statusLabel, valueColor = statusColor, modifier = Modifier.weight(1.3f))
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Feeders list
                            Text(
                                text = "FEEDER DISTRIBUTION BREAKDOWN",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val feedersMap = feedersBySubstation[i]
                            if (feedersMap.isEmpty()) {
                                Text("No feeders registered under this substation.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(vertical = 4.dp))
                            } else {
                                feedersMap.forEach { (feederName, data) ->
                                    val houseCount = data.first
                                    val feederLoadKw = data.second
                                    val feederLoadShare = if (activeKw > 0) (feederLoadKw / activeKw) * 100 else 0.0

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), shape = RoundedCornerShape(8.dp))
                                            .padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.PowerInput, "Feeder", tint = subColors[i], modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Column {
                                                Text(feederName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                                Text("$houseCount households connected", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.1f kW", feederLoadKw),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.1f%% share", feederLoadShare),
                                                fontSize = 9.sp,
                                                color = subColors[i],
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Highest load households connected to this Substation
                            val houses = housesBySubstation[i]
                            val peakHousesInSub = houses.sortedByDescending { it.calculateTotalLoadKw(customWattages) }.take(3)
                            
                            if (peakHousesInSub.isNotEmpty()) {
                                Text(
                                    text = "TOP DEMAND HOUSES ON THIS SUBSTATION",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                peakHousesInSub.forEach { house ->
                                    val hLoadKw = house.calculateTotalLoadKw(customWattages)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onHouseClick(house.id) }
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Home, "House", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Unit ${house.houseNo} (${house.residentName})", fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.2f kW", hLoadKw),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (hLoadKw > 5.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                                            )
                                            Icon(Icons.Default.ChevronRight, "View Details", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 2: BLOCKS TAB CONTENT (GRID SYSTEM BLOCKS)
// ==========================================
@Composable
fun BlocksTabContent(
    activeDataset: List<HouseRecord>,
    customWattages: Map<ApplianceType, Int>,
    onHouseClick: (Int) -> Unit
) {
    // Process Blocks loads
    val blockDataMap = remember(activeDataset, customWattages) {
        val map = mutableMapOf<String, MutableList<HouseRecord>>()
        activeDataset.forEach { record ->
            val blockName = record.getBlock()
            val list = map.getOrPut(blockName) { mutableListOf() }
            list.add(record)
        }
        
        // Convert to summary items
        map.map { (block, houses) ->
            val totalKw = houses.sumOf { it.calculateTotalLoadKw(customWattages) }
            val avgKw = if (houses.isNotEmpty()) totalKw / houses.size else 0.0
            BlockSummary(
                blockName = block,
                houseCount = houses.size,
                totalLoadKw = totalKw,
                avgHouseLoadKw = avgKw,
                peakHouse = houses.maxByOrNull { it.calculateTotalLoadKw(customWattages) }
            )
        }.sortedBy { it.blockName }
    }

    val maxBlockLoad = remember(blockDataMap) {
        blockDataMap.maxOfOrNull { it.totalLoadKw } ?: 1.0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Blocks Summary Card KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val peakBlock = blockDataMap.maxByOrNull { it.totalLoadKw }
                val efficientBlock = blockDataMap.minByOrNull { it.totalLoadKw }

                // Peak Block KPI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("PEAK DEMAND BLOCK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(peakBlock?.blockName ?: "N/A", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                        Text(String.format(Locale.getDefault(), "%.1f kW total", peakBlock?.totalLoadKw ?: 0.0), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Lowest Demand Block KPI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("MOST EFFICIENT BLOCK", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(efficientBlock?.blockName ?: "N/A", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F766E))
                        Text(String.format(Locale.getDefault(), "%.1f kW total", efficientBlock?.totalLoadKw ?: 0.0), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 2. Blocks Loading Bar Charts Custom View
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BLOCK LOAD ANALYSIS COMPARISON (kW)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (blockDataMap.isEmpty()) {
                        Text("No active data to plot blocks.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        blockDataMap.forEach { item ->
                            val progressFraction = if (maxBlockLoad > 0) (item.totalLoadKw / maxBlockLoad).toFloat() else 0f
                            
                            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = item.blockName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = " (${item.houseCount} houses)",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.2f kW", item.totalLoadKw),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Visual bar representing block load weight
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(12.dp)
                                        .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(progressFraction)
                                            .fillMaxHeight()
                                            .background(
                                                brush = Brush.horizontalGradient(
                                                    colors = listOf(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        MaterialTheme.colorScheme.primary
                                                    )
                                                ),
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = String.format(Locale.getDefault(), "Avg: %.2f kW / house", item.avgHouseLoadKw),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    
                                    val pctOfMax = progressFraction * 100
                                    Text(
                                        text = String.format(Locale.getDefault(), "%.1f%% of peak block load", pctOfMax),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Peak House per Block breakdown
        item {
            Text(
                text = "INDIVIDUAL PEAK CONSUMERS BY BLOCK",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(blockDataMap) { summary ->
            val peak = summary.peakHouse
            if (peak != null) {
                val peakKw = peak.calculateTotalLoadKw(customWattages)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onHouseClick(peak.id) }
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingUp, "Peak", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${summary.blockName} Peak: Unit ${peak.houseNo}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Resident: ${peak.residentName}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f kW", peakKw),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = "View Audit",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// TAB 3: APPLIANCES TAB CONTENT (CATEGORIES & DETAILS)
// ==========================================
@Composable
fun AppliancesTabContent(
    activeDataset: List<HouseRecord>,
    customWattages: Map<ApplianceType, Int>
) {
    // 1. Calculate Aggregate Loads per category
    val categoryLoads = remember(activeDataset, customWattages) {
        val loads = mutableMapOf<ApplianceCategory, Double>()
        ApplianceCategory.values().forEach { category ->
            var totalWatts = 0.0
            activeDataset.forEach { house ->
                totalWatts += house.calculateCategoryLoad(category, customWattages)
            }
            loads[category] = totalWatts / 1000.0 // Convert to kW
        }
        loads.entries.sortedByDescending { it.value }
    }

    // 2. Calculate loads per individual appliance type
    val applianceLoads = remember(activeDataset, customWattages) {
        ApplianceType.values().map { type ->
            var totalQty = 0
            var totalKw = 0.0
            activeDataset.forEach { house ->
                val qty = house.getQuantity(type)
                totalQty += qty
                totalKw += (qty * (customWattages[type] ?: type.ratedWattage)) / 1000.0
            }
            ApplianceTypeSummary(
                applianceType = type,
                totalQty = totalQty,
                totalKw = totalKw
            )
        }.filter { it.totalQty > 0 }.sortedByDescending { it.totalKw }
    }

    val maxCategoryLoad = remember(categoryLoads) {
        categoryLoads.maxOfOrNull { it.value } ?: 1.0
    }
    val maxApplianceLoad = remember(applianceLoads) {
        applianceLoads.maxOfOrNull { it.totalKw } ?: 1.0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // 1. Core Appliance KPIs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val topCategory = categoryLoads.firstOrNull()
                val topAppliance = applianceLoads.firstOrNull()

                // Heavy Category KPI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("HEAVIEST CATEGORY", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(topCategory?.key?.displayName ?: "N/A", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(String.format(Locale.getDefault(), "%.1f kW demand", topCategory?.value ?: 0.0), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Heavy Appliance KPI
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(12.dp)),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("HEAVIEST APPLIANCE TYPE", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(topAppliance?.applianceType?.displayName ?: "N/A", fontSize = 12.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.error, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(String.format(Locale.getDefault(), "%.1f kW load (%d units)", topAppliance?.totalKw ?: 0.0, topAppliance?.totalQty ?: 0), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 2. Category Share Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant), shape = RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "APPLIANCE CATEGORY LOAD COMPARISON (kW)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    categoryLoads.forEach { entry ->
                        val cat = entry.key
                        val kw = entry.value
                        val fraction = if (maxCategoryLoad > 0) (kw / maxCategoryLoad).toFloat() else 0f

                        val icon = when (cat) {
                            ApplianceCategory.MAJOR_LOADS -> Icons.Default.Air
                            ApplianceCategory.FANS -> Icons.Default.Cyclone
                            ApplianceCategory.LIGHTING -> Icons.Default.Lightbulb
                            ApplianceCategory.SOCKETS -> Icons.Default.ElectricalServices
                            ApplianceCategory.OTHERS -> Icons.Default.DevicesOther
                        }

                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, cat.displayName, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(cat.displayName, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Text(
                                    text = String.format(Locale.getDefault(), "%.1f kW", kw),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(fraction)
                                        .fillMaxHeight()
                                        .background(
                                            brush = Brush.horizontalGradient(
                                                colors = listOf(
                                                    MaterialTheme.colorScheme.primaryContainer,
                                                    MaterialTheme.colorScheme.primary
                                                )
                                            ),
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Detailed individual appliances list
        item {
            Text(
                text = "DETAILED LOAD CONTRIBUTION BY APPLIANCE TYPE",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(applianceLoads) { item ->
            val fraction = if (maxApplianceLoad > 0) (item.totalKw / maxApplianceLoad).toFloat() else 0f
            
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1.3f)) {
                            Text(
                                text = item.applianceType.displayName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${item.applianceType.category.displayName} • Rating: ${customWattages[item.applianceType] ?: item.applianceType.ratedWattage}W",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(
                            horizontalAlignment = Alignment.End,
                            modifier = Modifier.weight(0.7f)
                        ) {
                            Text(
                                text = String.format(Locale.getDefault(), "%.2f kW", item.totalKw),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Qty: ${item.totalQty} Units",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Appliance loading weight bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f), shape = CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(fraction)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.85f), shape = CircleShape)
                        )
                    }
                }
            }
        }
    }
}

// Simple summaries data classes
data class BlockSummary(
    val blockName: String,
    val houseCount: Int,
    val totalLoadKw: Double,
    val avgHouseLoadKw: Double,
    val peakHouse: HouseRecord?
)

data class ApplianceTypeSummary(
    val applianceType: ApplianceType,
    val totalQty: Int,
    val totalKw: Double
)

@Composable
fun MetricBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(8.dp))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp))
            .padding(6.dp)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Simple quad helper class
data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

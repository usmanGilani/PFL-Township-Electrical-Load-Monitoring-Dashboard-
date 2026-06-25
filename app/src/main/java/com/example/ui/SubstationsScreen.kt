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

    // Interactive simulation controls
    var powerFactor by remember { mutableFloatStateOf(0.90f) }
    var useFilteredScope by remember { mutableStateOf(false) }
    var selectedSubstationId by remember { mutableStateOf<Int?>(null) }

    // Choose target dataset
    val activeDataset = if (useFilteredScope) filteredRecords else allRecords

    // Substation definitions
    val substationRatings = listOf(1250.0, 1000.0, 1600.0) // kVA ratings
    val substationNames = listOf("Substation 1", "Substation 2", "Substation 3")
    val substationSectors = listOf("North Sector", "South Sector", "East/West Sector")

    // Helper: Map feeder to substation index (0, 1, or 2)
    fun getSubstationIndexForFeeder(feeder: String): Int {
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

    // Process data grouped by substation
    val substationLoads = remember(activeDataset, customWattages) {
        val loads = doubleArrayOf(0.0, 0.0, 0.0)
        activeDataset.forEach { record ->
            val idx = getSubstationIndexForFeeder(record.gridFeeder)
            loads[idx] += record.calculateTotalLoadKw(customWattages)
        }
        loads
    }

    // Group houses by Substation for detailed lists
    val housesBySubstation = remember(activeDataset) {
        val lists = listOf(mutableListOf<HouseRecord>(), mutableListOf<HouseRecord>(), mutableListOf<HouseRecord>())
        activeDataset.forEach { record ->
            val idx = getSubstationIndexForFeeder(record.gridFeeder)
            lists[idx].add(record)
        }
        lists
    }

    // Group feeders by Substation for detailed metrics
    val feedersBySubstation = remember(activeDataset, customWattages) {
        val maps = listOf(mutableMapOf<String, Pair<Int, Double>>(), mutableMapOf<String, Pair<Int, Double>>(), mutableMapOf<String, Pair<Int, Double>>())
        activeDataset.forEach { record ->
            val idx = getSubstationIndexForFeeder(record.gridFeeder)
            val current = maps[idx][record.gridFeeder] ?: Pair(0, 0.0)
            maps[idx][record.gridFeeder] = Pair(
                current.first + 1,
                current.second + record.calculateTotalLoadKw(customWattages)
            )
        }
        maps
    }

    // Computed totals
    val totalActiveLoadKw = substationLoads.sum()
    val totalCapacityKva = substationRatings.sum()
    val totalCapacityKwAtPf = totalCapacityKva * powerFactor
    val overallLoadingPercentage = if (totalCapacityKwAtPf > 0) (totalActiveLoadKw / totalCapacityKwAtPf) * 100 else 0.0

    // Design Colors for Substations
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
                    text = "GRID TELEMETRY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Substation Loading",
                    fontSize = 22.sp,
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
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Substations",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            // 1. Interactive Simulation Controls Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "SIMULATION CONTROL",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    letterSpacing = 1.sp
                               )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Interactive Transformer Analysis",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Scope Toggle (Township Total vs Filtered Houses)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { useFilteredScope = !useFilteredScope }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("scope_toggle")
                            ) {
                                Icon(
                                    imageVector = if (useFilteredScope) Icons.Default.FilterAlt else Icons.Default.Public,
                                    contentDescription = "Scope",
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (useFilteredScope) "Filtered Scope" else "Township Full",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Power Factor Configurator Slider
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
                                    fontWeight = FontWeight.SemiBold,
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

                        Spacer(modifier = Modifier.height(6.dp))

                        Slider(
                            value = powerFactor,
                            onValueChange = { powerFactor = Math.round(it * 100f) / 100f },
                            valueRange = 0.80f..1.00f,
                            steps = 19, // 0.01 increments from 0.80 to 1.00
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("power_factor_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("0.80 (Inductive / Poor)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("0.90 (Standard)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("1.00 (Ideal / Resistive)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 2. Summary Dashboard Metrics (Connected / Apparent / Overall loading)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // KPI Total Demand Load
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "ACTIVE DEMAND",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f kW", totalActiveLoadKw),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Sum of active houses", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // KPI Est. Apparent Load
                    val totalApparentLoadKva = totalActiveLoadKw / powerFactor
                    Card(
                        modifier = Modifier
                            .weight(1.1f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "APPARENT LOAD",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f kVA", totalApparentLoadKva),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("kW / $powerFactor PF", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // KPI Overall Grid Load %
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                "GRID LOADING",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            val overallColor = when {
                                overallLoadingPercentage <= 75.0 -> Color(0xFF0F766E)
                                overallLoadingPercentage <= 95.0 -> Color(0xFFB45309)
                                else -> Color(0xFFE11D48)
                            }
                            Text(
                                text = String.format(Locale.getDefault(), "%.1f%%", overallLoadingPercentage),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = overallColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("Of ${totalCapacityKva.toInt()} kVA cap", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // 3. Custom Charts Summary Row (Donut load share + comparison capacity bars)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.outline), shape = RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "VISUAL ANALYSIS & LOAD SHARE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (totalActiveLoadKw <= 0) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.QueryStats, "No Data", tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No active loads to plot charts.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Left: Custom Canvas Donut Chart (Proportional Share)
                                Box(
                                    modifier = Modifier
                                        .size(130.dp)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val total = totalActiveLoadKw.toFloat()
                                        var startAngle = -90f
                                        substationLoads.forEachIndexed { i, load ->
                                            if (load > 0) {
                                                val sweepAngle = (load.toFloat() / total) * 360f
                                                drawArc(
                                                    color = subColors[i],
                                                    startAngle = startAngle,
                                                    sweepAngle = sweepAngle,
                                                    useCenter = false,
                                                    style = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                                                )
                                                startAngle += sweepAngle
                                            }
                                        }
                                        // Draw a simple gray background ring underneath if needed
                                        if (total <= 0f) {
                                            drawCircle(
                                                color = Color.LightGray.copy(alpha = 0.3f),
                                                style = Stroke(width = 16.dp.toPx())
                                            )
                                        }
                                    }

                                    // Display total inside ring
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "GRID SHARE",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            letterSpacing = 0.5.sp
                                        )
                                        Text(
                                            text = String.format(Locale.getDefault(), "%.0f%%", overallLoadingPercentage),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                // Right: Legends with mini progress and percentages
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    substationNames.forEachIndexed { i, name ->
                                        val load = substationLoads[i]
                                        val sharePercent = if (totalActiveLoadKw > 0) (load / totalActiveLoadKw) * 100 else 0.0
                                        
                                        Column {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Box(
                                                        modifier = Modifier
                                                            .size(10.dp)
                                                            .background(subColors[i], shape = RoundedCornerShape(3.dp))
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(name, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                                }
                                                Text(
                                                    text = String.format(Locale.getDefault(), "%.1f%%", sharePercent),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = subColors[i]
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = String.format(Locale.getDefault(), "%.1f kW demand", load),
                                                fontSize = 9.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.padding(start = 16.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant,
                                thickness = 1.dp
                            )

                            // Comparative Active vs Capacity Bar Chart
                            Text(
                                text = "ACTIVE DEMAND VS RATED TRANSFORMER CAPACITY",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )

                            substationNames.forEachIndexed { i, name ->
                                val load = substationLoads[i]
                                val capacityKva = substationRatings[i]
                                val capacityKw = capacityKva * powerFactor
                                val loadingPercent = if (capacityKw > 0) (load / capacityKw) * 100 else 0.0
                                val barFraction = Math.min(1.0, load / capacityKw).toFloat()

                                val barColor = when {
                                    loadingPercent <= 75.0 -> subColors[i]
                                    loadingPercent <= 95.0 -> Color(0xFFB45309) // Warning Orange
                                    else -> Color(0xFFD92727) // Danger Red
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
                                                text = String.format(Locale.getDefault(), " / %.0f kW Max", capacityKw),
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
                                            .height(8.dp)
                                            .background(MaterialTheme.colorScheme.outlineVariant, shape = CircleShape)
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
                            }
                        }
                    }
                }
            }

            // 4. Detailed Section Header: Grid Statuses & Substation Breakdown
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
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

            // 5. Individual Substation Cards with Glow Indicators & Feeder Lists
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
                        .clickable { selectedSubstationId = if (isExpanded) null else i }
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
                                .background(MaterialTheme.colorScheme.outlineVariant, shape = CircleShape)
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
}

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

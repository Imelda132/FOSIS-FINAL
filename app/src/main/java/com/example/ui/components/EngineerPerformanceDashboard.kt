package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.PRESET_SYSTEM_USERS
import com.example.data.model.UserRole
import com.example.data.model.WorkflowStatus
import com.example.ui.theme.*

/**
 * Data model representing aggregated performance KPIs for a single engineer.
 */
data class EngineerKpiData(
    val name: String,
    val email: String,
    val initials: String,
    val phone: String,
    val region: String,
    val totalTickets: Int,
    val troubleshootCount: Int,
    val maintenanceCount: Int,
    val adminCount: Int,
    val inSlaCount: Int,
    val overSlaCount: Int,
    val completedCount: Int,
    val approvedCount: Int,
    val slaRatePercent: Double,
    val completionRatePercent: Double,
    val infraBreakdown: Map<String, Int>
)

/**
 * Summary dashboard view visualizing engineer performance KPIs with modern interactive charts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EngineerPerformanceDashboard(
    items: List<FosisItemEntity>,
    onSelectItem: (FosisItemEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTypeFilter by remember { mutableStateOf<FosisType?>(null) }
    var selectedMonthFilter by remember { mutableStateOf("ALL") }
    var selectedEngineerName by remember { mutableStateOf<String?>(null) }
    var chartViewMode by remember { mutableStateOf("VOLUME") } // VOLUME, SLA, INFRA

    // 1. Filter items by selected period / type if needed
    val filteredItems = remember(items, selectedTypeFilter, selectedMonthFilter) {
        items.filter { item ->
            val typeMatch = selectedTypeFilter == null || item.type == selectedTypeFilter
            val monthMatch = selectedMonthFilter == "ALL" || item.bulan.equals(selectedMonthFilter, ignoreCase = true)
            typeMatch && monthMatch
        }
    }

    // 2. Aggregate KPI per engineer
    val allEngineers = remember {
        PRESET_SYSTEM_USERS.filter {
            it.role == UserRole.TEKNISI ||
                    it.role == UserRole.IC_TROUBLESHOOT ||
                    it.role == UserRole.IC_MAINTENANCE
        }
    }

    val engineerKpiList = remember(filteredItems, allEngineers) {
        val grouped = filteredItems.groupBy { it.engineerName.trim() }

        // Map all known engineers + any extra names in items
        val engineerNames = (allEngineers.map { it.name } + grouped.keys).distinct().filter { it.isNotBlank() }

        engineerNames.map { name ->
            val user = allEngineers.find { it.name.equals(name, ignoreCase = true) }
            val engItems = grouped[name] ?: emptyList()
            val total = engItems.size
            val tsItems = engItems.filter { it.type == FosisType.TROUBLESHOOT }
            val mntItems = engItems.filter { it.type == FosisType.MAINTENANCE }
            val admItems = engItems.filter { it.type == FosisType.ADMINISTRASI }

            val inSla = tsItems.count { it.slaStatus.equals("IN SLA", ignoreCase = true) }
            val overSla = tsItems.count { it.slaStatus.equals("OVER SLA", ignoreCase = true) }
            val slaRate = if (tsItems.isNotEmpty()) (inSla.toDouble() * 100.0) / tsItems.size else 100.0

            val completed = engItems.count {
                it.maintenanceStatus.equals("DONE", ignoreCase = true) ||
                        it.adminStatus.equals("DONE", ignoreCase = true) ||
                        it.workflowStatus == WorkflowStatus.APPROVED_DEPT_HEAD ||
                        it.workflowStatus == WorkflowStatus.LOCKED_ADMIN
            }
            val compRate = if (total > 0) (completed.toDouble() * 100.0) / total else 0.0
            val approved = engItems.count { it.workflowStatus == WorkflowStatus.APPROVED_DEPT_HEAD }

            val infraMap = engItems.groupBy { it.getEffectiveInfraCategory() }.mapValues { it.value.size }

            val initials = if (user != null && user.avatarInitials.isNotBlank()) {
                user.avatarInitials
            } else {
                name.split(" ").take(2).mapNotNull { it.firstOrNull()?.toString() }.joinToString("")
            }

            EngineerKpiData(
                name = name,
                email = user?.email ?: "${name.lowercase().replace(" ", ".")}@fosis-ops.id",
                initials = initials,
                phone = user?.phone ?: "+62812XXXXXX",
                region = user?.region ?: "Tim Field Engineer",
                totalTickets = total,
                troubleshootCount = tsItems.size,
                maintenanceCount = mntItems.size,
                adminCount = admItems.size,
                inSlaCount = inSla,
                overSlaCount = overSla,
                completedCount = completed,
                approvedCount = approved,
                slaRatePercent = slaRate,
                completionRatePercent = compRate,
                infraBreakdown = infraMap
            )
        }.sortedByDescending { it.totalTickets }
    }

    // Filtered by search query
    val displayedEngineers = remember(engineerKpiList, searchQuery) {
        if (searchQuery.isBlank()) engineerKpiList
        else engineerKpiList.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
        }
    }

    // Team Summary Calculations
    val totalTeamTickets = filteredItems.size
    val activeEngineersCount = engineerKpiList.count { it.totalTickets > 0 }
    val topPerformer = engineerKpiList.maxByOrNull { it.totalTickets }
    val teamAvgSla = if (engineerKpiList.any { it.troubleshootCount > 0 }) {
        val totalTs = engineerKpiList.sumOf { it.troubleshootCount }
        val totalInSla = engineerKpiList.sumOf { it.inSlaCount }
        if (totalTs > 0) (totalInSla.toDouble() * 100.0) / totalTs else 100.0
    } else {
        100.0
    }

    val selectedEngineerData = engineerKpiList.find { it.name == selectedEngineerName }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // --- 1. HERO HEADER WITH KPI SUMMARY ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF0F172A),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)))),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "KPI Kinerja Engineer",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Matriks Produktivitas & Kepatuhan SLA Personel Lapangan",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1E293B)
                    ) {
                        Text(
                            text = "${allEngineers.size} Teknisi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4 Metric Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiStatBadge(
                        title = "Aktif Menangani",
                        value = "$activeEngineersCount Personel",
                        icon = Icons.Default.Engineering,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatBadge(
                        title = "Rata-rata SLA",
                        value = "${String.format("%.1f", teamAvgSla)}%",
                        icon = Icons.Default.Timer,
                        color = if (teamAvgSla >= 85.0) Color(0xFF4ADE80) else Color(0xFFF87171),
                        modifier = Modifier.weight(1f)
                    )
                    KpiStatBadge(
                        title = "Top Resolver",
                        value = topPerformer?.name?.split(" ")?.firstOrNull() ?: "-",
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFFBBF24),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 2. FILTER & SEARCH CONTROLS ---
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Search Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari nama atau email teknisi...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = VibrantPrimary,
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                )

                // Filter Row: Module Type & Month
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modul:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )

                    FilterChip(
                        selected = selectedTypeFilter == null,
                        onClick = { selectedTypeFilter = null },
                        label = { Text("Semua Modul", fontSize = 11.sp) },
                        shape = RoundedCornerShape(10.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = VibrantPrimaryContainer,
                            selectedLabelColor = VibrantOnPrimaryContainer
                        )
                    )

                    FosisType.values().forEach { type ->
                        FilterChip(
                            selected = selectedTypeFilter == type,
                            onClick = { selectedTypeFilter = if (selectedTypeFilter == type) null else type },
                            label = { Text(type.label, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = VibrantPrimaryContainer,
                                selectedLabelColor = VibrantOnPrimaryContainer
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bulan:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )

                    val months = listOf("ALL" to "Semua", "JANUARY" to "Jan", "FEBRUARY" to "Feb", "MARCH" to "Mar")
                    months.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedMonthFilter == key,
                            onClick = { selectedMonthFilter = key },
                            label = { Text(label, fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEFF6FF),
                                selectedLabelColor = Color(0xFF1D4ED8)
                            )
                        )
                    }
                }
            }
        }

        // --- 3. CHART VISUALIZATION SECTION ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Chart Switcher Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = when (chartViewMode) {
                                "VOLUME" -> "Distribusi Volume Tiket per Engineer"
                                "SLA" -> "Rasio Kepatuhan SLA per Engineer (%)"
                                else -> "Spesialisasi Infrastruktur FO"
                            },
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground
                        )
                        Text(
                            text = "Klik baris engineer untuk melihat rincian riwayat pekerjaan",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    // Mode Toggle Buttons
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(2.dp)
                    ) {
                        ChartToggleOption("VOLUME", "Volume", chartViewMode == "VOLUME") { chartViewMode = "VOLUME" }
                        ChartToggleOption("SLA", "SLA %", chartViewMode == "SLA") { chartViewMode = "SLA" }
                        ChartToggleOption("INFRA", "Aset FO", chartViewMode == "INFRA") { chartViewMode = "INFRA" }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chart Canvas / Bar Chart
                when (chartViewMode) {
                    "VOLUME" -> {
                        EngineerVolumeBarChart(
                            engineers = displayedEngineers.take(8),
                            maxTickets = displayedEngineers.maxOfOrNull { it.totalTickets }?.coerceAtLeast(1) ?: 1,
                            selectedEngineer = selectedEngineerName,
                            onSelectEngineer = {
                                selectedEngineerName = if (selectedEngineerName == it) null else it
                            }
                        )
                    }
                    "SLA" -> {
                        EngineerSlaBarChart(
                            engineers = displayedEngineers.filter { it.troubleshootCount > 0 }.take(8),
                            selectedEngineer = selectedEngineerName,
                            onSelectEngineer = {
                                selectedEngineerName = if (selectedEngineerName == it) null else it
                            }
                        )
                    }
                    "INFRA" -> {
                        EngineerInfraDistributionChart(
                            engineers = displayedEngineers,
                            selectedEngineer = selectedEngineerData
                        )
                    }
                }
            }
        }

        // --- 4. SELECTED ENGINEER DETAIL DRILLDOWN (IF SELECTED) ---
        AnimatedVisibility(visible = selectedEngineerData != null) {
            selectedEngineerData?.let { engineer ->
                val engineerTickets = remember(engineer.name, filteredItems) {
                    filteredItems.filter { it.engineerName.equals(engineer.name, ignoreCase = true) }
                }

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF8FAFC),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, VibrantPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Header with close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(VibrantPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = engineer.initials,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = engineer.name,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VibrantOnBackground
                                    )
                                    Text(
                                        text = "${engineer.email} • ${engineer.region}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            IconButton(onClick = { selectedEngineerName = null }) {
                                Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF64748B))
                            }
                        }

                        Divider(color = Color(0xFFE2E8F0))

                        // Stats Grid for this Engineer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            EngineerDetailMiniStat(
                                label = "Total Tiket",
                                value = "${engineer.totalTickets}",
                                subtitle = "TS: ${engineer.troubleshootCount} | Mnt: ${engineer.maintenanceCount}",
                                color = VibrantPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            EngineerDetailMiniStat(
                                label = "SLA Compliance",
                                value = "${String.format("%.1f", engineer.slaRatePercent)}%",
                                subtitle = "${engineer.inSlaCount} In SLA / ${engineer.troubleshootCount}",
                                color = if (engineer.slaRatePercent >= 85) Color(0xFF16A34A) else Color(0xFFDC2626),
                                modifier = Modifier.weight(1f)
                            )
                            EngineerDetailMiniStat(
                                label = "Disahkan DeptHead",
                                value = "${engineer.approvedCount}",
                                subtitle = "${engineer.completedCount} Selesai",
                                color = Color(0xFF0284C7),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Recent tickets list for this engineer
                        Text(
                            text = "Tiket Ditangani (${engineerTickets.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground
                        )

                        if (engineerTickets.isEmpty()) {
                            Text(
                                text = "Belum ada tiket yang terhubung ke personel ini pada filter aktif.",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                engineerTickets.take(4).forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.White,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { onSelectItem(item) }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text(
                                                    text = item.getInfraEmoji(),
                                                    fontSize = 16.sp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "${item.ticketNo} - ${item.title}",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${item.type.label} • ${item.bulan} ${item.tahun} • ${item.siteLocationName}",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF64748B)
                                                    )
                                                }
                                            }

                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (item.slaStatus == "IN SLA") Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                                            ) {
                                                Text(
                                                    text = item.slaStatus,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (item.slaStatus == "IN SLA") Color(0xFF166534) else Color(0xFF991B1B),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                )
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

        // --- 5. ENGINEER LEADERBOARD & DIRECTORY TABLE ---
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Tabel Peringkat & Kinerja Tim (${displayedEngineers.size})",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantOnBackground
                    )

                    Text(
                        text = "Urut: Total Tiket",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = VibrantPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(24.dp))
                    Text("Nama Engineer", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.weight(1.5f))
                    Text("Total", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                    Text("SLA %", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                    Text("Status", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF64748B), modifier = Modifier.width(70.dp), textAlign = TextAlign.End)
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Table Rows
                displayedEngineers.forEachIndexed { index, engineer ->
                    val isSelected = engineer.name == selectedEngineerName
                    val rankColor = when (index) {
                        0 -> Color(0xFFEAB308) // Gold
                        1 -> Color(0xFF94A3B8) // Silver
                        2 -> Color(0xFFB45309) // Bronze
                        else -> Color(0xFF64748B)
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) VibrantPrimaryContainer.copy(alpha = 0.4f) else Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selectedEngineerName = if (isSelected) null else engineer.name
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Number with icon for top 3
                            Box(modifier = Modifier.width(24.dp)) {
                                if (index < 3 && engineer.totalTickets > 0) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = rankColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Text(
                                        text = "${index + 1}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = rankColor
                                    )
                                }
                            }

                            // Name and Region
                            Column(modifier = Modifier.weight(1.5f)) {
                                Text(
                                    text = engineer.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantOnBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "TS: ${engineer.troubleshootCount} • MNT: ${engineer.maintenanceCount} • ADM: ${engineer.adminCount}",
                                    fontSize = 9.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }

                            // Total Tickets
                            Text(
                                text = "${engineer.totalTickets}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantOnBackground,
                                modifier = Modifier.width(42.dp),
                                textAlign = TextAlign.Center
                            )

                            // SLA %
                            Text(
                                text = "${String.format("%.0f", engineer.slaRatePercent)}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (engineer.slaRatePercent >= 85) Color(0xFF16A34A) else Color(0xFFDC2626),
                                modifier = Modifier.width(55.dp),
                                textAlign = TextAlign.Center
                            )

                            // Badge
                            Box(modifier = Modifier.width(70.dp), contentAlignment = Alignment.CenterEnd) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (engineer.totalTickets > 0) Color(0xFFEFF6FF) else Color(0xFFF1F5F9)
                                ) {
                                    Text(
                                        text = if (engineer.totalTickets > 0) "Aktif" else "Standby",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (engineer.totalTickets > 0) Color(0xFF1D4ED8) else Color(0xFF94A3B8),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (index < displayedEngineers.size - 1) {
                        Divider(color = Color(0xFFF1F5F9), thickness = 0.8.dp)
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------------------
// HELPER COMPOSABLES & CHARTS
// ----------------------------------------------------------------------------

@Composable
fun KpiStatBadge(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = title,
                fontSize = 9.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
        }
    }
}

@Composable
fun ChartToggleOption(
    id: String,
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) VibrantPrimary else Color.Transparent)
            .clickable { onSelect() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF64748B)
        )
    }
}

@Composable
fun EngineerVolumeBarChart(
    engineers: List<EngineerKpiData>,
    maxTickets: Int,
    selectedEngineer: String?,
    onSelectEngineer: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        engineers.forEach { eng ->
            val isSelected = eng.name == selectedEngineer
            val totalFraction = (eng.totalTickets.toFloat() / maxTickets.toFloat()).coerceIn(0.05f, 1f)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                    .clickable { onSelectEngineer(eng.name) }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = eng.name,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) VibrantPrimary else VibrantOnBackground
                    )
                    Text(
                        text = "${eng.totalTickets} Tiket (TS: ${eng.troubleshootCount}, Mnt: ${eng.maintenanceCount}, Adm: ${eng.adminCount})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stacked Bar Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(totalFraction)
                            .fillMaxHeight()
                    ) {
                        // Troubleshoot Segment
                        if (eng.troubleshootCount > 0 && eng.totalTickets > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(eng.troubleshootCount.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(0xFF6366F1))
                            )
                        }
                        // Maintenance Segment
                        if (eng.maintenanceCount > 0 && eng.totalTickets > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(eng.maintenanceCount.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(0xFF0EA5E9))
                            )
                        }
                        // Admin Segment
                        if (eng.adminCount > 0 && eng.totalTickets > 0) {
                            Box(
                                modifier = Modifier
                                    .weight(eng.adminCount.toFloat())
                                    .fillMaxHeight()
                                    .background(Color(0xFF10B981))
                            )
                        }
                    }
                }
            }
        }

        // Legend
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            LegendDot(color = Color(0xFF6366F1), label = "Troubleshoot")
            LegendDot(color = Color(0xFF0EA5E9), label = "Maintenance")
            LegendDot(color = Color(0xFF10B981), label = "Administrasi")
        }
    }
}

@Composable
fun EngineerSlaBarChart(
    engineers: List<EngineerKpiData>,
    selectedEngineer: String?,
    onSelectEngineer: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (engineers.isEmpty()) {
            Text(
                text = "Belum ada data penanganan Troubleshoot untuk perhitungan SLA.",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
            )
        } else {
            engineers.forEach { eng ->
                val isSelected = eng.name == selectedEngineer
                val inSlaFraction = (eng.inSlaCount.toFloat() / eng.troubleshootCount.toFloat().coerceAtLeast(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFFEFF6FF) else Color.Transparent)
                        .clickable { onSelectEngineer(eng.name) }
                        .padding(vertical = 4.dp, horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = eng.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = VibrantOnBackground
                        )
                        Text(
                            text = "${String.format("%.1f", eng.slaRatePercent)}% (${eng.inSlaCount}/${eng.troubleshootCount} In SLA)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (eng.slaRatePercent >= 85) Color(0xFF16A34A) else Color(0xFFDC2626)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Progress Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEE2E2)) // Red background for Over SLA
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(inSlaFraction)
                                .fillMaxHeight()
                                .background(Color(0xFF22C55E)) // Green for In SLA
                        )
                    }
                }
            }

            // Legend
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                LegendDot(color = Color(0xFF22C55E), label = "In SLA (Tepat Waktu)")
                LegendDot(color = Color(0xFFEF4444), label = "Over SLA (Terlambat)")
            }
        }
    }
}

@Composable
fun EngineerInfraDistributionChart(
    engineers: List<EngineerKpiData>,
    selectedEngineer: EngineerKpiData?
) {
    // Total infra breakdown
    val infraMap = if (selectedEngineer != null) {
        selectedEngineer.infraBreakdown
    } else {
        val aggregated = mutableMapOf<String, Int>()
        engineers.forEach { eng ->
            eng.infraBreakdown.forEach { (k, v) ->
                aggregated[k] = (aggregated[k] ?: 0) + v
            }
        }
        aggregated
    }

    val totalInfra = infraMap.values.sum().coerceAtLeast(1)

    val infraColors = mapOf(
        "Kabel" to Color(0xFF2563EB),
        "Closure" to Color(0xFF7C3AED),
        "Tiang" to Color(0xFFD97706),
        "Handhole" to Color(0xFF059669),
        "Mainhole" to Color(0xFFDC2626)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (selectedEngineer != null) "Aset Dikerjakan oleh ${selectedEngineer.name}" else "Total Persebaran Infrastruktur Tim FO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF334155)
        )

        // Donut / Ring Chart + Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(110.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val radius = (size.minDimension - strokeWidth) / 2
                    val center = Offset(size.width / 2, size.height / 2)

                    drawCircle(
                        color = Color(0xFFF1F5F9),
                        radius = radius,
                        center = center,
                        style = Stroke(width = strokeWidth)
                    )

                    var currentAngle = -90f
                    infraMap.forEach { (infra, count) ->
                        val sweep = (count.toFloat() / totalInfra.toFloat()) * 360f
                        val color = infraColors[infra] ?: Color(0xFF64748B)
                        if (sweep > 0f) {
                            drawArc(
                                color = color,
                                startAngle = currentAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            currentAngle += sweep
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$totalInfra",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantOnBackground
                    )
                    Text(
                        text = "Aset FO",
                        fontSize = 9.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Legend Breakdown
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                infraMap.forEach { (infra, count) ->
                    val percent = (count.toDouble() * 100.0) / totalInfra.toDouble()
                    val color = infraColors[infra] ?: Color(0xFF64748B)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(infra, fontSize = 11.sp, color = Color(0xFF334155))
                        }
                        Text(
                            text = "${String.format("%.1f", percent)}% ($count)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 10.sp, color = Color(0xFF64748B))
    }
}

@Composable
fun EngineerDetailMiniStat(
    label: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color)
            Text(label, fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF334155))
            Text(subtitle, fontSize = 8.sp, color = Color(0xFF94A3B8), maxLines = 1)
        }
    }
}

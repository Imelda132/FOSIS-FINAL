package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FosisConstants
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType

/**
 * ============================================================================
 * SCREEN: AdminScreen
 * TUJUAN: Menampilkan rekap administrasi operasional (Image 3) yang mendukung 2 mode tampilan:
 * 1. SPREADSHEET_TABLE : Tampilan tabel matriks spreadsheet (seperti di Excel asli).
 * 2. CARD_LIST          : Tampilan kartu daftar tiket CC, permit, dan report material FAM.
 * ============================================================================
 */
enum class AdminViewMode(val label: String) {
    SPREADSHEET_TABLE("Tabel Matriks (Excel)"),
    CARD_LIST("Daftar Tiket & Berkas")
}

data class MasterAdminRow(
    val tanggal: Int,
    val bulan: String,
    val tahun: String,
    val task: String,
    val subTask: String,
    val subTask1: String,
    val via: String,
    val status: String,
    val kategori: String,
    val acttivity: String
)

val MASTER_ADMIN_SPREADSHEET_DATA: List<MasterAdminRow> = listOf(
    MasterAdminRow(1, "JANUARY", "2025", "REPORT", "FINANCE", "FI TR", "WA", "DONE", "REQUEST INTERNAL", "SERVICE OPERATION"),
    MasterAdminRow(2, "FEBRUARY", "2026", "PERMIT", "VENDOR", "FI CA", "TEAMS", "CONTINUE", "REQUEST EXTERNAL", "SERVICE MAINTENACE"),
    MasterAdminRow(3, "MARCH", "2027", "REKONSILIASI", "FAM", "FI PC", "EMAIL", "ON HOLD", "REFUNDS", "VENDOR"),
    MasterAdminRow(4, "APRIL", "", "TICKET", "PROCUREMENT", "VE MANAGED SERVICE", "PORTAL", "CANCEL", "", "REG. SURABAYA"),
    MasterAdminRow(5, "MAY", "", "", "EXTERNAL", "VE MATERIAL", "VISIT", "", "", "REG. BANDUNG"),
    MasterAdminRow(6, "JUNE", "", "", "CUSTOMER CARE", "VE STOCK OPNAME", "HARDCOPY", "", "", "ADMIN"),
    MasterAdminRow(7, "JULY", "", "", "CLS", "PROC BAK", "SOFTCOPY", "", "", ""),
    MasterAdminRow(8, "AUGUST", "", "", "NOC", "VE BAST", "", "", "", ""),
    MasterAdminRow(9, "SEPTEMBER", "", "", "SERDEL", "VE BAK", "", "", "", ""),
    MasterAdminRow(10, "OCTOBER", "", "", "FIELD ENGINEER", "MATERIAL", "", "", "", ""),
    MasterAdminRow(11, "NOVEMBER", "", "", "MONTHLY", "SURAT IJIN", "", "", "", ""),
    MasterAdminRow(12, "DECEMBER", "", "", "YEARLY", "OPEN TICKET", "", "", "", ""),
    MasterAdminRow(13, "", "", "", "MIDDLE YEAR", "CLOSED TICKET", "", "", "", ""),
    MasterAdminRow(14, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(15, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(16, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(17, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(18, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(19, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(20, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(21, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(22, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(23, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(24, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(25, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(26, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(27, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(28, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(29, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(30, "", "", "", "", "", "", "", "", ""),
    MasterAdminRow(31, "", "", "", "", "", "", "", "", "")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    items: List<FosisItemEntity>,
    onSelectItem: (FosisItemEntity) -> Unit,
    onAddNew: () -> Unit
) {
    var viewMode by remember { mutableStateOf(AdminViewMode.SPREADSHEET_TABLE) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedTask by remember { mutableStateOf("ALL") }
    var selectedStatus by remember { mutableStateOf("ALL") }
    var selectedActivity by remember { mutableStateOf("ALL") }

    val adminItems = items.filter { it.type == FosisType.ADMINISTRASI }

    val filteredItems = adminItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.ticketNo.contains(searchQuery, ignoreCase = true) ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.adminTask.contains(searchQuery, ignoreCase = true) ||
                item.subTask.contains(searchQuery, ignoreCase = true) ||
                item.subTask1.contains(searchQuery, ignoreCase = true) ||
                item.adminActivity.contains(searchQuery, ignoreCase = true) ||
                item.viaChannel.contains(searchQuery, ignoreCase = true)
        val matchesTask = selectedTask == "ALL" || item.adminTask.equals(selectedTask, ignoreCase = true)
        val matchesStatus = selectedStatus == "ALL" || item.adminStatus.equals(selectedStatus, ignoreCase = true)
        val matchesActivity = selectedActivity == "ALL" || item.adminActivity.equals(selectedActivity, ignoreCase = true)
        matchesSearch && matchesTask && matchesStatus && matchesActivity
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddNew,
                containerColor = Color(0xFFD97706),
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Tambah Administrasi", fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 14.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Brand Header Banner
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFFFBEB),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFD97706),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Description, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Data Administrasi & Logistik FOSIS",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "Tabel Acuan Master Excel (ADMIN.png) & Berkas Tiket",
                                fontSize = 11.sp,
                                color = Color(0xFF92400E)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "${adminItems.size} Data Aktif",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // View Mode Switcher
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                AdminViewMode.entries.forEachIndexed { index, mode ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = AdminViewMode.entries.size),
                        onClick = { viewMode = mode },
                        selected = viewMode == mode,
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = Color(0xFFD97706),
                            activeContentColor = Color.White,
                            inactiveContainerColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (mode == AdminViewMode.SPREADSHEET_TABLE) Icons.Default.TableChart else Icons.Default.ViewList,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(mode.label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Cari task, sub task, BAST, vendor, activity...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFFD97706)) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color(0xFF64748B))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = Color(0xFFD97706),
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedPlaceholderColor = Color(0xFF64748B),
                    unfocusedPlaceholderColor = Color(0xFF94A3B8)
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips (DONE, CONTINUE, ON HOLD, CANCEL as in ADMIN.png)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == "ALL",
                    onClick = { selectedStatus = "ALL" },
                    label = { Text("Semua Status", fontSize = 11.sp) }
                )
                listOf("DONE", "CONTINUE", "ON HOLD", "CANCEL").forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text(status, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Task Filter Chips (REPORT, PERMIT, REKONSILIASI, TICKET as in ADMIN.png)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedTask == "ALL",
                    onClick = { selectedTask = "ALL" },
                    label = { Text("Semua Task", fontSize = 11.sp) }
                )
                FosisConstants.TASK_ADMIN.forEach { task ->
                    FilterChip(
                        selected = selectedTask == task,
                        onClick = { selectedTask = task },
                        label = { Text(task, fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (viewMode) {
                AdminViewMode.SPREADSHEET_TABLE -> {
                    AdminSpreadsheetView(
                        onAddNew = onAddNew
                    )
                }

                AdminViewMode.CARD_LIST -> {
                    if (filteredItems.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FolderOpen, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tidak ada berkas administrasi yang cocok dengan filter.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 90.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(filteredItems) { item ->
                                AdminCardItem(item = item, onClick = { onSelectItem(item) })
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Visual Spreadsheet Matrix Table matching ADMIN.png exactly
 */
@Composable
fun AdminSpreadsheetView(
    onAddNew: () -> Unit
) {
    val horizontalScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(horizontalScroll)
            ) {
                // Table Header row (Pink / Magenta from ADMIN.png)
                Row(
                    modifier = Modifier
                        .background(Color(0xFFFBCFE8))
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TableCellHeader("Tanggal", 65.dp)
                    TableCellHeader("Bulan", 110.dp)
                    TableCellHeader("Tahun", 75.dp)
                    TableCellHeader("TASK", 120.dp)
                    TableCellHeader("SUB TASK", 135.dp)
                    TableCellHeader("SUB TASK 1", 175.dp)
                    TableCellHeader("Via", 90.dp)
                    TableCellHeader("Status", 105.dp)
                    TableCellHeader("Kategori", 160.dp)
                    TableCellHeader("Acttivity", 170.dp)
                }

                Divider(color = Color(0xFFF472B6), thickness = 2.dp)

                // Table Data Rows
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(MASTER_ADMIN_SPREADSHEET_DATA) { row ->
                        val isEven = row.tanggal % 2 == 0
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(if (isEven) Color(0xFFFAFAFA) else Color.White)
                                .border(0.5.dp, Color(0xFFE2E8F0))
                                .padding(vertical = 6.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Tanggal
                            TableCellText(text = row.tanggal.toString(), width = 65.dp, isBold = true, align = TextAlign.Center)

                            // Bulan with specialized pastel chips matching ADMIN.png
                            Box(modifier = Modifier.width(110.dp), contentAlignment = Alignment.CenterStart) {
                                if (row.bulan.isNotEmpty()) {
                                    val (chipBg, chipTextColor) = getMonthColors(row.bulan)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = chipBg,
                                        border = if (row.bulan == "APRIL") androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF059669)) else null
                                    ) {
                                        Text(
                                            text = row.bulan,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = chipTextColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Tahun
                            TableCellText(text = row.tahun, width = 75.dp, align = TextAlign.Center)

                            // TASK with vibrant badges
                            Box(modifier = Modifier.width(120.dp), contentAlignment = Alignment.CenterStart) {
                                if (row.task.isNotEmpty()) {
                                    val (taskBg, taskTextColor) = getTaskColors(row.task)
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = taskBg
                                    ) {
                                        Text(
                                            text = row.task,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = taskTextColor,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // SUB TASK
                            TableCellText(text = row.subTask, width = 135.dp)

                            // SUB TASK 1
                            TableCellText(text = row.subTask1, width = 175.dp, isBold = row.subTask1.isNotEmpty())

                            // Via
                            Box(modifier = Modifier.width(90.dp), contentAlignment = Alignment.CenterStart) {
                                if (row.via.isNotEmpty()) {
                                    Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
                                        Text(
                                            text = row.via,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF334155),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Status with colored chips
                            Box(modifier = Modifier.width(105.dp), contentAlignment = Alignment.CenterStart) {
                                if (row.status.isNotEmpty()) {
                                    val (statusBg, statusTextColor) = getStatusColors(row.status)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = statusBg
                                    ) {
                                        Text(
                                            text = row.status,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = statusTextColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }

                            // Kategori
                            TableCellText(text = row.kategori, width = 160.dp)

                            // Acttivity
                            TableCellText(text = row.acttivity, width = 170.dp, isBold = true)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TableCellHeader(title: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = title,
        fontWeight = FontWeight.Black,
        fontSize = 11.sp,
        color = Color(0xFF831843),
        textAlign = TextAlign.Center,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun TableCellText(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    isBold: Boolean = false,
    align: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        color = if (text.isNotEmpty()) Color(0xFF1E293B) else Color.Transparent,
        textAlign = align,
        modifier = Modifier
            .width(width)
            .padding(horizontal = 4.dp)
    )
}

private fun getMonthColors(month: String): Pair<Color, Color> {
    return when (month) {
        "JANUARY" -> Color(0xFF94A3B8) to Color(0xFF0F172A)
        "FEBRUARY" -> Color(0xFFCBD5E1) to Color(0xFF1E293B)
        "MARCH" -> Color(0xFFBAE6FD) to Color(0xFF0369A1)
        "APRIL" -> Color(0xFFFBCFE8) to Color(0xFF9D174D)
        "MAY" -> Color(0xFFFFE4E6) to Color(0xFF9F1239)
        "JUNE" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "JULY" -> Color(0xFFE0F2FE) to Color(0xFF0284C7)
        "AUGUST" -> Color(0xFFFCE7F3) to Color(0xFFBE185D)
        "SEPTEMBER" -> Color(0xFFFEF9C3) to Color(0xFF854D0E)
        "OCTOBER" -> Color(0xFF86EFAC) to Color(0xFF14532D)
        "NOVEMBER" -> Color(0xFF38BDF8) to Color(0xFF0C4A6E)
        "DECEMBER" -> Color(0xFFFDBA74) to Color(0xFF9A3412)
        else -> Color(0xFFF1F5F9) to Color(0xFF334155)
    }
}

private fun getTaskColors(task: String): Pair<Color, Color> {
    return when (task) {
        "REPORT" -> Color(0xFF22C55E) to Color.White
        "PERMIT" -> Color(0xFFEAB308) to Color(0xFF422006)
        "REKONSILIASI" -> Color(0xFFF97316) to Color.White
        "TICKET" -> Color(0xFFEA580C) to Color.White
        else -> Color(0xFFD97706) to Color.White
    }
}

private fun getStatusColors(status: String): Pair<Color, Color> {
    return when (status) {
        "DONE" -> Color(0xFFDCFCE7) to Color(0xFF15803D)
        "CONTINUE" -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
        "ON HOLD" -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "CANCEL" -> Color(0xFFFFE4E6) to Color(0xFFBE123C)
        else -> Color(0xFFF1F5F9) to Color(0xFF475569)
    }
}

@Composable
private fun AdminCardItem(
    item: FosisItemEntity,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFEF3C7)
                    ) {
                        Text(
                            text = "ADMINISTRASI",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF92400E),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.ticketNo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val (statusBg, statusTextColor) = getStatusColors(item.adminStatus)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusBg
                ) {
                    Text(
                        text = item.adminStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusTextColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Metadata: Tanggal, Task, Sub Task 1, Via
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFF1F5F9)) {
                    Text(
                        text = "Tgl: ${item.tanggal} ${item.bulan.take(3)} ${item.tahun}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                val (taskBg, taskTextColor) = getTaskColors(item.adminTask)
                Surface(shape = RoundedCornerShape(4.dp), color = taskBg) {
                    Text(
                        text = item.adminTask,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = taskTextColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFEFF6FF)) {
                    Text(
                        text = item.subTask1.ifEmpty { item.subTask },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D4ED8),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }

                Surface(shape = RoundedCornerShape(4.dp), color = Color(0xFFECFDF5)) {
                    Text(
                        text = "Via: ${item.viaChannel}",
                        fontSize = 10.sp,
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Activity: ${item.adminActivity}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Staf: ${item.engineerName}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

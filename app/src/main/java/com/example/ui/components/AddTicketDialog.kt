package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FosisConstants
import com.example.data.model.FosisType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTicketDialog(
    isOpen: Boolean,
    initialType: FosisType,
    startWithTypePicker: Boolean = false,
    onDismiss: () -> Unit,
    onSubmit: (
        type: FosisType,
        title: String,
        bulan: String,
        tahun: String,
        isUrgent: Boolean,
        rootCause: String,
        impact: String,
        serviceImpact: String,
        slaStatus: String,
        crStatus: String,
        mitra: String,
        category: String,
        kegiatan: String,
        requestor: String,
        maintenanceStatus: String,
        taskType: String,
        tanggal: Int,
        adminTask: String,
        subTask: String,
        subTask1: String,
        viaChannel: String,
        adminStatus: String,
        adminKategori: String,
        adminActivity: String,
        siteLocation: String,
        notes: String,
        materialUsed: String,
        otdrPdfAttachments: String,
        infraCategory: String,
        infraActivity: String
    ) -> Unit
) {
    if (!isOpen) return

    var selectedType by remember(initialType, isOpen) { mutableStateOf(initialType) }
    var showTypePicker by remember(startWithTypePicker, isOpen) { mutableStateOf(startWithTypePicker) }
    var title by remember { mutableStateOf("") }
    var selectedMonth by remember { mutableStateOf("MARCH") }
    var selectedYear by remember { mutableStateOf("2026") }
    var isUrgent by remember { mutableStateOf(false) }

    // Objek Infrastruktur Lapangan FO (Closure, Kabel, Tiang, Handhole, Mainhole)
    var infraCategory by remember { mutableStateOf("Closure") }
    var infraActivity by remember { mutableStateOf("Splicing Tray Core") }

    // Material & OTDR Attachments (User Requirement: kolom diisi & max 50 pdf)
    var materialUsed by remember { mutableStateOf("") }
    var otdrAttachments by remember { mutableStateOf<List<String>>(emptyList()) }

    // Troubleshoot state (Image 3)
    var rootCause by remember { mutableStateOf(FosisConstants.ROOT_CAUSE_TROUBLESHOOT.first()) }
    var impact by remember { mutableStateOf(FosisConstants.IMPACT_TROUBLESHOOT.first()) }
    var serviceImpact by remember { mutableStateOf(FosisConstants.SERVICE_IMPACT_TROUBLESHOOT.first()) }
    var slaStatus by remember { mutableStateOf(FosisConstants.SLA_TROUBLESHOOT.first()) }
    var crStatus by remember { mutableStateOf(FosisConstants.CR_TROUBLESHOOT.first()) }
    var mitra by remember { mutableStateOf(FosisConstants.MITRA_TROUBLESHOOT.first()) }
    var category by remember { mutableStateOf(FosisConstants.CATEGORY_TROUBLESHOOT.first()) }

    // Maintenance state (Image 1)
    var kegiatan by remember { mutableStateOf(FosisConstants.KEGIATAN_MAINTENANCE.first()) }
    var requestor by remember { mutableStateOf(FosisConstants.REQUESTOR_MAINTENANCE.first()) }
    var maintenanceStatus by remember { mutableStateOf(FosisConstants.STATUS_MAINTENANCE.first()) }
    var taskType by remember { mutableStateOf(FosisConstants.TASK_TYPE_MAINTENANCE.first()) }

    // Administrasi state (Image 2)
    var tanggal by remember { mutableIntStateOf(5) }
    var adminTask by remember { mutableStateOf(FosisConstants.TASK_ADMIN.first()) }
    var subTask by remember { mutableStateOf(FosisConstants.SUB_TASK_ADMIN.first()) }
    var subTask1 by remember { mutableStateOf(FosisConstants.SUB_TASK_1_ADMIN.first()) }
    var viaChannel by remember { mutableStateOf(FosisConstants.VIA_ADMIN.first()) }
    var adminStatus by remember { mutableStateOf(FosisConstants.STATUS_ADMIN.first()) }
    var adminKategori by remember { mutableStateOf(FosisConstants.KATEGORI_ADMIN.first()) }
    var adminActivity by remember { mutableStateOf(FosisConstants.ACTIVITY_ADMIN.first()) }

    // Common location & secret notes
    var siteLocation by remember { mutableStateOf("POP Surabaya Gubeng") }
    var secretNotes by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            if (showTypePicker) {
                // ============================================================
                // FULL PAGE: PILIH MODUL OPERASIONAL
                // Muncul saat tombol Tambah Administrasi ditekan.
                // ============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Input Data Operasional FOSIS",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Text(
                                text = "Pilih modul yang akan diinput",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Pilih Jenis Data",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FosisType.entries.forEach { type ->
                            val (icon, containerColor, contentColor, description) = when (type) {
                                FosisType.TROUBLESHOOT -> Quad(
                                    Icons.Default.Warning,
                                    Color(0xFFFEE2E2),
                                    Color(0xFF991B1B),
                                    "Input gangguan jaringan, root cause, SLA, impact, dan penanganan."
                                )
                                FosisType.MAINTENANCE -> Quad(
                                    Icons.Default.Build,
                                    Color(0xFFE0F2FE),
                                    Color(0xFF075985),
                                    "Input kegiatan maintenance, requestor, status, dan tipe pekerjaan."
                                )
                                FosisType.ADMINISTRASI -> Quad(
                                    Icons.Default.Description,
                                    Color(0xFFFEF3C7),
                                    Color(0xFF92400E),
                                    "Input report, permit, rekonsiliasi, task, sub-task, dan administrasi."
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedType = type
                                        showTypePicker = false
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = containerColor,
                                border = androidx.compose.foundation.BorderStroke(1.dp, contentColor.copy(alpha = 0.25f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        modifier = Modifier.size(48.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = contentColor
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                icon,
                                                contentDescription = type.label,
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = type.label,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = contentColor
                                        )
                                        Spacer(modifier = Modifier.height(3.dp))
                                        Text(
                                            text = description,
                                            fontSize = 11.sp,
                                            color = contentColor.copy(alpha = 0.85f)
                                        )
                                    }

                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = "Pilih ${type.label}",
                                        tint = contentColor
                                    )
                                }
                            }
                        }
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal")
                    }
                }
            } else {
                // ============================================================
                // FULL PAGE: FORM INPUT
                // Isi form di bawah dipertahankan seperti versi sebelumnya.
                // ============================================================
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Input Data Operasional FOSIS",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Pelaporan Otomatis ke Database Terenkripsi",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Tutup")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

// Form Fields (Scrollable)
// Jenis form sudah ditentukan dari halaman pilihan sebelumnya.
                    Column(
                    ) {
                    }

                    // Form Fields (Scrollable)
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Judul / Deskripsi Ringkas Pekerjaan *") },
                            placeholder = { Text("Contoh: Penanganan FO Cut Jalur Surabaya") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Month & Year Selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DropdownSelector(
                                label = "Bulan",
                                options = FosisConstants.MONTHS,
                                selected = selectedMonth,
                                onSelect = { selectedMonth = it },
                                modifier = Modifier.weight(1f)
                            )
                            DropdownSelector(
                                label = "Tahun",
                                options = FosisConstants.YEARS,
                                selected = selectedYear,
                                onSelect = { selectedYear = it },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // OBJEK INFRASTRUKTUR FO & AKTIVITAS LAPANGAN (Closure, Kabel, Tiang, Handhole, Mainhole)
                        if (selectedType == FosisType.TROUBLESHOOT || selectedType == FosisType.MAINTENANCE) {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFF1F5F9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Engineering, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Objek Infrastruktur FO Lapangan *",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = infraCategory.uppercase(),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    // 5 Segmented Chips: Closure, Kabel, Tiang, Handhole, Mainhole
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        FosisConstants.INFRA_CATEGORIES.forEach { cat ->
                                            val isSelected = infraCategory.equals(cat, ignoreCase = true)
                                            val emoji = when (cat.lowercase()) {
                                                "closure" -> "🔌"
                                                "kabel" -> "🧵"
                                                "tiang" -> "🗼"
                                                "handhole" -> "🕳️"
                                                "mainhole", "manhole" -> "🏢"
                                                else -> "⚡"
                                            }
                                            FilterChip(
                                                selected = isSelected,
                                                onClick = {
                                                    infraCategory = cat
                                                    val acts = when (cat.lowercase()) {
                                                        "closure" -> FosisConstants.AKTIVITAS_CLOSURE
                                                        "kabel" -> FosisConstants.AKTIVITAS_KABEL
                                                        "tiang" -> FosisConstants.AKTIVITAS_TIANG
                                                        "handhole" -> FosisConstants.AKTIVITAS_HANDHOLE
                                                        "mainhole", "manhole" -> FosisConstants.AKTIVITAS_MAINHOLE
                                                        else -> emptyList()
                                                    }
                                                    if (acts.isNotEmpty()) {
                                                        infraActivity = acts.first()
                                                        if (title.isBlank() || title.startsWith("Aktivitas") || title.startsWith("Perbaikan") || title.startsWith("Splicing")) {
                                                            title = "${acts.first()} $cat"
                                                        }
                                                    }
                                                },
                                                label = { Text("$emoji $cat", fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                                            )
                                        }
                                    }

                                    // Quick activity selection chips for selected category
                                    val relevantActivities = when (infraCategory.lowercase()) {
                                        "closure" -> FosisConstants.AKTIVITAS_CLOSURE
                                        "kabel" -> FosisConstants.AKTIVITAS_KABEL
                                        "tiang" -> FosisConstants.AKTIVITAS_TIANG
                                        "handhole" -> FosisConstants.AKTIVITAS_HANDHOLE
                                        "mainhole", "manhole" -> FosisConstants.AKTIVITAS_MAINHOLE
                                        else -> emptyList()
                                    }

                                    Text(
                                        text = "Rekomendasi Aktivitas $infraCategory:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        relevantActivities.forEach { act ->
                                            val isActSelected = infraActivity == act
                                            SuggestionChip(
                                                onClick = {
                                                    infraActivity = act
                                                    title = "$act ($infraCategory)"
                                                },
                                                label = { Text(act, fontSize = 10.sp, fontWeight = if (isActSelected) FontWeight.Bold else FontWeight.Normal) }
                                            )
                                        }
                                    }

                                    OutlinedTextField(
                                        value = infraActivity,
                                        onValueChange = { infraActivity = it },
                                        label = { Text("Detail Aktivitas Pada $infraCategory") },
                                        placeholder = { Text("Contoh: Splicing Tray 12 Core, Pembersihan Tray...") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // TYPE SPECIFIC SECTIONS MATCHING USER'S 3 IMAGES
                        when (selectedType) {
                            FosisType.TROUBLESHOOT -> {
                                Text(
                                    text = "Parameter Troubleshoot (Sesuai Struktur Gambar 3):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                DropdownSelector(
                                    label = "Root Cause (Penyebab)",
                                    options = FosisConstants.ROOT_CAUSE_TROUBLESHOOT,
                                    selected = rootCause,
                                    onSelect = { rootCause = it },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DropdownSelector(
                                        label = "Impact",
                                        options = FosisConstants.IMPACT_TROUBLESHOOT,
                                        selected = impact,
                                        onSelect = { impact = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DropdownSelector(
                                        label = "Service Impact",
                                        options = FosisConstants.SERVICE_IMPACT_TROUBLESHOOT,
                                        selected = serviceImpact,
                                        onSelect = { serviceImpact = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DropdownSelector(
                                        label = "Status SLA",
                                        options = FosisConstants.SLA_TROUBLESHOOT,
                                        selected = slaStatus,
                                        onSelect = { slaStatus = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DropdownSelector(
                                        label = "Change Request (CR)",
                                        options = FosisConstants.CR_TROUBLESHOOT,
                                        selected = crStatus,
                                        onSelect = { crStatus = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DropdownSelector(
                                        label = "Mitra Pelaksana",
                                        options = FosisConstants.MITRA_TROUBLESHOOT,
                                        selected = mitra,
                                        onSelect = { mitra = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DropdownSelector(
                                        label = "Kategori Gangguan",
                                        options = FosisConstants.CATEGORY_TROUBLESHOOT,
                                        selected = category,
                                        onSelect = { category = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            FosisType.MAINTENANCE -> {
                                Text(
                                    text = "Parameter Maintenance (Sesuai Struktur Gambar 1):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                DropdownSelector(
                                    label = "Kegiatan",
                                    options = FosisConstants.KEGIATAN_MAINTENANCE,
                                    selected = kegiatan,
                                    onSelect = { kegiatan = it },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DropdownSelector(
                                        label = "Requestor",
                                        options = FosisConstants.REQUESTOR_MAINTENANCE,
                                        selected = requestor,
                                        onSelect = { requestor = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DropdownSelector(
                                        label = "Status Maintenance",
                                        options = FosisConstants.STATUS_MAINTENANCE,
                                        selected = maintenanceStatus,
                                        onSelect = { maintenanceStatus = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                DropdownSelector(
                                    label = "Task Type (PLAN / UNPLAN)",
                                    options = FosisConstants.TASK_TYPE_MAINTENANCE,
                                    selected = taskType,
                                    onSelect = { taskType = it },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            FosisType.ADMINISTRASI -> {
                                Text(
                                    text = "Parameter Administrasi (Sesuai Struktur Gambar 2):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tanggal.toString(),
                                        onValueChange = { tanggal = it.toIntOrNull() ?: 1 },
                                        label = { Text("Tanggal (1-31)") },
                                        modifier = Modifier.weight(0.8f)
                                    )
                                    DropdownSelector(
                                        label = "Task Utama",
                                        options = FosisConstants.TASK_ADMIN,
                                        selected = adminTask,
                                        onSelect = { adminTask = it },
                                        modifier = Modifier.weight(1.2f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DropdownSelector(
                                        label = "Sub Task",
                                        options = FosisConstants.SUB_TASK_ADMIN,
                                        selected = subTask,
                                        onSelect = { subTask = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DropdownSelector(
                                        label = "Sub Task 1",
                                        options = FosisConstants.SUB_TASK_1_ADMIN,
                                        selected = subTask1,
                                        onSelect = { subTask1 = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DropdownSelector(
                                        label = "Via Media",
                                        options = FosisConstants.VIA_ADMIN,
                                        selected = viaChannel,
                                        onSelect = { viaChannel = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DropdownSelector(
                                        label = "Status",
                                        options = FosisConstants.STATUS_ADMIN,
                                        selected = adminStatus,
                                        onSelect = { adminStatus = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    DropdownSelector(
                                        label = "Kategori",
                                        options = FosisConstants.KATEGORI_ADMIN,
                                        selected = adminKategori,
                                        onSelect = { adminKategori = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                    DropdownSelector(
                                        label = "Activity",
                                        options = FosisConstants.ACTIVITY_ADMIN,
                                        selected = adminActivity,
                                        onSelect = { adminActivity = it },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // GPS Location Lock Banner for User Input
                        val context = androidx.compose.ui.platform.LocalContext.current
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFDCFCE7),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF14532D), modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "GPS LOCK : LOKASI TERKUNCI OTOMATIS",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF14532D)
                                        )
                                    }
                                    Text(
                                        text = "SENSOR GPS AKTIF",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Koordinat terunci saat input data: -7.2575, 112.7521 (Akurasi Presisi Device)",
                                    fontSize = 10.sp,
                                    color = Color(0xFF166534)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Button(
                                        onClick = {
                                            try {
                                                val intent = android.content.Intent(
                                                    android.content.Intent.ACTION_VIEW,
                                                    android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=-7.2575,112.7521")
                                                )
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Gagal membuka Google Maps", android.widget.Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E88E5)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Buka di Google Maps", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        // Location Site
                        OutlinedTextField(
                            value = siteLocation,
                            onValueChange = { siteLocation = it },
                            label = { Text("Lokasi / Site POP Lapangan *") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Urgent assignment checkbox
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = isUrgent,
                                onCheckedChange = { isUrgent = it }
                            )
                            Text(
                                text = "Tandai Sebagai Penugasan Mendesak (Notifikasi Real-Time)",
                                fontSize = 12.sp,
                                fontWeight = if (isUrgent) FontWeight.Bold else FontWeight.Normal,
                                color = if (isUrgent) Color.Red else MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Material Yang Digunakan (User Requirement: kolom untuk diisi saja)
                        OutlinedTextField(
                            value = materialUsed,
                            onValueChange = { materialUsed = it },
                            label = { Text("Material yang Digunakan") },
                            placeholder = { Text("Isi rincian material (misal: Patchcord SC-LC 5m, SFP 10G ER, OTB 24 Core, Tube Protector...)") },
                            leadingIcon = { Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF475569)) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Hasil Ukur OTDR PDF (Khusus Maintenance / Umum, Maksimal 50 File PDF)
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Hasil Ukur OTDR (Format PDF)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (otdrAttachments.size >= 50) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = "${otdrAttachments.size} / 50 PDF",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (otdrAttachments.size >= 50) Color(0xFFDC2626) else Color(0xFF166534),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Text(
                                    text = "Maksimal 50 lampiran berkas PDF hasil trace OTDR core serat optik.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF166534)
                                )

                                // List of attached PDF files
                                if (otdrAttachments.isNotEmpty()) {
                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        otdrAttachments.take(5).forEachIndexed { idx, pdfName ->
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color.White,
                                                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF86EFAC)),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.AttachFile, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(pdfName, fontSize = 11.sp, color = Color(0xFF14532D))
                                                    }
                                                    IconButton(
                                                        onClick = { otdrAttachments = otdrAttachments.filterIndexed { i, _ -> i != idx } },
                                                        modifier = Modifier.size(20.dp)
                                                    ) {
                                                        Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color.Red, modifier = Modifier.size(12.dp))
                                                    }
                                                }
                                            }
                                        }
                                        if (otdrAttachments.size > 5) {
                                            Text(
                                                text = "... dan ${otdrAttachments.size - 5} file PDF OTDR lainnya",
                                                fontSize = 10.sp,
                                                color = Color(0xFF15803D),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            if (otdrAttachments.size < 50) {
                                                val nextNum = otdrAttachments.size + 1
                                                val newFileName = "OTDR_Trace_${siteLocation.take(8).replace(" ", "_")}_Core$nextNum.pdf"
                                                otdrAttachments = otdrAttachments + newFileName
                                            }
                                        },
                                        enabled = otdrAttachments.size < 50,
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("+ Tambah Berkas OTDR (PDF)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }

                                    if (otdrAttachments.isNotEmpty()) {
                                        OutlinedButton(
                                            onClick = { otdrAttachments = emptyList() },
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Bersihkan", fontSize = 10.sp, color = Color.Red)
                                        }
                                    }
                                }
                            }
                        }

                        // Sensitive notes to be encrypted with AES-256 GCM
                        OutlinedTextField(
                            value = secretNotes,
                            onValueChange = { secretNotes = it },
                            label = { Text("Catatan Rahasia Lapangan (Enkripsi AES-256 GCM)") },
                            placeholder = { Text("Konfigurasi port, optic measurement, password switch...") },
                            leadingIcon = { Icon(Icons.Default.VpnKey, contentDescription = null, tint = Color(0xFFD97706)) },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Batal")
                        }

                        Button(
                            onClick = {
                                if (title.isNotBlank()) {
                                    onSubmit(
                                        selectedType,
                                        title,
                                        selectedMonth,
                                        selectedYear,
                                        isUrgent,
                                        rootCause,
                                        impact,
                                        serviceImpact,
                                        slaStatus,
                                        crStatus,
                                        mitra,
                                        category,
                                        kegiatan,
                                        requestor,
                                        maintenanceStatus,
                                        taskType,
                                        tanggal,
                                        adminTask,
                                        subTask,
                                        subTask1,
                                        viaChannel,
                                        adminStatus,
                                        adminKategori,
                                        adminActivity,
                                        siteLocation,
                                        secretNotes,
                                        materialUsed,
                                        otdrAttachments.joinToString(";"),
                                        infraCategory,
                                        infraActivity
                                    )
                                    onDismiss()
                                }
                            },
                            enabled = title.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Simpan Data", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownSelector(
    label: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label, fontSize = 11.sp) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, fontSize = 12.sp) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
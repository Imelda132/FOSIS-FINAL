package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PermitEntity
import com.example.data.model.PermitStatus
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*
import com.example.ui.viewmodel.FosisUiState
import com.example.ui.viewmodel.FosisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermitScreen(
    uiState: FosisUiState,
    viewModel: FosisViewModel
) {
    val context = LocalContext.current
    val permits = uiState.permits
    val activeUser = uiState.activeUser
    val isAdmin = activeUser?.role == UserRole.ADMIN

    var showAddDialog by remember { mutableStateOf(false) }
    var showExportMenu by remember { mutableStateOf(false) }
    var selectedPermitForDetail by remember { mutableStateOf<PermitEntity?>(null) }

    val dateFormat = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VibrantBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Banner Header
        item {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = VibrantPrimaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = VibrantPrimary,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.AssignmentTurnedIn,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Form Perijinan Kerja (SIK)",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantOnPrimaryContainer
                                )
                                Text(
                                    text = "Permit to Work & Prosedur Keselamatan K3",
                                    fontSize = 11.sp,
                                    color = VibrantOnPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }

                        // Export Button for ALL TEAMS
                        Box {
                            Button(
                                onClick = { showExportMenu = true },
                                colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                                shape = RoundedCornerShape(14.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Export SIK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showExportMenu,
                                onDismissRequest = { showExportMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Export Excel (.csv)") },
                                    leadingIcon = { Icon(Icons.Default.TableChart, contentDescription = null) },
                                    onClick = {
                                        showExportMenu = false
                                        viewModel.exportData(context, "PERMITS", "EXCEL")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export Dokumen Resmi PDF") },
                                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, contentDescription = null) },
                                    onClick = {
                                        showExportMenu = false
                                        viewModel.exportData(context, "PERMITS", "PDF")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Export Slide Presentasi (PPT)") },
                                    leadingIcon = { Icon(Icons.Default.Slideshow, contentDescription = null) },
                                    onClick = {
                                        showExportMenu = false
                                        viewModel.exportData(context, "PERMITS", "PPT")
                                    }
                                )
                            }
                        }
                    }

                    // Stat summary pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val approvedCount = permits.count { it.status == PermitStatus.DISETUJUI_ADMIN }
                        val pendingCount = permits.count { it.status == PermitStatus.DIAJUKAN }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Total Ijin Kerja", fontSize = 10.sp, color = VibrantOnPrimaryContainer)
                                Text("${permits.size}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = VibrantOnPrimaryContainer)
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Disetujui Admin", fontSize = 10.sp, color = Color(0xFF166534))
                                Text("$approvedCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Menunggu", fontSize = 10.sp, color = Color(0xFF9A3412))
                                Text("$pendingCount", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF9A3412))
                            }
                        }
                    }
                }
            }
        }

        // Action Toolbar: Ajukan Ijin Baru + Import (Hanya dari Admin)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Button 1: Ajukan Ijin Baru (Team / IC / Admin)
                if (activeUser?.role != UserRole.DEPT_HEAD) {
                    Button(
                        onClick = { showAddDialog = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ajukan Form Ijin", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Button 2: IMPORT DATA PERIJINAN (HANYA DARI ADMIN)
                OutlinedButton(
                    onClick = {
                        viewModel.setImportDialogOpen(true)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFEFF6FF),
                        contentColor = Color(0xFF1D4ED8)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color(0xFF93C5FD)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(46.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudUpload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Import Perijinan SIK",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Empty state notice
        if (permits.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AssignmentLate,
                            contentDescription = null,
                            tint = Color(0xFF938F99),
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Daftar Form Perijinan Masih Kosong",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground
                        )
                        Text(
                            text = "Database perijinan saat ini bersih (0 data). Tim lapangan dapat menekan tombol 'Ajukan Form Ijin' atau Admin (Sanderlina Imelda) dapat mengimpor berkas perijinan kerja.",
                            fontSize = 12.sp,
                            color = VibrantOnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        } else {
            items(permits, key = { it.id }) { permit ->
                PermitCardItem(
                    permit = permit,
                    isAdmin = isAdmin,
                    onApprove = { isApproved -> viewModel.approvePermit(permit, isApproved) },
                    onDelete = { viewModel.deletePermit(permit) },
                    onExportPdf = {
                        viewModel.selectPermit(permit)
                        viewModel.exportData(context, "PERMITS", "PDF")
                    }
                )
            }
        }
    }

    // Modal: Ajukan Form Ijin Baru
    if (showAddDialog) {
        AddPermitDialog(
            user = activeUser,
            currentLat = uiState.userLatitude,
            currentLng = uiState.userLongitude,
            onDismiss = { showAddDialog = false },
            onSubmit = { site, workType, start, end, hours, count, names, jsa, notes ->
                viewModel.createPermit(site, workType, start, end, hours, count, names, jsa, notes)
                showAddDialog = false
            }
        )
    }

    // Modal: Import Perijinan
    if (uiState.isImportDialogOpen) {
        ImportPermitsAdminDialog(
            onDismiss = { viewModel.setImportDialogOpen(false) },
            onImport = { batch ->
                viewModel.importPermitsFromAdmin(batch)
            }
        )
    }
}

@Composable
fun PermitCardItem(
    permit: PermitEntity,
    isAdmin: Boolean,
    onApprove: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit
) {
    val statusColor = when (permit.status) {
        PermitStatus.DISETUJUI_ADMIN -> Color(0xFF16A34A)
        PermitStatus.DIAJUKAN -> Color(0xFFEA580C)
        PermitStatus.DITOLAK -> Color(0xFFDC2626)
        PermitStatus.SELESAI -> Color(0xFF2563EB)
        PermitStatus.DRAFT -> Color(0xFF6B7280)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Top row: Permit No & Status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFEADDFF)
                    ) {
                        Text(
                            text = permit.permitNo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF21005D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (permit.importedByAdmin) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEFF6FF)
                        ) {
                            Text(
                                text = "Admin Import",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF1D4ED8),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = permit.status.label,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Location & Work Type
            Text(
                text = permit.siteLocation,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = VibrantOnBackground
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Construction, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Jenis Pekerjaan: ${permit.workType}",
                    fontSize = 12.sp,
                    color = VibrantOnSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Pemohon: ${permit.applicantName} (${permit.companyOrVendor})",
                    fontSize = 12.sp,
                    color = VibrantOnSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.DateRange, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Periode: ${permit.startDate} s/d ${permit.endDate} • ${permit.workHours}",
                    fontSize = 12.sp,
                    color = VibrantOnSurfaceVariant
                )
            }

            // Device GPS tag
            val context = androidx.compose.ui.platform.LocalContext.current
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF3EDF7),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = android.content.Intent(
                                android.content.Intent.ACTION_VIEW,
                                android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${permit.latitude},${permit.longitude}")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            android.widget.Toast.makeText(context, "Gagal membuka Google Maps", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = null, tint = VibrantPrimary, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Device GPS: ${String.format("%.5f", permit.latitude)}, ${String.format("%.5f", permit.longitude)}",
                            fontSize = 11.sp,
                            color = Color(0xFF49454F)
                        )
                    }
                    Text(
                        text = "Buka Maps ↗",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantPrimary
                    )
                }
            }

            // JSA Checklist snippet
            Text(
                text = "K3 / APD: ${permit.jsaChecklist}",
                fontSize = 11.sp,
                color = Color(0xFF64748B),
                maxLines = 1
            )

            // Action Buttons
            Divider(color = Color(0xFFF3EDF7), thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = androidx.compose.ui.platform.LocalContext.current
                
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Export PDF button for everyone
                    OutlinedButton(
                        onClick = onExportPdf,
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF SIK", fontSize = 11.sp)
                    }

                    // WhatsApp Send Notification Button
                    Button(
                        onClick = {
                            com.example.util.WhatsAppHelper.sendWhatsAppMessage(
                                context = context,
                                rawPhone = "081234567890",
                                subject = "[Surat Ijin Kerja / SIK] ${permit.permitNo}",
                                bodyMessage = "Notifikasi Surat Ijin Kerja SIK:\n• No SIK: ${permit.permitNo}\n• Status: ${permit.status.label}\n• Lokasi: ${permit.siteLocation}\n• Pemohon: ${permit.applicantName} (${permit.companyOrVendor})\n• Pekerjaan: ${permit.workType}\n• Periode: ${permit.startDate} s/d ${permit.endDate}"
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim WA (+62)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Admin-specific actions
                if (isAdmin) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (permit.status != PermitStatus.DISETUJUI_ADMIN) {
                            Button(
                                onClick = { onApprove(true) },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text("Setujui", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPermitDialog(
    user: UserEntity?,
    currentLat: Double,
    currentLng: Double,
    onDismiss: () -> Unit,
    onSubmit: (site: String, workType: String, start: String, end: String, hours: String, count: Int, names: String, jsa: String, notes: String) -> Unit
) {
    var siteLocation by remember { mutableStateOf("") }
    var workType by remember { mutableStateOf("Optical Fiber & Splicing") }
    var startDate by remember { mutableStateOf("2026-03-05") }
    var endDate by remember { mutableStateOf("2026-03-07") }
    var workHours by remember { mutableStateOf("08:00 - 17:00 WIB") }
    var workersCount by remember { mutableStateOf("2") }
    var workersNames by remember { mutableStateOf(user?.name ?: "Tim Lapangan") }
    var jsaChecklist by remember { mutableStateOf("Helm K3, Rompi Reflektor, Sepatu Safety, Full Body Harness") }
    var notes by remember { mutableStateOf("") }

    val workTypeOptions = listOf(
        "Optical Fiber & Splicing",
        "Ketinggian (Tower BTS & Rooftop)",
        "Power & Kelistrikan / Genset",
        "Datacenter & Server POP",
        "Ruang Terbatas (Manhole)"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pengajuan Form Ijin Kerja (SIK)", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
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
                                        text = "GPS LOCK: LOKASI PERIJINAN TERKUNCI",
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
                                text = "Koordinat terunci otomatis: ${String.format("%.5f", currentLat)}, ${String.format("%.5f", currentLng)}",
                                fontSize = 10.sp,
                                color = Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = {
                                    try {
                                        val intent = android.content.Intent(
                                            android.content.Intent.ACTION_VIEW,
                                            android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=$currentLat,$currentLng")
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
                item {
                    OutlinedTextField(
                        value = siteLocation,
                        onValueChange = { siteLocation = it },
                        label = { Text("Site Tujuan / Lokasi Kerja", color = Color(0xFF334155)) },
                        placeholder = { Text("misal: POP Gubeng, Tower Darmo", color = Color(0xFF94A3B8)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VibrantPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1),
                            focusedPlaceholderColor = Color(0xFF94A3B8),
                            unfocusedPlaceholderColor = Color(0xFF94A3B8)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Text("Kategori Pekerjaan:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                    workTypeOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { workType = opt }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = workType == opt, onClick = { workType = opt })
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(opt, fontSize = 12.sp, color = Color(0xFF1E293B))
                        }
                    }
                }
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Tgl Mulai", color = Color(0xFF334155)) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = VibrantPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("Tgl Selesai", color = Color(0xFF334155)) },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 12.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                focusedBorderColor = VibrantPrimary,
                                unfocusedBorderColor = Color(0xFFCBD5E1)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = workHours,
                        onValueChange = { workHours = it },
                        label = { Text("Jam Kerja", color = Color(0xFF334155)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VibrantPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = workersNames,
                        onValueChange = { workersNames = it },
                        label = { Text("Nama Personil Tim", color = Color(0xFF334155)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VibrantPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = jsaChecklist,
                        onValueChange = { jsaChecklist = it },
                        label = { Text("Checklist K3 & APD", color = Color(0xFF334155)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VibrantPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Tambahan", color = Color(0xFF334155)) },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF0F172A),
                            unfocusedTextColor = Color(0xFF0F172A),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = VibrantPrimary,
                            unfocusedBorderColor = Color(0xFFCBD5E1)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (siteLocation.isNotBlank()) {
                        onSubmit(
                            siteLocation,
                            workType,
                            startDate,
                            endDate,
                            workHours,
                            workersCount.toIntOrNull() ?: 2,
                            workersNames,
                            jsaChecklist,
                            notes
                        )
                    }
                }
            ) {
                Text("Ajukan SIK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ImportPermitsAdminDialog(
    onDismiss: () -> Unit,
    onImport: (List<PermitEntity>?) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = VibrantPrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Import Data Perijinan (Admin)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sebagai Admin (Sanderlina Imelda), Anda memiliki otorisasi penuh untuk mengimpor batch berkas perijinan kerja resmi ke dalam sistem.",
                    fontSize = 13.sp,
                    color = VibrantOnBackground
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Format Template Impor:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D4ED8)
                        )
                        Text(
                            text = "• Surat Ijin SIK/2026/03/IMP-01 (Vendor GENTRI - Splicing)\n• Surat Ijin SIK/2026/03/IMP-02 (Tim Power Regional - Tower)",
                            fontSize = 11.sp,
                            color = Color(0xFF1E40AF),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onImport(null) },
                colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary)
            ) {
                Text("Jalankan Import Batch")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

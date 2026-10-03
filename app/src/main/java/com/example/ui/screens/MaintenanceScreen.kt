package com.example.ui.screens

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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FosisConstants
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.ui.theme.*

/**
 * ============================================================================
 * SCREEN: MaintenanceScreen
 * TUJUAN: Menampilkan rekap kegiatan pemeliharaan preventif OSP, Mainhole (MH),
 * Handhole (HH), perbaikan core, validasi, dan patroli jalur fiber optik (Image 2).
 *
 * FITUR UTAMA:
 * 1. Search Bar : Cari berdasarkan No Tiket, Nama Mainhole / Lokasi, Kegiatan, atau Requestor.
 * 2. Filter Kegiatan : Maintenance, Validation, Activation Backhaul, Storing, Support, dll.
 * 3. Filter Status : DONE, ON HOLD, ON PROGRESS, CANCEL.
 * 4. Summary Chips : Ringkasan total jadwal PLAN vs UNPLAN.
 * 5. Kartu Jadwal : Menampilkan ID pekerjaan, tanggal request & target eksekusi,
 *    nama pemohon (SM, SERDEL, CLS, NOC), status pengerjaan, dan ikon aset FO.
 * 6. FAB (+) : Tambah form penugasan maintenance baru.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaintenanceScreen(
    items: List<FosisItemEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedKegiatan: String,
    onSelectKegiatan: (String) -> Unit,
    selectedStatus: String,
    onSelectStatus: (String) -> Unit,
    onSelectItem: (FosisItemEntity) -> Unit,
    onAddNew: () -> Unit
) {
    val maintenanceItems = items.filter { it.type == FosisType.MAINTENANCE }

    val filteredItems = maintenanceItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.ticketNo.contains(searchQuery, ignoreCase = true) ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.kegiatan.contains(searchQuery, ignoreCase = true) ||
                item.requestor.contains(searchQuery, ignoreCase = true)
        val matchesKegiatan = selectedKegiatan == "ALL" || item.kegiatan.equals(selectedKegiatan, ignoreCase = true)
        val matchesStatus = selectedStatus == "ALL" || item.maintenanceStatus.equals(selectedStatus, ignoreCase = true)
        matchesSearch && matchesKegiatan && matchesStatus
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNew,
                containerColor = Color(0xFF0284C7),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Maintenance")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Cari kegiatan maintenance, requestor, site...", color = Color(0xFF64748B)) },
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A), fontSize = 13.sp),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = VibrantPrimary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Hapus", tint = Color(0xFF64748B))
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color(0xFF0F172A),
                    unfocusedTextColor = Color(0xFF0F172A),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = VibrantPrimary,
                    unfocusedBorderColor = Color(0xFFCBD5E1),
                    focusedPlaceholderColor = Color(0xFF64748B),
                    unfocusedPlaceholderColor = Color(0xFF94A3B8)
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedStatus == "ALL",
                    onClick = { onSelectStatus("ALL") },
                    label = { Text("Semua Status (${maintenanceItems.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedStatus == "DONE",
                    onClick = { onSelectStatus("DONE") },
                    label = { Text("DONE (${maintenanceItems.count { it.maintenanceStatus == "DONE" }})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedStatus == "ON PROGRESS",
                    onClick = { onSelectStatus("ON PROGRESS") },
                    label = { Text("ON PROGRESS (${maintenanceItems.count { it.maintenanceStatus == "ON PROGRESS" }})", fontSize = 11.sp) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Kegiatan Horizontal Filter Chips (from Image 1)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedKegiatan == "ALL",
                    onClick = { onSelectKegiatan("ALL") },
                    label = { Text("Semua Kegiatan", fontSize = 10.sp) }
                )
                FosisConstants.KEGIATAN_MAINTENANCE.take(8).forEach { keg ->
                    FilterChip(
                        selected = selectedKegiatan == keg,
                        onClick = { onSelectKegiatan(keg) },
                        label = { Text(keg, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada kegiatan maintenance yang cocok.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(filteredItems) { item ->
                        MaintenanceCardItem(item = item, onClick = { onSelectItem(item) })
                    }
                }
            }
        }
    }
}

@Composable
private fun MaintenanceCardItem(
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
                        color = Color(0xFFE0F2FE)
                    ) {
                        Text(
                            text = "MAINTENANCE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0369A1),
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

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (item.maintenanceStatus == "DONE") Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = item.maintenanceStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.maintenanceStatus == "DONE") Color(0xFF15803D) else Color(0xFFB45309),
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

            // Metadata from Image 1: Kegiatan, Requestor, Task Type (PLAN/UNPLAN)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF1F5F9),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFCBD5E1))
                ) {
                    Text(
                        text = "Kegiatan: ${item.kegiatan}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFEFF6FF),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFBFDBFE))
                ) {
                    Text(
                        text = "Req: ${item.requestor}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1D4ED8),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                    )
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFAF5FF),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFE9D5FF))
                ) {
                    Text(
                        text = "Tipe: ${item.taskType}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF7E22CE),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📍 ${item.siteLocationName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Teknisi: ${item.engineerName}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                val context = androidx.compose.ui.platform.LocalContext.current
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFDCFCE7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                    modifier = Modifier.clickable {
                        com.example.util.WhatsAppHelper.sendWhatsAppMessage(
                            context = context,
                            rawPhone = "081234567890",
                            subject = "[Tiket Maintenance] ${item.ticketNo} - ${item.title}",
                            bodyMessage = "Pemberitahuan Maintenance Preventif:\n• Tiket: ${item.ticketNo}\n• Kegiatan: ${item.kegiatan}\n• Status: ${item.maintenanceStatus}\n• Lokasi: ${item.siteLocationName}\n• Req: ${item.requestor}"
                        )
                    }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = null,
                            tint = Color(0xFF16A34A),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Kirim WA (+62)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
    }
}

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
import androidx.compose.runtime.*
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
 * SCREEN: TroubleshootScreen
 * TUJUAN: Menampilkan seluruh rekap tiket penanganan gangguan jaringan FO (Image 1).
 *
 * FITUR UTAMA:
 * 1. Search Bar : Pencarian real-time berdasarkan No Tiket, Nama Pelanggan, Site, atau Root Cause.
 * 2. Filter SLA : Filter cepat (ALL, IN SLA, OVER SLA).
 * 3. Filter Root Cause : Filter akar masalah (CABLE, DEVICE, GOVERNMENT PROJECT, HEAVY VEHICLES, dll.).
 * 4. Summary Chips : Ringkasan jumlah tiket (Total, In SLA, Over SLA).
 * 5. Kartu Tiket : Menampilkan nomor tiket, judul pelanggan, status SLA badge warna kontras tinggi,
 *    dampak teknis (Impact), service impact, mitra kontraktor, dan level workflow approval.
 * 6. FAB (+) : Membuka dialog penambahan tiket gangguan baru.
 * ============================================================================
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TroubleshootScreen(
    items: List<FosisItemEntity>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedSla: String,
    onSelectSla: (String) -> Unit,
    selectedRootCause: String,
    onSelectRootCause: (String) -> Unit,
    onSelectItem: (FosisItemEntity) -> Unit,
    onAddNew: () -> Unit
) {
    val troubleshootItems = items.filter { it.type == FosisType.TROUBLESHOOT }

    val filteredItems = troubleshootItems.filter { item ->
        val matchesSearch = searchQuery.isBlank() ||
                item.ticketNo.contains(searchQuery, ignoreCase = true) ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.siteLocationName.contains(searchQuery, ignoreCase = true) ||
                item.rootCause.contains(searchQuery, ignoreCase = true)
        val matchesSla = selectedSla == "ALL" || item.slaStatus.equals(selectedSla, ignoreCase = true)
        val matchesRootCause = selectedRootCause == "ALL" || item.rootCause.equals(selectedRootCause, ignoreCase = true)
        matchesSearch && matchesSla && matchesRootCause
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNew,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Tiket Troubleshoot")
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
                placeholder = { Text("Cari tiket, site, atau root cause...", color = Color(0xFF64748B)) },
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

            // SLA Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedSla == "ALL",
                    onClick = { onSelectSla("ALL") },
                    label = { Text("Semua SLA (${troubleshootItems.size})", fontSize = 11.sp) }
                )
                FilterChip(
                    selected = selectedSla == "IN SLA",
                    onClick = { onSelectSla("IN SLA") },
                    label = {
                        Text(
                            "IN SLA (${troubleshootItems.count { it.slaStatus == "IN SLA" }})",
                            fontSize = 11.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                )
                FilterChip(
                    selected = selectedSla == "OVER SLA",
                    onClick = { onSelectSla("OVER SLA") },
                    label = {
                        Text(
                            "OVER SLA (${troubleshootItems.count { it.slaStatus == "OVER SLA" }})",
                            fontSize = 11.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Root Cause Horizontal Filter Chips (from Image 3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = selectedRootCause == "ALL",
                    onClick = { onSelectRootCause("ALL") },
                    label = { Text("Semua Root Cause", fontSize = 10.sp) }
                )
                FosisConstants.ROOT_CAUSE_TROUBLESHOOT.take(8).forEach { cause ->
                    FilterChip(
                        selected = selectedRootCause == cause,
                        onClick = { onSelectRootCause(cause) },
                        label = { Text(cause, fontSize = 10.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // List of Troubleshoot Tickets
            if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Tidak ada data tiket troubleshoot yang cocok.",
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
                        TroubleshootCardItem(
                            item = item,
                            onClick = { onSelectItem(item) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TroubleshootCardItem(
    item: FosisItemEntity,
    onClick: () -> Unit
) {
    val isInSla = item.slaStatus == "IN SLA"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Ticket No & SLA Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFEE2E2)
                    ) {
                        Text(
                            text = "TROUBLESHOOT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF991B1B),
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
                    color = if (isInSla) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                ) {
                    Text(
                        text = item.slaStatus,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isInSla) Color(0xFF15803D) else Color(0xFFDC2626),
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

            // Specs Grid from Image 3: Root Cause, Impact, Service Impact, Mitra
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ParamBadge(label = "Cause", value = item.rootCause, bgColor = Color(0xFFE0F2FE), textColor = Color(0xFF0369A1))
                ParamBadge(label = "Impact", value = item.impact, bgColor = Color(0xFFFEF3C7), textColor = Color(0xFFB45309))
                ParamBadge(label = "Service", value = item.serviceImpact, bgColor = Color(0xFFF1F5F9), textColor = Color(0xFF334155))
                ParamBadge(label = "Mitra", value = item.mitra, bgColor = Color(0xFFDCFCE7), textColor = Color(0xFF15803D))
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Engineer & Location & WhatsApp Dispatch
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
                            subject = "[Tiket Troubleshoot] ${item.ticketNo} - ${item.title}",
                            bodyMessage = "Pemberitahuan SIK Troubleshoot:\n• Tiket: ${item.ticketNo}\n• SLA: ${item.slaStatus}\n• Lokasi: ${item.siteLocationName}\n• Root Cause: ${item.rootCause}\n• Teknisi: ${item.engineerName}"
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

@Composable
private fun ParamBadge(label: String, value: String, bgColor: Color, textColor: Color = Color(0xFF0F172A)) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.25f))
    ) {
        Text(
            text = "$label: ${value.take(12)}",
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
        )
    }
}

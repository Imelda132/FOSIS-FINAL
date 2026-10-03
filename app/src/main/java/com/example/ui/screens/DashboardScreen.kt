package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.data.model.AuditLogEntity
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.components.EngineerPerformanceDashboard
import com.example.ui.components.ExecutiveKpiOverview
import com.example.ui.theme.*
import com.example.ui.viewmodel.FosisNavTab
import com.example.ui.viewmodel.FosisUiState
import com.example.ui.viewmodel.FosisViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    uiState: FosisUiState,
    viewModel: FosisViewModel,
    onNavigateTab: (FosisNavTab) -> Unit,
    onOpenAddModal: () -> Unit,
    onSelectItem: (FosisItemEntity) -> Unit
) {
    val context = LocalContext.current
    val items = uiState.items
    val auditLogs = uiState.auditLogs
    val activeUser = uiState.activeUser
    val isDeptHead = activeUser?.role == UserRole.DEPT_HEAD
    val isAdmin = activeUser?.role == UserRole.ADMIN

    var editableDeptHeadComment by remember(uiState.executiveDeptHeadComment) {
        mutableStateOf(uiState.executiveDeptHeadComment)
    }

    val dateFormat = SimpleDateFormat("HH:mm:ss", Locale("id", "ID"))

    // Pulsing animation for Live Team Location
    val infiniteTransition = rememberInfiniteTransition(label = "LivePulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    var dashboardViewTab by remember { mutableStateOf("ENGINEER_KPI") } // "EXECUTIVE" or "ENGINEER_KPI"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(VibrantBackground)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 0: VIEW MODE SWITCHER (Executive Overview vs Engineer Performance KPIs)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Option 1: Executive Overview
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (dashboardViewTab == "EXECUTIVE") VibrantPrimaryContainer else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { dashboardViewTab = "EXECUTIVE" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dashboard,
                                contentDescription = null,
                                tint = if (dashboardViewTab == "EXECUTIVE") VibrantOnPrimaryContainer else VibrantOnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Ringkasan Operasional",
                                fontSize = 12.sp,
                                fontWeight = if (dashboardViewTab == "EXECUTIVE") FontWeight.Bold else FontWeight.Medium,
                                color = if (dashboardViewTab == "EXECUTIVE") VibrantOnPrimaryContainer else VibrantOnSurfaceVariant
                            )
                        }
                    }

                    // Option 2: Engineer Performance KPIs
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (dashboardViewTab == "ENGINEER_KPI") VibrantPrimaryContainer else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { dashboardViewTab = "ENGINEER_KPI" }
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = null,
                                tint = if (dashboardViewTab == "ENGINEER_KPI") VibrantOnPrimaryContainer else VibrantOnSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "KPI Kinerja Engineer",
                                fontSize = 12.sp,
                                fontWeight = if (dashboardViewTab == "ENGINEER_KPI") FontWeight.Bold else FontWeight.Medium,
                                color = if (dashboardViewTab == "ENGINEER_KPI") VibrantOnPrimaryContainer else VibrantOnSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        if (dashboardViewTab == "ENGINEER_KPI") {
            // RENDER DEDICATED ENGINEER PERFORMANCE KPI VIEW
            item {
                EngineerPerformanceDashboard(
                    items = items,
                    onSelectItem = onSelectItem
                )
            }
        } else {
            // Section 1: Executive KPI & Workload Diagram with percentages
            item {
                ExecutiveKpiOverview(items = items)
            }

            // Section 2: Live Team Location Card
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
                    shadowElevation = 1.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateTab(FosisNavTab.GEOLOKASI) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
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
                                        .background(Color(0xFF22C55E).copy(alpha = pulseAlpha))
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Live Fleet & Location Tracking",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantOnBackground
                                )
                            }

                            Text(
                                text = "MONITORING KENDARAAN",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantPrimary,
                                modifier = Modifier.clickable { onNavigateTab(FosisNavTab.GEOLOKASI) }
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFF3F3F3),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = VibrantPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = uiState.userLocationAddress,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = VibrantOnBackground
                                        )
                                    }
                                    Text(
                                        text = "Koordinat: ${String.format("%.4f", uiState.userLatitude)}, ${String.format("%.4f", uiState.userLongitude)} • Lokasi disesuaikan device",
                                        fontSize = 11.sp,
                                        color = VibrantOnSurfaceVariant,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 3: EXPORT DATA OPERASIONAL (MINGGUAN / BULANAN - EXCEL, PDF, PPT)
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFEFF6FF),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.FileDownload,
                                            contentDescription = null,
                                            tint = Color(0xFF1D4ED8),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Export Data Operasional",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VibrantOnBackground
                                    )
                                    Text(
                                        text = "Pilihan periode Mingguan / Bulanan ke Excel, PDF, dan PPT",
                                        fontSize = 11.sp,
                                        color = VibrantOnSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Period Filter Chips: Mingguan vs Bulanan vs Semua
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val periods = listOf(
                                "ALL" to "Semua Periode",
                                "WEEKLY" to "Mingguan (7 Hari)",
                                "MONTHLY" to "Bulanan (30 Hari)"
                            )
                            periods.forEach { (key, label) ->
                                val isSelected = uiState.filterPeriod == key
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.setFilterPeriod(key) },
                                    label = { Text(label, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = VibrantPrimaryContainer,
                                        selectedLabelColor = VibrantOnPrimaryContainer
                                    )
                                )
                            }
                        }

                        // Export Action Buttons (Excel, PDF, PPT)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Excel Button
                            Button(
                                onClick = { viewModel.exportData(context, "TICKETS", "EXCEL") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Excel", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // PDF Button
                            Button(
                                onClick = { viewModel.exportData(context, "TICKETS", "PDF") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // PPT Button
                            Button(
                                onClick = { viewModel.exportData(context, "TICKETS", "PPT") },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC2410C)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Slideshow, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PPT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Section 4: Quick Operational Navigation Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Modul Operasional FOSIS",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantOnBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickNavTile(
                            title = "Troubleshoot",
                            subtitle = "Incident & Gangguan",
                            icon = Icons.Default.Warning,
                            badgeColor = Color(0xFFB3261E),
                            onClick = { onNavigateTab(FosisNavTab.TROUBLESHOOT) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickNavTile(
                            title = "Maintenance",
                            subtitle = "Preventif & Plan",
                            icon = Icons.Default.Build,
                            badgeColor = Color(0xFF0284C7),
                            onClick = { onNavigateTab(FosisNavTab.MAINTENANCE) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickNavTile(
                            title = "Form Perijinan",
                            subtitle = "Surat Ijin Kerja (SIK)",
                            icon = Icons.Default.AssignmentTurnedIn,
                            badgeColor = Color(0xFF16A34A),
                            onClick = { onNavigateTab(FosisNavTab.PERIJINAN) },
                            modifier = Modifier.weight(1f)
                        )
                        QuickNavTile(
                            title = "Administrasi",
                            subtitle = "Laporan & Logistik",
                            icon = Icons.Default.FolderShared,
                            badgeColor = Color(0xFF6750A4),
                            onClick = { onNavigateTab(FosisNavTab.ADMINISTRASI) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 5: MANDATORY REQUIREMENT - KOLOM KOMENTAR & EVALUASI DEPT. HEAD (DI KOLOM BAWAH)
            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFFF0FDF4),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF86EFAC)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.RateReview,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Kolom Komentar & Evaluasi Dept. Head",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF14532D)
                                    )
                                    Text(
                                        text = "Khusus Mas Rizki Firdaus (Dept. Head Review)",
                                        fontSize = 11.sp,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = if (isDeptHead) "Akses Edit Dept. Head" else "Read-Only View",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        if (isDeptHead || isAdmin) {
                            // Editable text field for Dept Head
                            OutlinedTextField(
                                value = editableDeptHeadComment,
                                onValueChange = { editableDeptHeadComment = it },
                                placeholder = {
                                    Text(
                                        "Tuliskan evaluasi, instruksi perubahan, atau catatan resmi Dept. Head di sini...",
                                        color = Color(0xFF64748B),
                                        fontSize = 12.sp
                                    )
                                },
                                textStyle = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF0F172A),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 100.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedTextColor = Color(0xFF0F172A),
                                    unfocusedTextColor = Color(0xFF0F172A),
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedBorderColor = Color(0xFF16A34A),
                                    unfocusedBorderColor = Color(0xFF86EFAC),
                                    focusedPlaceholderColor = Color(0xFF64748B),
                                    unfocusedPlaceholderColor = Color(0xFF94A3B8),
                                    cursorColor = Color(0xFF16A34A)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Button(
                                    onClick = { viewModel.updateDeptHeadComment(editableDeptHeadComment) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                                ) {
                                    Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Simpan Evaluasi Dept. Head", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            // Read-only presentation for Field Engineers and IC
                            val displayComment = uiState.executiveDeptHeadComment.ifBlank {
                                "Pekerjaan FO berjalan sesuai target SLA dan SOP K3. Tetap prioritaskan safety first, dokumentasi titik sambungan closure/handhole, dan koordinasi cepat tim lapangan."
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = "\"$displayComment\"",
                                        fontSize = 12.sp,
                                        color = Color(0xFF0F172A),
                                        lineHeight = 18.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "— Mas Rizki Firdaus (Department Head)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                }
                            }
                            Text(
                                text = "Catatan: Sesuai SOP, anggota tim lapangan dan IC dapat melihat evaluasi di atas. Kolom komentar hanya dapat diubah oleh Mas Rizki Firdaus (Dept. Head).",
                                fontSize = 10.sp,
                                color = Color(0xFF4B5563),
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickNavTile(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
        shadowElevation = 1.dp,
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = badgeColor.copy(alpha = 0.14f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = VibrantOnBackground)
                Text(text = subtitle, fontSize = 10.sp, color = VibrantOnSurfaceVariant)
            }
        }
    }
}

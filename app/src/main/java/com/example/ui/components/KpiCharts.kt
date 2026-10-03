package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.ui.theme.*

@Composable
fun ExecutiveKpiOverview(items: List<FosisItemEntity>) {
    val totalTickets = items.size
    val troubleshootItems = items.filter { it.type == FosisType.TROUBLESHOOT }
    val inSlaCount = troubleshootItems.count { it.slaStatus == "IN SLA" }
    val overSlaCount = troubleshootItems.count { it.slaStatus == "OVER SLA" }
    
    val slaRatePercent = if (troubleshootItems.isNotEmpty()) {
        (inSlaCount.toDouble() * 100.0) / troubleshootItems.size.toDouble()
    } else {
        0.0
    }
    val overSlaRatePercent = if (troubleshootItems.isNotEmpty()) {
        (overSlaCount.toDouble() * 100.0) / troubleshootItems.size.toDouble()
    } else {
        0.0
    }

    val maintenanceItems = items.filter { it.type == FosisType.MAINTENANCE }
    val completedMaintenance = maintenanceItems.count { it.maintenanceStatus == "DONE" }
    val mntRatePercent = if (maintenanceItems.isNotEmpty()) {
        (completedMaintenance.toDouble() * 100.0) / maintenanceItems.size.toDouble()
    } else {
        0.0
    }

    val pendingApproval = items.count { !it.isLocked }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Workload & KPI Hero Card with explicit percentages
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = VibrantPrimaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Workload & KPI Diagram",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnPrimaryContainer
                        )
                        Text(
                            text = "Kompilasi rasio performa & kepatuhan SLA dalam persentase (%)",
                            fontSize = 11.sp,
                            color = VibrantOnPrimaryContainer.copy(alpha = 0.85f)
                        )
                    }

                    // Percentage Target Pill
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.7f)
                    ) {
                        Text(
                            text = if (totalTickets > 0) "${String.format("%.1f", slaRatePercent)}% SLA" else "0.0% (Empty)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                if (totalTickets == 0) {
                    // Clean Empty State
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Semua Data Operasional Masih Bersih (0 Tiket)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantOnPrimaryContainer
                            )
                            Text(
                                text = "Persentase SLA: 0.0% • Persentase Maintenance: 0.0%\nDiagram akan terhitung otomatis saat tiket baru dibuat.",
                                fontSize = 11.sp,
                                color = VibrantOnPrimaryContainer.copy(alpha = 0.8f),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                } else {
                    // Cadence Distribution with Percentages
                    val distribution = listOf(
                        "Troubleshoot" to if (totalTickets > 0) (troubleshootItems.size * 100) / totalTickets else 0,
                        "Maintenance" to if (totalTickets > 0) (maintenanceItems.size * 100) / totalTickets else 0,
                        "Administrasi" to if (totalTickets > 0) ((totalTickets - troubleshootItems.size - maintenanceItems.size) * 100) / totalTickets else 0
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        distribution.forEach { (category, percent) ->
                            val fraction = (percent / 100f).coerceIn(0.12f, 1f)
                            Column(
                                modifier = Modifier.weight(1f),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$percent%",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantOnPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height((50 * fraction).dp)
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(VibrantPrimary)
                                )
                            }
                        }
                    }

                    // Distribution labels
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        distribution.forEach { (category, _) ->
                            Text(
                                text = category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantOnPrimaryContainer,
                                modifier = Modifier.widthIn(max = 90.dp),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Metric Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            VibrantMetricCard(
                title = "Total Tiket",
                value = "$totalTickets",
                subtitle = "100.0% Basis",
                icon = Icons.Default.AssignmentTurnedIn,
                badgeColor = VibrantPrimary,
                modifier = Modifier.weight(1f)
            )

            VibrantMetricCard(
                title = "SLA Compliance",
                value = "${String.format("%.1f", slaRatePercent)}%",
                subtitle = "$inSlaCount/$totalTickets Tiket",
                icon = Icons.Default.Timer,
                badgeColor = if (slaRatePercent >= 80.0) VibrantTertiary else VibrantError,
                modifier = Modifier.weight(1f)
            )

            VibrantMetricCard(
                title = "Mnt Done",
                value = "${String.format("%.1f", mntRatePercent)}%",
                subtitle = "$completedMaintenance Mnt Selesai",
                icon = Icons.Default.BuildCircle,
                badgeColor = Color(0xFF0284C7),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Detailed Graphic Card with SLA Ring & KPI Details
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Text(
                            text = "Diagram Analisis SLA & Performa",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground
                        )
                        Text(
                            text = "Perhitungan persentase real-time operasional",
                            fontSize = 11.sp,
                            color = VibrantOnSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = VibrantPrimaryContainer
                    ) {
                        Text(
                            text = "Angka % Akumulatif",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Chart with bold percentage in center
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 14.dp.toPx()
                            val radius = (size.minDimension - strokeWidth) / 2
                            val center = Offset(size.width / 2, size.height / 2)

                            // Background track
                            drawCircle(
                                color = Color(0xFFF3EDF7),
                                radius = radius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )

                            if (troubleshootItems.isNotEmpty()) {
                                val sweepOver = (overSlaCount.toFloat() / troubleshootItems.size.toFloat()) * 360f
                                val sweepIn = (inSlaCount.toFloat() / troubleshootItems.size.toFloat()) * 360f

                                // Over SLA arc (Red)
                                if (sweepOver > 0f) {
                                    drawArc(
                                        color = VibrantError,
                                        startAngle = -90f,
                                        sweepAngle = sweepOver,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }

                                // In SLA arc (Violet/Primary)
                                if (sweepIn > 0f) {
                                    drawArc(
                                        color = VibrantPrimary,
                                        startAngle = -90f + sweepOver,
                                        sweepAngle = sweepIn,
                                        useCenter = false,
                                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                                    )
                                }
                            }
                        }

                        // Text in center of Donut
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${String.format("%.1f", slaRatePercent)}%",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (slaRatePercent >= 80 || troubleshootItems.isEmpty()) VibrantPrimary else VibrantError
                            )
                            Text(
                                text = "IN SLA",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF79747E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Legend and exact breakdown
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DiagramPercentageItem(
                            label = "In SLA (Kepatuhan)",
                            percentage = "${String.format("%.1f", slaRatePercent)}%",
                            countText = "$inSlaCount Tiket",
                            color = VibrantPrimary
                        )

                        DiagramPercentageItem(
                            label = "Over SLA (Keterlambatan)",
                            percentage = "${String.format("%.1f", overSlaRatePercent)}%",
                            countText = "$overSlaCount Tiket",
                            color = VibrantError
                        )

                        DiagramPercentageItem(
                            label = "Maintenance Selesai",
                            percentage = "${String.format("%.1f", mntRatePercent)}%",
                            countText = "$completedMaintenance/${maintenanceItems.size} Kegiatan",
                            color = Color(0xFF0284C7)
                        )
                    }
                }

                // Root Cause Breakdown Section with exact percentages
                if (troubleshootItems.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Divider(color = Color(0xFFF3EDF7), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Diagram Root Cause Gangguan (%)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = VibrantOnBackground
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val rootCauseGroups = troubleshootItems.groupBy { it.rootCause.ifBlank { "OTHER" } }
                    rootCauseGroups.entries.take(4).forEach { (cause, group) ->
                        val causePercent = (group.size.toDouble() * 100.0) / troubleshootItems.size.toDouble()
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(cause, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF49454F))
                                Text(
                                    "${String.format("%.1f", causePercent)}% (${group.size})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = VibrantPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { (causePercent / 100.0).toFloat() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = VibrantPrimary,
                                trackColor = Color(0xFFF3EDF7)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiagramPercentageItem(
    label: String,
    percentage: String,
    countText: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = VibrantOnBackground
            )
            Text(
                text = countText,
                fontSize = 10.sp,
                color = VibrantOnSurfaceVariant
            )
        }
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.12f)
        ) {
            Text(
                text = percentage,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun VibrantMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    badgeColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
        shadowElevation = 1.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = badgeColor.copy(alpha = 0.14f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = VibrantOnBackground
            )

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = VibrantOnBackground
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = VibrantOnSurfaceVariant
            )
        }
    }
}

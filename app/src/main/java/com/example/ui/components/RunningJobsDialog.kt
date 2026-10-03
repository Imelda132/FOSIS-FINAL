package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.WorkflowStatus
import com.example.ui.theme.*

@Composable
fun RunningJobsDialog(
    isOpen: Boolean,
    items: List<FosisItemEntity>,
    onDismiss: () -> Unit,
    onSelectItem: (FosisItemEntity) -> Unit
) {
    if (!isOpen) return
    val context = LocalContext.current

    // Running jobs: tickets not yet fully approved by Dept Head
    val runningJobs = items.filter { it.workflowStatus != WorkflowStatus.APPROVED_DEPT_HEAD }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 580.dp)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFEFF6FF),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Notifikasi",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Notifikasi Pekerjaan Berjalan",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Text(
                                text = "Muncul otomatis saat membuka akses aplikasi",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Tutup", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))



                Spacer(modifier = Modifier.height(14.dp))

                if (runningJobs.isEmpty()) {
                    // Empty state (user asked for clean initial database)
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFF8FAFC),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Tidak Ada Pekerjaan Berjalan yang Tertunda",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Semua tiket operasional telah selesai, atau Anda belum menginput tiket baru pada database bersih ini.",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Daftar Tiket Pekerjaan yang Masih Aktif (${runningJobs.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF334155),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(runningJobs, key = { it.id }) { item ->
                            val typeColor = when (item.type) {
                                FosisType.TROUBLESHOOT -> Color(0xFFDC2626)
                                FosisType.MAINTENANCE -> Color(0xFF0284C7)
                                FosisType.ADMINISTRASI -> Color(0xFFD97706)
                            }

                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFFFAFAFA),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5E7EB)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectItem(item)
                                        onDismiss()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = typeColor.copy(alpha = 0.12f)
                                            ) {
                                                Text(
                                                    text = item.type.label,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = typeColor,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = item.ticketNo,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                        }

                                        Text(
                                            text = item.title,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF334155),
                                            maxLines = 1
                                        )

                                        Text(
                                            text = "Petugas: ${item.engineerName} • ${item.siteLocationName}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF64748B)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = when (item.workflowStatus) {
                                            WorkflowStatus.DRAFT -> Color(0xFFF3F4F6)
                                            WorkflowStatus.SUBMITTED_TEKNISI -> Color(0xFFFEF3C7)
                                            WorkflowStatus.VERIFIED_LEADER -> Color(0xFFE0E7FF)
                                            WorkflowStatus.LOCKED_ADMIN -> Color(0xFFFEE2E2)
                                            WorkflowStatus.APPROVED_DEPT_HEAD -> Color(0xFFDCFCE7)
                                            WorkflowStatus.REJECTED -> Color(0xFFFFE4E6)
                                            else -> Color(0xFFF3F4F6)
                                        }
                                    ) {
                                        Text(
                                            text = item.workflowStatus.label,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = when (item.workflowStatus) {
                                                WorkflowStatus.DRAFT -> Color(0xFF4B5563)
                                                WorkflowStatus.SUBMITTED_TEKNISI -> Color(0xFFB45309)
                                                WorkflowStatus.VERIFIED_LEADER -> Color(0xFF4338CA)
                                                WorkflowStatus.LOCKED_ADMIN -> Color(0xFFB91C1C)
                                                WorkflowStatus.APPROVED_DEPT_HEAD -> Color(0xFF15803D)
                                                WorkflowStatus.REJECTED -> Color(0xFFBE123C)
                                                else -> Color(0xFF4B5563)
                                            },
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lanjutkan ke Dashboard FOSIS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

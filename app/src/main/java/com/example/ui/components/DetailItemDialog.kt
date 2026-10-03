package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.FosisItemEntity
import com.example.data.model.FosisType
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.data.model.WorkflowStatus
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun DetailItemDialog(
    item: FosisItemEntity?,
    activeUser: UserEntity?,
    onDismiss: () -> Unit,
    onSubmitTeknisi: (FosisItemEntity) -> Unit,
    onVerifyLeader: (FosisItemEntity) -> Unit,
    onToggleLockAdmin: (FosisItemEntity) -> Unit,
    onApproveDeptHead: (FosisItemEntity, String) -> Unit,
    onExportPdf: (FosisItemEntity) -> Unit
) {
    if (item == null) return

    var deptHeadCommentInput by remember {
        mutableStateOf(item.deptHeadComment ?: "")
    }

    val context = LocalContext.current
    val role = activeUser?.role
    val isAdmin = role == UserRole.ADMIN
    val isIc = (role == UserRole.IC_TROUBLESHOOT || role == UserRole.IC_MAINTENANCE)
    val isDeptHead = role == UserRole.DEPT_HEAD
    val isTeknisi = role == UserRole.TEKNISI

    val canVerify = isAdmin || isIc

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.94f)
                .padding(12.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // HEADER
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = when (item.type) {
                                FosisType.TROUBLESHOOT -> Color(0xFFFEE2E2)
                                FosisType.MAINTENANCE -> Color(0xFFE0F2FE)
                                FosisType.ADMINISTRASI -> Color(0xFFFEF3C7)
                            },
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = item.type.label.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = item.ticketNo,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(Modifier.height(8.dp))

                Text(
                    text = item.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                Divider(modifier = Modifier.padding(vertical = 10.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // ===============================
                    // WORKFLOW TRACKER
                    // ===============================
                    Text(
                        text = "Alur Kerja 4-Tier (Teknisi → IC Verifikasi → Admin → Dept Head)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    WorkflowStepBar(currentStatus = item.workflowStatus)
                    Spacer(Modifier.height(12.dp))

                    // ===============================
                    // LOCK STATUS
                    // ===============================
                    if (item.isLocked) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7),
                            border = BorderStroke(1.dp, Color(0xFFFCD34D))
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Tiket Terkunci (Locked). Menunggu proses verifikasi Admin.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF92400E)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    // ===============================
                    // DETAIL INFORMATION
                    // ===============================
                    Text(
                        text = "Spesifikasi Detail Tiket:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            SpecRow("Periode", "${item.bulan} ${item.tahun}")
                            SpecRow("Status SLA", item.slaStatus, item.slaStatus == "OVER SLA")
                            SpecRow("Root Cause", item.rootCause)
                            SpecRow("Service Impact", item.serviceImpact)
                            SpecRow("Mitra / Vendor", item.mitra)
                            SpecRow("Teknisi Lapangan", item.engineerName)
                            SpecRow("Lokasi Site", item.siteLocationName)
                            SpecRow(
                                "GPS Device",
                                "${String.format("%.5f", item.latitude)}, ${String.format("%.5f", item.longitude)}"
                            )
                            Button(
                                onClick = {
                                    val uri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${item.latitude},${item.longitude}")
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, uri)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Buka Lokasi Google Maps", fontSize = 11.sp)
                            }
                            SpecRow("Material Digunakan", item.materialUsed.ifBlank { "Tidak ada rincian material" })
                        }
                    }

                    // ===============================
                    // LAMPIRAN PDF OTDR
                    // ===============================
                    val pdfFiles = item.otdrPdfAttachments.split(";").filter { it.isNotBlank() }
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PictureAsPdf,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Lampiran Berkas OTDR (PDF)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                                Text("${pdfFiles.size}/50 PDF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            if (pdfFiles.isEmpty()) {
                                Text("Belum ada berkas PDF OTDR.", fontSize = 11.sp)
                            } else {
                                pdfFiles.take(6).forEach { file ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(file, fontSize = 11.sp)
                                        Text("PDF Valid", fontSize = 10.sp)
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(14.dp))

                    // ===============================
                    // KOMENTAR DEPT HEAD
                    // ===============================
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.RateReview, contentDescription = null, tint = Color(0xFF15803D))
                                Spacer(Modifier.width(6.dp))
                                Text("Komentar Dept Head", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            if (isDeptHead || isAdmin) {
                                OutlinedTextField(
                                    value = deptHeadCommentInput,
                                    onValueChange = { deptHeadCommentInput = it },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text("Masukkan catatan Dept Head...") }
                                )
                                Spacer(Modifier.height(8.dp))
                                Button(
                                    onClick = { onApproveDeptHead(item, deptHeadCommentInput) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF15803D))
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null)
                                    Spacer(Modifier.width(5.dp))
                                    Text("Simpan Persetujuan")
                                }
                            } else {
                                Text(
                                    text = if (item.deptHeadComment.isNullOrBlank()) "Belum ada komentar Dept Head." else item.deptHeadComment!!,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // ===============================
                    // ACTION BUTTONS
                    // ===============================
                    Spacer(Modifier.height(10.dp))
                    Divider()
                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // EXPORT PDF
                        OutlinedButton(
                            onClick = { onExportPdf(item) },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF", fontSize = 11.sp)
                        }

                        // WHATSAPP
                        Button(
                            onClick = {
                                com.example.util.WhatsAppHelper.sendWhatsAppMessage(
                                    context = context,
                                    rawPhone = "081234567890",
                                    subject = "[Notifikasi Tiket] ${item.ticketNo}",
                                    bodyMessage = """
                                        Tiket FOSIS
                                        Nomor : ${item.ticketNo}
                                        Judul : ${item.title}
                                        Status : ${item.workflowStatus.label}
                                        Lokasi : ${item.siteLocationName}
                                    """.trimIndent()
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("WA", fontSize = 11.sp)
                        }

                        // ============================
                        // TEKNISI SUBMIT
                        // ============================
                        if (isTeknisi && item.workflowStatus == WorkflowStatus.DRAFT) {
                            Button(onClick = { onSubmitTeknisi(item) }) {
                                Icon(Icons.Default.Send, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Submit")
                            }
                        }

                        // ============================
                        // IC / ADMIN VERIFY
                        // ============================
                        if (canVerify && (item.workflowStatus == WorkflowStatus.SUBMITTED_TEKNISI || item.workflowStatus == WorkflowStatus.DRAFT)) {
                            Button(
                                onClick = { onVerifyLeader(item) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A))
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Verifikasi")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkflowStepBar(currentStatus: WorkflowStatus) {
    val steps = listOf("Teknisi", "IC", "Admin", "Dept Head")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val completed = currentStatus.step >= index + 1
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(if(completed) Color(0xFF16A34A) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    if(completed){
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }else{
                        Text(
                            text = "${index + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = label,
                    fontSize = 9.sp,
                    fontWeight = if(completed) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String, isHighlight: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 11.sp)
        Text(
            value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isHighlight) Color.Red else Color.Black
        )
    }
}
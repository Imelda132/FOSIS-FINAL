package com.example.ui.screens

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
import com.example.data.model.ChatMessageEntity
import com.example.data.model.FosisItemEntity
import com.example.data.model.MessageType
import com.example.data.model.PermitEntity
import com.example.data.model.UserEntity
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FieldChatScreen(
    messages: List<ChatMessageEntity>,
    activeUser: UserEntity?,
    userLatitude: Double,
    userLongitude: Double,
    userAddress: String,
    items: List<FosisItemEntity>,
    permits: List<PermitEntity>,
    onSendMessage: (String, MessageType, Long?, String, String, String) -> Unit,
    onSelectItem: (FosisItemEntity) -> Unit
) {
    val context = LocalContext.current
    var textInput by remember { mutableStateOf("") }
    var isAttachFormDialogOpen by remember { mutableStateOf(false) }
    var isSimulatedOffline by remember { mutableStateOf(false) }

    val currentUserName = activeUser?.name ?: "Sanderlina Imelda"
    val currentUserEmail = activeUser?.email ?: "sanderlina.imelda@gmail.com"

    val timeFormatter = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .padding(12.dp)
    ) {
        // Top Offline Chat Banner
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = if (isSimulatedOffline) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isSimulatedOffline) Color(0xFFFCA5A5) else Color(0xFF86EFAC)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSimulatedOffline) Color(0xFFEF4444) else Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isSimulatedOffline) "MODE OFFLINE - TERSIMPAN LOKAL" else "CHAT GRATIS TIM LAPANGAN (ONLINE)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSimulatedOffline) Color(0xFF991B1B) else Color(0xFF166534)
                        )
                        Text(
                            text = "Bebas kuota • Pesan, Lokasi GPS & Form otomatis tersimpan di HP & tersinkronisasi",
                            fontSize = 10.sp,
                            color = if (isSimulatedOffline) Color(0xFF7F1D1D) else Color(0xFF15803D)
                        )
                    }
                }

                FilterChip(
                    selected = isSimulatedOffline,
                    onClick = { isSimulatedOffline = !isSimulatedOffline },
                    label = { Text(if (isSimulatedOffline) "Mode Offline" else "Online", fontSize = 10.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isSimulatedOffline) Icons.Default.CloudOff else Icons.Default.Wifi,
                            contentDescription = null,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message Thread (LazyColumn)
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            // Initial welcoming system message if empty
            if (messages.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFEFF6FF),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Chat, contentDescription = null, tint = Color(0xFF1D4ED8), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sistem Chat Lapangan FOSIS Aktif",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E40AF)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Gunakan kolom chat ini untuk berkirim pesan gratis antar 21 anggota tim, membagikan lokasi GPS terkunci, dan melampirkan form pekerjaan/SIK saat offline maupun online.",
                                fontSize = 11.sp,
                                color = Color(0xFF1E3A8A)
                            )
                        }
                    }
                }
            }

            items(messages) { msg ->
                val isMe = msg.senderEmail.equals(currentUserEmail, ignoreCase = true) || msg.senderName.equals(currentUserName, ignoreCase = true)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
                        modifier = Modifier.widthIn(max = 290.dp)
                    ) {
                        // Sender label
                        Text(
                            text = "${msg.senderName} (${msg.senderRole})",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )

                        // Bubble Card
                        Surface(
                            shape = RoundedCornerShape(
                                topStart = 16.dp,
                                topEnd = 16.dp,
                                bottomStart = if (isMe) 16.dp else 4.dp,
                                bottomEnd = if (isMe) 4.dp else 16.dp
                            ),
                            color = if (isMe) Color(0xFF2563EB) else Color.White,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isMe) Color(0xFF1D4ED8) else Color(0xFFE2E8F0)
                            ),
                            shadowElevation = 1.dp
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                when (msg.messageType) {
                                    MessageType.TEXT -> {
                                        Text(
                                            text = msg.messageText,
                                            fontSize = 13.sp,
                                            color = if (isMe) Color.White else Color(0xFF0F172A)
                                        )
                                    }

                                    MessageType.LOCATION -> {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = if (isMe) Color(0xFFFDE047) else Color(0xFFDC2626),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "BERBAGI LOKASI GPS",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = if (isMe) Color.White else Color(0xFF0F172A)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Koordinat: ${String.format("%.5f", msg.locationLatitude)}, ${String.format("%.5f", msg.locationLongitude)}",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isMe) Color(0xFF93C5FD) else Color(0xFF0284C7)
                                            )
                                            if (msg.locationAddress.isNotBlank()) {
                                                Text(
                                                    text = msg.locationAddress,
                                                    fontSize = 10.sp,
                                                    color = if (isMe) Color(0xFFE0E7FF) else Color(0xFF475569)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = {
                                                    try {
                                                        val intent = android.content.Intent(
                                                            android.content.Intent.ACTION_VIEW,
                                                            android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${msg.locationLatitude},${msg.locationLongitude}")
                                                        )
                                                        context.startActivity(intent)
                                                    } catch (e: Exception) {
                                                        android.widget.Toast.makeText(context, "Gagal membuka Google Maps", android.widget.Toast.LENGTH_SHORT).show()
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = if (isMe) Color.White else Color(0xFF1E88E5)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Place,
                                                    contentDescription = null,
                                                    tint = if (isMe) Color(0xFF1E88E5) else Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = "Buka di Google Maps",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isMe) Color(0xFF1E88E5) else Color.White
                                                )
                                            }
                                        }
                                    }

                                    MessageType.FORM_TICKET, MessageType.FORM_PERMIT -> {
                                        Column {
                                            Surface(
                                                shape = RoundedCornerShape(8.dp),
                                                color = if (isMe) Color(0xFF1D4ED8) else Color(0xFFF1F5F9),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Column(modifier = Modifier.padding(8.dp)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(
                                                            text = if (msg.messageType == MessageType.FORM_TICKET) "📋 FORM TIKET PEKERJAAN" else "📄 FORM PERIJINAN (SIK)",
                                                            fontSize = 10.sp,
                                                            fontWeight = FontWeight.Black,
                                                            color = if (isMe) Color(0xFFFDE047) else Color(0xFF1D4ED8)
                                                        )
                                                        Surface(
                                                            shape = RoundedCornerShape(4.dp),
                                                            color = Color(0xFFDCFCE7)
                                                        ) {
                                                            Text(
                                                                text = msg.attachedFormStatus.ifBlank { "IN PROGRESS" },
                                                                fontSize = 8.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = Color(0xFF15803D),
                                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                            )
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(3.dp))
                                                    Text(
                                                        text = msg.attachedFormTitle,
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isMe) Color.White else Color(0xFF0F172A)
                                                    )
                                                    if (msg.messageText.isNotBlank()) {
                                                        Text(
                                                            text = "Catatan: ${msg.messageText}",
                                                            fontSize = 10.sp,
                                                            color = if (isMe) Color(0xFFE0E7FF) else Color(0xFF475569)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Timestamp & Offline Status
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.End,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = timeFormatter.format(Date(msg.timestamp)),
                                        fontSize = 9.sp,
                                        color = if (isMe) Color(0xFFBFDBFE) else Color(0xFF94A3B8)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    if (msg.isOfflinePending) {
                                        Text(
                                            text = "⏳ Pending (Offline)",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isMe) Color(0xFFFDE047) else Color(0xFFD97706)
                                        )
                                    } else {
                                        Icon(
                                            Icons.Default.DoneAll,
                                            contentDescription = "Synced",
                                            tint = if (isMe) Color(0xFF86EFAC) else Color(0xFF16A34A),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Attachments Bar (Share GPS Location & Form)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            OutlinedButton(
                onClick = {
                    onSendMessage(
                        "Lokasi Lapangan Saya",
                        MessageType.LOCATION,
                        null,
                        "",
                        "",
                        ""
                    )
                    android.widget.Toast.makeText(context, "Lokasi GPS terkini dikirim ke chat!", android.widget.Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFDC2626))
                Spacer(modifier = Modifier.width(4.dp))
                Text("📍 Kirim Lokasi GPS", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { isAttachFormDialogOpen = true },
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF2563EB))
                Spacer(modifier = Modifier.width(4.dp))
                Text("📋 Lampirkan Form", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Text Message Input Bar
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Tulis pesan tim (offline/online)...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    )
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput, MessageType.TEXT, null, "", "", "")
                            textInput = ""
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2563EB))
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Kirim", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }

    // Attach Form Picker Dialog
    if (isAttachFormDialogOpen) {
        AlertDialog(
            onDismissRequest = { isAttachFormDialogOpen = false },
            title = {
                Text("Pilih Form Untuk Dikirim Ke Chat", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                ) {
                    Text("Pilih Tiket / SIK yang ingin dibagikan:", fontSize = 12.sp, color = Color(0xFF64748B))
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(items) { item ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF8FAFC),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSendMessage(
                                            "Lampiran Tiket: ${item.ticketNo}",
                                            MessageType.FORM_TICKET,
                                            item.id,
                                            "${item.ticketNo} - ${item.title}",
                                            item.type.name,
                                            item.workflowStatus.label
                                        )
                                        isAttachFormDialogOpen = false
                                        android.widget.Toast.makeText(context, "Form Tiket ${item.ticketNo} dilampirkan!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(item.ticketNo, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                        Text(item.title, fontSize = 11.sp, color = Color(0xFF475569), maxLines = 1)
                                    }
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        items(permits) { permit ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF0FDF4),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSendMessage(
                                            "Lampiran SIK: ${permit.permitNo}",
                                            MessageType.FORM_PERMIT,
                                            permit.id,
                                            "${permit.permitNo} - ${permit.siteLocation}",
                                            "SIK",
                                            permit.status.name
                                        )
                                        isAttachFormDialogOpen = false
                                        android.widget.Toast.makeText(context, "Form SIK ${permit.permitNo} dilampirkan!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(permit.permitNo, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF166534))
                                        Text("Lokasi: ${permit.siteLocation}", fontSize = 11.sp, color = Color(0xFF15803D), maxLines = 1)
                                    }
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { isAttachFormDialogOpen = false }) {
                    Text("Batal")
                }
            }
        )
    }
}

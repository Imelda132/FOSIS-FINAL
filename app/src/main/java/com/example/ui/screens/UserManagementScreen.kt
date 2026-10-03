package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    users: List<UserEntity>,
    activeUser: UserEntity?,
    onUpdateUser: (UserEntity) -> Unit,
    onAddUser: (name: String, role: UserRole, teamName: String, phone: String, passwordPin: String) -> Unit = { _, _, _, _, _ -> },
    onDeleteUser: (UserEntity) -> Unit = {},
    onTrigger2FATest: (String, String) -> Unit = { _, _ -> }
) {
    val isAdmin = activeUser?.role == UserRole.ADMIN
    var showAddDialog by remember { mutableStateOf(false) }
    var userToEdit by remember { mutableStateOf<UserEntity?>(null) }
    var userToDelete by remember { mutableStateOf<UserEntity?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Admin / Team Access Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAdmin) Color(0xFFF3E8FF) else Color(0xFFF0FDF4)
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isAdmin) Color(0xFFD8B4FE) else Color(0xFFBBF7D0)
            )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = if (isAdmin) Icons.Default.AdminPanelSettings else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = if (isAdmin) Color(0xFF7E22CE) else Color(0xFF16A34A),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Daftar Data Akses & Hak Akses Tim",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isAdmin) Color(0xFF581C87) else Color(0xFF14532D)
                            )
                            Text(
                                text = if (isAdmin)
                                    "Akses Penuh Admin: Tambah (Create), Edit, Hapus (Delete), dan Lihat (View) Data Akses Tim"
                                else
                                    "Akses Tim (Read-Only): Tim dapat melihat seluruh daftar nama & data akses. Tambah/Edit/Hapus khusus Admin.",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAdmin) Color(0xFF6B21A8) else Color(0xFF15803D)
                            )
                        }
                    }

                    if (isAdmin) {
                        Button(
                            onClick = { showAddDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Akses Tim", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Card Simulasi Kirim WA Ke No HP Admin (+6289665805758)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFDCFCE7),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Simulasi Notifikasi WA Admin (+6289665805758)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF14532D)
                    )
                    Text(
                        text = "Kirim notifikasi data staf ke WhatsApp Admin +6289665805758",
                        fontSize = 10.sp,
                        color = Color(0xFF166534)
                    )
                }
                val context = androidx.compose.ui.platform.LocalContext.current
                Button(
                    onClick = {
                        val targetPhone = "+6289665805758"
                        com.example.util.WhatsAppHelper.sendWhatsAppMessage(
                            context = context,
                            rawPhone = targetPhone,
                            subject = "AKUN ADMIN FOSIS OPERATIONAL",
                            bodyMessage = "Halo Sanderlina Imelda (Admin),\n\nBerikut data akun FOSIS Anda:\n• Akun Admin: Sanderlina Imelda\n• No. HP: $targetPhone\n• Status: Aktif (Super Admin)"
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kirim WA (+62)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (!isAdmin) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFEFF6FF),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Tim dapat melihat seluruh daftar anggota & data akses di bawah ini. Pembaruan/Tambah data dilakukan oleh Admin.",
                        fontSize = 12.sp,
                        color = Color(0xFF1E40AF)
                    )
                }
            }
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(users) { user ->
                UserPermissionCard(
                    user = user,
                    isActive = user.name == activeUser?.name,
                    isAdmin = isAdmin,
                    onUpdateUser = onUpdateUser,
                    onEditUser = { userToEdit = user },
                    onDeleteUser = { userToDelete = user },
                    onTest2FA = {
                        onTrigger2FATest(user.email, "Uji Coba Otentikasi 2FA Akun ${user.name}")
                    }
                )
            }
        }
    }

    // Modal Add Staff / Team
    if (showAddDialog) {
        AddEditStaffDialog(
            title = "Tambah Akses Tim Baru",
            initialName = "",
            initialRole = UserRole.TEKNISI,
            initialTeam = "Tim Lapangan",
            initialPhone = "+6281200000000",
            initialPasswordPin = "1234567890",
            onDismiss = { showAddDialog = false },
            onSubmit = { name, role, team, phone, passwordPin ->
                onAddUser(name, role, team, phone, passwordPin)
                showAddDialog = false
            }
        )
    }

    // Modal Edit Staff / Team
    userToEdit?.let { target ->
        AddEditStaffDialog(
            title = "Edit Data Akses: ${target.name}",
            initialName = target.name,
            initialRole = target.role,
            initialTeam = target.region,
            initialPhone = target.phone,
            initialPasswordPin = target.passwordPin,
            onDismiss = { userToEdit = null },
            onSubmit = { name, role, team, phone, passwordPin ->
                onUpdateUser(
                    target.copy(
                        name = name,
                        role = role,
                        region = team,
                        phone = phone,
                        passwordPin = passwordPin
                    )
                )
                userToEdit = null
            }
        )
    }

    // Modal Confirm Delete Staff
    userToDelete?.let { target ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
            title = { Text("Konfirmasi Hapus Staf", fontWeight = FontWeight.Bold) },
            text = { Text("Apakah Anda yakin ingin menghapus staf '${target.name}' (${target.region}) dari daftar?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteUser(target)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Ya, Hapus Staf")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { userToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
private fun UserPermissionCard(
    user: UserEntity,
    isActive: Boolean,
    isAdmin: Boolean,
    onUpdateUser: (UserEntity) -> Unit,
    onEditUser: () -> Unit,
    onDeleteUser: () -> Unit,
    onTest2FA: () -> Unit
) {
    var expandedPermissions by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // User Header Row (NO EMAIL DISPLAYED)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                when (user.role) {
                                    UserRole.DEPT_HEAD -> Color(0xFF0F4C81)
                                    UserRole.ADMIN -> Color(0xFF6750A4)
                                    UserRole.IC_TROUBLESHOOT -> Color(0xFFB3261E)
                                    UserRole.IC_MAINTENANCE -> Color(0xFF0284C7)
                                    UserRole.TEKNISI -> Color(0xFF10B981)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.avatarInitials.ifBlank { user.name.take(2).uppercase() },
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.name,
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "Sedang Login",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF16A34A),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        val roleText = if (user.region == user.role.label) user.role.label else "${user.region} • ${user.role.label}"
                        Text(
                            text = roleText,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        val context = androidx.compose.ui.platform.LocalContext.current
                        val formattedPhone = com.example.util.WhatsAppHelper.formatToIndonesianWhatsApp(user.phone)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "No. HP: $formattedPhone",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.clickable {
                                    com.example.util.WhatsAppHelper.sendWhatsAppMessage(
                                        context = context,
                                        rawPhone = formattedPhone,
                                        subject = "[Akses Akun FOSIS] ${user.name} - ${user.region}",
                                        bodyMessage = "Halo ${user.name},\nBerikut data akses akun FOSIS Anda:\n• Nama Staf / Tim: ${user.name}\n• Kelompok / Role: ${user.region} (${user.role.label})\n• No. WhatsApp: $formattedPhone\n• Password PIN: ${user.passwordPin}\n\nSilakan gunakan PIN numerik ini untuk login ke aplikasi FOSIS."
                                    )
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(10.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("WA (+62)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                }
                            }
                        }
                        val canViewPin = isAdmin
                        var isPinVisible by remember { mutableStateOf(false) }
                        
                        if (canViewPin) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Password PIN: ${if (isPinVisible) user.passwordPin else "•".repeat(user.passwordPin.length.coerceAtLeast(4))}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF16A34A)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = { isPinVisible = !isPinVisible }, modifier = Modifier.size(24.dp)) {
                                    Icon(
                                        imageVector = if (isPinVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Lihat PIN",
                                        modifier = Modifier.size(14.dp),
                                        tint = Color.Gray
                                    )
                                }
                            }
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Password PIN: •••••••• (Khusus Admin)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF9CA3AF)
                                )
                            }
                        }
                    }
                }

                Row {
                    if (isAdmin) {
                        IconButton(onClick = onEditUser, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Staf", tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                        }
                        if (user.role != UserRole.ADMIN) {
                            IconButton(onClick = onDeleteUser, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus Staf", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                            }
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF3F4F6)
                        ) {
                            Text(
                                text = "Lihat Saja",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF6B7280),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (isAdmin) {
                        IconButton(onClick = { expandedPermissions = !expandedPermissions }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                imageVector = if (expandedPermissions) Icons.Default.ExpandLess else Icons.Default.Tune,
                                contentDescription = "Kelola Izin",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Permission Switches
            if (expandedPermissions && isAdmin) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Konfigurasi Hak Akses Staf (RBAC):",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                PermissionSwitch(
                    title = "Izin Input Data (Create)",
                    subtitle = "Membuat tiket troubleshoot, maintenance, administrasi baru",
                    checked = user.canCreate,
                    onCheckedChange = { onUpdateUser(user.copy(canCreate = it)) }
                )

                PermissionSwitch(
                    title = "Izin Edit Tiket (Update)",
                    subtitle = "Memperbarui data dan spesifikasi lapangan",
                    checked = user.canEdit,
                    onCheckedChange = { onUpdateUser(user.copy(canEdit = it)) }
                )

                PermissionSwitch(
                    title = "Izin Hapus Data (Delete)",
                    subtitle = "Menghapus data tiket yang salah atau dibatalkan",
                    checked = user.canDelete,
                    onCheckedChange = { onUpdateUser(user.copy(canDelete = it)) }
                )

                PermissionSwitch(
                    title = "Izin Ekspor / Impor File",
                    subtitle = "Ekspor PDF/Excel/PPT dan impor berkas perijinan SIK",
                    checked = user.canImportPermit,
                    onCheckedChange = { onUpdateUser(user.copy(canImportPermit = it, canExportPdf = it)) }
                )
            }
        }
    }
}

@Composable
private fun PermissionSwitch(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddEditStaffDialog(
    title: String,
    initialName: String,
    initialRole: UserRole,
    initialTeam: String,
    initialPhone: String,
    initialPasswordPin: String = "1234567890",
    onDismiss: () -> Unit,
    onSubmit: (name: String, role: UserRole, team: String, phone: String, passwordPin: String) -> Unit
) {
    var name by remember { mutableStateOf(initialName) }
    var selectedRole by remember { mutableStateOf(initialRole) }
    var teamName by remember { mutableStateOf(initialTeam) }
    var phone by remember { mutableStateOf(com.example.util.WhatsAppHelper.formatToIndonesianWhatsApp(initialPhone)) }
    var passwordPin by remember { mutableStateOf(initialPasswordPin) }

    val teamsList = listOf("Tim Lapangan", "IC Service Operation", "IC Service Maintenance", "Administrasi", "Departemen Head")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("User : Nama Tim / Staf") },
                    placeholder = { Text("Masukkan nama tim atau staf...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordPin,
                    onValueChange = { input ->
                        // Hanya angka (digits only)
                        passwordPin = input.filter { it.isDigit() }
                    },
                    label = { Text("Password (Hanya Angka)") },
                    placeholder = { Text("Contoh: 1234567890") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Pilih Tim / Kelompok:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    teamsList.forEach { team ->
                        FilterChip(
                            selected = teamName == team,
                            onClick = {
                                teamName = team
                                when (team) {
                                    "Tim Lapangan" -> selectedRole = UserRole.TEKNISI
                                    "IC Service Operation" -> selectedRole = UserRole.IC_TROUBLESHOOT
                                    "IC Service Maintenance" -> selectedRole = UserRole.IC_MAINTENANCE
                                    "Administrasi" -> selectedRole = UserRole.ADMIN
                                    "Departemen Head" -> selectedRole = UserRole.DEPT_HEAD
                                }
                            },
                            label = { Text(team, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { input ->
                        // Strip hyphens and spaces automatically
                        val clean = input.replace("-", "").replace(" ", "")
                        phone = com.example.util.WhatsAppHelper.formatToIndonesianWhatsApp(clean)
                    },
                    label = { Text("Nomor Telepon / WhatsApp (+62)") },
                    placeholder = { Text("Contoh: +6281234567890") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val cleanPhone = com.example.util.WhatsAppHelper.formatToIndonesianWhatsApp(phone)
                        onSubmit(name, selectedRole, teamName, cleanPhone, passwordPin)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7E22CE))
            ) {
                Text("Simpan Akses Tim")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

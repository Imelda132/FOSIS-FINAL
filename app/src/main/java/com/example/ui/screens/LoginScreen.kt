package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.FosisUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    uiState: FosisUiState,
    onLogin: (UserEntity, String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    val availableUsers = if (uiState.users.isNotEmpty()) uiState.users else PRESET_SYSTEM_USERS

    val adminUser = availableUsers.firstOrNull { it.role == UserRole.ADMIN } ?: OFFICIAL_ADMIN_USER
    val deptHeadUser = availableUsers.firstOrNull { it.role == UserRole.DEPT_HEAD } ?: OFFICIAL_DEPT_HEAD
    val icOperationList = availableUsers.filter { it.role == UserRole.IC_TROUBLESHOOT }
    val icMaintenanceList = availableUsers.filter { it.role == UserRole.IC_MAINTENANCE }

    var selectedUser by remember(availableUsers) {
        mutableStateOf(adminUser)
    }

    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showUserDropdown by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VibrantBackground),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, VibrantOutline),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .widthIn(max = 540.dp)
                .padding(vertical = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header & Brand
                item {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = VibrantPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = "Security",
                                    tint = VibrantPrimary,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "FOSIS",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = VibrantOnBackground,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Field Operation Support & Information System",
                            fontSize = 11.sp,
                            color = VibrantOnSurfaceVariant,
                            textAlign = TextAlign.Center
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF3E8FF),
                            modifier = Modifier.padding(top = 6.dp)
                        ) {
                            Text(
                                text = "Sistem Login Otentikasi Staf & Tim Lapangan",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF6B21A8),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Account Selection Section
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "1. Pilih Nama Staf / Akun Pengguna:",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        // Currently selected user card
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF16A34A)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showUserDropdown = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF16A34A),
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = selectedUser.avatarInitials.ifBlank { selectedUser.name.take(2).uppercase() },
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = selectedUser.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF111827)
                                        )
                                        val displayRoleText = if (selectedUser.region == selectedUser.role.label) {
                                            selectedUser.role.label
                                        } else {
                                            "${selectedUser.region} • ${selectedUser.role.label}"
                                        }
                                        Text(
                                            text = displayRoleText,
                                            fontSize = 11.sp,
                                            color = Color(0xFF15803D),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Pilih Staf",
                                    tint = Color(0xFF15803D)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showUserDropdown,
                            onDismissRequest = { showUserDropdown = false },
                            modifier = Modifier.heightIn(max = 420.dp).fillMaxWidth(0.85f),
                            containerColor = Color(0xFF1F2937)
                        ) {
                            // Header Struktur / Manajerial
                            DropdownMenuItem(
                                text = {
                                    Text("--- MANAJERIAL & ADMIN ---", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC084FC))
                                },
                                onClick = {},
                                enabled = false
                            )

                            // 1. Administrasi (Sanderlina Imelda)
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(start = 6.dp)) {
                                        Text(adminUser.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text("Admin", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    selectedUser = adminUser
                                    showUserDropdown = false
                                }
                            )

                            // 2. Departemen Head (Mas Rizki Firdaus)
                            DropdownMenuItem(
                                text = {
                                    Column(modifier = Modifier.padding(start = 6.dp)) {
                                        Text(deptHeadUser.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        Text("Departement Head", fontSize = 10.sp, color = Color(0xFF9CA3AF), fontWeight = FontWeight.SemiBold)
                                    }
                                },
                                onClick = {
                                    selectedUser = deptHeadUser
                                    showUserDropdown = false
                                }
                            )

                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF374151))

                            // 3. IC Service Operation
                            DropdownMenuItem(
                                text = {
                                    Text("--- IC SERVICE OPERATION (${icOperationList.size} Personil) ---", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF87171))
                                },
                                onClick = {},
                                enabled = false
                            )

                            icOperationList.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.padding(start = 6.dp)) {
                                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            Text("IC Service Operation", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                                        }
                                    },
                                    onClick = {
                                        selectedUser = user
                                        showUserDropdown = false
                                    }
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFF374151))

                            // 4. IC Service Maintenance
                            DropdownMenuItem(
                                text = {
                                    Text("--- IC SERVICE MAINTENANCE (${icMaintenanceList.size} Personil) ---", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                },
                                onClick = {},
                                enabled = false
                            )

                            icMaintenanceList.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.padding(start = 6.dp)) {
                                            Text(user.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                            Text("IC Service Maintenance", fontSize = 10.sp, color = Color(0xFF9CA3AF))
                                        }
                                    },
                                    onClick = {
                                        selectedUser = user
                                        showUserDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Password Section (With Checkbox toggle and empty default)
                item {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "2. Kata Sandi (Password):",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("Masukkan Password...", color = Color(0xFF9CA3AF)) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) androidx.compose.ui.text.input.VisualTransformation.None else PasswordVisualTransformation(),
                            textStyle = TextStyle(
                                color = Color(0xFF111827),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {
                                focusManager.clearFocus()
                                onLogin(selectedUser, passwordInput)
                            }),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF111827),
                                unfocusedTextColor = Color(0xFF111827),
                                focusedContainerColor = Color(0xFFFFFFFF),
                                unfocusedContainerColor = Color(0xFFFFFFFF),
                                focusedBorderColor = Color(0xFF16A34A),
                                unfocusedBorderColor = Color(0xFF9CA3AF),
                                cursorColor = Color(0xFF16A34A)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Checkbox: Tampilkan Password
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                        ) {
                            Checkbox(
                                checked = isPasswordVisible,
                                onCheckedChange = { isPasswordVisible = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFF6750A4),
                                    uncheckedColor = Color(0xFF6B7280)
                                )
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "Tampilkan Password",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color(0xFF374151),
                                modifier = Modifier.clickable { isPasswordVisible = !isPasswordVisible }
                            )
                        }

                        // Error message if any
                        uiState.loginError?.let { err ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                            ) {
                                Text(
                                    text = err,
                                    color = Color(0xFFB91C1C),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // Submit Login Button
                item {
                    Button(
                        onClick = {
                            focusManager.clearFocus()
                            onLogin(selectedUser, passwordInput)
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = VibrantPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Masuk Sebagai ${selectedUser.name.take(24)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

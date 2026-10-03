package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FosisTopBar(
    activeUser: UserEntity?,
    allUsers: List<UserEntity>,
    isOnline: Boolean,
    pendingSyncCount: Int,
    runningJobsCount: Int = 0,
    onOpenRunningJobs: (() -> Unit)? = null,
    onOpenOutlook: (() -> Unit)? = null,
    showMenuButton: Boolean = false,
    onOpenMenu: (() -> Unit)? = null,
    onToggleNetwork: () -> Unit,
    onManualSync: () -> Unit,
    onSwitchUser: (UserEntity) -> Unit,
    onLogout: () -> Unit,
    onClearData: (() -> Unit)? = null
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Surface(
        color = VibrantBackground,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (showMenuButton && onOpenMenu != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, VibrantOutlineVariant),
                            shadowElevation = 1.dp,
                            modifier = Modifier.size(42.dp)
                        ) {
                            IconButton(
                                onClick = onOpenMenu,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Buka menu navigasi",
                                    tint = VibrantOnBackground,
                                    modifier = Modifier.size(23.dp)
                                )
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(VibrantPrimaryContainer)
                            .clickable { showUserMenu = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = activeUser?.avatarInitials ?: "SI",
                            color = VibrantOnPrimaryContainer,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isOnline) Color.White else Color(0xFFFFFBEB),
                        border = BorderStroke(
                            1.dp,
                            if (isOnline) VibrantOutline else Color(0xFFFDE68A)
                        ),
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onToggleNetwork() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isOnline) Color(0xFF10B981) else Color(0xFFF59E0B))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOnline) "Online" else "Offline",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VibrantOnSurfaceVariant
                            )
                            if (pendingSyncCount > 0) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Surface(shape = CircleShape, color = VibrantPrimary) {
                                    Text(
                                        text = "$pendingSyncCount",
                                        fontSize = 10.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = { onOpenRunningJobs?.invoke() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        BadgedBox(
                            badge = {
                                if (runningJobsCount > 0) {
                                    Badge(containerColor = Color(0xFF2563EB)) {
                                        Text(
                                            "$runningJobsCount",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Pekerjaan Berjalan",
                                tint = if (runningJobsCount > 0) Color(0xFF2563EB) else VibrantOnSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Box {
                        val roleBadgeColor = when (activeUser?.role) {
                            UserRole.ADMIN -> Color(0xFF6750A4)
                            UserRole.IC_TROUBLESHOOT -> Color(0xFFB3261E)
                            UserRole.IC_MAINTENANCE -> Color(0xFF0284C7)
                            UserRole.DEPT_HEAD -> Color(0xFF0F766E)
                            else -> Color(0xFF49454F)
                        }

                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = roleBadgeColor,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { showUserMenu = true }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Ganti Akun",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activeUser?.role?.label ?: "Akun",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false },
                            modifier = Modifier.heightIn(max = 380.dp)
                        ) {
                            Text(
                                text = "Pilih Akun / Ganti Sesi (24 Akun)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantOnSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                            HorizontalDivider(color = VibrantOutlineVariant)

                            allUsers.take(8).forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = user.name,
                                                    fontWeight = if (user.email == activeUser?.email) FontWeight.Bold else FontWeight.Normal,
                                                    fontSize = 12.sp
                                                )
                                                if (user.email == activeUser?.email) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "• Aktif",
                                                        fontSize = 10.sp,
                                                        color = Color(0xFF10B981),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "${user.role.label} • ${user.email}",
                                                fontSize = 10.sp,
                                                color = Color.Gray
                                            )
                                        }
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        if (user.email != activeUser?.email) onSwitchUser(user)
                                    }
                                )
                            }

                            HorizontalDivider(color = VibrantOutlineVariant)

                            if (activeUser?.role == UserRole.ADMIN && onClearData != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "Kosongkan Semua Data (Uji Mandiri)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFDC2626)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = Color(0xFFDC2626))
                                    },
                                    onClick = {
                                        showUserMenu = false
                                        onClearData()
                                    }
                                )
                            }

                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Keluar (Logout) ke Layar Login",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = VibrantPrimary
                                    )
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Logout, contentDescription = null, tint = VibrantPrimary)
                                },
                                onClick = {
                                    showUserMenu = false
                                    onLogout()
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Field Operations",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = VibrantOnBackground,
                            fontSize = 20.sp
                        )
                    )
                    Text(
                        text = "Pengguna: ${activeUser?.name ?: "Sanderlina Imelda"} • ${activeUser?.role?.label ?: "Admin"}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = VibrantOnSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                OutlinedButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Keluar", fontSize = 10.sp)
                }
            }
        }
    }
}

package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.VibrantPrimary
import com.example.ui.viewmodel.FosisNavTab


private val DrawerBackground = Color(0xFF0B2354)
private val DrawerSelected = Color(0xFF193B76)
private val DrawerText = Color(0xFFF7F9FF)
private val DrawerMuted = Color(0xFFAFC0DF)


@Composable
fun MobileNavigationDrawer(
    activeUser: UserEntity?,
    activeTab: FosisNavTab,
    menuTabs: List<Triple<FosisNavTab, ImageVector, String>>,
    onSelectTab: (FosisNavTab) -> Unit,
    onClose: () -> Unit
) {

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(320.dp),
        color = DrawerBackground,
        shadowElevation = 12.dp
    ) {

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                )
        ) {


            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {


                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = Color.White
                ) {

                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text(
                            text = activeUser?.avatarInitials ?: "FO",
                            color = VibrantPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }


                Spacer(
                    modifier = Modifier.width(12.dp)
                )


                Column(
                    modifier = Modifier.weight(1f)
                ) {

                    Text(
                        text = "FIELD OPERATIONS",
                        color = DrawerText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )


                    Text(
                        text = "${activeUser?.name ?: "Pengguna"} • ${
                            activeUser?.role?.label ?: "Admin"
                        }",
                        color = DrawerMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }


                IconButton(
                    onClick = onClose
                ) {

                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup menu",
                        tint = DrawerText
                    )
                }
            }


            Spacer(
                modifier = Modifier.height(26.dp)
            )


            // ==========================
            // UTAMA
            // ==========================

            DrawerSectionTitle("UTAMA")


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.DASHBOARD
                },
                activeTab,
                onSelectTab
            )



            Spacer(
                modifier = Modifier.height(18.dp)
            )



            // ==========================
            // OPERATIONAL FIELD ENGINEER
            // ==========================

            DrawerSectionTitle(
                "OPERATIONAL FIELD ENGINEER"
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.TROUBLESHOOT
                },
                activeTab,
                onSelectTab
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.MAINTENANCE
                },
                activeTab,
                onSelectTab
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.ADMINISTRASI
                },
                activeTab,
                onSelectTab
            )



            Spacer(
                modifier = Modifier.height(18.dp)
            )



            // ==========================
            // OPERATIONAL TIM FIELD ENGINEER
            // ==========================

            DrawerSectionTitle(
                "OPERATIONAL TIM FIELD ENGINEER"
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.GEOLOKASI
                },
                activeTab,
                onSelectTab
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.FIELD_CHAT
                },
                activeTab,
                onSelectTab
            )



            Spacer(
                modifier = Modifier.height(18.dp)
            )



            // ==========================
            // AKSES TIM FIELD ENGINEER
            // ==========================

            DrawerSectionTitle(
                "AKSES TIM FIELD ENGINEER"
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.ADMIN_USERS
                },
                activeTab,
                onSelectTab
            )



            Spacer(
                modifier = Modifier.height(18.dp)
            )



            // ==========================
            // OPERASIONAL (PALING BAWAH)
            // ==========================

            DrawerSectionTitle(
                "OPERASIONAL"
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.PERIJINAN
                },
                activeTab,
                onSelectTab
            )


            DrawerItem(
                menuTabs.firstOrNull {
                    it.first == FosisNavTab.WORKFLOW
                },
                activeTab,
                onSelectTab
            )

        }
    }
}



@Composable
private fun DrawerSectionTitle(
    title: String
) {

    Text(
        text = title,
        color = DrawerMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(
            horizontal = 10.dp,
            vertical = 5.dp
        )
    )
}




@Composable
private fun DrawerItem(
    tab: Triple<FosisNavTab, ImageVector, String>?,
    activeTab: FosisNavTab,
    onSelectTab: (FosisNavTab) -> Unit
) {

    if (tab == null) return


    val (
        navTab,
        icon,
        label
    ) = tab


    val selected =
        activeTab == navTab



    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clickable {
                onSelectTab(navTab)
            },
        shape = RoundedCornerShape(14.dp),
        color =
            if(selected)
                DrawerSelected
            else
                Color.Transparent
    ) {


        Row(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 12.dp
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {


            Icon(
                imageVector = icon,
                contentDescription = label,
                tint =
                    if(selected)
                        Color.White
                    else
                        DrawerText,
                modifier = Modifier.size(21.dp)
            )


            Spacer(
                modifier = Modifier.width(14.dp)
            )


            Text(
                text = label,
                color =
                    if(selected)
                        Color.White
                    else
                        DrawerText,

                fontSize = 14.sp,

                fontWeight =
                    if(selected)
                        FontWeight.Bold
                    else
                        FontWeight.Medium
            )
        }
    }
}
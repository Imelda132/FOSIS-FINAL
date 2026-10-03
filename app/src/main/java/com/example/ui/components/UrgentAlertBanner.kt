package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FosisItemEntity
import com.example.ui.theme.*

@Composable
fun UrgentAlertBanner(
    urgentItem: FosisItemEntity?,
    onAccept: (FosisItemEntity) -> Unit,
    onDismiss: () -> Unit,
    onViewDetail: (FosisItemEntity) -> Unit
) {
    AnimatedVisibility(
        visible = urgentItem != null,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        if (urgentItem != null) {
            Surface(
                color = VibrantErrorContainer, // #F2B8B5
                shape = RoundedCornerShape(24.dp), // rounded-3xl
                border = androidx.compose.foundation.BorderStroke(1.dp, VibrantError), // #B3261E
                shadowElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Warning Icon in Dark Red Circle (Vibrant Palette #B3261E)
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(VibrantError),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Urgent",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Text Info
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Urgent Assignment",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = VibrantOnErrorContainer // #601410
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• ${urgentItem.ticketNo}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = VibrantOnErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        Text(
                            text = urgentItem.title,
                            fontSize = 12.sp,
                            color = VibrantOnErrorContainer,
                            maxLines = 1
                        )
                        Text(
                            text = "📍 ${urgentItem.siteLocationName} • SLA: ${urgentItem.slaStatus}",
                            fontSize = 10.sp,
                            color = VibrantOnErrorContainer.copy(alpha = 0.9f)
                        )
                    }

                    // Dispatch Button (Pill shape rounded-full)
                    Button(
                        onClick = { onAccept(urgentItem) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VibrantError,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "DISPATCH",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

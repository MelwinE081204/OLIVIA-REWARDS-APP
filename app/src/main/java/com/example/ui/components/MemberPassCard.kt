package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MembershipTier
import com.example.data.model.UserProfile

@Composable
fun MemberPassCard(
    profile: UserProfile,
    modifier: Modifier = Modifier,
    onExpandScan: () -> Unit = {}
) {
    var showBarcodeSide by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current
    var copiedNotice by remember { mutableStateOf(false) }

    val nextTier = when (profile.tier) {
        MembershipTier.BRONZE -> MembershipTier.SILVER
        MembershipTier.SILVER -> MembershipTier.GOLD
        MembershipTier.GOLD -> MembershipTier.PLATINUM
        MembershipTier.PLATINUM -> MembershipTier.DIAMOND
        MembershipTier.DIAMOND -> null
    }

    val progressToNext = if (nextTier != null) {
        val tierPointsRange = (nextTier.minPoints - profile.tier.minPoints).toFloat()
        val currentProgress = (profile.lifetimePoints - profile.tier.minPoints).toFloat()
        (currentProgress / tierPointsRange).coerceIn(0f, 1f)
    } else {
        1f
    }

    val pointsNeeded = if (nextTier != null) {
        (nextTier.minPoints - profile.lifetimePoints).coerceAtLeast(0)
    } else 0

    // Luxury Card Gradient (Deep Forest Emerald + Champagne Golden accents)
    val cardGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0D3E2C),
            Color(0xFF13523B),
            Color(0xFF09291D)
        )
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFF3D079).copy(alpha = 0.6f),
                        Color(0xFFFFFFFF).copy(alpha = 0.15f),
                        Color(0xFFB8860B).copy(alpha = 0.4f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            )
            .testTag("member_pass_card"),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(cardGradient)
                .padding(20.dp)
        ) {
            Column {
                // Header row: Brand title & VIP Tier Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFD4AF37),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.WorkspacePremium,
                                    contentDescription = "Olivia Crown",
                                    tint = Color(0xFF1B3D2F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "OLIVIA REWARDS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    letterSpacing = 2.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color(0xFFF7E7B4)
                            )
                            Text(
                                text = "MEMBER PRIVILEGE PASS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.5.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFFA5C4B4)
                            )
                        }
                    }

                    // VIP Status Chip
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(profile.tier.badgeColorHex).copy(alpha = 0.22f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            Color(profile.tier.badgeColorHex).copy(alpha = 0.8f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(profile.tier.badgeColorHex),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = profile.tier.title.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color(profile.tier.badgeColorHex)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Card Body: Flip between Balance & Barcode
                if (!showBarcodeSide) {
                    // Front side: Points, Member Name, Tier Progress
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "AVAILABLE BALANCE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    letterSpacing = 1.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = Color(0xFFC7E2D5)
                            )
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "%,d".format(profile.pointsBalance),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 38.sp
                                    ),
                                    color = Color(0xFFFFFFFF)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PTS",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFF7D56B)
                                    )
                                )
                            }
                        }

                        // Tap to scan barcode button
                        Surface(
                            onClick = { showBarcodeSide = true },
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFFFFFFF).copy(alpha = 0.15f),
                            modifier = Modifier.testTag("flip_to_barcode_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCodeScanner,
                                    contentDescription = "Scan",
                                    tint = Color(0xFFF7E7B4),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Tap to Scan",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFFFFFFF)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Tier Progress Bar
                    if (nextTier != null) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$pointsNeeded pts to ${nextTier.title}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFFC0D9CD)
                                )
                                Text(
                                    text = "${(progressToNext * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = Color(0xFFF7D56B)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { progressToNext },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = Color(0xFFF3C952),
                                trackColor = Color(0xFFFFFFFF).copy(alpha = 0.2f)
                            )
                        }
                    } else {
                        Text(
                            text = "👑 Maximum VIP Tier Achieved! Enjoy 2x bonus points.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFF7E7B4)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Member Name & ID footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = profile.name.uppercase(),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp
                                ),
                                color = Color(0xFFFFFFFF)
                            )
                            Text(
                                text = profile.memberSince,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF8EB5A2)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    clipboardManager.setText(AnnotatedString(profile.memberId))
                                    copiedNotice = true
                                }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = profile.memberId,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFE2EFE7)
                                )
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy ID",
                                tint = Color(0xFFA5C4B4),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    // Back side: Digital Barcode for In-Store Scanning
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "SCAN AT CHECKOUT REGISTER",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.5.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFFF7E7B4)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // High contrast barcode
                        BarcodeView(
                            code = profile.barcodeNumber,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onExpandScan() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Member: ${profile.memberId}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFC7E2D5)
                            )

                            Surface(
                                onClick = { showBarcodeSide = false },
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFFFFF).copy(alpha = 0.2f),
                                modifier = Modifier.testTag("flip_to_card_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.CreditCard,
                                        contentDescription = "Show Card",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Show Card",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

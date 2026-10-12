package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.RedeemedVoucher
import com.example.ui.components.BarcodeView

@Composable
fun VouchersScreen(
    vouchers: List<RedeemedVoucher>,
    onOpenVoucherDetail: (RedeemedVoucher) -> Unit,
    onUseVoucher: (Long) -> Unit,
    onNavigateToCatalog: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val activeVouchers = vouchers.filter { !it.isUsed }
    val historyVouchers = vouchers.filter { it.isUsed }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("vouchers_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp)) {
            Text(
                text = "My Vouchers & Perks",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
            )
            Text(
                text = "Present your digital vouchers to the cashier when paying",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Active (${activeVouchers.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_active_vouchers")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Used History (${historyVouchers.size})", fontWeight = FontWeight.Bold) },
                modifier = Modifier.testTag("tab_history_vouchers")
            )
        }

        val displayList = if (selectedTab == 0) activeVouchers else historyVouchers

        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ConfirmationNumber,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (selectedTab == 0) "No active vouchers right now" else "No used vouchers yet",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (selectedTab == 0) "Redeem your earned points in the catalog!" else "Vouchers you redeem in store will show here.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selectedTab == 0) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onNavigateToCatalog,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Browse Rewards Catalog")
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
            ) {
                items(displayList) { voucher ->
                    VoucherTicketCard(
                        voucher = voucher,
                        onViewDetail = { onOpenVoucherDetail(voucher) },
                        onUse = { onUseVoucher(voucher.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun VoucherTicketCard(
    voucher: RedeemedVoucher,
    onViewDetail: () -> Unit,
    onUse: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("voucher_card_${voucher.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = voucher.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Value: ${voucher.valueLabel} • ${voucher.pointsSpent} pts spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (!voucher.isUsed) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (!voucher.isUsed) "READY TO USE" else "REDEEMED",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (!voucher.isUsed) Color(0xFF2E7D32) else MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Barcode preview
            if (!voucher.isUsed) {
                BarcodeView(
                    code = voucher.voucherCode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onViewDetail() }
                )
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "CODE: ${voucher.voucherCode} (USED)",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expires: ${voucher.expiresDate}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!voucher.isUsed) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onViewDetail,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Full Code")
                        }
                        Button(
                            onClick = onUse,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("mark_used_btn_${voucher.id}")
                        ) {
                            Text("Mark Used")
                        }
                    }
                }
            }
        }
    }
}

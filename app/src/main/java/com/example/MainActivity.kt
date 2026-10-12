package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.MemberBarcodeScanDialog
import com.example.ui.components.ReceiptClaimDialog
import com.example.ui.components.RedeemConfirmDialog
import com.example.ui.components.SpinWheelDialog
import com.example.ui.components.VoucherDetailDialog
import com.example.ui.screens.EarnHubScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.RewardsCatalogScreen
import com.example.ui.screens.VouchersScreen
import com.example.ui.theme.OliviaRewardsTheme
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.RewardsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OliviaRewardsTheme {
                OliviaRewardsApp()
            }
        }
    }
}

@Composable
fun OliviaRewardsApp(viewModel: RewardsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val rewards by viewModel.rewards.collectAsState()
    val vouchers by viewModel.vouchers.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val offers by viewModel.offers.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display snackbars when message arrives
    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    // Android System Back Navigation handling
    BackHandler(enabled = uiState.currentTab != AppNavTab.HOME) {
        viewModel.selectTab(AppNavTab.HOME)
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        bottomBar = {
            NavigationBar(
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                val activeVoucherCount = vouchers.count { !it.isUsed }

                // 1. Home
                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.HOME,
                    onClick = { viewModel.selectTab(AppNavTab.HOME) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == AppNavTab.HOME) Icons.Filled.Home else Icons.Outlined.Home,
                            contentDescription = "Home"
                        )
                    },
                    label = { Text("Home", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_home")
                )

                // 2. Rewards
                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.REWARDS,
                    onClick = { viewModel.selectTab(AppNavTab.REWARDS) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == AppNavTab.REWARDS) Icons.Filled.CardGiftcard else Icons.Outlined.CardGiftcard,
                            contentDescription = "Rewards"
                        )
                    },
                    label = { Text("Rewards", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_rewards")
                )

                // 3. Earn & Play
                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.EARN,
                    onClick = { viewModel.selectTab(AppNavTab.EARN) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == AppNavTab.EARN) Icons.Filled.Stars else Icons.Outlined.Stars,
                            contentDescription = "Earn"
                        )
                    },
                    label = { Text("Earn", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_earn")
                )

                // 4. My Perks / Vouchers
                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.VOUCHERS,
                    onClick = { viewModel.selectTab(AppNavTab.VOUCHERS) },
                    icon = {
                        BadgedBox(badge = {
                            if (activeVoucherCount > 0) {
                                Badge(containerColor = MaterialTheme.colorScheme.primary) {
                                    Text("$activeVoucherCount")
                                }
                            }
                        }) {
                            Icon(
                                imageVector = if (uiState.currentTab == AppNavTab.VOUCHERS) Icons.Filled.ConfirmationNumber else Icons.Outlined.ConfirmationNumber,
                                contentDescription = "My Perks"
                            )
                        }
                    },
                    label = { Text("Perks", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_vouchers")
                )

                // 5. Profile
                NavigationBarItem(
                    selected = uiState.currentTab == AppNavTab.PROFILE,
                    onClick = { viewModel.selectTab(AppNavTab.PROFILE) },
                    icon = {
                        Icon(
                            imageVector = if (uiState.currentTab == AppNavTab.PROFILE) Icons.Filled.Person else Icons.Outlined.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile", fontWeight = FontWeight.SemiBold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tab_profile")
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                AppNavTab.HOME -> {
                    HomeScreen(
                        profile = profile,
                        rewards = rewards,
                        offers = offers,
                        transactions = transactions,
                        onNavigateTab = { viewModel.selectTab(it) },
                        onOpenScan = { viewModel.showBarcodeScan(true) },
                        onOpenSpin = { viewModel.showSpinWheel(true) },
                        onOpenReceipt = { viewModel.showReceiptClaim(true) },
                        onOpenReferral = { viewModel.selectTab(AppNavTab.EARN) },
                        onClaimStreak = { viewModel.claimDailyStreak() },
                        onRedeemReward = { viewModel.openRedeemConfirmation(it) },
                        onToggleOffer = { id, active -> viewModel.toggleOffer(id, active) }
                    )
                }
                AppNavTab.REWARDS -> {
                    RewardsCatalogScreen(
                        rewards = rewards,
                        userPoints = profile.pointsBalance,
                        selectedCategory = uiState.selectedCategory,
                        searchQuery = uiState.searchQuery,
                        onSelectCategory = { viewModel.selectCategory(it) },
                        onSearchChange = { viewModel.setSearchQuery(it) },
                        onRedeemReward = { viewModel.openRedeemConfirmation(it) }
                    )
                }
                AppNavTab.EARN -> {
                    EarnHubScreen(
                        profile = profile,
                        onClaimStreak = { viewModel.claimDailyStreak() },
                        onOpenSpin = { viewModel.showSpinWheel(true) },
                        onOpenReceipt = { viewModel.showReceiptClaim(true) },
                        onApplyReferral = { viewModel.applyReferral(it) }
                    )
                }
                AppNavTab.VOUCHERS -> {
                    VouchersScreen(
                        vouchers = vouchers,
                        onOpenVoucherDetail = { viewModel.openVoucherDetail(it) },
                        onUseVoucher = { viewModel.markVoucherAsUsed(it) },
                        onNavigateToCatalog = { viewModel.selectTab(AppNavTab.REWARDS) }
                    )
                }
                AppNavTab.PROFILE -> {
                    ProfileScreen(
                        profile = profile,
                        transactions = transactions,
                        storeLocations = viewModel.storeLocations,
                        onUpdateName = { viewModel.updateUserName(it) },
                        onResetDemo = { viewModel.resetDemoData() }
                    )
                }
            }
        }
    }

    // Modal Dialogs
    if (uiState.showSpinWheel) {
        SpinWheelDialog(
            onDismiss = { viewModel.showSpinWheel(false) },
            onPrizeWon = { pts -> viewModel.spinLuckyWheel(pts) }
        )
    }

    if (uiState.showReceiptClaim) {
        ReceiptClaimDialog(
            userTier = profile.tier,
            onDismiss = { viewModel.showReceiptClaim(false) },
            onSubmitReceipt = { code, amount -> viewModel.claimReceipt(code, amount) }
        )
    }

    if (uiState.showBarcodeScan) {
        MemberBarcodeScanDialog(
            barcodeNumber = profile.barcodeNumber,
            memberId = profile.memberId,
            memberName = profile.name,
            onDismiss = { viewModel.showBarcodeScan(false) }
        )
    }

    uiState.selectedRewardToRedeem?.let { reward ->
        RedeemConfirmDialog(
            reward = reward,
            currentPoints = profile.pointsBalance,
            onDismiss = { viewModel.openRedeemConfirmation(null) },
            onConfirm = { viewModel.redeemReward(it) }
        )
    }

    uiState.selectedVoucherDetail?.let { voucher ->
        VoucherDetailDialog(
            voucher = voucher,
            onDismiss = { viewModel.openVoucherDetail(null) },
            onMarkAsUsed = { viewModel.markVoucherAsUsed(it) }
        )
    }
}

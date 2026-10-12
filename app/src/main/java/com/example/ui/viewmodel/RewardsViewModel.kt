package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.RewardDatabase
import com.example.data.model.MembershipTier
import com.example.data.model.PointsTransaction
import com.example.data.model.RedeemedVoucher
import com.example.data.model.RewardCategory
import com.example.data.model.RewardItem
import com.example.data.model.SpecialOffer
import com.example.data.model.StoreLocation
import com.example.data.model.UserProfile
import com.example.data.repository.RewardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    HOME("Home"),
    REWARDS("Rewards"),
    EARN("Earn & Play"),
    VOUCHERS("My Perks"),
    PROFILE("Profile")
}

data class RewardsUiState(
    val currentTab: AppNavTab = AppNavTab.HOME,
    val selectedCategory: RewardCategory = RewardCategory.ALL,
    val searchQuery: String = "",
    val showSpinWheel: Boolean = false,
    val showReceiptClaim: Boolean = false,
    val showBarcodeScan: Boolean = false,
    val showReferralModal: Boolean = false,
    val selectedRewardToRedeem: RewardItem? = null,
    val selectedVoucherDetail: RedeemedVoucher? = null,
    val snackbarMessage: String? = null
)

class RewardsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: RewardsRepository

    init {
        val db = RewardDatabase.getInstance(application)
        repository = RewardsRepository(db.rewardDao())
    }

    private val _uiState = MutableStateFlow(RewardsUiState())
    val uiState: StateFlow<RewardsUiState> = _uiState.asStateFlow()

    val userProfile: StateFlow<UserProfile> = repository.userProfileFlow
        .filterNotNull()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            UserProfile(
                name = "Olivia Sterling",
                memberId = "OLV-9428-2026",
                barcodeNumber = "942820260719",
                pointsBalance = 1850,
                lifetimePoints = 2850,
                currentStreak = 4,
                lastCheckInDate = "",
                tier = MembershipTier.GOLD,
                referralCode = "OLIVIA-VIP-2026",
                email = "olivia.sterling@rewards.com",
                memberSince = "November 2024"
            )
        )

    val rewards: StateFlow<List<RewardItem>> = repository.rewardsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vouchers: StateFlow<List<RedeemedVoucher>> = repository.vouchersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<PointsTransaction>> = repository.transactionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val offers: StateFlow<List<SpecialOffer>> = repository.offersFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val storeLocations: List<StoreLocation> = repository.storeLocations

    fun selectTab(tab: AppNavTab) {
        _uiState.value = _uiState.value.copy(currentTab = tab)
    }

    fun selectCategory(category: RewardCategory) {
        _uiState.value = _uiState.value.copy(selectedCategory = category)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun showSpinWheel(show: Boolean) {
        _uiState.value = _uiState.value.copy(showSpinWheel = show)
    }

    fun showReceiptClaim(show: Boolean) {
        _uiState.value = _uiState.value.copy(showReceiptClaim = show)
    }

    fun showBarcodeScan(show: Boolean) {
        _uiState.value = _uiState.value.copy(showBarcodeScan = show)
    }

    fun showReferralModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showReferralModal = show)
    }

    fun openRedeemConfirmation(reward: RewardItem?) {
        _uiState.value = _uiState.value.copy(selectedRewardToRedeem = reward)
    }

    fun openVoucherDetail(voucher: RedeemedVoucher?) {
        _uiState.value = _uiState.value.copy(selectedVoucherDetail = voucher)
    }

    fun clearSnackbar() {
        _uiState.value = _uiState.value.copy(snackbarMessage = null)
    }

    fun redeemReward(reward: RewardItem) {
        viewModelScope.launch {
            val result = repository.redeemReward(reward)
            result.onSuccess { voucher ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "🎉 Successfully redeemed ${reward.title}! Check My Perks tab.",
                    selectedVoucherDetail = voucher
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "Redemption failed: ${err.message}"
                )
            }
        }
    }

    fun markVoucherAsUsed(voucherId: Long) {
        viewModelScope.launch {
            repository.markVoucherUsed(voucherId)
            _uiState.value = _uiState.value.copy(
                selectedVoucherDetail = null,
                snackbarMessage = "Voucher redeemed successfully in store!"
            )
        }
    }

    fun claimDailyStreak() {
        viewModelScope.launch {
            val result = repository.claimDailyStreak()
            result.onSuccess { bonusPts ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "🔥 Daily Check-in Streak Claimed! +$bonusPts Points awarded."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = err.message ?: "Could not claim daily streak."
                )
            }
        }
    }

    fun spinLuckyWheel(points: Int) {
        viewModelScope.launch {
            val won = repository.spinLuckyWheel(points)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = "🎰 Spin & Win: +$won Points added to your balance!"
            )
        }
    }

    fun claimReceipt(code: String, amount: Double) {
        viewModelScope.launch {
            val result = repository.claimReceipt(code, amount)
            result.onSuccess { pts ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "🧾 Receipt approved! +$pts points credited."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = err.message ?: "Failed to process receipt."
                )
            }
        }
    }

    fun applyReferral(code: String) {
        viewModelScope.launch {
            val result = repository.applyReferralCode(code)
            result.onSuccess { pts ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = "🎁 Referral code applied! +$pts bonus points awarded."
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    snackbarMessage = err.message ?: "Invalid referral code."
                )
            }
        }
    }

    fun toggleOffer(offerId: Long, currentActivated: Boolean) {
        viewModelScope.launch {
            repository.toggleOffer(offerId, currentActivated)
            _uiState.value = _uiState.value.copy(
                snackbarMessage = if (!currentActivated) "Offer activated on your member card!" else "Offer deactivated."
            )
        }
    }

    fun updateUserName(name: String) {
        viewModelScope.launch {
            repository.updateUserName(name)
            _uiState.value = _uiState.value.copy(snackbarMessage = "Profile updated!")
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            repository.resetDemoBalance()
            _uiState.value = _uiState.value.copy(snackbarMessage = "Rewards data restored to demo defaults.")
        }
    }
}

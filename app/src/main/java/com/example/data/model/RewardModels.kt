package com.example.data.model

enum class MembershipTier(
    val title: String,
    val minPoints: Int,
    val maxPoints: Int,
    val multiplier: Double,
    val badgeColorHex: Long,
    val perks: List<String>
) {
    BRONZE(
        title = "Bronze Member",
        minPoints = 0,
        maxPoints = 999,
        multiplier = 1.0,
        badgeColorHex = 0xFFCD7F32,
        perks = listOf(
            "1x Points on all purchases",
            "Free birthday surprise coupon",
            "Access to member app offers"
        )
    ),
    SILVER(
        title = "Silver VIP",
        minPoints = 1000,
        maxPoints = 2499,
        multiplier = 1.25,
        badgeColorHex = 0xFF9EA3A8,
        perks = listOf(
            "1.25x Points multiplier",
            "Free size upgrade on all handcrafted beverages",
            "Monthly double points day",
            "Priority customer service"
        )
    ),
    GOLD(
        title = "Gold VIP",
        minPoints = 2500,
        maxPoints = 4999,
        multiplier = 1.5,
        badgeColorHex = 0xFFD4AF37,
        perks = listOf(
            "1.5x Points multiplier",
            "Complimentary monthly bakery pastry",
            "Double points every Wednesday",
            "Exclusive seasonal menu pre-tasting",
            "Free delivery on all app orders"
        )
    ),
    PLATINUM(
        title = "Platinum Elite",
        minPoints = 5000,
        maxPoints = 9999,
        multiplier = 1.75,
        badgeColorHex = 0xFF5B7F95,
        perks = listOf(
            "1.75x Points multiplier",
            "Chef's tasting invitation each quarter",
            "Reserve seating at flagship dining rooms",
            "Free artisan coffee every Friday",
            "Dedicated VIP concierge line"
        )
    ),
    DIAMOND(
        title = "Diamond WOW",
        minPoints = 10000,
        maxPoints = 999999,
        multiplier = 2.0,
        badgeColorHex = 0xFF6B4EE8,
        perks = listOf(
            "2x Points multiplier on everything",
            "All Platinum perks included",
            "Annual $100 VIP celebration dining credit",
            "Lifetime status guarantee",
            "Exclusive private dining access"
        )
    );

    companion object {
        fun fromPoints(lifetimePoints: Int): MembershipTier {
            return entries.findLast { lifetimePoints >= it.minPoints } ?: BRONZE
        }
    }
}

enum class RewardCategory(val displayName: String) {
    ALL("All Rewards"),
    DISCOUNT("Discounts"),
    CAFE_BEVERAGE("Cafe & Drinks"),
    DINING("Bistro & Dining"),
    MERCH("Merchandise"),
    VIP_EXPERIENCES("VIP Perks")
}

data class UserProfile(
    val name: String,
    val memberId: String,
    val barcodeNumber: String,
    val pointsBalance: Int,
    val lifetimePoints: Int,
    val currentStreak: Int,
    val lastCheckInDate: String,
    val tier: MembershipTier,
    val referralCode: String,
    val email: String,
    val memberSince: String
)

data class RewardItem(
    val id: Long,
    val title: String,
    val subtitle: String,
    val description: String,
    val pointsCost: Int,
    val category: RewardCategory,
    val estimatedValue: String,
    val terms: String,
    val iconName: String,
    val isFeatured: Boolean = false
)

data class RedeemedVoucher(
    val id: Long,
    val rewardId: Long,
    val title: String,
    val pointsSpent: Int,
    val voucherCode: String,
    val redeemedDate: String,
    val expiresDate: String,
    val isUsed: Boolean,
    val category: RewardCategory,
    val valueLabel: String
)

data class PointsTransaction(
    val id: Long,
    val title: String,
    val subtitle: String,
    val pointsDelta: Int,
    val type: TransactionType,
    val formattedDate: String
)

enum class TransactionType {
    EARNED,
    REDEEMED,
    STREAK,
    SPIN_WIN,
    REFERRAL,
    BONUS
}

data class SpecialOffer(
    val id: Long,
    val title: String,
    val tag: String,
    val description: String,
    val multiplierText: String,
    val expiryText: String,
    val isActivated: Boolean
)

data class StoreLocation(
    val id: Long,
    val name: String,
    val address: String,
    val hours: String,
    val distanceMiles: Double,
    val phone: String,
    val tag: String
)

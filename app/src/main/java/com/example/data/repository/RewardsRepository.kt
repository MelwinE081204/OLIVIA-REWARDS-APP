package com.example.data.repository

import com.example.data.local.OfferEntity
import com.example.data.local.RewardDao
import com.example.data.local.RewardItemEntity
import com.example.data.local.TransactionEntity
import com.example.data.local.UserProfileEntity
import com.example.data.local.VoucherEntity
import com.example.data.local.populateInitialData
import com.example.data.model.MembershipTier
import com.example.data.model.PointsTransaction
import com.example.data.model.RedeemedVoucher
import com.example.data.model.RewardCategory
import com.example.data.model.RewardItem
import com.example.data.model.SpecialOffer
import com.example.data.model.StoreLocation
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class RewardsRepository(private val dao: RewardDao) {

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val userProfileFlow: Flow<UserProfile?> = dao.getUserProfileFlow().map { entity ->
        entity?.toDomain()
    }

    val rewardsFlow: Flow<List<RewardItem>> = dao.getAllRewardsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val vouchersFlow: Flow<List<RedeemedVoucher>> = dao.getAllVouchersFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val transactionsFlow: Flow<List<PointsTransaction>> = dao.getAllTransactionsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val offersFlow: Flow<List<SpecialOffer>> = dao.getAllOffersFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    val storeLocations: List<StoreLocation> = listOf(
        StoreLocation(
            id = 1,
            name = "Olivia Flagship Bistro & Cafe",
            address = "450 Grand Avenue, Financial Center",
            hours = "7:00 AM – 10:00 PM Daily",
            distanceMiles = 0.8,
            phone = "(555) 234-8901",
            tag = "Flagship & Roastery"
        ),
        StoreLocation(
            id = 2,
            name = "Olivia Organic Bakery & Terrace",
            address = "128 Riverwalk Boulevard",
            hours = "6:30 AM – 8:00 PM Daily",
            distanceMiles = 1.9,
            phone = "(555) 678-4321",
            tag = "Artisan Bakery"
        ),
        StoreLocation(
            id = 3,
            name = "Olivia Lounge & Wine Parlor",
            address = "902 North Uptown Heights",
            hours = "11:00 AM – 11:30 PM Tue-Sun",
            distanceMiles = 3.4,
            phone = "(555) 890-1122",
            tag = "Evening Dining"
        ),
        StoreLocation(
            id = 4,
            name = "Olivia Express Market & Juice Bar",
            address = "77 Metro Station Pavilion",
            hours = "6:00 AM – 7:00 PM Mon-Sat",
            distanceMiles = 4.7,
            phone = "(555) 432-8877",
            tag = "Grab & Go"
        )
    )

    suspend fun redeemReward(reward: RewardItem): Result<RedeemedVoucher> {
        val currentProfile = dao.getUserProfileOnce() ?: return Result.failure(Exception("Profile not found"))

        if (currentProfile.pointsBalance < reward.pointsCost) {
            return Result.failure(Exception("Insufficient points balance"))
        }

        val todayStr = dateFormatter.format(Date())
        // Expiration in 30 days
        val expiryDate = Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)
        val expiryStr = dateFormatter.format(expiryDate)
        val randomSuffix = Random.nextInt(1000, 9999)
        val voucherCode = "OLV-${reward.category.name.take(4)}-$randomSuffix"

        val newBalance = currentProfile.pointsBalance - reward.pointsCost
        val updatedProfile = currentProfile.copy(pointsBalance = newBalance)
        dao.insertOrUpdateProfile(updatedProfile)

        val voucherEntity = VoucherEntity(
            rewardId = reward.id,
            title = reward.title,
            pointsSpent = reward.pointsCost,
            voucherCode = voucherCode,
            redeemedDate = todayStr,
            expiresDate = expiryStr,
            isUsed = false,
            categoryName = reward.category.name,
            valueLabel = reward.estimatedValue
        )
        val voucherId = dao.insertVoucher(voucherEntity)

        // Log transaction
        dao.insertTransaction(
            TransactionEntity(
                title = "Redeemed: ${reward.title}",
                subtitle = "Voucher Code: $voucherCode",
                pointsDelta = -reward.pointsCost,
                typeName = TransactionType.REDEEMED.name,
                formattedDate = todayStr
            )
        )

        return Result.success(voucherEntity.copy(id = voucherId).toDomain())
    }

    suspend fun markVoucherUsed(voucherId: Long) {
        dao.markVoucherAsUsed(voucherId)
    }

    suspend fun claimDailyStreak(): Result<Int> {
        val profile = dao.getUserProfileOnce() ?: return Result.failure(Exception("Profile not found"))
        val todayStr = dateFormatter.format(Date())

        if (profile.lastCheckInDate == todayStr) {
            return Result.failure(Exception("Already claimed today's streak reward!"))
        }

        val nextStreak = profile.currentStreak + 1
        // Progressive streak reward: 25, 35, 50, 75, 100, 150, 250 (Day 7 jackpot)
        val bonusPoints = when (nextStreak % 7) {
            1 -> 25
            2 -> 35
            3 -> 50
            4 -> 75
            5 -> 100
            6 -> 150
            0 -> 250 // 7th day
            else -> 50
        }

        val newBalance = profile.pointsBalance + bonusPoints
        val newLifetime = profile.lifetimePoints + bonusPoints
        val newTier = MembershipTier.fromPoints(newLifetime)

        dao.insertOrUpdateProfile(
            profile.copy(
                pointsBalance = newBalance,
                lifetimePoints = newLifetime,
                currentStreak = nextStreak,
                lastCheckInDate = todayStr,
                tierName = newTier.name
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                title = "Day $nextStreak Check-In Streak",
                subtitle = "Daily loyalty reward claimed",
                pointsDelta = bonusPoints,
                typeName = TransactionType.STREAK.name,
                formattedDate = todayStr
            )
        )

        return Result.success(bonusPoints)
    }

    suspend fun spinLuckyWheel(pointsWon: Int): Int {
        val profile = dao.getUserProfileOnce() ?: return 0
        val todayStr = dateFormatter.format(Date())

        val newBalance = profile.pointsBalance + pointsWon
        val newLifetime = profile.lifetimePoints + pointsWon
        val newTier = MembershipTier.fromPoints(newLifetime)

        dao.insertOrUpdateProfile(
            profile.copy(
                pointsBalance = newBalance,
                lifetimePoints = newLifetime,
                tierName = newTier.name
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                title = "Spin & Win Wheel Prize",
                subtitle = "Won $pointsWon bonus loyalty points",
                pointsDelta = pointsWon,
                typeName = TransactionType.SPIN_WIN.name,
                formattedDate = todayStr
            )
        )

        return pointsWon
    }

    suspend fun claimReceipt(receiptCode: String, amount: Double): Result<Int> {
        val profile = dao.getUserProfileOnce() ?: return Result.failure(Exception("Profile not found"))
        if (receiptCode.isBlank() || amount <= 0.0) {
            return Result.failure(Exception("Please enter a valid receipt number and amount"))
        }

        val todayStr = dateFormatter.format(Date())
        val currentTier = MembershipTier.valueOf(profile.tierName)
        // Base rate: 10 points per dollar, multiplied by user tier
        val calculatedPoints = ((amount * 10) * currentTier.multiplier).toInt().coerceAtLeast(10)

        val newBalance = profile.pointsBalance + calculatedPoints
        val newLifetime = profile.lifetimePoints + calculatedPoints
        val newTier = MembershipTier.fromPoints(newLifetime)

        dao.insertOrUpdateProfile(
            profile.copy(
                pointsBalance = newBalance,
                lifetimePoints = newLifetime,
                tierName = newTier.name
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                title = "Order Receipt Claim (#${receiptCode.takeLast(6)})",
                subtitle = "$${String.format(Locale.US, "%.2f", amount)} spent • ${currentTier.multiplier}x ${currentTier.title}",
                pointsDelta = calculatedPoints,
                typeName = TransactionType.EARNED.name,
                formattedDate = todayStr
            )
        )

        return Result.success(calculatedPoints)
    }

    suspend fun applyReferralCode(code: String): Result<Int> {
        val profile = dao.getUserProfileOnce() ?: return Result.failure(Exception("Profile not found"))
        val trimmed = code.trim().uppercase(Locale.US)

        if (trimmed == profile.referralCode.uppercase(Locale.US)) {
            return Result.failure(Exception("Cannot redeem your own referral code"))
        }
        if (trimmed.length < 5) {
            return Result.failure(Exception("Invalid referral code format"))
        }

        val bonus = 500
        val todayStr = dateFormatter.format(Date())
        val newBalance = profile.pointsBalance + bonus
        val newLifetime = profile.lifetimePoints + bonus
        val newTier = MembershipTier.fromPoints(newLifetime)

        dao.insertOrUpdateProfile(
            profile.copy(
                pointsBalance = newBalance,
                lifetimePoints = newLifetime,
                tierName = newTier.name
            )
        )

        dao.insertTransaction(
            TransactionEntity(
                title = "Friend Referral Bonus ($trimmed)",
                subtitle = "Welcome referral reward credited",
                pointsDelta = bonus,
                typeName = TransactionType.REFERRAL.name,
                formattedDate = todayStr
            )
        )

        return Result.success(bonus)
    }

    suspend fun toggleOffer(offerId: Long, currentActivated: Boolean) {
        dao.updateOfferStatus(offerId, !currentActivated)
    }

    suspend fun updateUserName(name: String) {
        val profile = dao.getUserProfileOnce() ?: return
        dao.insertOrUpdateProfile(profile.copy(name = name))
    }

    suspend fun resetDemoBalance() {
        populateInitialData(dao)
    }

    // Converters
    private fun UserProfileEntity.toDomain(): UserProfile {
        val tier = runCatching { MembershipTier.valueOf(tierName) }.getOrDefault(MembershipTier.GOLD)
        return UserProfile(
            name = name,
            memberId = memberId,
            barcodeNumber = barcodeNumber,
            pointsBalance = pointsBalance,
            lifetimePoints = lifetimePoints,
            currentStreak = currentStreak,
            lastCheckInDate = lastCheckInDate,
            tier = tier,
            referralCode = referralCode,
            email = email,
            memberSince = memberSince
        )
    }

    private fun RewardItemEntity.toDomain(): RewardItem {
        val cat = runCatching { RewardCategory.valueOf(categoryName) }.getOrDefault(RewardCategory.DISCOUNT)
        return RewardItem(
            id = id,
            title = title,
            subtitle = subtitle,
            description = description,
            pointsCost = pointsCost,
            category = cat,
            estimatedValue = estimatedValue,
            terms = terms,
            iconName = iconName,
            isFeatured = isFeatured
        )
    }

    private fun VoucherEntity.toDomain(): RedeemedVoucher {
        val cat = runCatching { RewardCategory.valueOf(categoryName) }.getOrDefault(RewardCategory.DISCOUNT)
        return RedeemedVoucher(
            id = id,
            rewardId = rewardId,
            title = title,
            pointsSpent = pointsSpent,
            voucherCode = voucherCode,
            redeemedDate = redeemedDate,
            expiresDate = expiresDate,
            isUsed = isUsed,
            category = cat,
            valueLabel = valueLabel
        )
    }

    private fun TransactionEntity.toDomain(): PointsTransaction {
        val type = runCatching { TransactionType.valueOf(typeName) }.getOrDefault(TransactionType.EARNED)
        return PointsTransaction(
            id = id,
            title = title,
            subtitle = subtitle,
            pointsDelta = pointsDelta,
            type = type,
            formattedDate = formattedDate
        )
    }

    private fun OfferEntity.toDomain(): SpecialOffer {
        return SpecialOffer(
            id = id,
            title = title,
            tag = tag,
            description = description,
            multiplierText = multiplierText,
            expiryText = expiryText,
            isActivated = isActivated
        )
    }
}

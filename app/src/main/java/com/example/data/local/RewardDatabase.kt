package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserProfileEntity::class,
        RewardItemEntity::class,
        VoucherEntity::class,
        TransactionEntity::class,
        OfferEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class RewardDatabase : RoomDatabase() {
    abstract fun rewardDao(): RewardDao

    companion object {
        @Volatile
        private var INSTANCE: RewardDatabase? = null

        fun getInstance(context: Context): RewardDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RewardDatabase::class.java,
                    "olivia_rewards_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Pre-populate data when database is first created
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateInitialData(database.rewardDao())
                }
            }
        }
    }
}

suspend fun populateInitialData(dao: RewardDao) {
    // 1. Initial User Profile
    val initialProfile = UserProfileEntity(
        id = 1,
        name = "Olivia Sterling",
        memberId = "OLV-9428-2026",
        barcodeNumber = "942820260719",
        pointsBalance = 1850,
        lifetimePoints = 2850, // Gold VIP
        currentStreak = 4,
        lastCheckInDate = "",
        tierName = "GOLD",
        referralCode = "OLIVIA-VIP-2026",
        email = "olivia.sterling@rewards.com",
        memberSince = "November 2024"
    )
    dao.insertOrUpdateProfile(initialProfile)

    // 2. Initial Catalog Rewards
    val rewardsList = listOf(
        RewardItemEntity(
            title = "$5 Off Any Order",
            subtitle = "Valid in-store or mobile pickup",
            description = "Enjoy $5 off your next order of artisan pastries, brunch bowls, or organic beverages at any Olivia location.",
            pointsCost = 500,
            categoryName = "DISCOUNT",
            estimatedValue = "$5.00",
            terms = "Single use only. Minimum order $10. Cannot be combined with other coupons.",
            iconName = "discount",
            isFeatured = true
        ),
        RewardItemEntity(
            title = "Complimentary Artisan Beverage",
            subtitle = "Handcrafted coffee, matcha, or tea",
            description = "Redeem for any hot or iced handcrafted beverage in any size, including seasonal specialty lattes.",
            pointsCost = 450,
            categoryName = "CAFE_BEVERAGE",
            estimatedValue = "$6.75",
            terms = "Includes standard dairy alternatives and flavor syrups.",
            iconName = "coffee",
            isFeatured = true
        ),
        RewardItemEntity(
            title = "$10 Bistro & Dining Credit",
            subtitle = "Delicious breakfast, lunch, or dinner",
            description = "Get $10 toward your dining bill. Crafted with wholesome organic ingredients and local farm-fresh produce.",
            pointsCost = 950,
            categoryName = "DISCOUNT",
            estimatedValue = "$10.00",
            terms = "Valid for dine-in or takeaway. Applicable to food items only.",
            iconName = "restaurant",
            isFeatured = true
        ),
        RewardItemEntity(
            title = "Signature Organic Bakery Treat",
            subtitle = "Fresh morning croissant or tart",
            description = "Treat yourself to our daily baked almond croissant, cinnamon brioche, or seasonal fruit galette.",
            pointsCost = 350,
            categoryName = "CAFE_BEVERAGE",
            estimatedValue = "$4.95",
            terms = "Subject to daily pastry case availability.",
            iconName = "bakery",
            isFeatured = false
        ),
        RewardItemEntity(
            title = "$25 Olivia VIP Gift Card",
            subtitle = "Digital card stored directly in app",
            description = "A versatile $25 credit loaded to your digital account balance for any dining, cafe, or merchandise purchases.",
            pointsCost = 2200,
            categoryName = "DISCOUNT",
            estimatedValue = "$25.00",
            terms = "No expiration date. Transferable to friends or family.",
            iconName = "card",
            isFeatured = true
        ),
        RewardItemEntity(
            title = "Olivia Canvas Tote & Steel Tumbler",
            subtitle = "Eco-friendly sustainable merchandise",
            description = "High quality 16oz double-wall insulated tumbler with bamboo lid paired with a heavy-duty organic canvas tote.",
            pointsCost = 1600,
            categoryName = "MERCH",
            estimatedValue = "$32.00",
            terms = "Available for pickup at all flagship locations.",
            iconName = "merch",
            isFeatured = false
        ),
        RewardItemEntity(
            title = "Chef's 3-Course Tasting Dinner for 2",
            subtitle = "Curated culinary experience",
            description = "An unforgettable evening featuring our chef's seasonal tasting menu paired with house mocktails or wines.",
            pointsCost = 4200,
            categoryName = "VIP_EXPERIENCES",
            estimatedValue = "$120.00",
            terms = "Reservation required 48 hours in advance. Weekend blackout dates may apply.",
            iconName = "stars",
            isFeatured = true
        ),
        RewardItemEntity(
            title = "Priority Weekend Brunch Reservation",
            subtitle = "Skip the waitlist with VIP table",
            description = "Instant VIP table seating during prime weekend brunch hours (10:00 AM - 2:00 PM) plus complimentary welcome mimosas.",
            pointsCost = 750,
            categoryName = "VIP_EXPERIENCES",
            estimatedValue = "$20.00",
            terms = "For parties up to 4 guests. Must book at least 2 hours ahead.",
            iconName = "event",
            isFeatured = false
        )
    )
    dao.insertRewards(rewardsList)

    // 3. Initial Active Voucher
    val initialVoucher = VoucherEntity(
        rewardId = 1,
        title = "$5 Off Any Order",
        pointsSpent = 500,
        voucherCode = "OLV-DISC-8819",
        redeemedDate = "Oct 10, 2026",
        expiresDate = "Nov 10, 2026",
        isUsed = false,
        categoryName = "DISCOUNT",
        valueLabel = "$5.00 OFF"
    )
    dao.insertVoucher(initialVoucher)

    // 4. Initial Points Transactions
    val initialTransactions = listOf(
        TransactionEntity(
            title = "Welcome Loyalty Bonus",
            subtitle = "Account activation perk",
            pointsDelta = 500,
            typeName = "BONUS",
            formattedDate = "Oct 01, 2026"
        ),
        TransactionEntity(
            title = "Dining Visit • Downtown Flagship",
            subtitle = "Order #48291 • Earned 1.5x VIP rate",
            pointsDelta = 320,
            typeName = "EARNED",
            formattedDate = "Oct 04, 2026"
        ),
        TransactionEntity(
            title = "Spin & Win Wheel Prize",
            subtitle = "Daily lucky wheel spin",
            pointsDelta = 250,
            typeName = "SPIN_WIN",
            formattedDate = "Oct 07, 2026"
        ),
        TransactionEntity(
            title = "Redeemed $5 Order Voucher",
            subtitle = "Saved into My Vouchers",
            pointsDelta = -500,
            typeName = "REDEEMED",
            formattedDate = "Oct 10, 2026"
        ),
        TransactionEntity(
            title = "4-Day Login Streak Bonus",
            subtitle = "Consistent loyalty check-ins",
            pointsDelta = 120,
            typeName = "STREAK",
            formattedDate = "Oct 11, 2026"
        )
    )
    initialTransactions.forEach { dao.insertTransaction(it) }

    // 5. Special Promotional Offers
    val initialOffers = listOf(
        OfferEntity(
            id = 1,
            title = "Double Points Wednesdays",
            tag = "2x Multiplier",
            description = "Earn 20 points per dollar spent on every beverage & bistro order all day Wednesday.",
            multiplierText = "2X PTS",
            expiryText = "Active Every Wed",
            isActivated = true
        ),
        OfferEntity(
            id = 2,
            title = "Birthday Celebration Gift",
            tag = "VIP Perk",
            description = "Enjoy a free artisan dessert or specialty flight during your birthday week.",
            multiplierText = "FREE GIFT",
            expiryText = "Valid this month",
            isActivated = true
        ),
        OfferEntity(
            id = 3,
            title = "Weekend Morning Early Bird",
            tag = "Bonus +150",
            description = "Visit any Olivia location before 9:30 AM on Saturday or Sunday for an extra 150 points.",
            multiplierText = "+150 PTS",
            expiryText = "Sat & Sun Only",
            isActivated = false
        ),
        OfferEntity(
            id = 4,
            title = "Invite Friends & Earn",
            tag = "Referral Bonus",
            description = "Share your personal VIP code. You both receive 500 points when they make their first order.",
            multiplierText = "+500 PTS",
            expiryText = "No Expiry",
            isActivated = true
        )
    )
    dao.insertOffers(initialOffers)
}

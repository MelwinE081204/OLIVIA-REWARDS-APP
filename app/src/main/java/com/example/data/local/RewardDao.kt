package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {

    // User Profile
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfileEntity?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfileOnce(): UserProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: UserProfileEntity)

    // Rewards Catalog
    @Query("SELECT * FROM rewards ORDER BY pointsCost ASC")
    fun getAllRewardsFlow(): Flow<List<RewardItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<RewardItemEntity>)

    @Query("SELECT COUNT(*) FROM rewards")
    suspend fun getRewardCount(): Int

    // Vouchers
    @Query("SELECT * FROM vouchers ORDER BY id DESC")
    fun getAllVouchersFlow(): Flow<List<VoucherEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVoucher(voucher: VoucherEntity): Long

    @Query("UPDATE vouchers SET isUsed = 1 WHERE id = :voucherId")
    suspend fun markVoucherAsUsed(voucherId: Long)

    // Transactions History
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactionsFlow(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    // Offers
    @Query("SELECT * FROM offers")
    fun getAllOffersFlow(): Flow<List<OfferEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOffers(offers: List<OfferEntity>)

    @Query("UPDATE offers SET isActivated = :activated WHERE id = :offerId")
    suspend fun updateOfferStatus(offerId: Long, activated: Boolean)

    @Query("SELECT COUNT(*) FROM offers")
    suspend fun getOfferCount(): Int
}

package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val memberId: String,
    val barcodeNumber: String,
    val pointsBalance: Int,
    val lifetimePoints: Int,
    val currentStreak: Int,
    val lastCheckInDate: String,
    val tierName: String,
    val referralCode: String,
    val email: String,
    val memberSince: String
)

@Entity(tableName = "rewards")
data class RewardItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val description: String,
    val pointsCost: Int,
    val categoryName: String,
    val estimatedValue: String,
    val terms: String,
    val iconName: String,
    val isFeatured: Boolean
)

@Entity(tableName = "vouchers")
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rewardId: Long,
    val title: String,
    val pointsSpent: Int,
    val voucherCode: String,
    val redeemedDate: String,
    val expiresDate: String,
    val isUsed: Boolean,
    val categoryName: String,
    val valueLabel: String
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val subtitle: String,
    val pointsDelta: Int,
    val typeName: String,
    val formattedDate: String
)

@Entity(tableName = "offers")
data class OfferEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val tag: String,
    val description: String,
    val multiplierText: String,
    val expiryText: String,
    val isActivated: Boolean
)

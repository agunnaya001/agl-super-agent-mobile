package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wallet_accounts")
data class WalletAccountEntity(
    @PrimaryKey val address: String,
    val label: String,
    val isPrimary: Boolean = false,
    val addedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val hash: String,
    val walletAddress: String,
    val fromAddress: String,
    val toAddress: String,
    val value: String,
    val tokenSymbol: String,
    val type: String,
    val status: String,
    val blockNumber: Long,
    val gasUsedGwei: Double,
    val gasFeeUsd: Double,
    val timestamp: Long,
    val methodCalled: String? = null,
    val contractAddress: String? = null,
    val simpleExplanation: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sender: String, // "USER" or "AGENT"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val relatedAddressOrTx: String? = null,
    val riskLevel: String? = null,
    val suggestedActionsJson: String? = null
)

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val xpReward: Int,
    val aglReward: Double,
    val category: String,
    val currentProgress: Int,
    val maxProgress: Int,
    val isClaimed: Boolean = false,
    val iconEmoji: String
)

@Entity(tableName = "rewards_history")
data class RewardHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val aglAmount: Double,
    val xpAmount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "CLAIMED", "AVAILABLE", "LOCKED"
    val source: String // "QUEST", "DAILY_CHECKIN", "LESSON", "STAKING"
)

@Entity(tableName = "contract_scans")
data class ContractScanEntity(
    @PrimaryKey val contractAddress: String,
    val name: String,
    val network: String,
    val isVerified: Boolean,
    val isProxy: Boolean,
    val ownerOrAdmin: String?,
    val tokenSymbol: String?,
    val summaryExplanation: String,
    val securityRisk: String, // "LOW_CONCERN", "REVIEW", "HIGH_CONCERN"
    val securityReasonsJson: String,
    val scanTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_progress")
data class UserProgressEntity(
    @PrimaryKey val id: Int = 1,
    val currentWalletAddress: String,
    val totalXp: Long,
    val level: Int,
    val consecutiveStreakDays: Int,
    val lastCheckInTimestamp: Long,
    val completedLessonIdsCsv: String = "",
    val unlockedBadgeIdsCsv: String = "",
    val preferredNetwork: String = "Base Mainnet",
    val aiResponseStyle: String = "SIMPLE", // "SIMPLE" or "TECHNICAL"
    val notifyTx: Boolean = true,
    val notifyRewards: Boolean = true,
    val notifyQuests: Boolean = true,
    val notifySecurity: Boolean = true
)

@Entity(tableName = "app_notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // "SECURITY", "TRANSACTION", "REWARD", "MISSION", "PRICE_ALERT"
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val referenceId: String? = null
)

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tokenSymbol: String = "AGL",
    val targetPriceUsd: Double,
    val condition: String, // "ABOVE" or "BELOW"
    val note: String = "",
    val isEnabled: Boolean = true,
    val isTriggered: Boolean = false,
    val triggerCount: Int = 0,
    val lastTriggeredPriceUsd: Double? = null,
    val lastTriggeredTimestamp: Long? = null,
    val createdTimestamp: Long = System.currentTimeMillis(),
    val oracleSource: String = "Chainlink Aggregator V3 (Base)",
    val oneTimeOnly: Boolean = false
)

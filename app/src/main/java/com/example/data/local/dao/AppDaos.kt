package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.ContractScanEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.QuestEntity
import com.example.data.local.entities.RewardHistoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProgressEntity
import com.example.data.local.entities.WalletAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_accounts ORDER BY isPrimary DESC, addedTimestamp ASC")
    fun getAllWallets(): Flow<List<WalletAccountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWallet(wallet: WalletAccountEntity)

    @Query("DELETE FROM wallet_accounts WHERE address = :address")
    suspend fun deleteWallet(address: String)

    @Query("UPDATE wallet_accounts SET isPrimary = (address = :selectedAddress)")
    suspend fun setPrimaryWallet(selectedAddress: String)

    @Query("SELECT * FROM transactions WHERE walletAddress = :walletAddress ORDER BY timestamp DESC")
    fun getTransactionsForWallet(walletAddress: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<TransactionEntity>)

    @Query("SELECT * FROM transactions WHERE hash = :txHash LIMIT 1")
    suspend fun getTransactionByHash(txHash: String): TransactionEntity?
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests")
    fun getAllQuests(): Flow<List<QuestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("UPDATE quests SET currentProgress = MIN(maxProgress, currentProgress + :increment) WHERE id = :questId")
    suspend fun incrementQuestProgress(questId: String, increment: Int)

    @Query("UPDATE quests SET isClaimed = 1 WHERE id = :questId")
    suspend fun markQuestClaimed(questId: String)
}

@Dao
interface RewardDao {
    @Query("SELECT * FROM rewards_history ORDER BY timestamp DESC")
    fun getAllRewards(): Flow<List<RewardHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: RewardHistoryEntity): Long
}

@Dao
interface ContractScanDao {
    @Query("SELECT * FROM contract_scans ORDER BY scanTimestamp DESC")
    fun getAllScans(): Flow<List<ContractScanEntity>>

    @Query("SELECT * FROM contract_scans WHERE contractAddress = :address LIMIT 1")
    suspend fun getScanByAddress(address: String): ContractScanEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ContractScanEntity)
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    fun getUserProgress(): Flow<UserProgressEntity?>

    @Query("SELECT * FROM user_progress WHERE id = 1 LIMIT 1")
    suspend fun getUserProgressOnce(): UserProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProgress(progress: UserProgressEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM app_notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE app_notifications SET isRead = 1 WHERE id = :id")
    suspend fun markAsRead(id: Long)

    @Query("UPDATE app_notifications SET isRead = 1")
    suspend fun markAllAsRead()
}

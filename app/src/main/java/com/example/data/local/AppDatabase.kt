package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.ChatDao
import com.example.data.local.dao.ContractScanDao
import com.example.data.local.dao.NotificationDao
import com.example.data.local.dao.PriceAlertDao
import com.example.data.local.dao.QuestDao
import com.example.data.local.dao.RewardDao
import com.example.data.local.dao.UserProgressDao
import com.example.data.local.dao.WalletDao
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.ContractScanEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PriceAlertEntity
import com.example.data.local.entities.QuestEntity
import com.example.data.local.entities.RewardHistoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProgressEntity
import com.example.data.local.entities.WalletAccountEntity

@Database(
    entities = [
        WalletAccountEntity::class,
        TransactionEntity::class,
        ChatMessageEntity::class,
        QuestEntity::class,
        RewardHistoryEntity::class,
        ContractScanEntity::class,
        UserProgressEntity::class,
        NotificationEntity::class,
        PriceAlertEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun chatDao(): ChatDao
    abstract fun questDao(): QuestDao
    abstract fun rewardDao(): RewardDao
    abstract fun contractScanDao(): ContractScanDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun notificationDao(): NotificationDao
    abstract fun priceAlertDao(): PriceAlertDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "agl_super_agent.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}

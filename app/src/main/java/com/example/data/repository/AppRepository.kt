package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.ContractScanEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.QuestEntity
import com.example.data.local.entities.RewardHistoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProgressEntity
import com.example.data.local.entities.WalletAccountEntity
import com.example.data.model.AglEcosystemContract
import com.example.data.model.AglEcosystemStats
import com.example.data.model.BaseTransaction
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.LeaderboardUser
import com.example.data.model.LearningLesson
import com.example.data.model.PortfolioSummary
import com.example.data.model.QuestCategory
import com.example.data.model.QuestItem
import com.example.data.model.RiskLevel
import com.example.data.model.SecurityRiskReport
import com.example.data.model.SmartContractDetails
import com.example.data.model.SuperAgentTier
import com.example.data.model.TokenAsset
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.UserProfile
import com.example.data.remote.BlockchainService
import com.example.data.remote.GeminiServiceClient
import com.example.data.remote.blockchain.services.LiveWalletState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepository(private val database: AppDatabase) {

    private val geminiClient = GeminiServiceClient()

    val allWallets: Flow<List<WalletAccountEntity>> = database.walletDao().getAllWallets()
    val allTransactions: Flow<List<BaseTransaction>> = database.walletDao().getAllTransactions()
        .map { list -> list.map { it.toModel() } }
        .flowOn(Dispatchers.Default)

    val chatMessages: Flow<List<ChatMessageEntity>> = database.chatDao().getAllMessages()
    val allQuests: Flow<List<QuestItem>> = database.questDao().getAllQuests()
        .map { entities ->
            if (entities.isEmpty()) {
                LearningLessonsData.defaultQuests
            } else {
                entities.map { it.toModel() }
            }
        }
        .flowOn(Dispatchers.Default)

    val rewardsHistory: Flow<List<RewardHistoryEntity>> = database.rewardDao().getAllRewards()
    val notifications: Flow<List<NotificationEntity>> = database.notificationDao().getAllNotifications()
    val userProgress: Flow<UserProgressEntity?> = database.userProgressDao().getUserProgress()

    suspend fun initializeSeedDataIfNeeded() = withContext(Dispatchers.IO) {
        val existingWallets = database.walletDao().getAllWallets().firstOrNull()
        if (existingWallets.isNullOrEmpty()) {
            val defaultWallet = BlockchainService.DEFAULT_DEMO_WALLET
            database.walletDao().insertWallet(
                WalletAccountEntity(
                    address = defaultWallet,
                    label = "Primary Base Wallet (Watch-Only)",
                    isPrimary = true
                )
            )

            val initialTxs = BlockchainService.getInitialTransactions(defaultWallet)
            database.walletDao().insertTransactions(initialTxs.map { it.toEntity(defaultWallet) })

            // Insert initial default quests
            val questEntities = LearningLessonsData.defaultQuests.map { it.toEntity() }
            database.questDao().insertQuests(questEntities)

            // Insert initial user progress
            val initialProgress = UserProgressEntity(
                id = 1,
                currentWalletAddress = defaultWallet,
                totalXp = 12450,
                level = 24,
                consecutiveStreakDays = 7,
                lastCheckInTimestamp = System.currentTimeMillis(),
                completedLessonIdsCsv = "lesson_blockchain_fundamentals,lesson_ethereum,lesson_base_l2",
                unlockedBadgeIdsCsv = "badge_genesis,badge_security_guardian,badge_staking_master,badge_base_explorer"
            )
            database.userProgressDao().saveUserProgress(initialProgress)

            // Initial rewards history
            database.rewardDao().insertReward(
                RewardHistoryEntity(
                    title = "Daily Check-In Streak (Day 7)",
                    description = "Received 7-day streak Super Agent multiplier bonus",
                    aglAmount = 15.0,
                    xpAmount = 250,
                    status = "CLAIMED",
                    source = "DAILY_CHECKIN"
                )
            )
            database.rewardDao().insertReward(
                RewardHistoryEntity(
                    title = "Base L2 Explorer Bounty",
                    description = "Completed verified contract interactions",
                    aglAmount = 30.0,
                    xpAmount = 400,
                    status = "CLAIMED",
                    source = "QUEST"
                )
            )

            // Initial greeting from AI
            database.chatDao().insertMessage(
                ChatMessageEntity(
                    sender = "AGENT",
                    text = "Welcome to **AGL Super Agent Mobile**! I am your AI Web3 intelligence command center on Base. You can ask me to explain your transactions, analyze smart contracts, audit security risks, check AGL token stats, or guide your Web3 learning. What would you like to explore today?"
                )
            )

            // Initial notification
            database.notificationDao().insertNotification(
                NotificationEntity(
                    title = "Welcome to AGL Super Agent",
                    message = "Your Base wallet intelligence center is online. 45 AGL in unclaimed quest rewards ready!",
                    type = "REWARD",
                    referenceId = "initial_welcome"
                )
            )
        }
    }

    suspend fun getActiveWalletAddress(): String = withContext(Dispatchers.IO) {
        val progress = database.userProgressDao().getUserProgressOnce()
        progress?.currentWalletAddress ?: BlockchainService.DEFAULT_DEMO_WALLET
    }

    suspend fun setActiveWalletAddress(address: String) = withContext(Dispatchers.IO) {
        database.walletDao().setPrimaryWallet(address)
        val progress = database.userProgressDao().getUserProgressOnce()
        if (progress != null) {
            database.userProgressDao().saveUserProgress(progress.copy(currentWalletAddress = address))
        }
        val txs = BlockchainService.getInitialTransactions(address)
        database.walletDao().insertTransactions(txs.map { it.toEntity(address) })
    }

    suspend fun addWatchWallet(address: String, label: String) = withContext(Dispatchers.IO) {
        val cleanAddr = address.trim()
        database.walletDao().insertWallet(
            WalletAccountEntity(
                address = cleanAddr,
                label = label.ifBlank { "Base Wallet (${cleanAddr.take(6)}...)" },
                isPrimary = false
            )
        )
    }

    suspend fun getWalletTokens(address: String): List<TokenAsset> {
        return BlockchainService.getTokensForWallet(address)
    }

    suspend fun getLiveWalletState(address: String): LiveWalletState = withContext(Dispatchers.IO) {
        BlockchainService.walletService.getLiveWalletState(address)
    }

    suspend fun getPortfolioSummary(address: String): PortfolioSummary {
        val liveState = getLiveWalletState(address)
        val tokens = getWalletTokens(address)
        val totalVal = tokens.sumOf { it.totalValueUsd }
        val aglBal = liveState.formattedAglBalance.toDoubleOrNull() ?: 1250.45
        val wAglBal = liveState.formattedWAglBalance.toDoubleOrNull() ?: 250.0
        val creditsVal = (liveState.formattedCredits.toDoubleOrNull() ?: 1420.0).toInt()

        return PortfolioSummary(
            totalBalanceUsd = if (totalVal > 0) totalVal else 6850.20,
            change24hPercent = 6.42,
            change24hUsd = (if (totalVal > 0) totalVal else 6850.20) * 0.0642,
            aglBalance = aglBal,
            aglStaked = wAglBal,
            aglRewardsEarned = 185.0,
            aglCredits = creditsVal
        )
    }

    suspend fun getTransactionByHash(hash: String): BaseTransaction? = withContext(Dispatchers.IO) {
        val entity = database.walletDao().getTransactionByHash(hash)
        entity?.toModel()
    }

    suspend fun sendChatMessage(userText: String): ChatMessageEntity = withContext(Dispatchers.IO) {
        val userMsg = ChatMessageEntity(
            sender = "USER",
            text = userText
        )
        database.chatDao().insertMessage(userMsg)

        // Increment quest for AI inquiry
        database.questDao().incrementQuestProgress("quest_ai_explain", 1)

        val activeWallet = getActiveWalletAddress()
        val allHistory = database.chatDao().getAllMessages().first()
        val historyPairs = allHistory.map { Pair(it.sender, it.text) }

        val systemPrompt = """
            You are AGL Super Agent, an advanced AI-powered Web3 command assistant on the Base blockchain.
            Your purpose is to convert complex blockchain information into simple, clear, and actionable explanations for mobile users.
            Active wallet: $activeWallet
            Base Chain ID: 8453 (Base Mainnet)
            AGL Token: 0x892a0138cA092d6e3556F8e7d8258380D6c4A143
            You provide concise, friendly, secure guidance. Always maintain security principles: never ask for private keys or seed phrases, and clearly warn about unverified contracts.
        """.trimIndent()

        val aiResult = geminiClient.askAssistant(systemPrompt, historyPairs, userText)
        val responseText = if (aiResult.isSuccess) {
            aiResult.getOrThrow()
        } else {
            BlockchainService.generateLocalAIExplanation(userText, activeWallet)
        }

        val agentMsg = ChatMessageEntity(
            sender = "AGENT",
            text = responseText
        )
        database.chatDao().insertMessage(agentMsg)
        agentMsg
    }

    suspend fun clearChatHistory() = withContext(Dispatchers.IO) {
        database.chatDao().clearHistory()
        database.chatDao().insertMessage(
            ChatMessageEntity(
                sender = "AGENT",
                text = "Chat history cleared. How can I assist with your Base on-chain activity today?"
            )
        )
    }

    suspend fun analyzeContract(address: String): SmartContractDetails = withContext(Dispatchers.IO) {
        val details = BlockchainService.analyzeSmartContract(address)
        database.contractScanDao().insertScan(
            ContractScanEntity(
                contractAddress = details.address,
                name = details.name,
                network = details.network,
                isVerified = details.isVerified,
                isProxy = details.isProxy,
                ownerOrAdmin = details.ownerOrAdmin,
                tokenSymbol = details.tokenSymbol,
                summaryExplanation = details.summaryExplanation,
                securityRisk = details.securityRisk.name,
                securityReasonsJson = details.securityReasons.joinToString("|")
            )
        )
        database.questDao().incrementQuestProgress("quest_scan_contract", 1)
        awardXp(50)
        details
    }

    suspend fun auditSecurity(target: String): SecurityRiskReport = withContext(Dispatchers.IO) {
        val report = BlockchainService.auditSecurityTarget(target)
        database.questDao().incrementQuestProgress("quest_security_audit", 1)
        awardXp(75)

        if (report.riskLevel == RiskLevel.HIGH_CONCERN) {
            database.notificationDao().insertNotification(
                NotificationEntity(
                    title = "High Risk Contract Detected",
                    message = "Security Audit flagged ${target.take(12)}... as HIGH CONCERN. Do not approve transactions.",
                    type = "SECURITY",
                    referenceId = target
                )
            )
        }
        report
    }

    suspend fun claimQuest(questId: String): Boolean = withContext(Dispatchers.IO) {
        val quests = database.questDao().getAllQuests().first()
        val quest = quests.find { it.id == questId } ?: return@withContext false
        if (quest.currentProgress >= quest.maxProgress && !quest.isClaimed) {
            database.questDao().markQuestClaimed(questId)
            awardXp(quest.xpReward)

            database.rewardDao().insertReward(
                RewardHistoryEntity(
                    title = "Quest Reward: ${quest.title}",
                    description = "Claimed ${quest.aglReward} AGL and ${quest.xpReward} XP",
                    aglAmount = quest.aglReward,
                    xpAmount = quest.xpReward,
                    status = "CLAIMED",
                    source = "QUEST"
                )
            )

            database.notificationDao().insertNotification(
                NotificationEntity(
                    title = "Quest Completed!",
                    message = "Claimed +${quest.aglReward} AGL and +${quest.xpReward} XP for '${quest.title}'",
                    type = "REWARD",
                    referenceId = questId
                )
            )
            return@withContext true
        }
        false
    }

    suspend fun claimDailyCheckIn(): Boolean = withContext(Dispatchers.IO) {
        val progress = database.userProgressDao().getUserProgressOnce() ?: return@withContext false
        val now = System.currentTimeMillis()
        val oneDay = 86400000L
        if (now - progress.lastCheckInTimestamp > 12 * 3600000L) {
            val newStreak = progress.consecutiveStreakDays + 1
            val xpGain = 100 * (1 + (newStreak.coerceAtMost(10) * 0.1)).toInt()
            val aglGain = 5.0

            database.userProgressDao().saveUserProgress(
                progress.copy(
                    consecutiveStreakDays = newStreak,
                    lastCheckInTimestamp = now,
                    totalXp = progress.totalXp + xpGain
                )
            )

            database.rewardDao().insertReward(
                RewardHistoryEntity(
                    title = "Day $newStreak Daily Check-In Bonus",
                    description = "Earned streak multiplier reward",
                    aglAmount = aglGain,
                    xpAmount = xpGain,
                    status = "CLAIMED",
                    source = "DAILY_CHECKIN"
                )
            )

            database.questDao().incrementQuestProgress("quest_daily_checkin", 1)
            return@withContext true
        }
        false
    }

    suspend fun completeLessonQuiz(lessonId: String, score: Int, total: Int): Boolean = withContext(Dispatchers.IO) {
        val progress = database.userProgressDao().getUserProgressOnce() ?: return@withContext false
        val completedList = progress.completedLessonIdsCsv.split(",").filter { it.isNotBlank() }.toMutableSet()
        if (!completedList.contains(lessonId)) {
            completedList.add(lessonId)
            val lesson = LearningLessonsData.lessons.find { it.id == lessonId }
            val earnedXp = lesson?.xpReward ?: 100
            awardXp(earnedXp)

            database.userProgressDao().saveUserProgress(
                progress.copy(
                    completedLessonIdsCsv = completedList.joinToString(",")
                )
            )

            database.questDao().incrementQuestProgress("quest_learn_lessons", 1)

            database.rewardDao().insertReward(
                RewardHistoryEntity(
                    title = "Completed: ${lesson?.title ?: "Web3 Lesson"}",
                    description = "Scored $score/$total on quiz. Earned $earnedXp XP",
                    aglAmount = 2.0,
                    xpAmount = earnedXp,
                    status = "CLAIMED",
                    source = "LESSON"
                )
            )
            return@withContext true
        }
        false
    }

    private suspend fun awardXp(amount: Int) {
        val progress = database.userProgressDao().getUserProgressOnce() ?: return
        val newTotal = progress.totalXp + amount
        val newLevel = (newTotal / 500).toInt() + 1
        database.userProgressDao().saveUserProgress(
            progress.copy(
                totalXp = newTotal,
                level = newLevel
            )
        )
    }

    suspend fun getUserProfile(): UserProfile = withContext(Dispatchers.IO) {
        val progress = database.userProgressDao().getUserProgressOnce()
            ?: UserProgressEntity(
                currentWalletAddress = BlockchainService.DEFAULT_DEMO_WALLET,
                totalXp = 12450,
                level = 24,
                consecutiveStreakDays = 7,
                lastCheckInTimestamp = System.currentTimeMillis()
            )

        val completedLessons = progress.completedLessonIdsCsv.split(",").filter { it.isNotBlank() }.size
        val badges = LearningLessonsData.defaultBadges.map { badge ->
            val isUnlocked = progress.unlockedBadgeIdsCsv.contains(badge.id)
            badge.copy(isUnlocked = isUnlocked)
        }

        val tier = when {
            progress.totalXp >= 20000 -> SuperAgentTier.DIAMOND
            progress.totalXp >= 10000 -> SuperAgentTier.GOLD
            progress.totalXp >= 5000 -> SuperAgentTier.SILVER
            else -> SuperAgentTier.BRONZE
        }

        UserProfile(
            walletAddress = progress.currentWalletAddress,
            totalXp = progress.totalXp,
            level = progress.level,
            tier = tier,
            questsCompletedCount = 14,
            lessonsCompletedCount = completedLessons,
            contractsAnalyzedCount = 8,
            totalRewardsClaimedAgl = 185.0,
            consecutiveStreakDays = progress.consecutiveStreakDays,
            badges = badges
        )
    }

    fun getEcosystemContracts(): List<AglEcosystemContract> = BlockchainService.AGL_CONTRACTS
    fun getEcosystemStats(): AglEcosystemStats = BlockchainService.getEcosystemStats()
    fun getLeaderboard(timeframe: LeaderboardTimeframe): List<LeaderboardUser> = LearningLessonsData.getLeaderboard(timeframe)
    fun getAllLessons(): List<LearningLesson> = LearningLessonsData.lessons

    suspend fun markNotificationAsRead(id: Long) = withContext(Dispatchers.IO) {
        database.notificationDao().markAsRead(id)
    }

    suspend fun markAllNotificationsAsRead() = withContext(Dispatchers.IO) {
        database.notificationDao().markAllAsRead()
    }

    suspend fun updateSettings(
        preferredNetwork: String,
        aiResponseStyle: String,
        notifyTx: Boolean,
        notifyRewards: Boolean,
        notifyQuests: Boolean,
        notifySecurity: Boolean
    ) = withContext(Dispatchers.IO) {
        val current = database.userProgressDao().getUserProgressOnce() ?: return@withContext
        database.userProgressDao().saveUserProgress(
            current.copy(
                preferredNetwork = preferredNetwork,
                aiResponseStyle = aiResponseStyle,
                notifyTx = notifyTx,
                notifyRewards = notifyRewards,
                notifyQuests = notifyQuests,
                notifySecurity = notifySecurity
            )
        )
    }
}

// Extension mappers
fun BaseTransaction.toEntity(walletAddress: String): TransactionEntity {
    return TransactionEntity(
        hash = hash,
        walletAddress = walletAddress,
        fromAddress = fromAddress,
        toAddress = toAddress,
        value = value,
        tokenSymbol = tokenSymbol,
        type = type.name,
        status = status.name,
        blockNumber = blockNumber,
        gasUsedGwei = gasUsedGwei,
        gasFeeUsd = gasFeeUsd,
        timestamp = timestamp,
        methodCalled = methodCalled,
        contractAddress = contractAddress,
        simpleExplanation = simpleExplanation
    )
}

fun TransactionEntity.toModel(): BaseTransaction {
    val txType = try {
        TransactionType.valueOf(type)
    } catch (e: Exception) {
        TransactionType.CONTRACT_CALL
    }
    val txStatus = try {
        TransactionStatus.valueOf(status)
    } catch (e: Exception) {
        TransactionStatus.SUCCESS
    }
    return BaseTransaction(
        hash = hash,
        fromAddress = fromAddress,
        toAddress = toAddress,
        value = value,
        tokenSymbol = tokenSymbol,
        type = txType,
        status = txStatus,
        blockNumber = blockNumber,
        gasUsedGwei = gasUsedGwei,
        gasFeeUsd = gasFeeUsd,
        timestamp = timestamp,
        methodCalled = methodCalled,
        contractAddress = contractAddress,
        simpleExplanation = simpleExplanation
    )
}

fun QuestItem.toEntity(): QuestEntity {
    return QuestEntity(
        id = id,
        title = title,
        description = description,
        xpReward = xpReward,
        aglReward = aglReward,
        category = category.name,
        currentProgress = currentProgress,
        maxProgress = maxProgress,
        isClaimed = isClaimed,
        iconEmoji = iconEmoji
    )
}

fun QuestEntity.toModel(): QuestItem {
    val cat = try {
        QuestCategory.valueOf(category)
    } catch (e: Exception) {
        QuestCategory.DAILY
    }
    return QuestItem(
        id = id,
        title = title,
        description = description,
        xpReward = xpReward,
        aglReward = aglReward,
        category = cat,
        currentProgress = currentProgress,
        maxProgress = maxProgress,
        isClaimed = isClaimed,
        iconEmoji = iconEmoji
    )
}

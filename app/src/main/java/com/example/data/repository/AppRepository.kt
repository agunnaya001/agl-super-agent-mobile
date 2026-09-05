package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.ContractScanEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PriceAlertEntity
import com.example.data.local.entities.QuestEntity
import com.example.data.local.entities.RewardHistoryEntity
import com.example.data.local.entities.TransactionEntity
import com.example.data.local.entities.UserProgressEntity
import com.example.data.local.entities.WalletAccountEntity
import com.example.data.model.AglEcosystemContract
import com.example.data.model.AglEcosystemStats
import com.example.data.model.AglOraclePriceData
import com.example.data.model.BaseTransaction
import com.example.data.model.AiSuggestion
import com.example.data.model.AiSuggestionCategory
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
import com.example.ui.viewmodel.AiSubTab
import com.example.util.PriceAlertNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class AppRepository(
    private val database: AppDatabase,
    private val notificationManager: PriceAlertNotificationManager? = null
) {

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
    val allPriceAlerts: Flow<List<PriceAlertEntity>> = database.priceAlertDao().getAllAlerts()

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

            // Initial price alert thresholds
            database.priceAlertDao().insertAlert(
                PriceAlertEntity(
                    tokenSymbol = "AGL",
                    targetPriceUsd = 3.80,
                    condition = "ABOVE",
                    note = "Resistance Breakout Target (Base Ecosystem Expansion)",
                    isEnabled = true,
                    oracleSource = "Chainlink Aggregator V3 (Base)"
                )
            )
            database.priceAlertDao().insertAlert(
                PriceAlertEntity(
                    tokenSymbol = "AGL",
                    targetPriceUsd = 3.20,
                    condition = "BELOW",
                    note = "DCA Dip Accumulation Zone",
                    isEnabled = true,
                    oracleSource = "Chainlink & Aerodrome Oracle"
                )
            )
            database.priceAlertDao().insertAlert(
                PriceAlertEntity(
                    tokenSymbol = "AGL",
                    targetPriceUsd = 4.50,
                    condition = "ABOVE",
                    note = "New Cycle High Target",
                    isEnabled = false,
                    oracleSource = "Chainlink Aggregator V3 (Base)"
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
        val txsResult = BlockchainService.fetchRecentTransactions(address)
        val txs = if (txsResult.isSuccess) txsResult.getOrThrow() else BlockchainService.getInitialTransactions(address)
        database.walletDao().insertTransactions(txs.map { it.toEntity(address) })
    }

    suspend fun refreshRecentTransactions(walletAddress: String? = null): Result<List<BaseTransaction>> = withContext(Dispatchers.IO) {
        val targetWallet = walletAddress ?: getActiveWalletAddress()
        val indexResult = BlockchainService.fetchRecentTransactions(targetWallet)
        if (indexResult.isSuccess) {
            val txs = indexResult.getOrThrow()
            database.walletDao().insertTransactions(txs.map { it.toEntity(targetWallet) })
            Result.success(txs)
        } else {
            indexResult
        }
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

        // Fetch real-time on-chain state from Base contracts
        val liveState = getLiveWalletState(activeWallet)
        val latestBlock = BlockchainService.rpcService.ethBlockNumber().getOrDefault(50740000L)
        val gasPrice = BlockchainService.rpcService.ethGasPrice().getOrNull()
        val gasGwei = if (gasPrice != null) {
            "%.4f".format(gasPrice.toDouble() / 1e9)
        } else {
            "0.0012"
        }
        val governorDetails = BlockchainService.governorService.getGovernorDetails().getOrNull()

        val systemPrompt = """
            You are AGL Super Agent, an advanced AI-powered Web3 command assistant on the Base blockchain (Chain ID: 8453).
            Your purpose is to convert complex blockchain information into simple, clear, actionable, and accurate explanations for mobile users.

            REAL-TIME CONTRACT & WALLET TELEMETRY (FETCHED LIVE FROM BASE RPC):
            - Active Wallet: $activeWallet
            - Live AGL Token Balance: ${liveState.formattedAglBalance} AGL (USD Value: $${"%.2f".format((liveState.formattedAglBalance.replace(",", "").toDoubleOrNull() ?: 0.0) * 3.42)})
            - Live Staked wAGL: ${liveState.formattedWAglBalance} wAGL (Active Voting Power: ${liveState.formattedVotingPower} votes)
            - Live Base Native ETH: ${liveState.formattedEthBalance} ETH (USD Value: $${"%.2f".format((liveState.formattedEthBalance.replace(",", "").toDoubleOrNull() ?: 0.0) * 2680.50)})
            - Live AGL Compute Credits: ${liveState.formattedCredits} Credits (Contract: 0x13866F31c60822Ff70684213b9727915Ddf2c183)
            - Latest Base Block: #$latestBlock
            - Base L2 Gas Price: $gasGwei Gwei
            - Agunnaya DAO Governor: ${governorDetails?.name ?: "Agunnaya DAO"} (0x3fFCb92A17caeaAd1342DD76978b566C8aEC7010)
            - Quorum Requirement: ${governorDetails?.formattedQuorum ?: "4,000,000 wAGL"}
            - Staking Rewards APR: 18.5%
            - Verified Base Contracts:
              * AGL Token: 0xEA1221B4d80A89BD8C75248Fae7c176BD1854698 (ERC-20, 18 Decimals)
              * wAGL Votes Wrapper: 0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69
              * AGL Credits: 0x13866F31c60822Ff70684213b9727915Ddf2c183
              * Timelock Controller: 0x900D315C91D9e54F3fa3412D475009d905bf6744

            CRITICAL DIRECTIVE:
            When the user asks natural language questions like 'What is my current AGL balance?', 'What are my balances?', or asks about their voting power, gas fees, or contracts, ALWAYS resolve the answer using these exact real-time numbers fetched from the contracts. Keep responses friendly, concise, and structured with bold highlights. Never ask for private keys or seed phrases.
        """.trimIndent()

        val aiResult = geminiClient.askAssistant(systemPrompt, historyPairs, userText)
        val responseText = if (aiResult.isSuccess) {
            aiResult.getOrThrow()
        } else {
            BlockchainService.generateLocalAIExplanation(userText, activeWallet, liveState)
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

    fun getAiSuggestions(
        activeWallet: String,
        portfolio: PortfolioSummary? = null
    ): List<AiSuggestion> {
        val shortAddr = if (activeWallet.length > 10) "${activeWallet.take(6)}...${activeWallet.takeLast(4)}" else activeWallet
        val unstakedAgl = portfolio?.aglBalance ?: 1250.45
        val unstakedStr = String.format(java.util.Locale.US, "%,.0f", unstakedAgl)

        return listOf(
            AiSuggestion(
                id = "sug_check_live_balance",
                category = AiSuggestionCategory.PORTFOLIO,
                title = "Check Live AGL Contract Balance",
                description = "Query verified Base Mainnet contract (0xEA1221B4d80A89BD8C75248Fae7c176BD1854698) for your real-time AGL & wAGL balances.",
                prompt = "What is my current AGL balance?",
                badge = "Live RPC",
                impactTag = "Real-Time",
                icon = "🪙",
                targetAiTab = AiSubTab.CHAT
            ),
            AiSuggestion(
                id = "sug_stake_yield",
                category = AiSuggestionCategory.OPTIMIZATION,
                title = "Stake Idle AGL (+18.5% APY)",
                description = "You have $unstakedStr AGL available. Stake into the Governor Timelock pool to earn Base compute rewards and boost voting power.",
                prompt = "How do I stake my $unstakedStr AGL tokens in the Base Governor Timelock contract to earn the 18.5% APY?",
                badge = "+18.5% APY",
                impactTag = "High Yield",
                icon = "⚡",
                targetAiTab = AiSubTab.CHAT
            ),
            AiSuggestion(
                id = "sug_audit_allowances",
                category = AiSuggestionCategory.SECURITY,
                title = "Audit Unlimited Token Allowances",
                description = "Scan wallet $shortAddr for lingering router approvals or unverified spenders to prevent unauthorized wallet drains.",
                prompt = "Audit all token approvals and contract allowances for wallet $activeWallet on Base Mainnet and highlight any security risks.",
                badge = "Security Audit",
                impactTag = "High Priority",
                icon = "🛡️",
                targetAiTab = AiSubTab.SECURITY_AUDIT
            ),
            AiSuggestion(
                id = "sug_gas_optimization",
                category = AiSuggestionCategory.OPTIMIZATION,
                title = "Base L2 Low Gas Window (~0.001 Gwei)",
                description = "Current Base priority gas fees are minimal. Perfect window for contract deployments, batch transfers, and DEX trades.",
                prompt = "Analyze current Base network gas trends and suggest cost-saving execution strategies for my upcoming transactions.",
                badge = "Low Gas",
                impactTag = "Save 85%",
                icon = "⛽",
                targetAiTab = AiSubTab.CHAT
            ),
            AiSuggestion(
                id = "sug_governance_proposal_4",
                category = AiSuggestionCategory.GOVERNANCE,
                title = "Vote on AGL Proposal #4 (Gas Subsidy)",
                description = "Agunnaya DAO Proposal #4 proposes a 25,000 AGL grant for Base AI compute gas subsidies. Active voting concludes soon.",
                prompt = "Explain Agunnaya DAO Proposal #4 regarding AI compute gas subsidies, voting requirements, and community discussion points.",
                badge = "Governance",
                impactTag = "Active Vote",
                icon = "🏛️",
                targetAiTab = AiSubTab.CHAT
            ),
            AiSuggestion(
                id = "sug_wrap_wagL",
                category = AiSuggestionCategory.GOVERNANCE,
                title = "Wrap AGL for On-Chain Voting Power",
                description = "Convert native AGL to Wrapped AGL (wAGL at 0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69) to delegate or cast votes.",
                prompt = "How do I wrap my AGL into wAGL (0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69) to participate in on-chain Base governance?",
                badge = "Voting Power",
                impactTag = "DAO Action",
                icon = "🗳️",
                targetAiTab = AiSubTab.CHAT
            ),
            AiSuggestion(
                id = "sug_scan_timelock",
                category = AiSuggestionCategory.CONTRACT,
                title = "Analyze Timelock Controller Contract",
                description = "Inspect the Agunnaya Timelock contract (0x900D315C91D9e54F3fa3412D475009d905bf6744) to verify security controls.",
                prompt = "Perform a deep-dive security inspection on the Agunnaya Timelock contract at 0x900D315C91D9e54F3fa3412D475009d905bf6744.",
                badge = "Contract Scan",
                impactTag = "Verified",
                icon = "📜",
                targetAiTab = AiSubTab.CONTRACT_ANALYZER,
                contractAddress = "0x900D315C91D9e54F3fa3412D475009d905bf6744"
            ),
            AiSuggestion(
                id = "sug_aerodrome_lp",
                category = AiSuggestionCategory.PORTFOLIO,
                title = "Aerodrome Slipstream LP Strategy",
                description = "Explore concentrated liquidity routing for AGL/ETH on Base to optimize fee capture and earn trading revenue.",
                prompt = "Explain how to provide liquidity for AGL on Base's Aerodrome DEX and how concentrated liquidity fee tiers work.",
                badge = "Portfolio",
                impactTag = "DEX Yield",
                icon = "💎",
                targetAiTab = AiSubTab.CHAT
            ),
            AiSuggestion(
                id = "sug_smart_wallet_aa",
                category = AiSuggestionCategory.SECURITY,
                title = "Upgrade to ERC-4337 Smart Account",
                description = "Learn how Base Smart Wallets use passkeys, paymasters for gas sponsorship, and batched transaction execution.",
                prompt = "Explain how ERC-4337 Account Abstraction and Coinbase Smart Wallets work on Base, and what security benefits they provide.",
                badge = "Account Abstraction",
                impactTag = "Web3 UX",
                icon = "🔑",
                targetAiTab = AiSubTab.CHAT
            )
        )
    }

    fun getFollowUpSuggestions(lastReply: String): List<String> {
        val lower = lastReply.lowercase(java.util.Locale.ROOT)
        return when {
            lower.contains("balance") || lower.contains("token") || lower.contains("price") -> listOf(
                "How do I bridge more ETH to Base?",
                "What is the contract address of wAGL?",
                "How can I stake AGL for APY rewards?"
            )
            lower.contains("governance") || lower.contains("proposal") || lower.contains("timelock") -> listOf(
                "How does the timelock delay protect the DAO?",
                "How do I delegate my wAGL voting power?",
                "What is the quorum threshold for proposals?"
            )
            lower.contains("gas") || lower.contains("gwei") || lower.contains("fee") -> listOf(
                "Why is Base L2 so much cheaper than Ethereum?",
                "How do EIP-4844 blobs lower Base transaction costs?",
                "Can transactions be sponsored by paymasters?"
            )
            lower.contains("contract") || lower.contains("security") || lower.contains("risk") -> listOf(
                "How do I revoke an unlimited token allowance?",
                "What are common smart contract attack vectors on L2?",
                "Check if this contract is verified on Basescan"
            )
            lower.contains("quest") || lower.contains("reward") || lower.contains("xp") -> listOf(
                "How do I unlock Level 4 Super Agent tier?",
                "What are the highest-yield daily quests?",
                "How do streak multipliers boost AGL claims?"
            )
            else -> listOf(
                "Explain my last Base transaction",
                "Simulate staking 1,000 AGL tokens",
                "Audit my wallet security score"
            )
        }
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

    // =========================================================================
    // Ecosystem Contracts Data Layer
    // =========================================================================

    suspend fun getAglTokenMetadata() = withContext(Dispatchers.IO) {
        BlockchainService.aglTokenService.getMetadata().getOrNull()
    }

    suspend fun getCreditsInfo(address: String) = withContext(Dispatchers.IO) {
        BlockchainService.aglCreditsService.getCreditsInfo(address).getOrNull()
    }

    suspend fun previewCredits(amountAglWei: java.math.BigInteger) = withContext(Dispatchers.IO) {
        BlockchainService.aglCreditsService.previewCredits(amountAglWei).getOrNull()
    }

    suspend fun getWagLInfo(address: String) = withContext(Dispatchers.IO) {
        BlockchainService.wagLService.getAccountInfo(address).getOrNull()
    }

    suspend fun getStakingInfo() = withContext(Dispatchers.IO) {
        BlockchainService.aglStakingService.getStakingInfo().getOrNull()
    }

    suspend fun getStakingTiers() = withContext(Dispatchers.IO) {
        BlockchainService.aglStakingService.getStakingTiers().getOrDefault(emptyList())
    }

    suspend fun getUserStakingPositions(address: String) = withContext(Dispatchers.IO) {
        BlockchainService.aglStakingService.getUserPositions(address).getOrDefault(emptyList())
    }

    suspend fun getGovernorDetails() = withContext(Dispatchers.IO) {
        BlockchainService.governorService.getGovernorDetails().getOrNull()
    }

    suspend fun getGovernanceProposals(walletAddress: String? = null) = withContext(Dispatchers.IO) {
        val active = walletAddress ?: getActiveWalletAddress()
        BlockchainService.governorService.getProposals(active).getOrDefault(emptyList())
    }

    suspend fun getTimelockInfo() = withContext(Dispatchers.IO) {
        BlockchainService.timelockService.getTimelockInfo().getOrNull()
    }

    suspend fun runDiagnostics() = withContext(Dispatchers.IO) {
        BlockchainService.verifier.runFullDiagnostics()
    }

    fun getTxEngine(): com.example.data.remote.blockchain.tx.TransactionPipelineEngine {
        return BlockchainService.txEngine
    }

    // Price Alert & External Oracle Engine
    suspend fun fetchOraclePrice(): Result<AglOraclePriceData> = withContext(Dispatchers.IO) {
        val result = BlockchainService.aglPriceOracleService.fetchCurrentPrice()
        if (result.isSuccess) {
            val priceData = result.getOrThrow()
            checkPriceAlerts(priceData.currentPriceUsd, priceData)
        }
        result
    }

    suspend fun simulateOraclePrice(priceUsd: Double?): Result<AglOraclePriceData> = withContext(Dispatchers.IO) {
        BlockchainService.aglPriceOracleService.setSimulatedPriceOverride(priceUsd)
        val result = BlockchainService.aglPriceOracleService.fetchCurrentPrice()
        if (result.isSuccess) {
            val priceData = result.getOrThrow()
            checkPriceAlerts(priceData.currentPriceUsd, priceData)
        }
        result
    }

    suspend fun addPriceAlert(
        targetPriceUsd: Double,
        condition: String,
        note: String,
        oneTimeOnly: Boolean = false,
        oracleSource: String = "Chainlink Aggregator V3 (Base)"
    ): Long = withContext(Dispatchers.IO) {
        val alert = PriceAlertEntity(
            tokenSymbol = "AGL",
            targetPriceUsd = targetPriceUsd,
            condition = condition,
            note = note,
            isEnabled = true,
            isTriggered = false,
            oracleSource = oracleSource,
            oneTimeOnly = oneTimeOnly
        )
        val id = database.priceAlertDao().insertAlert(alert)
        val currentPrice = BlockchainService.aglPriceOracleService.getLatestCachedPrice()
        checkPriceAlerts(currentPrice.currentPriceUsd, currentPrice)
        id
    }

    suspend fun togglePriceAlert(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        database.priceAlertDao().setAlertEnabled(id, enabled)
    }

    suspend fun rearmPriceAlert(id: Long) = withContext(Dispatchers.IO) {
        database.priceAlertDao().rearmAlert(id)
        database.priceAlertDao().setAlertEnabled(id, true)
    }

    suspend fun deletePriceAlert(id: Long) = withContext(Dispatchers.IO) {
        database.priceAlertDao().deleteAlert(id)
    }

    suspend fun clearAllPriceAlerts() = withContext(Dispatchers.IO) {
        database.priceAlertDao().clearAllAlerts()
    }

    suspend fun checkPriceAlerts(
        currentPrice: Double,
        oracleData: AglOraclePriceData
    ): List<PriceAlertEntity> = withContext(Dispatchers.IO) {
        val activeAlerts = database.priceAlertDao().getActiveAlerts()
        val triggered = mutableListOf<PriceAlertEntity>()

        for (alert in activeAlerts) {
            val shouldTrigger = when (alert.condition) {
                "ABOVE" -> currentPrice >= alert.targetPriceUsd
                "BELOW" -> currentPrice <= alert.targetPriceUsd
                else -> false
            }

            if (shouldTrigger && !alert.isTriggered) {
                val now = System.currentTimeMillis()
                database.priceAlertDao().markAlertTriggered(alert.id, currentPrice, now)
                if (alert.oneTimeOnly) {
                    database.priceAlertDao().setAlertEnabled(alert.id, false)
                }

                notificationManager?.sendPriceAlertNotification(
                    alert = alert,
                    currentPrice = currentPrice,
                    oracleProvider = oracleData.oracleProvider
                )

                val conditionLabel = if (alert.condition == "ABOVE") "rises above" else "drops below"
                database.notificationDao().insertNotification(
                    NotificationEntity(
                        title = "🪙 AGL Price Alert Triggered!",
                        message = "AGL has hit $${"%.3f".format(currentPrice)} (Threshold: $conditionLabel $${"%.3f".format(alert.targetPriceUsd)}). Oracle: ${oracleData.oracleProvider}",
                        type = "PRICE_ALERT",
                        referenceId = "alert_${alert.id}_$now"
                    )
                )

                triggered.add(alert.copy(isTriggered = true, lastTriggeredPriceUsd = currentPrice))
            }
        }
        triggered
    }

    suspend fun testTriggerAlert(alertId: Long): Boolean = withContext(Dispatchers.IO) {
        val alert = database.priceAlertDao().getAlertById(alertId) ?: return@withContext false
        val oracleData = BlockchainService.aglPriceOracleService.getLatestCachedPrice()
        val simulatedPrice = if (alert.condition == "ABOVE") {
            alert.targetPriceUsd + 0.05
        } else {
            alert.targetPriceUsd - 0.05
        }
        val now = System.currentTimeMillis()
        database.priceAlertDao().markAlertTriggered(alert.id, simulatedPrice, now)
        notificationManager?.sendPriceAlertNotification(
            alert = alert,
            currentPrice = simulatedPrice,
            oracleProvider = oracleData.oracleProvider
        )
        database.notificationDao().insertNotification(
            NotificationEntity(
                title = "🪙 AGL Price Alert Test Hit!",
                message = "Test triggered: AGL threshold $${alert.targetPriceUsd} met with simulated price $${"%.3f".format(simulatedPrice)}.",
                type = "PRICE_ALERT",
                referenceId = "test_alert_${alert.id}"
            )
        )
        true
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

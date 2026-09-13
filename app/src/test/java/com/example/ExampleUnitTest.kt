package com.example

import com.example.data.model.AiSuggestionCategory
import com.example.data.remote.BlockchainService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun aiSuggestionCategories_haveValidMetadata() {
        assertEquals("All", AiSuggestionCategory.ALL.label)
        assertEquals("✨", AiSuggestionCategory.ALL.emoji)
        assertEquals("Security", AiSuggestionCategory.SECURITY.label)
        assertEquals("Gas & Yield", AiSuggestionCategory.OPTIMIZATION.label)
        assertEquals("Portfolio", AiSuggestionCategory.PORTFOLIO.label)
        assertEquals("Governance", AiSuggestionCategory.GOVERNANCE.label)
        assertEquals("Contracts", AiSuggestionCategory.CONTRACT.label)
    }

    @Test
    fun localAIExplanation_handlesKeyConcepts() {
        val balanceExplanation = BlockchainService.generateLocalAIExplanation("what is my balance", "0x1234567890abcdef1234567890abcdef12345678")
        assertTrue(balanceExplanation.contains("Base Mainnet"))
        assertTrue(balanceExplanation.contains("AGL Token"))

        val govExplanation = BlockchainService.generateLocalAIExplanation("how does governance voting work", "0x1234567890abcdef1234567890abcdef12345678")
        assertTrue(govExplanation.contains("Governor Contract"))
        assertTrue(govExplanation.contains("wAGL"))

        val secExplanation = BlockchainService.generateLocalAIExplanation("audit security risks", "0x1234567890abcdef1234567890abcdef12345678")
        assertTrue(secExplanation.contains("Security"))

        val contractExplanation = BlockchainService.generateLocalAIExplanation("analyze smart contract", "0x1234567890abcdef1234567890abcdef12345678")
        assertTrue(contractExplanation.contains("Smart Contract"))
    }

    @Test
    fun localAIExplanation_resolvesAglBalanceWithRealTimeContractData() {
        val customLiveState = com.example.data.remote.blockchain.services.LiveWalletState(
            address = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
            chainId = 8453,
            formattedAglBalance = "3,500.50",
            formattedWAglBalance = "500.00",
            formattedEthBalance = "1.2500",
            formattedCredits = "2,000.00",
            formattedVotingPower = "500.00",
            isBaseMainnet = true
        )

        val reply = BlockchainService.generateLocalAIExplanation(
            prompt = "What is my current AGL balance?",
            walletAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
            liveState = customLiveState
        )

        assertTrue(reply.contains("3,500.50 AGL"))
        assertTrue(reply.contains("500.00 wAGL"))
        assertTrue(reply.contains("0xEA1221B4d80A89BD8C75248Fae7c176BD1854698"))
        assertTrue(reply.contains("Base Mainnet"))
        assertTrue(reply.contains("ERC-20"))
    }

    @Test
    fun transactionAiSummarizer_generatesDescriptiveHumanReadableSummaries() {
        val summarizer = com.example.data.remote.blockchain.services.TransactionAiSummarizer()

        val stakeTx = com.example.data.model.BaseTransaction(
            hash = "0x123",
            fromAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
            toAddress = com.example.data.remote.blockchain.config.BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT,
            value = "250.00",
            tokenSymbol = "AGL",
            type = com.example.data.model.TransactionType.STAKE_AGL,
            status = com.example.data.model.TransactionStatus.SUCCESS,
            blockNumber = 50000000L,
            gasUsedGwei = 0.001,
            gasFeeUsd = 0.004,
            timestamp = System.currentTimeMillis()
        )
        val stakeSummary = summarizer.generateHeuristicSummary(stakeTx)
        assertTrue(stakeSummary.contains("Staked 250.00 AGL"))
        assertTrue(stakeSummary.contains("wAGL") || stakeSummary.contains("Wrapped AGL"))

        val transferInTx = com.example.data.model.BaseTransaction(
            hash = "0x456",
            fromAddress = "0x1111222233334444555566667777888899990000",
            toAddress = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e",
            value = "0.25",
            tokenSymbol = "ETH",
            type = com.example.data.model.TransactionType.TRANSFER_IN,
            status = com.example.data.model.TransactionStatus.SUCCESS,
            blockNumber = 50000100L,
            gasUsedGwei = 0.001,
            gasFeeUsd = 0.003,
            timestamp = System.currentTimeMillis()
        )
        val inSummary = summarizer.generateHeuristicSummary(transferInTx)
        assertTrue(inSummary.contains("Received 0.25 ETH"))
        assertTrue(inSummary.contains("Base L2"))
    }

    @Test
    fun transactionIndexer_providesRichTransactionsWithSummaries() {
        val indexer = com.example.data.remote.blockchain.services.BaseTransactionIndexerService()
        val mockTxs = indexer.getMockBaseTransactions("0x742d35Cc6634C0532925a3b844Bc454e4438f44e")
        assertTrue(mockTxs.isNotEmpty())
        assertTrue(mockTxs.any { it.type == com.example.data.model.TransactionType.STAKE_AGL })
        assertTrue(mockTxs.any { it.type == com.example.data.model.TransactionType.SWAP })
        assertTrue(mockTxs.any { it.type == com.example.data.model.TransactionType.CLAIM_REWARD })
    }

    @Test
    fun questsXpProgress_calculatesLevelAndProgressCorrectly() {
        val totalXp = 12450L
        val level = (totalXp / 500).toInt() + 1
        val currentLevelStartXp = (level - 1) * 500L
        val nextLevelTargetXp = level * 500L
        val xpInCurrentLevel = totalXp - currentLevelStartXp
        val xpNeededForNextLevel = nextLevelTargetXp - totalXp
        val progressRatio = (xpInCurrentLevel.toFloat() / 500f).coerceIn(0f, 1f)

        assertEquals(25, level)
        assertEquals(12000L, currentLevelStartXp)
        assertEquals(12500L, nextLevelTargetXp)
        assertEquals(450L, xpInCurrentLevel)
        assertEquals(50L, xpNeededForNextLevel)
        assertEquals(0.9f, progressRatio, 0.001f)
    }

    @Test
    fun ecosystemMilestones_haveValidRewardConfigsAndProgression() {
        val milestones = com.example.ui.components.defaultEcosystemMilestones
        assertTrue(milestones.isNotEmpty())

        val lvl25 = milestones.find { it.targetLevel == 25 }
        assertNotNull(lvl25)
        assertEquals(12500L, lvl25!!.targetXp)
        assertTrue(lvl25.aglReward > 0.0)
        assertTrue(lvl25.creditsReward > 0L)
        assertFalse(lvl25.perkTitle.isBlank())

        // Check strictly ascending target XPs
        for (i in 0 until milestones.size - 1) {
            assertTrue(milestones[i].targetXp < milestones[i + 1].targetXp)
        }
    }
}


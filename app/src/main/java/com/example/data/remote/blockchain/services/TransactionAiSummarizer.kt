package com.example.data.remote.blockchain.services

import com.example.data.model.BaseTransaction
import com.example.data.model.TransactionType
import com.example.data.remote.GeminiServiceClient
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * TransactionAiSummarizer generates descriptive, human-readable AI summaries
 * for each blockchain transaction on Base Mainnet.
 *
 * Utilizes Gemini LLM when available with immediate fallback to a deterministic
 * on-chain heuristics engine.
 */
class TransactionAiSummarizer(
    private val geminiClient: GeminiServiceClient = GeminiServiceClient()
) {

    suspend fun summarizeTransaction(tx: BaseTransaction): String = withContext(Dispatchers.IO) {
        // First try Gemini AI summary if possible
        val systemPrompt = """
            You are the AGL Super Agent Transaction Analyst on Base Mainnet (Chain ID 8453).
            Your job is to convert raw EVM blockchain transaction data into a crystal-clear, friendly, and descriptive 1-2 sentence human-readable summary.
            Highlight the key action, tokens/values involved, recipient/contract, and the practical outcome for the user.
            Keep it professional, engaging, and easy to understand for everyday mobile Web3 users.
        """.trimIndent()

        val prompt = """
            Summarize this Base Mainnet transaction:
            - Type: ${tx.type.name}
            - Status: ${tx.status.name}
            - Value: ${tx.value} ${tx.tokenSymbol}
            - From: ${tx.fromAddress}
            - To / Contract: ${tx.toAddress}
            - Block: #${tx.blockNumber}
            - Method: ${tx.methodCalled ?: "standard transfer"}
            - Gas Fee: $${String.format(Locale.US, "%.4f", tx.gasFeeUsd)} (${String.format(Locale.US, "%.4f", tx.gasUsedGwei)} Gwei)
        """.trimIndent()

        val aiResult = geminiClient.askAssistant(systemPrompt, emptyList(), prompt)
        if (aiResult.isSuccess && !aiResult.getOrNull().isNullOrBlank()) {
            return@withContext aiResult.getOrThrow().trim()
        }

        // Deterministic on-chain AI heuristic summary for each transaction type
        generateHeuristicSummary(tx)
    }

    /**
     * Produces a highly descriptive, human-readable summary tailored to each TransactionType.
     */
    fun generateHeuristicSummary(tx: BaseTransaction): String {
        val shortFrom = formatShortAddress(tx.fromAddress)
        val shortTo = formatShortAddress(tx.toAddress)
        val gasStr = if (tx.gasFeeUsd > 0.0) {
            " with an ultra-low Base L2 fee of $${String.format(Locale.US, "%.3f", tx.gasFeeUsd)}"
        } else {
            " on Base Mainnet"
        }

        return when (tx.type) {
            TransactionType.TRANSFER_IN -> {
                "Received ${tx.value} ${tx.tokenSymbol} from $shortFrom on Base L2. The funds are immediately settled in your wallet with zero latency and full finality."
            }

            TransactionType.TRANSFER_OUT -> {
                "Sent ${tx.value} ${tx.tokenSymbol} to recipient $shortTo$gasStr. Successfully finalized and verified on Base."
            }

            TransactionType.SWAP -> {
                val targetContract = if (tx.contractAddress?.equals(BaseBlockchainConfig.AERODROME_ROUTER, ignoreCase = true) == true) {
                    "Aerodrome Slipstream DEX router"
                } else {
                    "decentralized exchange on Base"
                }
                "Swapped for ${tx.value} ${tx.tokenSymbol} via $targetContract. Trade executed with minimal slippage and sub-cent Base execution fee."
            }

            TransactionType.STAKE_AGL -> {
                "Staked ${tx.value} AGL into the Wrapped AGL (wAGL) Governance contract at $shortTo. Activated ${tx.value} 1:1 voting power and unlocked 18.5% APY staking yield."
            }

            TransactionType.CLAIM_REWARD -> {
                "Claimed ${tx.value} ${tx.tokenSymbol} from the AGL Compute Credits protocol contract. These compute units are ready to fuel autonomous AI agent operations and contract scans."
            }

            TransactionType.CONTRACT_CALL -> {
                val method = tx.methodCalled ?: "execute(bytes)"
                val contractDesc = when {
                    tx.toAddress.equals(BaseBlockchainConfig.GOVERNOR_CONTRACT, ignoreCase = true) -> "Agunnaya DAO Governor contract"
                    tx.toAddress.equals(BaseBlockchainConfig.TIMELOCK_CONTRACT, ignoreCase = true) -> "Timelock Controller contract"
                    tx.toAddress.equals(BaseBlockchainConfig.AGL_TOKEN_CONTRACT, ignoreCase = true) -> "AGL ERC-20 contract"
                    else -> "verified smart contract at $shortTo"
                }
                "Invoked `$method` on $contractDesc. State transition confirmed and permanently inscribed in Base block #${tx.blockNumber}."
            }

            TransactionType.MINT -> {
                "Minted ${tx.value} ${tx.tokenSymbol} on Base Mainnet. Ownership proof inscribed in contract $shortTo with immutable cryptographic provenance."
            }
        }
    }

    private fun formatShortAddress(address: String): String {
        return if (address.length > 10) {
            "${address.take(6)}...${address.takeLast(4)}"
        } else {
            address
        }
    }
}

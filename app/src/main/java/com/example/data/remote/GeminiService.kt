package com.example.data.remote

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.content
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

data class GroundedWebSource(
    val title: String,
    val url: String
)

data class AiGroundedResponse(
    val text: String,
    val searchQueries: List<String> = emptyList(),
    val webSources: List<GroundedWebSource> = emptyList(),
    val modelUsed: String = "gemini-3.8-flash"
)

/**
 * GeminiServiceClient integrates with Firebase AI Logic (Firebase.ai) and the Gemini API
 * using App Check attestation for secure, keyless cloud AI inference.
 */
class GeminiServiceClient {
    companion object {
        private const val TAG = "GeminiServiceClient"
        const val MODEL_FLASH = "gemini-3.8-flash"
        const val MODEL_PRO = "gemini-3.8-flash"
        const val MODEL_FLASH_LITE = "gemini-3.5-flash-lite"
    }

    private fun resolveModelName(model: String): String {
        return when (model) {
            "gemini-3.5-flash", "gemini-3.8-flash" -> MODEL_FLASH
            "gemini-3.1-pro-preview", "gemini-3-pro", "gemini-4-argon" -> MODEL_FLASH
            "gemini-3.1-flash-lite-preview", "gemini-3.5-flash-lite" -> MODEL_FLASH_LITE
            else -> if (model.isNotBlank()) model else MODEL_FLASH
        }
    }

    suspend fun askAssistant(
        systemPrompt: String,
        history: List<Pair<String, String>>, // sender ("user" / "model") to text
        userPrompt: String,
        enableSearchGrounding: Boolean = true,
        model: String = MODEL_FLASH
    ): Result<AiGroundedResponse> = withContext(Dispatchers.IO) {
        val selectedModel = resolveModelName(model)
        try {
            val generativeModel = Firebase.ai.generativeModel(
                modelName = selectedModel,
                systemInstruction = if (systemPrompt.isNotBlank()) {
                    content { text(systemPrompt) }
                } else null
            )

            val historyContents = history.takeLast(10).mapNotNull { (role, text) ->
                if (text.isNotBlank()) {
                    val roleTag = if (role.equals("USER", ignoreCase = true)) "user" else "model"
                    content(roleTag) { text(text) }
                } else null
            }

            val responseText = withTimeout(60_000L) {
                if (historyContents.isNotEmpty()) {
                    val chat = generativeModel.startChat(history = historyContents)
                    val response = chat.sendMessage(userPrompt)
                    response.text
                } else {
                    val response = generativeModel.generateContent(userPrompt)
                    response.text
                }
            } ?: "No response received from Gemini AI."

            Result.success(
                AiGroundedResponse(
                    text = responseText,
                    searchQueries = emptyList(),
                    webSources = emptyList(),
                    modelUsed = selectedModel
                )
            )
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini inference timed out after 60s", e)
            Result.failure(e)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Firebase AI generation error", e)
            Result.failure(e)
        }
    }

    suspend fun auditSolidityCode(
        codeOrAddress: String,
        isSolidityCode: Boolean
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                You are the Agunnaya Super Agent Senior Smart Contract Security Auditor on Base L2 (Chain ID 8453).
                Analyze the provided Solidity smart contract code or address deeply and produce a structured vulnerability and optimization audit.

                Include these sections:
                1. 🛡️ **Executive Safety Score & Risk Level**: (Score out of 100, Level: SAFE / LOW / MEDIUM / CRITICAL)
                2. 🔍 **Threat Vectors & Vulnerabilities**: Check for Reentrancy, Unchecked External Calls, Integer Overflows, Arbitrary Transfers, Front-running / MEV sensitivity, Centralized Owner Backdoors (mint, pause, blacklist, withdrawal).
                3. 🍯 **Honeypot & Tax Analysis**: Check if buy/sell tax can be set to 100%, if transfers can be disabled, or if trading can be blocked.
                4. ⛽ **Base L2 Gas & EIP-4844 Optimizations**: Storage packing, immutable variables, calldata usage.
                5. 🛠️ **Remediation & Secure Code Snippet**: Provide concrete code corrections.
            """.trimIndent()

            val prompt = if (isSolidityCode) {
                "Audit the following Solidity Smart Contract code for vulnerabilities and security flaws:\n\n```solidity\n$codeOrAddress\n```"
            } else {
                "Perform a security audit on the smart contract address on Base Mainnet: $codeOrAddress"
            }

            val res = askAssistant(
                systemPrompt = systemPrompt,
                history = emptyList(),
                userPrompt = prompt,
                enableSearchGrounding = false,
                model = MODEL_PRO
            )

            if (res.isSuccess) {
                Result.success(res.getOrThrow().text)
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Smart contract audit failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun auditContractSecurityVulnerabilities(
        contractAddress: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                You are the AGL Super Agent Senior Smart Contract Security Auditor on Base Mainnet (Chain ID 8453, OP Stack L2).
                Your task is to analyze the smart contract address on Base Mainnet and generate a rigorous, structured vulnerability summary report.

                You must format your response with markdown:
                ### 🛡️ Smart Contract Security Vulnerability Audit
                **Target Address**: `$contractAddress`
                **Network**: Base Mainnet (Chain ID: 8453)

                #### 1. 📊 Executive Risk Assessment
                - **Safety Score**: [0-100] / 100
                - **Risk Rating**: [SAFE / LOW CONCERN / REVIEW NEEDED / HIGH CONCERN / CRITICAL]
                - **Contract Archetype**: [ERC-20 Token / Governance / DEX Pool / Vault / Custom Contract]

                #### 2. 🚨 Potential Vulnerabilities & Threat Vectors
                - **Reentrancy Protection**: State variable update ordering, checks-effects-interactions, and nonReentrant guards.
                - **Access Control & Centralized Roles**: Owner privileges, arbitrary minting, pause/unpause locks, blacklist mechanisms, or treasury withdrawal backdoors.
                - **Honeypot & Economic Traps**: Fee-on-transfer hidden taxes, max transaction/wallet limits, and liquidity drain risks.
                - **Arithmetic & Call Integrity**: Integer overflow/underflow protections, unchecked low-level calls, and delegatecall safety.
                - **Front-Running / MEV Vulnerability**: Sandwich attack susceptibility, slippage guards, and oracle manipulation risks.

                #### 3. ⚡ Base L2 Specific Considerations
                - Sequencer uptime dependencies, L1 data fee (EIP-4844 blobs) optimizations, and standard EVM opcode compatibility.

                #### 4. 💡 Actionable Security Recommendations
                - Clear, prioritized checklist for users before interacting or approving token allowances.
            """.trimIndent()

            val prompt = "Perform a smart contract security vulnerability audit for contract address on Base Mainnet: $contractAddress"

            val res = askAssistant(
                systemPrompt = systemPrompt,
                history = emptyList(),
                userPrompt = prompt,
                enableSearchGrounding = false,
                model = MODEL_PRO
            )

            if (res.isSuccess) {
                Result.success(res.getOrThrow().text)
            } else {
                val fallbackRes = askAssistant(
                    systemPrompt = systemPrompt,
                    history = emptyList(),
                    userPrompt = prompt,
                    enableSearchGrounding = false,
                    model = MODEL_FLASH
                )
                if (fallbackRes.isSuccess) {
                    Result.success(fallbackRes.getOrThrow().text)
                } else {
                    Result.failure(res.exceptionOrNull() ?: Exception("Security audit failed"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun generateDeFiPortfolioPlan(
        walletAddress: String,
        balancesSummary: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val systemPrompt = """
                You are the Agunnaya Super Agent DeFi Yield & Portfolio Strategist on Base Layer 2.
                Given the user's current token holdings and wallet balances, formulate a comprehensive, non-custodial DeFi yield and risk diversification strategy.
                
                Protocols available on Base:
                - Agunnaya Timelock Staking (18.5% APY in AGL compute rewards)
                - Aerodrome Finance Slipstream (AGL/USDC, AGL/WETH concentrated liquidity pools)
                - Wrapped AGL (wAGL) on-chain DAO governance voting
                - AGL Credits compute pipeline
                
                Provide:
                1. 📊 **Portfolio Health & Diversification Rating** (0 to 100)
                2. ⚡ **Target Asset Allocation Recommendation** (e.g. 40% Staked AGL, 30% Aerodrome LP, 20% wAGL Governance, 10% Gas ETH Reserve)
                3. 💡 **Step-by-Step Actionable Yield Boosters**
                4. ⚠️ **Risk Factors & Impermanent Loss Mitigation**
            """.trimIndent()

            val userPrompt = "Analyze my wallet ($walletAddress) token balances and formulate my optimal Base DeFi yield strategy:\n$balancesSummary"

            val res = askAssistant(
                systemPrompt = systemPrompt,
                history = emptyList(),
                userPrompt = userPrompt,
                enableSearchGrounding = true,
                model = MODEL_FLASH
            )

            if (res.isSuccess) {
                Result.success(res.getOrThrow().text)
            } else {
                Result.failure(res.exceptionOrNull() ?: Exception("Portfolio generation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchLiveBaseMarketIntelligence(query: String = "Base L2 crypto ecosystem trends"): Result<AiGroundedResponse> = withContext(Dispatchers.IO) {
        val systemPrompt = """
            You are the Agunnaya Super Agent Live Base Market Intelligence Radar.
            Provide real-time intelligence on the Base Layer 2 ecosystem, trending tokens, DEX volumes (Aerodrome, Uniswap), L2 gas fees, and Agunnaya DAO updates.
            Format with clear bullet points, emojis, and actionable insights.
        """.trimIndent()

        askAssistant(
            systemPrompt = systemPrompt,
            history = emptyList(),
            userPrompt = query,
            enableSearchGrounding = true,
            model = MODEL_FLASH
        )
    }
}

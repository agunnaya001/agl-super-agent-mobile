package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @param:Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @param:Json(name = "role") val role: String? = null,
    @param:Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GeminiGoogleSearch(
    @param:Json(name = "searchThreshold") val searchThreshold: Double? = null
)

@JsonClass(generateAdapter = true)
data class GeminiTool(
    @param:Json(name = "googleSearch") val googleSearch: GeminiGoogleSearch? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @param:Json(name = "contents") val contents: List<GeminiContent>,
    @param:Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null,
    @param:Json(name = "tools") val tools: List<GeminiTool>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiWebChunk(
    @param:Json(name = "uri") val uri: String? = null,
    @param:Json(name = "title") val title: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGroundingChunk(
    @param:Json(name = "web") val web: GeminiWebChunk? = null
)

@JsonClass(generateAdapter = true)
data class GeminiGroundingMetadata(
    @param:Json(name = "webSearchQueries") val webSearchQueries: List<String>? = null,
    @param:Json(name = "groundingChunks") val groundingChunks: List<GeminiGroundingChunk>? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @param:Json(name = "content") val content: GeminiContent?,
    @param:Json(name = "groundingMetadata") val groundingMetadata: GeminiGroundingMetadata? = null
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @param:Json(name = "candidates") val candidates: List<GeminiCandidate>?
)

data class GroundedWebSource(
    val title: String,
    val url: String
)

data class AiGroundedResponse(
    val text: String,
    val searchQueries: List<String> = emptyList(),
    val webSources: List<GroundedWebSource> = emptyList(),
    val modelUsed: String = "gemini-3.5-flash"
)

interface GeminiApiService {
    @POST("v1beta/models/{model}:generateContent")
    suspend fun generateContentDynamic(
        @Path("model") model: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse

    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

class GeminiServiceClient {
    private val apiService: GeminiApiService

    companion object {
        const val MODEL_FLASH = "gemini-3.5-flash"
        const val MODEL_PRO = "gemini-3.1-pro-preview"
        const val MODEL_FLASH_LITE = "gemini-3.1-flash-lite-preview"
    }

    init {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .build()

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

        apiService = retrofit.create(GeminiApiService::class.java)
    }

    suspend fun askAssistant(
        systemPrompt: String,
        history: List<Pair<String, String>>, // sender ("user" / "model") to text
        userPrompt: String,
        enableSearchGrounding: Boolean = true,
        model: String = MODEL_FLASH
    ): Result<AiGroundedResponse> = withContext(Dispatchers.IO) {
        try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNullOrBlank() || apiKey == "MY_GEMINI_API_KEY") {
                return@withContext Result.failure(IllegalStateException("GEMINI_API_KEY_NOT_CONFIGURED"))
            }

            val contents = mutableListOf<GeminiContent>()
            history.takeLast(6).forEach { (role, text) ->
                contents.add(
                    GeminiContent(
                        role = if (role.equals("USER", ignoreCase = true)) "user" else "model",
                        parts = listOf(GeminiPart(text = text))
                    )
                )
            }
            contents.add(
                GeminiContent(
                    role = "user",
                    parts = listOf(GeminiPart(text = userPrompt))
                )
            )

            val systemInstruction = GeminiContent(
                parts = listOf(GeminiPart(text = systemPrompt))
            )

            val tools = if (enableSearchGrounding && model == MODEL_FLASH) {
                listOf(GeminiTool(googleSearch = GeminiGoogleSearch()))
            } else {
                null
            }

            val request = GeminiRequest(
                contents = contents,
                systemInstruction = systemInstruction,
                tools = tools
            )

            val response = try {
                apiService.generateContentDynamic(model, apiKey, request)
            } catch (e: Exception) {
                // Fallback to flash without dynamic path if path is rejected
                apiService.generateContent(apiKey, request.copy(tools = null))
            }

            val candidate = response.candidates?.firstOrNull()
            val replyText = candidate?.content?.parts?.firstOrNull()?.text
                ?: "No response received from AI Super Agent."

            val searchQueries = candidate?.groundingMetadata?.webSearchQueries ?: emptyList()
            val sources = candidate?.groundingMetadata?.groundingChunks?.mapNotNull { chunk ->
                val uri = chunk.web?.uri
                val title = chunk.web?.title ?: uri
                if (!uri.isNullOrBlank() && !title.isNullOrBlank()) {
                    GroundedWebSource(title = title, url = uri)
                } else null
            }?.distinctBy { it.url } ?: emptyList()

            Result.success(
                AiGroundedResponse(
                    text = replyText,
                    searchQueries = searchQueries,
                    webSources = sources,
                    modelUsed = model
                )
            )
        } catch (e: Exception) {
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
                // Fallback to flash if pro fails or rate limited
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
            Provide real-time, Google Search grounded intelligence on the Base Layer 2 ecosystem, trending tokens, DEX volumes (Aerodrome, Uniswap), L2 gas fees, and Agunnaya DAO updates.
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

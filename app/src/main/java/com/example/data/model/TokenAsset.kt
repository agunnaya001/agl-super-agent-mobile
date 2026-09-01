package com.example.data.model

data class TokenAsset(
    val symbol: String,
    val name: String,
    val balance: Double,
    val priceUsd: Double,
    val change24h: Double,
    val iconEmoji: String,
    val contractAddress: String,
    val isNative: Boolean = false,
    val isEcosystemToken: Boolean = false
) {
    val totalValueUsd: Double get() = balance * priceUsd
}

data class PortfolioSummary(
    val totalBalanceUsd: Double,
    val change24hPercent: Double,
    val change24hUsd: Double,
    val aglBalance: Double,
    val aglStaked: Double,
    val aglRewardsEarned: Double,
    val aglCredits: Int,
    val networkName: String = "Base Mainnet",
    val chainId: Int = 8453
)

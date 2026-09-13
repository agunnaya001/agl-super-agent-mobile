package com.example.data.model

data class AglEcosystemContract(
    val id: String,
    val name: String,
    val purpose: String,
    val contractAddress: String,
    val network: String = "Base Mainnet",
    val status: String = "Active",
    val verified: Boolean = true,
    val type: String,
    val iconEmoji: String
)

data class AglEcosystemStats(
    val tokenSymbol: String = "AGL",
    val currentPriceUsd: Double = 3.42,
    val marketCapUsd: Double = 85400000.0,
    val circulatingSupply: String = "25,000,000 AGL",
    val totalStakedAgl: String = "14,820,000 AGL",
    val stakingAprPercent: Double = 18.5,
    val totalRewardsDistributedUsd: Double = 1450000.0,
    val totalActiveAgents: Long = 42890,
    val totalContractAuditsCompleted: Long = 189400
)

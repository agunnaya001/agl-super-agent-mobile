package com.example.data.model

data class AglOraclePriceData(
    val tokenSymbol: String = "AGL",
    val currentPriceUsd: Double = 3.42,
    val roundId: String = "18446744073709553210",
    val updatedAt: Long = System.currentTimeMillis(),
    val oracleProvider: String = "Chainlink Aggregator V3 (Base)",
    val contractAddress: String = "0x19273c5bF7A74E9A4B6a2D927E259Fe4d21658bE",
    val decimals: Int = 8,
    val confidenceScore: Double = 0.998,
    val change24hPercent: Double = 8.65,
    val high24hUsd: Double = 3.65,
    val low24hUsd: Double = 3.18,
    val isLive: Boolean = true,
    val latencyMs: Long = 140,
    val heartbeatSeconds: Int = 3600,
    val network: String = "Base Mainnet"
)

package com.example.data.model

enum class RiskLevel {
    LOW_CONCERN,
    REVIEW,
    HIGH_CONCERN
}

data class ContractFunction(
    val name: String,
    val isWrite: Boolean,
    val signature: String,
    val description: String,
    val isDangerous: Boolean = false
)

data class ContractEvent(
    val name: String,
    val signature: String,
    val description: String
)

data class SmartContractDetails(
    val address: String,
    val name: String,
    val network: String = "Base Mainnet (Chain ID 8453)",
    val isVerified: Boolean = true,
    val isProxy: Boolean = false,
    val implementationAddress: String? = null,
    val ownerOrAdmin: String? = null,
    val tokenSymbol: String? = null,
    val totalSupply: String? = null,
    val readFunctions: List<ContractFunction> = emptyList(),
    val writeFunctions: List<ContractFunction> = emptyList(),
    val events: List<ContractEvent> = emptyList(),
    val totalTransactions: Long = 0,
    val summaryExplanation: String = "",
    val securityRisk: RiskLevel = RiskLevel.LOW_CONCERN,
    val securityReasons: List<String> = emptyList(),
    val isAglEcosystemContract: Boolean = false
)

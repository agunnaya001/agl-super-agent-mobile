package com.example.data.model

data class SecurityRiskReport(
    val targetInput: String,
    val targetType: SecurityTargetType,
    val riskLevel: RiskLevel,
    val riskScore: Int, // 0 to 100
    val summary: String,
    val reasons: List<String>,
    val flags: List<RiskFlag>,
    val recommendations: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SecurityTargetType {
    WALLET_ADDRESS,
    CONTRACT_ADDRESS,
    TRANSACTION_HASH,
    TOKEN_APPROVAL
}

data class RiskFlag(
    val title: String,
    val description: String,
    val severity: RiskLevel
)

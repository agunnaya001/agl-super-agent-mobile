package com.example.data.model

enum class TransactionType {
    TRANSFER_IN,
    TRANSFER_OUT,
    SWAP,
    CONTRACT_CALL,
    MINT,
    STAKE_AGL,
    CLAIM_REWARD
}

enum class TransactionStatus {
    SUCCESS,
    PENDING,
    FAILED
}

data class BaseTransaction(
    val hash: String,
    val fromAddress: String,
    val toAddress: String,
    val value: String,
    val tokenSymbol: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val blockNumber: Long,
    val gasUsedGwei: Double,
    val gasFeeUsd: Double,
    val timestamp: Long,
    val methodCalled: String? = null,
    val contractAddress: String? = null,
    val simpleExplanation: String? = null
)

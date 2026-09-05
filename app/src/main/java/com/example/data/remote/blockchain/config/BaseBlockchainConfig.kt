package com.example.data.remote.blockchain.config

import com.example.data.model.AglEcosystemContract

object BaseBlockchainConfig {

    const val CHAIN_ID = 8453L
    const val NETWORK_NAME = "Base Mainnet"
    const val CURRENCY_SYMBOL = "ETH"
    const val EXPLORER_BASE_URL = "https://basescan.org"

    // Multi-RPC resilient endpoints with automatic failover
    const val PRIMARY_RPC = "https://mainnet.base.org"
    val RPC_ENDPOINTS = listOf(
        "https://mainnet.base.org",
        "https://base-rpc.publicnode.com",
        "https://1rpc.io/base",
        "https://base.llamarpc.com",
        "https://base.drpc.org"
    )

    // Production Base Mainnet Ecosystem Contracts
    const val AGL_TOKEN_CONTRACT = "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698"
    const val AGL_CREDITS_CONTRACT = "0x13866F31c60822Ff70684213b9727915Ddf2c183"
    const val AGL_VOTES_WRAPPER_CONTRACT = "0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69"
    const val STAKING_CONTRACT = "0xd4B61B4876c15e78e0275EbA52cf62D55ED5fD30"
    const val GOVERNOR_CONTRACT = "0x3fFCb92A17caeaAd1342DD76978b566C8aEC7010"
    const val TIMELOCK_CONTRACT = "0x900D315C91D9e54F3fa3412D475009d905bf6744"
    const val DEFAULT_DEMO_WALLET = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e"

    // Default reference addresses on Base
    const val USDC_CONTRACT = "0x833589fCD6eDb6E08f4c7C32D4f71b54bdA02913"
    const val CBETH_CONTRACT = "0x2Ae3F1Ec7F1F5012CFEab0185bfc7aa3cf0DEc22"
    const val AERODROME_ROUTER = "0xcF77a3Ba9A5CA399B7c97c748561549285932570"

    // Verified Ecosystem Contracts metadata
    val ECOSYSTEM_CONTRACTS = listOf(
        AglEcosystemContract(
            id = "agl_token",
            name = "Agunnaya Labs (AGL)",
            purpose = "Core utility, staking, and ecosystem currency on Base Mainnet",
            contractAddress = AGL_TOKEN_CONTRACT,
            network = NETWORK_NAME,
            status = "Active",
            verified = true,
            type = "ERC-20 Token",
            iconEmoji = "🪙"
        ),
        AglEcosystemContract(
            id = "agl_credits",
            name = "AGL Compute Credits",
            purpose = "On-chain compute and AI execution credit billing contract",
            contractAddress = AGL_CREDITS_CONTRACT,
            network = NETWORK_NAME,
            status = "Active",
            verified = true,
            type = "Compute Protocol",
            iconEmoji = "⚡"
        ),
        AglEcosystemContract(
            id = "agl_votes_wrapper",
            name = "Wrapped AGL (wAGL)",
            purpose = "ERC-20 Votes wrapper for DAO governance snapshot checkpointing",
            contractAddress = AGL_VOTES_WRAPPER_CONTRACT,
            network = NETWORK_NAME,
            status = "Active",
            verified = true,
            type = "ERC20Votes Wrapper",
            iconEmoji = "🗳️"
        ),
        AglEcosystemContract(
            id = "agl_staking",
            name = "AGL Staking",
            purpose = "Time-locked yield staking, reward pool distribution, and tier management",
            contractAddress = STAKING_CONTRACT,
            network = NETWORK_NAME,
            status = "Active",
            verified = true,
            type = "Staking Pool",
            iconEmoji = "💎"
        ),
        AglEcosystemContract(
            id = "agl_governor",
            name = "Agunnaya DAO Governor",
            purpose = "On-chain governance voting, proposal lifecycle, and quorum tracking",
            contractAddress = GOVERNOR_CONTRACT,
            network = NETWORK_NAME,
            status = "Active",
            verified = true,
            type = "DAO Governor",
            iconEmoji = "🏛️"
        ),
        AglEcosystemContract(
            id = "agl_timelock",
            name = "Timelock Controller",
            purpose = "Security timelock executing community-approved governance actions",
            contractAddress = TIMELOCK_CONTRACT,
            network = NETWORK_NAME,
            status = "Active",
            verified = true,
            type = "Timelock Controller",
            iconEmoji = "⏳"
        )
    )

    fun getExplorerAddressUrl(address: String): String {
        return "$EXPLORER_BASE_URL/address/$address"
    }

    fun getExplorerTxUrl(txHash: String): String {
        return "$EXPLORER_BASE_URL/tx/$txHash"
    }

    fun getExplorerTokenUrl(address: String): String {
        return "$EXPLORER_BASE_URL/token/$address"
    }
}

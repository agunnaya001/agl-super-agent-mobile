package com.example.data.model

enum class AiPersona(
    val id: String,
    val title: String,
    val subtitle: String,
    val emoji: String,
    val modelAlias: String,
    val systemPromptDirective: String,
    val supportsSearchGrounding: Boolean = true
) {
    DEFI_STRATEGIST(
        id = "defi_strategist",
        title = "DeFi Strategist",
        subtitle = "Aerodrome LP & Staking Yields",
        emoji = "⚡",
        modelAlias = "gemini-3.5-flash",
        systemPromptDirective = "You are the Senior Base DeFi Strategist. Focus on maximizing yield via Aerodrome pools, wAGL staking, concentrated liquidity, and minimizing impermanent loss.",
        supportsSearchGrounding = true
    ),
    BASE_ALPHA(
        id = "base_alpha",
        title = "Base Alpha & News",
        subtitle = "Live Google Search Grounding",
        emoji = "🌐",
        modelAlias = "gemini-3.5-flash",
        systemPromptDirective = "You are the Live Base Ecosystem Alpha & News Scout. Use Google Search Grounding to fetch the latest breaking crypto news, Base TVL metrics, trending tokens, and protocol upgrades.",
        supportsSearchGrounding = true
    ),
    SECURITY_SENTINEL(
        id = "security_sentinel",
        title = "Security Sentinel",
        subtitle = "Vulnerability & Drainer Defense",
        emoji = "🛡️",
        modelAlias = "gemini-3.1-pro-preview",
        systemPromptDirective = "You are the Lead Smart Contract Security Auditor and Web3 Drainer Defense Sentinel. Inspect EVM bytecode, token approvals, reentrancy vulnerabilities, honeypots, and phishing signatures.",
        supportsSearchGrounding = false
    ),
    GAS_OPTIMIZER(
        id = "gas_optimizer",
        title = "Gas & MEV Optimizer",
        subtitle = "Predictive Base L2 Gas Timing",
        emoji = "⛽",
        modelAlias = "gemini-3.1-flash-lite-preview",
        systemPromptDirective = "You are the Base L2 Gas & MEV Optimizer. Provide fast, precise fee timing suggestions, EIP-4844 blob gas analysis, and transaction batching optimizations.",
        supportsSearchGrounding = false
    ),
    WEB3_TUTOR(
        id = "web3_tutor",
        title = "Web3 Academy Tutor",
        subtitle = "Interactive Blockchain Mentor",
        emoji = "🎓",
        modelAlias = "gemini-3.5-flash",
        systemPromptDirective = "You are the Agunnaya Web3 Academy Mentor. Provide clear, educational, beginner-to-advanced explanations of smart contracts, DAOs, Layer 2 rollups, and cryptography.",
        supportsSearchGrounding = true
    )
}

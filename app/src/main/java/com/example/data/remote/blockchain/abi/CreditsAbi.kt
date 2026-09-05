package com.example.data.remote.blockchain.abi

import java.math.BigInteger

/**
 * Complete ABI definitions for AGL Credits on Base Mainnet (0x13866F31c60822Ff70684213b9727915Ddf2c183)
 */
object CreditsAbi {

    // Reads
    val SELECTOR_BURN_ADDRESS = EvmCoder.functionSelector("BURN_ADDRESS()")
    val SELECTOR_AGL_TOKEN = EvmCoder.functionSelector("aglToken()")
    val SELECTOR_CREDITS_PER_AGL = EvmCoder.functionSelector("creditsPerAGL()")
    val SELECTOR_OWNER = EvmCoder.functionSelector("owner()")
    val SELECTOR_PAUSED = EvmCoder.functionSelector("paused()")
    val SELECTOR_PREVIEW_CREDITS = EvmCoder.functionSelector("previewCredits(uint256)")
    val SELECTOR_TOTAL_AGL_BURNED = EvmCoder.functionSelector("totalAGLBurned()")
    val SELECTOR_TOTAL_AGL_BURNED_BY = EvmCoder.functionSelector("totalAGLBurnedBy(address)")
    val SELECTOR_TOTAL_CREDITS_PURCHASED = EvmCoder.functionSelector("totalCreditsPurchased(address)")

    // Writes
    val SELECTOR_PURCHASE_CREDITS = EvmCoder.functionSelector("purchaseCredits(uint256)")
    val SELECTOR_SET_CREDITS_PER_AGL = EvmCoder.functionSelector("setCreditsPerAGL(uint256)")
    val SELECTOR_PAUSE = EvmCoder.functionSelector("pause()")
    val SELECTOR_UNPAUSE = EvmCoder.functionSelector("unpause()")
    val SELECTOR_TRANSFER_OWNERSHIP = EvmCoder.functionSelector("transferOwnership(address)")
    val SELECTOR_RENOUNCE_OWNERSHIP = EvmCoder.functionSelector("renounceOwnership()")

    // Event Topics
    val TOPIC_CREDITS_PURCHASED = EvmCoder.eventTopic("CreditsPurchased(address,uint256,uint256)")
    val TOPIC_RATE_UPDATED = EvmCoder.eventTopic("RateUpdated(uint256)")
    val TOPIC_PAUSED = EvmCoder.eventTopic("Paused(address)")
    val TOPIC_UNPAUSED = EvmCoder.eventTopic("Unpaused(address)")
    val TOPIC_OWNERSHIP_TRANSFERRED = EvmCoder.eventTopic("OwnershipTransferred(address,address)")

    // Encoders
    fun encodeTotalCreditsPurchased(account: String): String {
        return SELECTOR_TOTAL_CREDITS_PURCHASED + EvmCoder.encodeAddress(account)
    }

    fun encodeTotalAglBurnedBy(account: String): String {
        return SELECTOR_TOTAL_AGL_BURNED_BY + EvmCoder.encodeAddress(account)
    }

    fun encodePreviewCredits(amount: BigInteger): String {
        return SELECTOR_PREVIEW_CREDITS + EvmCoder.encodeUint256(amount)
    }

    fun encodePurchaseCredits(amount: BigInteger): String {
        return SELECTOR_PURCHASE_CREDITS + EvmCoder.encodeUint256(amount)
    }

    fun encodeSetCreditsPerAGL(rate: BigInteger): String {
        return SELECTOR_SET_CREDITS_PER_AGL + EvmCoder.encodeUint256(rate)
    }
}

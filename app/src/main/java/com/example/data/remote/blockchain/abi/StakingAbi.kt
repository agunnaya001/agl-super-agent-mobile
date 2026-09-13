package com.example.data.remote.blockchain.abi

import java.math.BigInteger

/**
 * ABI selectors, event topics, and calldata encoders for the AGL Staking contract
 * on Base Mainnet (0xd4B61B4876c15e78e0275EbA52cf62D55ED5fD30).
 */
object StakingAbi {

    // Reads
    val SELECTOR_AGL_TOKEN = EvmCoder.functionSelector("aglToken()")
    val SELECTOR_OWNER = EvmCoder.functionSelector("owner()")
    val SELECTOR_PAUSED = EvmCoder.functionSelector("paused()")
    val SELECTOR_TOTAL_STAKED = EvmCoder.functionSelector("totalStaked()")
    val SELECTOR_REWARD_POOL_BALANCE = EvmCoder.functionSelector("rewardPoolBalance()")
    val SELECTOR_TIER_COUNT = EvmCoder.functionSelector("tierCount()")
    val SELECTOR_GET_TIER = EvmCoder.functionSelector("getTier(uint8)")
    val SELECTOR_TIERS = EvmCoder.functionSelector("tiers(uint256)")
    val SELECTOR_POSITION_COUNT = EvmCoder.functionSelector("positionCount(address)")
    val SELECTOR_GET_POSITION = EvmCoder.functionSelector("getPosition(address,uint256)")
    val SELECTOR_PENDING_REWARD = EvmCoder.functionSelector("pendingReward(address,uint256)")
    val SELECTOR_TOTAL_CLAIMABLE = EvmCoder.functionSelector("totalClaimable(address,uint256)")

    // Writes
    val SELECTOR_STAKE = EvmCoder.functionSelector("stake(uint256,uint8)")
    val SELECTOR_UNSTAKE = EvmCoder.functionSelector("unstake(uint256)")
    val SELECTOR_EMERGENCY_WITHDRAW = EvmCoder.functionSelector("emergencyWithdraw(uint256)")

    // Admin Writes
    val SELECTOR_ADD_TIER = EvmCoder.functionSelector("addTier(uint256,uint256,bool)")
    val SELECTOR_SET_TIER = EvmCoder.functionSelector("setTier(uint8,uint256,uint256,bool)")
    val SELECTOR_FUND_REWARD_POOL = EvmCoder.functionSelector("fundRewardPool(uint256)")
    val SELECTOR_WITHDRAW_REWARD_POOL = EvmCoder.functionSelector("withdrawRewardPool(uint256)")
    val SELECTOR_WITHDRAW_EXCESS_RESERVES = EvmCoder.functionSelector("withdrawExcessReserves(uint256)")
    val SELECTOR_PAUSE = EvmCoder.functionSelector("pause()")
    val SELECTOR_UNPAUSE = EvmCoder.functionSelector("unpause()")
    val SELECTOR_TRANSFER_OWNERSHIP = EvmCoder.functionSelector("transferOwnership(address)")
    val SELECTOR_RENOUNCE_OWNERSHIP = EvmCoder.functionSelector("renounceOwnership()")

    // Event Topics
    val TOPIC_STAKED = EvmCoder.eventTopic("Staked(address,uint256,uint8,uint256)")
    val TOPIC_UNSTAKED = EvmCoder.eventTopic("Unstaked(address,uint256,uint256)")
    val TOPIC_EMERGENCY_WITHDRAW = EvmCoder.eventTopic("EmergencyWithdraw(address,uint256,uint256)")
    val TOPIC_TIER_ADDED = EvmCoder.eventTopic("TierAdded(uint8,uint256,uint256)")
    val TOPIC_TIER_UPDATED = EvmCoder.eventTopic("TierUpdated(uint8,uint256,uint256,bool)")
    val TOPIC_REWARD_POOL_FUNDED = EvmCoder.eventTopic("RewardPoolFunded(address,uint256)")
    val TOPIC_REWARD_POOL_WITHDRAWN = EvmCoder.eventTopic("RewardPoolWithdrawn(address,uint256)")
    val TOPIC_OWNERSHIP_TRANSFERRED = EvmCoder.eventTopic("OwnershipTransferred(address,address)")
    val TOPIC_PAUSED = EvmCoder.eventTopic("Paused(address)")
    val TOPIC_UNPAUSED = EvmCoder.eventTopic("Unpaused(address)")

    // Encoders
    fun encodePositionCount(account: String): String {
        return SELECTOR_POSITION_COUNT + EvmCoder.encodeAddress(account)
    }

    fun encodeGetPosition(account: String, positionId: BigInteger): String {
        return SELECTOR_GET_POSITION + EvmCoder.encodeAddress(account) + EvmCoder.encodeUint256(positionId)
    }

    fun encodePendingReward(account: String, positionId: BigInteger): String {
        return SELECTOR_PENDING_REWARD + EvmCoder.encodeAddress(account) + EvmCoder.encodeUint256(positionId)
    }

    fun encodeTotalClaimable(account: String, positionId: BigInteger): String {
        return SELECTOR_TOTAL_CLAIMABLE + EvmCoder.encodeAddress(account) + EvmCoder.encodeUint256(positionId)
    }

    fun encodeGetTier(tierId: Int): String {
        return SELECTOR_GET_TIER + EvmCoder.encodeUint256(tierId.toLong())
    }

    fun encodeTiers(tierIndex: Long): String {
        return SELECTOR_TIERS + EvmCoder.encodeUint256(tierIndex)
    }

    fun encodeStake(amount: BigInteger, tierId: Int): String {
        return SELECTOR_STAKE + EvmCoder.encodeUint256(amount) + EvmCoder.encodeUint256(tierId.toLong())
    }

    fun encodeUnstake(positionId: BigInteger): String {
        return SELECTOR_UNSTAKE + EvmCoder.encodeUint256(positionId)
    }

    fun encodeEmergencyWithdraw(positionId: BigInteger): String {
        return SELECTOR_EMERGENCY_WITHDRAW + EvmCoder.encodeUint256(positionId)
    }
}

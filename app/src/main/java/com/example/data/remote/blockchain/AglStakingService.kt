package com.example.data.remote.blockchain

import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.abi.StakingAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.BaseRpcService
import com.example.data.remote.blockchain.rpc.EthLogItem
import java.math.BigInteger

data class StakingTier(
    val tierId: Int,
    val durationSeconds: Long,
    val formattedDuration: String,
    val aprPercent: Double,
    val isActive: Boolean
)

data class StakingPosition(
    val positionId: Long,
    val amountWei: BigInteger,
    val formattedAmount: String,
    val startTime: Long,
    val unlockTime: Long,
    val formattedUnlockDate: String,
    val tierId: Int,
    val aprPercent: Double,
    val pendingRewardWei: BigInteger,
    val formattedPendingReward: String,
    val totalClaimableWei: BigInteger,
    val formattedTotalClaimable: String,
    val isUnlocked: Boolean
)

data class StakingInfo(
    val contractAddress: String,
    val aglTokenAddress: String,
    val owner: String,
    val isPaused: Boolean,
    val totalStakedWei: BigInteger,
    val formattedTotalStaked: String,
    val rewardPoolBalanceWei: BigInteger,
    val formattedRewardPoolBalance: String,
    val tierCount: Int
)

data class StakingEventItem(
    val eventType: String,
    val userAddress: String,
    val amountTokens: String,
    val positionId: String,
    val transactionHash: String,
    val blockNumber: String
)

/**
 * Service to interact directly with the AGL Staking contract on Base Mainnet.
 * Contract: 0xd4B61B4876c15e78e0275EbA52cf62D55ED5fD30
 */
class AglStakingService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = BaseBlockchainConfig.STAKING_CONTRACT
) {

    suspend fun getStakingInfo(): Result<StakingInfo> {
        return try {
            val tokenRes = rpcService.ethCall(contractAddress, StakingAbi.SELECTOR_AGL_TOKEN).getOrNull()
            val ownerRes = rpcService.ethCall(contractAddress, StakingAbi.SELECTOR_OWNER).getOrNull()
            val pausedRes = rpcService.ethCall(contractAddress, StakingAbi.SELECTOR_PAUSED).getOrNull()
            val stakedRes = rpcService.ethCall(contractAddress, StakingAbi.SELECTOR_TOTAL_STAKED).getOrNull()
            val rewardPoolRes = rpcService.ethCall(contractAddress, StakingAbi.SELECTOR_REWARD_POOL_BALANCE).getOrNull()
            val tierCountRes = rpcService.ethCall(contractAddress, StakingAbi.SELECTOR_TIER_COUNT).getOrNull()

            val aglToken = EvmCoder.decodeAddress(tokenRes) ?: BaseBlockchainConfig.AGL_TOKEN_CONTRACT
            val owner = EvmCoder.decodeAddress(ownerRes) ?: BaseBlockchainConfig.TIMELOCK_CONTRACT
            val isPaused = EvmCoder.decodeBool(pausedRes)
            val totalStaked = EvmCoder.decodeUint256(stakedRes)
            val rewardPool = EvmCoder.decodeUint256(rewardPoolRes)
            val tierCount = EvmCoder.decodeUint256(tierCountRes).toInt().coerceAtLeast(0)

            Result.success(
                StakingInfo(
                    contractAddress = contractAddress,
                    aglTokenAddress = aglToken,
                    owner = owner,
                    isPaused = isPaused,
                    totalStakedWei = totalStaked,
                    formattedTotalStaked = "${EvmCoder.formatUnits(totalStaked, 18, 2)} AGL",
                    rewardPoolBalanceWei = rewardPool,
                    formattedRewardPoolBalance = "${EvmCoder.formatUnits(rewardPool, 18, 2)} AGL",
                    tierCount = tierCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getStakingTiers(): Result<List<StakingTier>> {
        return try {
            val tierCountRes = rpcService.ethCall(contractAddress, StakingAbi.SELECTOR_TIER_COUNT).getOrNull()
            val tierCount = EvmCoder.decodeUint256(tierCountRes).toInt().coerceIn(0, 10)

            val tiers = mutableListOf<StakingTier>()
            for (i in 0 until if (tierCount > 0) tierCount else 3) {
                val call = StakingAbi.encodeGetTier(i)
                val raw = rpcService.ethCall(contractAddress, call).getOrNull()
                if (raw != null && raw.length >= 66) {
                    val clean = EvmCoder.cleanHex(raw)
                    val durationSec = BigInteger(clean.substring(0, 64.coerceAtMost(clean.length)), 16).toLong()
                    val aprBps = if (clean.length >= 128) BigInteger(clean.substring(64, 128), 16).toDouble() / 100.0 else 12.0
                    val active = if (clean.length >= 192) BigInteger(clean.substring(128, 192), 16) != BigInteger.ZERO else true

                    val formattedDur = when {
                        durationSec >= 86400L * 365 -> "${durationSec / (86400L * 365)} Year(s)"
                        durationSec >= 86400L * 30 -> "${durationSec / (86400L * 30)} Month(s)"
                        durationSec >= 86400L -> "${durationSec / 86400L} Day(s)"
                        durationSec > 0L -> "${durationSec}s"
                        else -> "Flexible"
                    }

                    tiers.add(
                        StakingTier(
                            tierId = i,
                            durationSeconds = durationSec,
                            formattedDuration = formattedDur,
                            aprPercent = aprBps,
                            isActive = active
                        )
                    )
                } else {
                    // Fallback configuration if getTier tuple format varies
                    val (dur, apr) = when (i) {
                        0 -> Pair(30L * 86400L, 8.5)
                        1 -> Pair(90L * 86400L, 14.0)
                        else -> Pair(180L * 86400L, 22.0)
                    }
                    tiers.add(
                        StakingTier(
                            tierId = i,
                            durationSeconds = dur,
                            formattedDuration = "${dur / 86400L} Days",
                            aprPercent = apr,
                            isActive = true
                        )
                    )
                }
            }
            Result.success(tiers)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserPositions(account: String): Result<List<StakingPosition>> {
        return try {
            val countCall = StakingAbi.encodePositionCount(account)
            val countRes = rpcService.ethCall(contractAddress, countCall).getOrNull()
            val count = EvmCoder.decodeUint256(countRes).toInt().coerceIn(0, 50)

            val currentTimestamp = System.currentTimeMillis() / 1000L
            val positions = mutableListOf<StakingPosition>()

            for (i in 0 until count) {
                val posCall = StakingAbi.encodeGetPosition(account, BigInteger.valueOf(i.toLong()))
                val raw = rpcService.ethCall(contractAddress, posCall).getOrNull()
                val pendCall = StakingAbi.encodePendingReward(account, BigInteger.valueOf(i.toLong()))
                val pendRes = rpcService.ethCall(contractAddress, pendCall).getOrNull()
                val pending = EvmCoder.decodeUint256(pendRes)

                val claimCall = StakingAbi.encodeTotalClaimable(account, BigInteger.valueOf(i.toLong()))
                val claimRes = rpcService.ethCall(contractAddress, claimCall).getOrNull()
                val totalClaim = EvmCoder.decodeUint256(claimRes)

                if (raw != null && raw.length >= 130) {
                    val clean = EvmCoder.cleanHex(raw)
                    val amount = BigInteger(clean.substring(0, 64), 16)
                    val startTime = BigInteger(clean.substring(64, 128), 16).toLong()
                    val unlockTime = if (clean.length >= 192) BigInteger(clean.substring(128, 192), 16).toLong() else (startTime + 30 * 86400L)
                    val tierId = if (clean.length >= 256) BigInteger(clean.substring(192, 256), 16).toInt() else 0
                    val aprBps = if (clean.length >= 320) BigInteger(clean.substring(256, 320), 16).toDouble() / 100.0 else 12.0
                    val active = if (clean.length >= 384) BigInteger(clean.substring(320, 384), 16) != BigInteger.ZERO else true

                    if (active && amount > BigInteger.ZERO) {
                        positions.add(
                            StakingPosition(
                                positionId = i.toLong(),
                                amountWei = amount,
                                formattedAmount = "${EvmCoder.formatUnits(amount, 18, 2)} AGL",
                                startTime = startTime,
                                unlockTime = unlockTime,
                                formattedUnlockDate = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.US).format(java.util.Date(unlockTime * 1000L)),
                                tierId = tierId,
                                aprPercent = aprBps,
                                pendingRewardWei = pending,
                                formattedPendingReward = "${EvmCoder.formatUnits(pending, 18, 4)} AGL",
                                totalClaimableWei = totalClaim,
                                formattedTotalClaimable = "${EvmCoder.formatUnits(totalClaim, 18, 2)} AGL",
                                isUnlocked = currentTimestamp >= unlockTime
                            )
                        )
                    }
                }
            }

            Result.success(positions)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun encodeStake(amount: BigInteger, tierId: Int): String {
        return StakingAbi.encodeStake(amount, tierId)
    }

    fun encodeUnstake(positionId: BigInteger): String {
        return StakingAbi.encodeUnstake(positionId)
    }

    fun encodeEmergencyWithdraw(positionId: BigInteger): String {
        return StakingAbi.encodeEmergencyWithdraw(positionId)
    }

    suspend fun getStakingEvents(userAddress: String? = null, blockRange: Long = 10000L): Result<List<StakingEventItem>> {
        return try {
            val latestBlock = rpcService.ethBlockNumber().getOrDefault(50000000L)
            val fromBlock = "0x" + (latestBlock - blockRange).coerceAtLeast(1L).toString(16)
            val userTopic = userAddress?.let { "0x" + EvmCoder.encodeAddress(it) }

            val logs = rpcService.ethGetLogs(
                fromBlock = fromBlock,
                toBlock = "latest",
                address = contractAddress,
                topics = listOf(null, userTopic)
            ).getOrDefault(emptyList())

            val items = logs.mapNotNull { log ->
                val topic0 = log.topics?.firstOrNull() ?: return@mapNotNull null
                val type = when (topic0) {
                    StakingAbi.TOPIC_STAKED -> "Staked"
                    StakingAbi.TOPIC_UNSTAKED -> "Unstaked"
                    StakingAbi.TOPIC_EMERGENCY_WITHDRAW -> "Emergency Withdraw"
                    else -> null
                } ?: return@mapNotNull null

                val user = log.topics.getOrNull(1)?.let { EvmCoder.decodeAddress(it) } ?: userAddress ?: ""
                val amount = EvmCoder.decodeUint256(log.data)
                StakingEventItem(
                    eventType = type,
                    userAddress = user,
                    amountTokens = "${EvmCoder.formatUnits(amount, 18, 2)} AGL",
                    positionId = "#0",
                    transactionHash = log.transactionHash ?: "",
                    blockNumber = log.blockNumber ?: ""
                )
            }
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

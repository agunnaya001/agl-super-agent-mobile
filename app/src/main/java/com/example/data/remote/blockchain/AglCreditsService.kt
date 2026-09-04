package com.example.data.remote.blockchain

import com.example.data.remote.blockchain.abi.CreditsAbi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.EthLogItem
import java.math.BigInteger

/**
 * Model representing a parsed Credits Purchase event from Base Mainnet.
 */
data class CreditPurchaseEvent(
    val userAddress: String,
    val amountTokensWei: BigInteger,
    val formattedTokens: String,
    val transactionHash: String,
    val blockNumber: String
)

/**
 * Contract information and user state snapshot for AGL Compute Credits.
 */
data class AglCreditsInfo(
    val contractAddress: String,
    val owner: String?,
    val aglTokenAddress: String?,
    val isPaused: Boolean,
    val burnAddress: String?,
    val userCreditsPurchased: BigInteger,
    val formattedUserCredits: String
)

/**
 * Service to interact directly with the AGL Credits contract on Base Mainnet.
 *
 * Contract: 0x13866F31c60822Ff70684213b9727915Ddf2c183
 * Network: Base Mainnet (Chain ID: 8453)
 *
 * Provides functions to fetch credit balances, state info, and historical purchase
 * event activity using [BaseRpcService].
 */
class AglCreditsService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = AGL_CREDITS_CONTRACT
) {

    companion object {
        const val AGL_CREDITS_CONTRACT: String = "0x13866F31c60822Ff70684213b9727915Ddf2c183"
    }

    /**
     * Fetches general contract state and credit balance for a given wallet address.
     */
    suspend fun getCreditsInfo(userAddress: String? = null): Result<AglCreditsInfo> {
        return try {
            val ownerRes = rpcService.ethCall(contractAddress, CreditsAbi.SELECTOR_OWNER).getOrNull()
            val aglRes = rpcService.ethCall(contractAddress, CreditsAbi.SELECTOR_AGL_TOKEN).getOrNull()
            val pausedRes = rpcService.ethCall(contractAddress, CreditsAbi.SELECTOR_PAUSED).getOrNull()
            val burnRes = rpcService.ethCall(contractAddress, CreditsAbi.SELECTOR_BURN_ADDRESS).getOrNull()

            val userCredits = if (!userAddress.isNullOrBlank()) {
                val call = CreditsAbi.encodeTotalCreditsPurchased(userAddress)
                val res = rpcService.ethCall(contractAddress, call).getOrNull()
                EvmCoder.decodeUint256(res)
            } else {
                BigInteger.ZERO
            }

            val info = AglCreditsInfo(
                contractAddress = contractAddress,
                owner = EvmCoder.decodeAddress(ownerRes) ?: BaseBlockchainConfig.TIMELOCK_CONTRACT,
                aglTokenAddress = EvmCoder.decodeAddress(aglRes) ?: BaseBlockchainConfig.AGL_TOKEN_CONTRACT,
                isPaused = EvmCoder.decodeBool(pausedRes),
                burnAddress = EvmCoder.decodeAddress(burnRes),
                userCreditsPurchased = userCredits,
                formattedUserCredits = EvmCoder.formatUnits(userCredits, 18, 2)
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches the total credits purchased by a specific wallet address using BaseRpcService.
     * Function selector: 0x2ad18dee (totalCreditsPurchased(address))
     *
     * @param userAddress The 0x wallet address to query.
     */
    suspend fun getUserCredits(userAddress: String): Result<BigInteger> {
        val call = CreditsAbi.encodeTotalCreditsPurchased(userAddress)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            EvmCoder.decodeUint256(hex)
        }
    }

    /**
     * Checks if the AGL Credits contract is currently paused.
     */
    suspend fun isPaused(): Result<Boolean> {
        return rpcService.ethCall(contractAddress, CreditsAbi.SELECTOR_PAUSED).map { hex ->
            EvmCoder.decodeBool(hex)
        }
    }

    /**
     * Fetches the owner address of the AGL Credits contract.
     */
    suspend fun getOwner(): Result<String> {
        return rpcService.ethCall(contractAddress, CreditsAbi.SELECTOR_OWNER).map { hex ->
            EvmCoder.decodeAddress(hex) ?: BaseBlockchainConfig.TIMELOCK_CONTRACT
        }
    }

    /**
     * Fetches the AGL token address configured in the credits contract.
     */
    suspend fun getAglTokenAddress(): Result<String> {
        return rpcService.ethCall(contractAddress, CreditsAbi.SELECTOR_AGL_TOKEN).map { hex ->
            EvmCoder.decodeAddress(hex) ?: BaseBlockchainConfig.AGL_TOKEN_CONTRACT
        }
    }

    /**
     * Encodes calldata for purchasing credits with AGL tokens.
     * Function selector: 0xbef101fb (purchaseCredits(uint256))
     */
    fun encodePurchaseCredits(amountWei: BigInteger): String {
        return CreditsAbi.encodePurchaseCredits(amountWei)
    }

    /**
     * Fetches recent credit purchase activity logs for a user or the entire contract.
     * Queries event topic: CreditsPurchased(address indexed buyer, uint256 amount)
     *
     * @param userAddress Optional 0x wallet to filter activity for.
     * @param blockRange Number of blocks to look back from the latest block (defaults to 10,000).
     */
    suspend fun getPurchaseActivity(
        userAddress: String? = null,
        blockRange: Long = 10000L
    ): Result<List<CreditPurchaseEvent>> {
        return try {
            val latestBlock = rpcService.ethBlockNumber().getOrDefault(50000000L)
            val fromBlock = "0x" + (latestBlock - blockRange).coerceAtLeast(1L).toString(16)
            val userTopic = userAddress?.let { "0x" + EvmCoder.encodeAddress(it) }

            val topics = listOf(
                CreditsAbi.TOPIC_CREDITS_PURCHASED,
                userTopic
            )

            val logs = rpcService.ethGetLogs(
                fromBlock = fromBlock,
                toBlock = "latest",
                address = contractAddress,
                topics = topics
            ).getOrDefault(emptyList())

            val events = logs.mapNotNull { log ->
                val buyer = log.topics?.getOrNull(1)?.let { EvmCoder.decodeAddress(it) }
                    ?: userAddress
                    ?: return@mapNotNull null
                val amount = EvmCoder.decodeUint256(log.data)
                CreditPurchaseEvent(
                    userAddress = buyer,
                    amountTokensWei = amount,
                    formattedTokens = EvmCoder.formatUnits(amount, 18, 2),
                    transactionHash = log.transactionHash ?: "",
                    blockNumber = log.blockNumber ?: ""
                )
            }

            Result.success(events)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

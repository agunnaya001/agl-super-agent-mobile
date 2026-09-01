package com.example.data.remote.blockchain.services

import com.example.data.remote.blockchain.abi.CreditsAbi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.BaseRpcService
import java.math.BigInteger

data class AglCreditsInfo(
    val contractAddress: String,
    val owner: String?,
    val aglTokenAddress: String?,
    val isPaused: Boolean,
    val burnAddress: String?,
    val userCreditsPurchased: BigInteger,
    val formattedUserCredits: String
)

class AglCreditsService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = BaseBlockchainConfig.AGL_CREDITS_CONTRACT
) {

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

    suspend fun getUserCredits(userAddress: String): Result<BigInteger> {
        val call = CreditsAbi.encodeTotalCreditsPurchased(userAddress)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            EvmCoder.decodeUint256(hex)
        }
    }

    fun encodePurchaseCredits(amountWei: BigInteger): String {
        return CreditsAbi.encodePurchaseCredits(amountWei)
    }
}

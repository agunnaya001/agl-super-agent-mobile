package com.example.data.remote.blockchain.services

import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.abi.TimelockAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.BaseRpcService

data class TimelockInfo(
    val contractAddress: String,
    val minDelaySeconds: Long,
    val formattedMinDelay: String,
    val governorAddress: String
)

class TimelockService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = BaseBlockchainConfig.TIMELOCK_CONTRACT
) {

    suspend fun getTimelockInfo(): Result<TimelockInfo> {
        return try {
            val delayRes = rpcService.ethCall(contractAddress, TimelockAbi.SELECTOR_GET_MIN_DELAY).getOrNull()
                ?: rpcService.ethCall(contractAddress, TimelockAbi.SELECTOR_GET_MIN_DELAY_ALT).getOrNull()

            val minDelay = EvmCoder.decodeUint256(delayRes).toLong()
            val formatted = when {
                minDelay >= 86400L -> "${minDelay / 86400L} days (${minDelay}s)"
                minDelay >= 3600L -> "${minDelay / 3600L} hours (${minDelay}s)"
                minDelay > 0L -> "${minDelay} seconds"
                else -> "Immediate / Standard Delay"
            }

            val info = TimelockInfo(
                contractAddress = contractAddress,
                minDelaySeconds = minDelay,
                formattedMinDelay = formatted,
                governorAddress = BaseBlockchainConfig.GOVERNOR_CONTRACT
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun hasRole(roleHex: String, account: String): Result<Boolean> {
        val call = TimelockAbi.encodeHasRole(roleHex, account)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            EvmCoder.decodeBool(hex)
        }
    }
}

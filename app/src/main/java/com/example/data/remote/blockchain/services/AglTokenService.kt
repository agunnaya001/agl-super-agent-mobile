package com.example.data.remote.blockchain.services

import com.example.data.remote.blockchain.abi.Erc20Abi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.BaseRpcService
import com.example.data.remote.blockchain.rpc.EthLogItem
import java.math.BigInteger

data class TokenMetadata(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val totalSupply: BigInteger,
    val formattedTotalSupply: String,
    val contractAddress: String
)

class AglTokenService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = BaseBlockchainConfig.AGL_TOKEN_CONTRACT
) {

    suspend fun getMetadata(): Result<TokenMetadata> {
        return try {
            val nameRes = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_NAME).getOrNull()
            val symbolRes = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_SYMBOL).getOrNull()
            val decimalsRes = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_DECIMALS).getOrNull()
            val supplyRes = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_TOTAL_SUPPLY).getOrNull()

            val name = EvmCoder.decodeString(nameRes).ifBlank { "Agunnaya Labs" }
            val symbol = EvmCoder.decodeString(symbolRes).ifBlank { "AGL" }
            val decimals = EvmCoder.decodeUint256(decimalsRes).toInt().takeIf { it in 1..36 } ?: 18
            val supply = EvmCoder.decodeUint256(supplyRes)
            val formattedSupply = if (supply > BigInteger.ZERO) {
                "${EvmCoder.formatUnits(supply, decimals, 2)} $symbol"
            } else {
                "1,000,000,000 $symbol"
            }

            Result.success(
                TokenMetadata(
                    name = name,
                    symbol = symbol,
                    decimals = decimals,
                    totalSupply = supply,
                    formattedTotalSupply = formattedSupply,
                    contractAddress = contractAddress
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getBalanceOf(account: String): Result<BigInteger> {
        val calldata = Erc20Abi.encodeBalanceOf(account)
        val result = rpcService.ethCall(contractAddress, calldata)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    suspend fun getAllowance(owner: String, spender: String): Result<BigInteger> {
        val calldata = Erc20Abi.encodeAllowance(owner, spender)
        val result = rpcService.ethCall(contractAddress, calldata)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    fun encodeTransferData(recipient: String, amount: BigInteger): String {
        return Erc20Abi.encodeTransfer(recipient, amount)
    }

    fun encodeApproveData(spender: String, amount: BigInteger): String {
        return Erc20Abi.encodeApprove(spender, amount)
    }

    suspend fun getTransferLogs(account: String, blockRange: Long = 10000L): Result<List<EthLogItem>> {
        val latestBlockRes = rpcService.ethBlockNumber()
        val latestBlock = latestBlockRes.getOrDefault(50000000L)
        val fromBlock = "0x" + (latestBlock - blockRange).coerceAtLeast(1L).toString(16)
        val paddedAddress = "0x" + EvmCoder.encodeAddress(account)

        // Incoming transfers
        val toLogs = rpcService.ethGetLogs(
            fromBlock = fromBlock,
            toBlock = "latest",
            address = contractAddress,
            topics = listOf(Erc20Abi.TOPIC_TRANSFER, null, paddedAddress)
        ).getOrDefault(emptyList())

        // Outgoing transfers
        val fromLogs = rpcService.ethGetLogs(
            fromBlock = fromBlock,
            toBlock = "latest",
            address = contractAddress,
            topics = listOf(Erc20Abi.TOPIC_TRANSFER, paddedAddress, null)
        ).getOrDefault(emptyList())

        return Result.success((toLogs + fromLogs).distinctBy { it.transactionHash })
    }
}

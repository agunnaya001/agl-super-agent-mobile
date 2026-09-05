package com.example.data.remote.blockchain

import com.example.data.remote.blockchain.abi.Erc20Abi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.EthLogItem
import java.math.BigInteger

/**
 * Metadata snapshot for the AGL ERC-20 token on Base Mainnet.
 */
data class TokenMetadata(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val totalSupply: BigInteger,
    val formattedTotalSupply: String,
    val contractAddress: String
)

/**
 * Service to interact directly with the AGL Token contract on Base Mainnet.
 *
 * Contract: 0xEA1221B4d80A89BD8C75248Fae7c176BD1854698
 * Network: Base Mainnet (Chain ID: 8453)
 *
 * Utilizes [BaseRpcService] and Web3j to query token name, symbol, decimals,
 * balances, allowances, and transfer history.
 */
class AglTokenService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = AGL_TOKEN_CONTRACT
) {

    companion object {
        const val AGL_TOKEN_CONTRACT: String = "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698"
        const val DEFAULT_NAME: String = "Agunnaya Labs"
        const val DEFAULT_SYMBOL: String = "AGL"
        const val DEFAULT_DECIMALS: Int = 18
    }

    /**
     * Fetches the ERC-20 token name from Base Mainnet RPC using BaseRpcService.
     * Function selector: 0x06fdde03 (name())
     */
    suspend fun getName(): Result<String> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_NAME)
        return result.map { hex ->
            val decoded = EvmCoder.decodeString(hex).trim()
            if (decoded.isNotBlank()) decoded else DEFAULT_NAME
        }
    }

    /**
     * Fetches the ERC-20 token symbol from Base Mainnet RPC using BaseRpcService.
     * Function selector: 0x95d89b41 (symbol())
     */
    suspend fun getSymbol(): Result<String> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_SYMBOL)
        return result.map { hex ->
            val decoded = EvmCoder.decodeString(hex).trim()
            if (decoded.isNotBlank()) decoded else DEFAULT_SYMBOL
        }
    }

    /**
     * Fetches the ERC-20 token decimals from Base Mainnet RPC using BaseRpcService.
     * Function selector: 0x313ce567 (decimals())
     */
    suspend fun getDecimals(): Result<Int> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_DECIMALS)
        return result.map { hex ->
            val dec = EvmCoder.decodeUint256(hex).toInt()
            if (dec in 1..36) dec else DEFAULT_DECIMALS
        }
    }

    /**
     * Fetches the token balance (in smallest unit / Wei) for a given wallet address using BaseRpcService.
     * Function selector: 0x70a08231 (balanceOf(address))
     *
     * @param address The 0x wallet address to query on Base Mainnet.
     */
    suspend fun balanceOf(address: String): Result<BigInteger> {
        val calldata = Erc20Abi.encodeBalanceOf(address)
        val result = rpcService.ethCall(contractAddress, calldata)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    /**
     * Alias for [balanceOf] for consistency across services.
     */
    suspend fun getBalanceOf(address: String): Result<BigInteger> = balanceOf(address)

    /**
     * Fetches the total supply of AGL tokens.
     * Function selector: 0x18160ddd (totalSupply())
     */
    suspend fun getTotalSupply(): Result<BigInteger> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_TOTAL_SUPPLY)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    /**
     * Fetches the spend allowance granted by [owner] to [spender].
     * Function selector: 0xdd62ed3e (allowance(address,address))
     */
    suspend fun getAllowance(owner: String, spender: String): Result<BigInteger> {
        val calldata = Erc20Abi.encodeAllowance(owner, spender)
        val result = rpcService.ethCall(contractAddress, calldata)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    /**
     * Encodes calldata for ERC-20 transfer(address,uint256).
     */
    fun encodeTransferData(recipient: String, amount: BigInteger): String {
        return Erc20Abi.encodeTransfer(recipient, amount)
    }

    /**
     * Encodes calldata for ERC-20 approve(address,uint256).
     */
    fun encodeApproveData(spender: String, amount: BigInteger): String {
        return Erc20Abi.encodeApprove(spender, amount)
    }

    /**
     * Encodes calldata for burn(uint256).
     */
    fun encodeBurnData(amount: BigInteger): String {
        return Erc20Abi.encodeBurn(amount)
    }

    /**
     * Fetches contract owner address if applicable.
     */
    suspend fun getOwner(): Result<String> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_OWNER)
        return result.map { hex ->
            EvmCoder.decodeAddress(hex) ?: BaseBlockchainConfig.TIMELOCK_CONTRACT
        }
    }

    fun encodeTransferOwnershipData(newOwner: String): String {
        return Erc20Abi.encodeTransferOwnership(newOwner)
    }

    fun encodeRenounceOwnershipData(): String {
        return Erc20Abi.encodeRenounceOwnership()
    }

    /**
     * Fetches complete metadata (name, symbol, decimals, totalSupply) in a coordinated query.
     */
    suspend fun getMetadata(): Result<TokenMetadata> {
        return try {
            val name = getName().getOrDefault(DEFAULT_NAME)
            val symbol = getSymbol().getOrDefault(DEFAULT_SYMBOL)
            val decimals = getDecimals().getOrDefault(DEFAULT_DECIMALS)
            val supply = getTotalSupply().getOrDefault(BigInteger.ZERO)
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

    /**
     * Retrieves recent Transfer event logs for the given account within [blockRange].
     */
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

package com.example.data.remote.blockchain

import com.example.data.remote.blockchain.abi.Erc20Abi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.abi.VotesWrapperAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import java.math.BigInteger

/**
 * Underlying token metadata wrapped by the wAGL contract.
 */
data class UnderlyingTokenInfo(
    val address: String,
    val name: String,
    val symbol: String,
    val decimals: Int,
    val totalSupply: BigInteger,
    val formattedTotalSupply: String
)

/**
 * Snapshot metadata for the wAGL token on Base Mainnet.
 */
data class WagLTokenMetadata(
    val name: String,
    val symbol: String,
    val decimals: Int,
    val totalSupply: BigInteger,
    val formattedTotalSupply: String,
    val contractAddress: String,
    val underlyingTokenAddress: String?
)

/**
 * Account governance & balance details for wAGL.
 */
data class WagLAccountInfo(
    val address: String,
    val wAglBalance: BigInteger,
    val formattedBalance: String,
    val votingPower: BigInteger,
    val formattedVotingPower: String,
    val delegatee: String?,
    val numCheckpoints: Long
)

/**
 * Service to interact directly with the Wrapped AGL (wAGL / Votes Wrapper)
 * contract on Base Mainnet (Chain ID: 8453).
 *
 * Contract: 0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69
 *
 * Utilizes [BaseRpcService] and Web3j to read:
 * - Underlying token address & details (AGL Token: 0xEA1221B4d80A89BD8C75248Fae7c176BD1854698)
 * - wAGL account balance & voting power
 * - wAGL total supply
 * - Governance checkpoints and delegation
 */
class WagLService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = WAGL_CONTRACT_ADDRESS
) {

    companion object {
        const val WAGL_CONTRACT_ADDRESS: String = "0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69"
        const val DEFAULT_NAME: String = "Wrapped Agunnaya Labs"
        const val DEFAULT_SYMBOL: String = "wAGL"
        const val DEFAULT_DECIMALS: Int = 18
    }

    // =========================================================================
    // wAGL Token & Balance Queries (via Web3j)
    // =========================================================================

    /**
     * Fetches the wAGL token balance (in Wei) for a given account using Web3j.
     * Function selector: 0x70a08231 (balanceOf(address))
     *
     * @param account The 0x wallet address to query.
     */
    suspend fun balanceOf(account: String): Result<BigInteger> {
        return rpcService.getTokenBalance(contractAddress, account)
    }

    /**
     * Direct alias for [balanceOf] for explicit wAGL naming.
     */
    suspend fun getWAglBalance(account: String): Result<BigInteger> = balanceOf(account)

    /**
     * Fetches the formatted wAGL balance (e.g. "125.50 wAGL").
     */
    suspend fun getFormattedWAglBalance(account: String, decimals: Int = DEFAULT_DECIMALS): Result<String> {
        return balanceOf(account).map { balance ->
            "${EvmCoder.formatUnits(balance, decimals, 2)} $DEFAULT_SYMBOL"
        }
    }

    /**
     * Fetches the total circulating supply of wAGL tokens using Web3j.
     * Function selector: 0x18160ddd (totalSupply())
     */
    suspend fun getTotalSupply(): Result<BigInteger> {
        return rpcService.getTokenTotalSupply(contractAddress)
    }

    /**
     * Fetches the formatted total supply of wAGL tokens.
     */
    suspend fun getFormattedTotalSupply(decimals: Int = DEFAULT_DECIMALS): Result<String> {
        return getTotalSupply().map { supply ->
            "${EvmCoder.formatUnits(supply, decimals, 2)} $DEFAULT_SYMBOL"
        }
    }

    /**
     * Fetches the ERC-20 name of the wAGL contract.
     */
    suspend fun getName(): Result<String> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_NAME)
        return result.map { hex ->
            val decoded = EvmCoder.decodeString(hex).trim()
            if (decoded.isNotBlank()) decoded else DEFAULT_NAME
        }
    }

    /**
     * Fetches the ERC-20 symbol of the wAGL contract.
     */
    suspend fun getSymbol(): Result<String> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_SYMBOL)
        return result.map { hex ->
            val decoded = EvmCoder.decodeString(hex).trim()
            if (decoded.isNotBlank()) decoded else DEFAULT_SYMBOL
        }
    }

    /**
     * Fetches the decimals of the wAGL contract.
     */
    suspend fun getDecimals(): Result<Int> {
        val result = rpcService.ethCall(contractAddress, Erc20Abi.SELECTOR_DECIMALS)
        return result.map { hex ->
            val dec = EvmCoder.decodeUint256(hex).toInt()
            if (dec in 1..36) dec else DEFAULT_DECIMALS
        }
    }

    // =========================================================================
    // Underlying Token Info Queries (via Web3j)
    // =========================================================================

    /**
     * Reads the underlying token contract address wrapped by this contract.
     * Function selector: 0xfc0c546a (token() -> address)
     */
    suspend fun getUnderlyingTokenAddress(): Result<String> {
        val result = rpcService.ethCall(contractAddress, VotesWrapperAbi.SELECTOR_TOKEN)
        return result.map { hex ->
            EvmCoder.decodeAddress(hex) ?: BaseBlockchainConfig.AGL_TOKEN_CONTRACT
        }
    }

    /**
     * Reads complete information about the underlying token wrapped by wAGL
     * (address, name, symbol, decimals, and total supply) via Web3j.
     */
    suspend fun getUnderlyingTokenInfo(): Result<UnderlyingTokenInfo> {
        return try {
            val underlyingAddr = getUnderlyingTokenAddress().getOrDefault(BaseBlockchainConfig.AGL_TOKEN_CONTRACT)

            val nameRes = rpcService.ethCall(underlyingAddr, Erc20Abi.SELECTOR_NAME).getOrNull()
            val symbolRes = rpcService.ethCall(underlyingAddr, Erc20Abi.SELECTOR_SYMBOL).getOrNull()
            val decRes = rpcService.ethCall(underlyingAddr, Erc20Abi.SELECTOR_DECIMALS).getOrNull()
            val supplyRes = rpcService.ethCall(underlyingAddr, Erc20Abi.SELECTOR_TOTAL_SUPPLY).getOrNull()

            val rawName = EvmCoder.decodeString(nameRes).trim()
            val name = if (rawName.isNotBlank()) rawName else "Agunnaya Labs"

            val rawSymbol = EvmCoder.decodeString(symbolRes).trim()
            val symbol = if (rawSymbol.isNotBlank()) rawSymbol else "AGL"

            val rawDec = EvmCoder.decodeUint256(decRes).toInt()
            val decimals = if (rawDec in 1..36) rawDec else 18

            val totalSupply = EvmCoder.decodeUint256(supplyRes)
            val formattedSupply = "${EvmCoder.formatUnits(totalSupply, decimals, 2)} $symbol"

            Result.success(
                UnderlyingTokenInfo(
                    address = underlyingAddr,
                    name = name,
                    symbol = symbol,
                    decimals = decimals,
                    totalSupply = totalSupply,
                    formattedTotalSupply = formattedSupply
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches complete metadata for the wAGL wrapper token including underlying token link.
     */
    suspend fun getMetadata(): Result<WagLTokenMetadata> {
        return try {
            val name = getName().getOrDefault(DEFAULT_NAME)
            val symbol = getSymbol().getOrDefault(DEFAULT_SYMBOL)
            val decimals = getDecimals().getOrDefault(DEFAULT_DECIMALS)
            val supply = getTotalSupply().getOrDefault(BigInteger.ZERO)
            val underlying = getUnderlyingTokenAddress().getOrNull()

            Result.success(
                WagLTokenMetadata(
                    name = name,
                    symbol = symbol,
                    decimals = decimals,
                    totalSupply = supply,
                    formattedTotalSupply = "${EvmCoder.formatUnits(supply, decimals, 2)} $symbol",
                    contractAddress = contractAddress,
                    underlyingTokenAddress = underlying
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================================================
    // Account Governance State & Checkpoints (via Web3j)
    // =========================================================================

    /**
     * Fetches comprehensive account information: wAGL balance, current voting power,
     * delegation target, and checkpoint count.
     */
    suspend fun getAccountInfo(account: String): Result<WagLAccountInfo> {
        return try {
            val balanceRes = rpcService.ethCall(contractAddress, Erc20Abi.encodeBalanceOf(account)).getOrNull()
            val votesRes = rpcService.ethCall(contractAddress, VotesWrapperAbi.encodeGetVotes(account)).getOrNull()
            val delegatesRes = rpcService.ethCall(contractAddress, VotesWrapperAbi.encodeDelegates(account)).getOrNull()
            val numCheckpointsRes = rpcService.ethCall(contractAddress, VotesWrapperAbi.encodeNumCheckpoints(account)).getOrNull()

            val balance = EvmCoder.decodeUint256(balanceRes)
            val votes = EvmCoder.decodeUint256(votesRes)
            val delegatee = EvmCoder.decodeAddress(delegatesRes)
            val checkpoints = EvmCoder.decodeUint256(numCheckpointsRes).toLong()

            val info = WagLAccountInfo(
                address = account,
                wAglBalance = balance,
                formattedBalance = "${EvmCoder.formatUnits(balance, 18, 2)} $DEFAULT_SYMBOL",
                votingPower = votes,
                formattedVotingPower = "${EvmCoder.formatUnits(votes, 18, 2)} Votes",
                delegatee = delegatee,
                numCheckpoints = checkpoints
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves the current voting power for [account].
     */
    suspend fun getVotes(account: String): Result<BigInteger> {
        val call = VotesWrapperAbi.encodeGetVotes(account)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            EvmCoder.decodeUint256(hex)
        }
    }

    /**
     * Retrieves the current delegate address for [account].
     */
    suspend fun getDelegates(account: String): Result<String?> {
        val call = VotesWrapperAbi.encodeDelegates(account)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            EvmCoder.decodeAddress(hex)
        }
    }

    // =========================================================================
    // Transaction Calldata Encoders
    // =========================================================================

    /**
     * Encodes calldata for depositFor(address account, uint256 amount)
     * Wraps underlying AGL into wAGL for the given account.
     */
    fun encodeDepositFor(account: String, amountWei: BigInteger): String {
        return VotesWrapperAbi.encodeDepositFor(account, amountWei)
    }

    /**
     * Encodes calldata for withdrawTo(address account, uint256 amount)
     * Unwraps wAGL back into underlying AGL sent to the given account.
     */
    fun encodeWithdrawTo(account: String, amountWei: BigInteger): String {
        return VotesWrapperAbi.encodeWithdrawTo(account, amountWei)
    }

    /**
     * Encodes calldata for delegate(address delegatee)
     * Delegates voting power to the given delegatee address.
     */
    fun encodeDelegate(delegatee: String): String {
        return VotesWrapperAbi.encodeDelegate(delegatee)
    }
}

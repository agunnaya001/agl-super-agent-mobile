package com.example.data.remote.blockchain

import com.example.data.remote.blockchain.abi.Erc20Abi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.EthLogItem
import com.example.data.remote.blockchain.rpc.EthTransactionItem
import com.example.data.remote.blockchain.rpc.EthTransactionReceipt
import com.example.data.remote.blockchain.rpc.JsonRpcStringResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.DefaultBlockParameterNumber
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.EthLog
import org.web3j.protocol.http.HttpService
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * BaseRpcService connects to the Base Mainnet RPC (Chain ID: 8453) utilizing Web3j
 * as the core blockchain engine with automated multi-node failover.
 *
 * Serves as the foundational RPC layer for:
 * - [com.example.data.remote.blockchain.services.AglTokenService]
 * - [com.example.data.remote.blockchain.services.WalletService]
 * - [com.example.data.remote.blockchain.services.WagLService]
 * - [com.example.data.remote.blockchain.services.GovernorService]
 * - [com.example.data.remote.blockchain.services.AglCreditsService]
 */
class BaseRpcService(
    private val rpcEndpoints: List<String> = BaseBlockchainConfig.RPC_ENDPOINTS
) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val currentEndpointIndex = AtomicInteger(0)
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Web3j client instances connected to Base Mainnet RPC endpoints with failover capabilities.
     */
    private val web3jClients: List<Web3j> = rpcEndpoints.map { endpoint ->
        Web3j.build(HttpService(endpoint, httpClient, false))
    }

    /**
     * The primary Web3j client instance connected to Base Mainnet.
     */
    val web3j: Web3j
        get() = web3jClients[currentEndpointIndex.get() % web3jClients.size.coerceAtLeast(1)]

    /**
     * Gets a Web3j client pointing to the specific endpoint index or primary node.
     */
    fun getWeb3jClient(index: Int = currentEndpointIndex.get()): Web3j {
        val safeIndex = Math.floorMod(index, web3jClients.size.coerceAtLeast(1))
        return web3jClients[safeIndex]
    }

    /**
     * Executes a Web3j operation with automatic failover across Base RPC nodes.
     */
    suspend fun <T> executeWeb3jWithRetry(
        operationName: String,
        block: (Web3j) -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        val totalEndpoints = web3jClients.size.coerceAtLeast(1)
        val initialIndex = currentEndpointIndex.get()

        for (attempt in 0 until totalEndpoints) {
            val endpointIndex = (initialIndex + attempt) % totalEndpoints
            val client = web3jClients[endpointIndex]
            try {
                val result = block(client)
                currentEndpointIndex.set(endpointIndex)
                return@withContext Result.success(result)
            } catch (e: Exception) {
                lastException = e
                delay(120L * (attempt + 1))
            }
        }
        Result.failure(lastException ?: IllegalStateException("All Base RPC endpoints failed for $operationName"))
    }

    // =========================================================================
    // Foundation for WalletService (Base Native ETH & Account State)
    // =========================================================================

    /**
     * Retrieves the native Base ETH balance (in Wei) for a given account.
     */
    suspend fun ethGetBalance(address: String, block: String = "latest"): Result<BigInteger> {
        val blockParam = parseBlockParameter(block)
        return executeWeb3jWithRetry("ethGetBalance") { client ->
            val response = client.ethGetBalance(address, blockParam).send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }
            response.balance ?: BigInteger.ZERO
        }
    }

    /**
     * Convenient alias for WalletService to fetch native ETH balance.
     */
    suspend fun getEthBalance(address: String): Result<BigInteger> = ethGetBalance(address)

    /**
     * Retrieves current Base Mainnet block height.
     */
    suspend fun ethBlockNumber(): Result<Long> {
        return executeWeb3jWithRetry("ethBlockNumber") { client ->
            val response = client.ethBlockNumber().send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }
            response.blockNumber.toLong()
        }
    }

    /**
     * Retrieves Base Mainnet Chain ID (8453).
     */
    suspend fun ethChainId(): Result<Long> {
        return executeWeb3jWithRetry("ethChainId") { client ->
            val response = client.ethChainId().send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }
            response.chainId.toLong()
        }
    }

    /**
     * Retrieves the current Base gas price in Wei.
     */
    suspend fun ethGasPrice(): Result<BigInteger> {
        return executeWeb3jWithRetry("ethGasPrice") { client ->
            val response = client.ethGasPrice().send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }
            response.gasPrice ?: BigInteger.ZERO
        }
    }

    /**
     * Checks if bytecode exists at the given address on Base Mainnet.
     */
    suspend fun ethGetCode(address: String, block: String = "latest"): Result<String> {
        val blockParam = parseBlockParameter(block)
        return executeWeb3jWithRetry("ethGetCode") { client ->
            val response = client.ethGetCode(address, blockParam).send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }
            response.code ?: "0x"
        }
    }

    // =========================================================================
    // Foundation for AglTokenService (Smart Contracts & ERC-20 Tokens)
    // =========================================================================

    /**
     * Executes a read-only smart contract call on Base using Web3j.
     */
    suspend fun ethCall(to: String, data: String, block: String = "latest"): Result<String> {
        val blockParam = parseBlockParameter(block)
        val cleanData = EvmCoder.ensureHexPrefix(data)

        return executeWeb3jWithRetry("ethCall") { client ->
            val tx = Transaction.createEthCallTransaction(null, to, cleanData)
            val response = client.ethCall(tx, blockParam).send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }
            response.value ?: "0x"
        }
    }

    /**
     * Direct alias for contract queries used by token and governance services.
     */
    suspend fun callContract(to: String, data: String): Result<String> = ethCall(to, data)

    /**
     * Directly queries an ERC-20 token balance for an account using Web3j.
     */
    suspend fun getTokenBalance(contractAddress: String, accountAddress: String): Result<BigInteger> {
        val calldata = Erc20Abi.encodeBalanceOf(accountAddress)
        val result = ethCall(contractAddress, calldata)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    /**
     * Directly queries an ERC-20 allowance using Web3j.
     */
    suspend fun getTokenAllowance(
        contractAddress: String,
        ownerAddress: String,
        spenderAddress: String
    ): Result<BigInteger> {
        val calldata = Erc20Abi.encodeAllowance(ownerAddress, spenderAddress)
        val result = ethCall(contractAddress, calldata)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    /**
     * Directly queries an ERC-20 total supply using Web3j.
     */
    suspend fun getTokenTotalSupply(contractAddress: String): Result<BigInteger> {
        val result = ethCall(contractAddress, Erc20Abi.SELECTOR_TOTAL_SUPPLY)
        return result.map { hex -> EvmCoder.decodeUint256(hex) }
    }

    // =========================================================================
    // Transactions, Nonce & Broadcast
    // =========================================================================

    /**
     * Retrieves the next transaction nonce for an account on Base Mainnet.
     */
    suspend fun getTransactionCount(address: String, block: String = "pending"): Result<BigInteger> {
        val blockParam = parseBlockParameter(block)
        return executeWeb3jWithRetry("getTransactionCount") { client ->
            val response = client.ethGetTransactionCount(address, blockParam).send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }
            response.transactionCount ?: BigInteger.ZERO
        }
    }

    /**
     * Broadcasts a raw signed transaction to Base Mainnet.
     */
    suspend fun ethSendRawTransaction(signedTransactionHex: String): Result<String> {
        val clean = EvmCoder.ensureHexPrefix(signedTransactionHex)
        return executeWeb3jWithRetry("ethSendRawTransaction") { client ->
            val response = client.ethSendRawTransaction(clean).send()
            if (response.hasError()) {
                throw IllegalStateException("Broadcast Error ${response.error.code}: ${response.error.message}")
            }
            response.transactionHash ?: throw IllegalStateException("No transaction hash returned from Base RPC")
        }
    }

    /**
     * Estimates gas needed for a transaction execution on Base Mainnet.
     */
    suspend fun ethEstimateGas(
        from: String? = null,
        to: String,
        data: String? = null,
        value: BigInteger = BigInteger.ZERO
    ): Result<BigInteger> {
        return executeWeb3jWithRetry("ethEstimateGas") { client ->
            val tx = Transaction.createFunctionCallTransaction(
                from,
                null,
                null,
                null,
                to,
                value,
                data
            )
            val response = client.ethEstimateGas(tx).send()
            if (response.hasError()) {
                // Fallback default for Base L2
                BigInteger.valueOf(120000L)
            } else {
                response.amountUsed ?: BigInteger.valueOf(120000L)
            }
        }
    }

    /**
     * Fetches transaction details by transaction hash using Web3j.
     */
    suspend fun ethGetTransactionByHash(txHash: String): Result<EthTransactionItem?> {
        return executeWeb3jWithRetry("ethGetTransactionByHash") { client ->
            val response = client.ethGetTransactionByHash(txHash).send()
            val tx = response.transaction.orElse(null)
            if (tx != null) {
                EthTransactionItem(
                    hash = tx.hash,
                    from = tx.from,
                    to = tx.to,
                    value = tx.value?.let { "0x" + it.toString(16) },
                    gas = tx.gas?.let { "0x" + it.toString(16) },
                    gasPrice = tx.gasPrice?.let { "0x" + it.toString(16) },
                    input = tx.input,
                    blockNumber = tx.blockNumber?.let { "0x" + it.toString(16) }
                )
            } else {
                null
            }
        }
    }

    /**
     * Fetches transaction receipt by hash using Web3j.
     */
    suspend fun ethGetTransactionReceipt(txHash: String): Result<EthTransactionReceipt?> {
        return executeWeb3jWithRetry("ethGetTransactionReceipt") { client ->
            val response = client.ethGetTransactionReceipt(txHash).send()
            val receipt = response.transactionReceipt.orElse(null)
            if (receipt != null) {
                EthTransactionReceipt(
                    transactionHash = receipt.transactionHash,
                    blockNumber = receipt.blockNumber?.let { "0x" + it.toString(16) },
                    from = receipt.from,
                    to = receipt.to,
                    gasUsed = receipt.gasUsed?.let { "0x" + it.toString(16) },
                    status = receipt.status,
                    effectiveGasPrice = receipt.effectiveGasPrice
                )
            } else {
                null
            }
        }
    }

    /**
     * Retrieves event logs matching the given filter using Web3j.
     */
    suspend fun ethGetLogs(
        fromBlock: String,
        toBlock: String = "latest",
        address: String? = null,
        topics: List<String?>? = null
    ): Result<List<EthLogItem>> {
        val startParam = parseBlockParameter(fromBlock)
        val endParam = parseBlockParameter(toBlock)

        return executeWeb3jWithRetry("ethGetLogs") { client ->
            val filter = EthFilter(startParam, endParam, address)
            if (!topics.isNullOrEmpty()) {
                val nonNullTopics = topics.filterNotNull()
                if (nonNullTopics.isNotEmpty()) {
                    filter.addSingleTopic(nonNullTopics[0])
                    for (i in 1 until nonNullTopics.size) {
                        filter.addOptionalTopics(nonNullTopics[i])
                    }
                }
            }

            val response = client.ethGetLogs(filter).send()
            if (response.hasError()) {
                throw IllegalStateException("RPC Error ${response.error.code}: ${response.error.message}")
            }

            response.logs.mapNotNull { result ->
                val logObj = result as? EthLog.LogObject
                val log = logObj?.get()
                if (log != null) {
                    EthLogItem(
                        address = log.address,
                        topics = log.topics,
                        data = log.data,
                        blockNumber = log.blockNumber?.let { "0x" + it.toString(16) },
                        transactionHash = log.transactionHash,
                        logIndex = log.logIndex?.let { "0x" + it.toString(16) }
                    )
                } else {
                    null
                }
            }
        }
    }

    // =========================================================================
    // Generic RPC Execution Helper
    // =========================================================================

    suspend fun <T> executeRpcWithRetry(
        method: String,
        params: List<Any>,
        resultParser: (String) -> T
    ): Result<T> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null
        val attempts = rpcEndpoints.size.coerceAtLeast(2)

        for (attempt in 0 until attempts) {
            val endpoint = rpcEndpoints[(currentEndpointIndex.get() + attempt) % rpcEndpoints.size]
            try {
                val requestId = System.currentTimeMillis() + attempt
                val requestPayload = mapOf(
                    "jsonrpc" to "2.0",
                    "method" to method,
                    "params" to params,
                    "id" to requestId
                )
                val jsonBody = moshi.adapter(Map::class.java).toJson(requestPayload)
                val request = Request.Builder()
                    .url(endpoint)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("User-Agent", "AGL-SuperAgent/1.0 (Android; Base 8453; Web3j)")
                    .post(jsonBody.toRequestBody(jsonMediaType))
                    .build()

                val response = httpClient.newCall(request).execute()
                val responseBody = response.body?.string() ?: ""

                if (!response.isSuccessful) {
                    if (response.code == 429) {
                        delay(200L * (attempt + 1))
                    }
                    continue
                }

                val parsed = resultParser(responseBody)
                return@withContext Result.success(parsed)
            } catch (e: Exception) {
                lastException = e
                delay(100L * (attempt + 1))
            }
        }
        Result.failure(lastException ?: IllegalStateException("All Base RPC endpoints failed for $method"))
    }

    private fun parseBlockParameter(block: String): DefaultBlockParameter {
        return when {
            block.equals("latest", ignoreCase = true) -> DefaultBlockParameterName.LATEST
            block.equals("earliest", ignoreCase = true) -> DefaultBlockParameterName.EARLIEST
            block.equals("pending", ignoreCase = true) -> DefaultBlockParameterName.PENDING
            block.startsWith("0x") -> {
                val num = EvmCoder.decodeUint256(block)
                DefaultBlockParameterNumber(num)
            }
            else -> {
                val num = block.toBigIntegerOrNull()
                if (num != null) DefaultBlockParameterNumber(num) else DefaultBlockParameterName.LATEST
            }
        }
    }
}

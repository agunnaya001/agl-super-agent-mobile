package com.example.data.remote.blockchain.rpc

import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
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
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class BaseRpcService(
    private val rpcEndpoints: List<String> = BaseBlockchainConfig.RPC_ENDPOINTS
) {

    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .writeTimeout(10, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val currentEndpointIndex = AtomicInteger(0)
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    private fun getNextRpcUrl(): String {
        val index = currentEndpointIndex.getAndUpdate { (it + 1) % rpcEndpoints.size }
        return rpcEndpoints[index]
    }

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
                    .addHeader("User-Agent", "AGL-SuperAgent/1.0 (Android; Base 8453)")
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

    suspend fun ethCall(to: String, data: String, block: String = "latest"): Result<String> {
        val callParam = mapOf("to" to to, "data" to EvmCoder.ensureHexPrefix(data))
        return executeRpcWithRetry("eth_call", listOf(callParam, block)) { body ->
            val adapter = moshi.adapter(JsonRpcStringResponse::class.java)
            val res = adapter.fromJson(body)
            if (res?.error != null) {
                throw IllegalStateException("RPC Error ${res.error.code}: ${res.error.message}")
            }
            res?.result ?: "0x"
        }
    }

    suspend fun ethGetBalance(address: String, block: String = "latest"): Result<BigInteger> {
        return executeRpcWithRetry("eth_getBalance", listOf(address, block)) { body ->
            val adapter = moshi.adapter(JsonRpcStringResponse::class.java)
            val res = adapter.fromJson(body)
            if (res?.error != null) {
                throw IllegalStateException("RPC Error: ${res.error.message}")
            }
            EvmCoder.decodeUint256(res?.result)
        }
    }

    suspend fun ethBlockNumber(): Result<Long> {
        return executeRpcWithRetry("eth_blockNumber", emptyList()) { body ->
            val adapter = moshi.adapter(JsonRpcStringResponse::class.java)
            val res = adapter.fromJson(body)
            val hex = res?.result ?: "0x0"
            EvmCoder.decodeUint256(hex).toLong()
        }
    }

    suspend fun ethChainId(): Result<Long> {
        return executeRpcWithRetry("eth_chainId", emptyList()) { body ->
            val adapter = moshi.adapter(JsonRpcStringResponse::class.java)
            val res = adapter.fromJson(body)
            val hex = res?.result ?: "0x2105"
            EvmCoder.decodeUint256(hex).toLong()
        }
    }

    suspend fun ethGetCode(address: String, block: String = "latest"): Result<String> {
        return executeRpcWithRetry("eth_getCode", listOf(address, block)) { body ->
            val adapter = moshi.adapter(JsonRpcStringResponse::class.java)
            val res = adapter.fromJson(body)
            res?.result ?: "0x"
        }
    }

    suspend fun ethGetTransactionByHash(txHash: String): Result<EthTransactionItem?> {
        return executeRpcWithRetry("eth_getTransactionByHash", listOf(txHash)) { body ->
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(mapType)
            val jsonMap = adapter.fromJson(body)
            val resultObj = jsonMap?.get("result")
            if (resultObj is Map<*, *>) {
                val itemAdapter = moshi.adapter(EthTransactionItem::class.java)
                val itemJson = moshi.adapter(Map::class.java).toJson(resultObj as Map<Any, Any>)
                itemAdapter.fromJson(itemJson)
            } else {
                null
            }
        }
    }

    suspend fun ethGetTransactionReceipt(txHash: String): Result<EthTransactionReceipt?> {
        return executeRpcWithRetry("eth_getTransactionReceipt", listOf(txHash)) { body ->
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(mapType)
            val jsonMap = adapter.fromJson(body)
            val resultObj = jsonMap?.get("result")
            if (resultObj is Map<*, *>) {
                val itemAdapter = moshi.adapter(EthTransactionReceipt::class.java)
                val itemJson = moshi.adapter(Map::class.java).toJson(resultObj as Map<Any, Any>)
                itemAdapter.fromJson(itemJson)
            } else {
                null
            }
        }
    }

    suspend fun ethGetLogs(
        fromBlock: String,
        toBlock: String = "latest",
        address: String? = null,
        topics: List<String?>? = null
    ): Result<List<EthLogItem>> {
        val filter = mutableMapOf<String, Any>(
            "fromBlock" to fromBlock,
            "toBlock" to toBlock
        )
        if (!address.isNullOrBlank()) {
            filter["address"] = address
        }
        if (!topics.isNullOrEmpty()) {
            filter["topics"] = topics.filterNotNull()
        }

        return executeRpcWithRetry("eth_getLogs", listOf(filter)) { body ->
            val mapType = Types.newParameterizedType(Map::class.java, String::class.java, Any::class.java)
            val adapter = moshi.adapter<Map<String, Any>>(mapType)
            val jsonMap = adapter.fromJson(body)
            val resultList = jsonMap?.get("result") as? List<*>
            val logAdapter = moshi.adapter(EthLogItem::class.java)
            resultList?.mapNotNull { item ->
                if (item is Map<*, *>) {
                    val itemJson = moshi.adapter(Map::class.java).toJson(item as Map<Any, Any>)
                    logAdapter.fromJson(itemJson)
                } else null
            } ?: emptyList()
        }
    }
}

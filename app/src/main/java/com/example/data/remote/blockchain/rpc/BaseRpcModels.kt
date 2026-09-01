package com.example.data.remote.blockchain.rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonRpcRequest(
    @Json(name = "jsonrpc") val jsonrpc: String = "2.0",
    @Json(name = "method") val method: String,
    @Json(name = "params") val params: List<Any>,
    @Json(name = "id") val id: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class JsonRpcError(
    @Json(name = "code") val code: Int? = null,
    @Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class JsonRpcStringResponse(
    @Json(name = "jsonrpc") val jsonrpc: String? = "2.0",
    @Json(name = "id") val id: Long? = null,
    @Json(name = "result") val result: String? = null,
    @Json(name = "error") val error: JsonRpcError? = null
)

@JsonClass(generateAdapter = true)
data class EthLogItem(
    @Json(name = "address") val address: String? = null,
    @Json(name = "topics") val topics: List<String>? = null,
    @Json(name = "data") val data: String? = null,
    @Json(name = "blockNumber") val blockNumber: String? = null,
    @Json(name = "transactionHash") val transactionHash: String? = null,
    @Json(name = "logIndex") val logIndex: String? = null
)

@JsonClass(generateAdapter = true)
data class EthTransactionItem(
    @Json(name = "hash") val hash: String? = null,
    @Json(name = "from") val from: String? = null,
    @Json(name = "to") val to: String? = null,
    @Json(name = "value") val value: String? = null,
    @Json(name = "gas") val gas: String? = null,
    @Json(name = "gasPrice") val gasPrice: String? = null,
    @Json(name = "input") val input: String? = null,
    @Json(name = "blockNumber") val blockNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class EthTransactionReceipt(
    @Json(name = "transactionHash") val transactionHash: String? = null,
    @Json(name = "blockNumber") val blockNumber: String? = null,
    @Json(name = "from") val from: String? = null,
    @Json(name = "to") val to: String? = null,
    @Json(name = "gasUsed") val gasUsed: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "effectiveGasPrice") val effectiveGasPrice: String? = null
)

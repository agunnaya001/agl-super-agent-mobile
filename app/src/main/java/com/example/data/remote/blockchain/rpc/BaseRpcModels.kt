package com.example.data.remote.blockchain.rpc

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class JsonRpcRequest(
    @param:Json(name = "jsonrpc") val jsonrpc: String = "2.0",
    @param:Json(name = "method") val method: String,
    @param:Json(name = "params") val params: List<Any>,
    @param:Json(name = "id") val id: Long = System.currentTimeMillis()
)

@JsonClass(generateAdapter = true)
data class JsonRpcError(
    @param:Json(name = "code") val code: Int? = null,
    @param:Json(name = "message") val message: String? = null
)

@JsonClass(generateAdapter = true)
data class JsonRpcStringResponse(
    @param:Json(name = "jsonrpc") val jsonrpc: String? = "2.0",
    @param:Json(name = "id") val id: Long? = null,
    @param:Json(name = "result") val result: String? = null,
    @param:Json(name = "error") val error: JsonRpcError? = null
)

@JsonClass(generateAdapter = true)
data class EthLogItem(
    @param:Json(name = "address") val address: String? = null,
    @param:Json(name = "topics") val topics: List<String>? = null,
    @param:Json(name = "data") val data: String? = null,
    @param:Json(name = "blockNumber") val blockNumber: String? = null,
    @param:Json(name = "transactionHash") val transactionHash: String? = null,
    @param:Json(name = "logIndex") val logIndex: String? = null
)

@JsonClass(generateAdapter = true)
data class EthTransactionItem(
    @param:Json(name = "hash") val hash: String? = null,
    @param:Json(name = "from") val from: String? = null,
    @param:Json(name = "to") val to: String? = null,
    @param:Json(name = "value") val value: String? = null,
    @param:Json(name = "gas") val gas: String? = null,
    @param:Json(name = "gasPrice") val gasPrice: String? = null,
    @param:Json(name = "input") val input: String? = null,
    @param:Json(name = "blockNumber") val blockNumber: String? = null
)

@JsonClass(generateAdapter = true)
data class EthTransactionReceipt(
    @param:Json(name = "transactionHash") val transactionHash: String? = null,
    @param:Json(name = "blockNumber") val blockNumber: String? = null,
    @param:Json(name = "from") val from: String? = null,
    @param:Json(name = "to") val to: String? = null,
    @param:Json(name = "gasUsed") val gasUsed: String? = null,
    @param:Json(name = "status") val status: String? = null,
    @param:Json(name = "effectiveGasPrice") val effectiveGasPrice: String? = null
)

package com.example.data.remote.blockchain.abi

import java.math.BigInteger

data class ChainlinkRoundData(
    val roundId: BigInteger,
    val answer: BigInteger,
    val startedAt: BigInteger,
    val updatedAt: BigInteger,
    val answeredInRound: BigInteger
)

object ChainlinkOracleAbi {

    // AggregatorV3Interface standard function selectors
    const val SELECTOR_LATEST_ROUND_DATA = "0xfeaf968c" // latestRoundData()
    const val SELECTOR_DECIMALS = "0x313ce567"          // decimals()
    const val SELECTOR_DESCRIPTION = "0x7284e416"       // description()
    const val SELECTOR_VERSION = "0x54fd4d50"           // version()

    fun encodeLatestRoundData(): String = SELECTOR_LATEST_ROUND_DATA
    fun encodeDecimals(): String = SELECTOR_DECIMALS
    fun encodeDescription(): String = SELECTOR_DESCRIPTION

    /**
     * Decodes the 5 return values of AggregatorV3Interface.latestRoundData():
     * uint80 roundId, int256 answer, uint256 startedAt, uint256 updatedAt, uint80 answeredInRound
     */
    fun decodeLatestRoundData(hexOutput: String): ChainlinkRoundData? {
        val clean = hexOutput.removePrefix("0x").trim()
        if (clean.length < 320) return null // 5 words * 64 hex chars = 320 hex chars

        return try {
            val roundIdHex = clean.substring(0, 64)
            val answerHex = clean.substring(64, 128)
            val startedAtHex = clean.substring(128, 192)
            val updatedAtHex = clean.substring(192, 256)
            val answeredInRoundHex = clean.substring(256, 320)

            val roundId = BigInteger(roundIdHex, 16)
            // Handle signed int256 for answer
            val answerBig = BigInteger(answerHex, 16)
            val answer = if (answerHex.startsWith("8") || answerHex.startsWith("9") ||
                answerHex.startsWith("a") || answerHex.startsWith("b") ||
                answerHex.startsWith("c") || answerHex.startsWith("d") ||
                answerHex.startsWith("e") || answerHex.startsWith("f") ||
                answerHex.startsWith("A") || answerHex.startsWith("B") ||
                answerHex.startsWith("C") || answerHex.startsWith("D") ||
                answerHex.startsWith("E") || answerHex.startsWith("F")
            ) {
                answerBig.subtract(BigInteger.ONE.shiftLeft(256))
            } else {
                answerBig
            }

            val startedAt = BigInteger(startedAtHex, 16)
            val updatedAt = BigInteger(updatedAtHex, 16)
            val answeredInRound = BigInteger(answeredInRoundHex, 16)

            ChainlinkRoundData(
                roundId = roundId,
                answer = answer,
                startedAt = startedAt,
                updatedAt = updatedAt,
                answeredInRound = answeredInRound
            )
        } catch (e: Exception) {
            null
        }
    }
}

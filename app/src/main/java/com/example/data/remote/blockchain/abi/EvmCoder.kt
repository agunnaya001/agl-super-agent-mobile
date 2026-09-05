package com.example.data.remote.blockchain.abi

import org.web3j.crypto.Hash
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.nio.charset.StandardCharsets

object EvmCoder {

    /**
     * Computes the Keccak-256 hash of a UTF-8 string or byte array.
     */
    fun keccak256(input: ByteArray): ByteArray {
        return Hash.sha3(input)
    }

    /**
     * Computes the standard 4-byte EVM function selector for a function signature.
     * E.g. functionSelector("balanceOf(address)") -> "0x70a08231"
     */
    fun functionSelector(signature: String): String {
        val hash = Hash.sha3(signature.toByteArray(StandardCharsets.UTF_8))
        val hex = hash.take(4).joinToString("") { "%02x".format(it) }
        return "0x$hex"
    }

    /**
     * Computes the standard 32-byte EVM event topic for an event signature.
     * E.g. eventTopic("Transfer(address,address,uint256)")
     */
    fun eventTopic(signature: String): String {
        val hash = Hash.sha3(signature.toByteArray(StandardCharsets.UTF_8))
        val hex = hash.joinToString("") { "%02x".format(it) }
        return "0x$hex"
    }

    fun cleanHex(hex: String): String {
        return if (hex.startsWith("0x", ignoreCase = true)) {
            hex.substring(2)
        } else {
            hex
        }
    }

    fun ensureHexPrefix(hex: String): String {
        return if (hex.startsWith("0x", ignoreCase = true)) {
            hex
        } else {
            "0x$hex"
        }
    }

    fun encodeAddress(address: String): String {
        val clean = cleanHex(address).lowercase()
        return clean.padStart(64, '0')
    }

    fun encodeUint256(value: BigInteger): String {
        return value.toString(16).padStart(64, '0')
    }

    fun encodeUint256(value: Long): String {
        return encodeUint256(BigInteger.valueOf(value))
    }

    fun encodeBytes32(bytes32Hex: String): String {
        val clean = cleanHex(bytes32Hex)
        return clean.padEnd(64, '0').take(64)
    }

    fun decodeAddress(hex: String?): String? {
        if (hex.isNullOrBlank() || hex == "0x") return null
        val clean = cleanHex(hex)
        if (clean.length < 40) return null
        val last40 = clean.takeLast(40)
        return "0x$last40"
    }

    fun decodeUint256(hex: String?): BigInteger {
        if (hex.isNullOrBlank() || hex == "0x") return BigInteger.ZERO
        val clean = cleanHex(hex)
        if (clean.isEmpty()) return BigInteger.ZERO
        return try {
            BigInteger(clean, 16)
        } catch (e: Exception) {
            BigInteger.ZERO
        }
    }

    fun decodeBool(hex: String?): Boolean {
        if (hex.isNullOrBlank() || hex == "0x") return false
        val clean = cleanHex(hex)
        return clean.endsWith("1")
    }

    fun decodeString(hex: String?): String {
        if (hex.isNullOrBlank() || hex == "0x") return ""
        val clean = cleanHex(hex)
        if (clean.length < 64) {
            // Might be direct ascii bytes or raw
            return try {
                val bytes = clean.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                String(bytes, StandardCharsets.UTF_8).trim { it <= ' ' || it == '\u0000' }
            } catch (e: Exception) {
                ""
            }
        }
        return try {
            val offsetInBytes = clean.substring(0, 64).toLong(16).toInt()
            val offsetInChars = offsetInBytes * 2
            if (offsetInChars + 64 > clean.length) {
                return ""
            }
            val lengthInBytes = clean.substring(offsetInChars, offsetInChars + 64).toLong(16).toInt()
            val lengthInChars = lengthInBytes * 2
            val dataStart = offsetInChars + 64
            if (dataStart + lengthInChars > clean.length) {
                return ""
            }
            val hexData = clean.substring(dataStart, dataStart + lengthInChars)
            val bytes = hexData.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
            String(bytes, StandardCharsets.UTF_8)
        } catch (e: Exception) {
            // Fallback for short ASCII strings
            try {
                val bytes = clean.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
                String(bytes, StandardCharsets.UTF_8).trim { it <= ' ' || it == '\u0000' }
            } catch (ex: Exception) {
                ""
            }
        }
    }

    fun formatUnits(wei: BigInteger, decimals: Int = 18, precision: Int = 4): String {
        if (wei == BigInteger.ZERO) return "0.00"
        val divisor = BigDecimal.TEN.pow(decimals)
        val value = BigDecimal(wei).divide(divisor, decimals, RoundingMode.HALF_UP)
        return value.setScale(precision, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    fun parseUnits(amount: String, decimals: Int = 18): BigInteger {
        val clean = amount.trim().replace(",", "")
        val dec = BigDecimal(clean)
        val multiplier = BigDecimal.TEN.pow(decimals)
        return dec.multiply(multiplier).toBigInteger()
    }

    fun formatEth(weiHex: String?, decimals: Int = 18): String {
        val wei = decodeUint256(weiHex)
        return formatUnits(wei, decimals)
    }
}

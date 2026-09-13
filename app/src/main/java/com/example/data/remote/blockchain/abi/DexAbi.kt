package com.example.data.remote.blockchain.abi

import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import java.math.BigInteger

/**
 * ABI encoding & decoding utilities for major Base Mainnet DEXes:
 * - Aerodrome Router (Base #1 DEX by volume & TVL)
 * - Uniswap V3 SwapRouter02 & QuoterV2
 * - 0x Exchange Proxy
 */
object DexAbi {

    // Aerodrome Router addresses on Base Mainnet
    const val AERODROME_ROUTER = "0xcF77a3Ba9A5CA399B7c97c748561549285932570"
    const val AERODROME_ROUTER_V2 = "0xcF77a3Ba9A5CA399B7c97c74884694265537232e"

    // Uniswap V3 Router & Quoter on Base Mainnet
    const val UNISWAP_V3_SWAP_ROUTER = "0x2626664c2603336E57B271c5C0b26F421741e481"
    const val UNISWAP_V3_QUOTER = "0x3d4e44Eb1374240CE5F1B871ab261CD16335B76a"

    // 0x Protocol Exchange Proxy on Base
    const val ZERO_X_EXCHANGE_PROXY = "0xdef1c0ded9bec7f1a1670819833240f027b25eff"

    // Wrapped Native Token on Base
    const val WETH_CONTRACT = "0x4200000000000000000000000000000000000006"

    /**
     * Function selectors
     */
    val SELECTOR_SWAP_EXACT_TOKENS_FOR_TOKENS = EvmCoder.functionSelector("swapExactTokensForTokens(uint256,uint256,(address,address,bool)[],address,uint256)")
    val SELECTOR_SWAP_EXACT_ETH_FOR_TOKENS = EvmCoder.functionSelector("swapExactETHForTokens(uint256,(address,address,bool)[],address,uint256)")
    val SELECTOR_SWAP_EXACT_TOKENS_FOR_ETH = EvmCoder.functionSelector("swapExactTokensForETH(uint256,uint256,(address,address,bool)[],address,uint256)")
    val SELECTOR_EXACT_INPUT_SINGLE = EvmCoder.functionSelector("exactInputSingle((address,address,uint24,address,uint256,uint256,uint160))")
    val SELECTOR_GET_AMOUNTS_OUT = EvmCoder.functionSelector("getAmountsOut(uint256,(address,address,bool)[])")

    /**
     * Encodes a standard Aerodrome Route struct array.
     */
    data class AerodromeRoute(
        val from: String,
        val to: String,
        val stable: Boolean
    )

    /**
     * Encodes Aerodrome swapExactTokensForTokens.
     */
    fun encodeAerodromeSwap(
        amountIn: BigInteger,
        amountOutMin: BigInteger,
        routes: List<AerodromeRoute>,
        to: String,
        deadline: Long = System.currentTimeMillis() / 1000 + 1200
    ): String {
        val selector = SELECTOR_SWAP_EXACT_TOKENS_FOR_TOKENS
        val amountInHex = EvmCoder.encodeUint256(amountIn)
        val amountOutMinHex = EvmCoder.encodeUint256(amountOutMin)
        val routesOffsetHex = EvmCoder.encodeUint256(160L) // 5 params * 32 bytes
        val toHex = EvmCoder.encodeAddress(to)
        val deadlineHex = EvmCoder.encodeUint256(deadline)

        // Routes array encoding
        val routesLengthHex = EvmCoder.encodeUint256(routes.size.toLong())
        val routesData = StringBuilder()
        for (r in routes) {
            routesData.append(EvmCoder.encodeAddress(r.from))
            routesData.append(EvmCoder.encodeAddress(r.to))
            routesData.append(if (r.stable) EvmCoder.encodeUint256(1) else EvmCoder.encodeUint256(0))
        }

        return selector + amountInHex + amountOutMinHex + routesOffsetHex + toHex + deadlineHex + routesLengthHex + routesData.toString()
    }

    /**
     * Encodes Uniswap V3 exactInputSingle params:
     * (tokenIn, tokenOut, fee, recipient, amountIn, amountOutMinimum, sqrtPriceLimitX96)
     */
    fun encodeUniswapV3ExactInputSingle(
        tokenIn: String,
        tokenOut: String,
        fee: Int = 3000, // 0.3% fee tier
        recipient: String,
        amountIn: BigInteger,
        amountOutMinimum: BigInteger,
        sqrtPriceLimitX96: BigInteger = BigInteger.ZERO
    ): String {
        val selector = SELECTOR_EXACT_INPUT_SINGLE
        val headOffset = EvmCoder.encodeUint256(32L) // tuple offset

        val tokenInHex = EvmCoder.encodeAddress(tokenIn)
        val tokenOutHex = EvmCoder.encodeAddress(tokenOut)
        val feeHex = EvmCoder.encodeUint256(fee.toLong())
        val recipientHex = EvmCoder.encodeAddress(recipient)
        val amountInHex = EvmCoder.encodeUint256(amountIn)
        val amountOutMinHex = EvmCoder.encodeUint256(amountOutMinimum)
        val sqrtPriceLimitHex = EvmCoder.encodeUint256(sqrtPriceLimitX96)

        return selector + headOffset + tokenInHex + tokenOutHex + feeHex + recipientHex + amountInHex + amountOutMinHex + sqrtPriceLimitHex
    }
}

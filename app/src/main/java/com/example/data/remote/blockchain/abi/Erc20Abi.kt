package com.example.data.remote.blockchain.abi

import java.math.BigInteger

object Erc20Abi {

    // Selectors
    const val SELECTOR_NAME = "0x06fdde03"
    const val SELECTOR_SYMBOL = "0x95d89b41"
    const val SELECTOR_DECIMALS = "0x313ce567"
    const val SELECTOR_TOTAL_SUPPLY = "0x18160ddd"
    const val SELECTOR_BALANCE_OF = "0x70a08231"
    const val SELECTOR_ALLOWANCE = "0xdd62ed3e"
    const val SELECTOR_TRANSFER = "0xa9059cbb"
    const val SELECTOR_APPROVE = "0x095ea7b3"
    const val SELECTOR_TRANSFER_FROM = "0x23b872dd"

    // Topics
    const val TOPIC_TRANSFER = "0xddf252ad1be2c89b69c2b068fc378daa952ba7f163c4a11628f55a4df523b3ef"
    const val TOPIC_APPROVAL = "0x8c5be1e5eb7d556b1f0b42c779857777b8d4239775f5e2f5b666958449e6a887"

    fun encodeBalanceOf(account: String): String {
        return SELECTOR_BALANCE_OF + EvmCoder.encodeAddress(account)
    }

    fun encodeAllowance(owner: String, spender: String): String {
        return SELECTOR_ALLOWANCE + EvmCoder.encodeAddress(owner) + EvmCoder.encodeAddress(spender)
    }

    fun encodeTransfer(recipient: String, amount: BigInteger): String {
        return SELECTOR_TRANSFER + EvmCoder.encodeAddress(recipient) + EvmCoder.encodeUint256(amount)
    }

    fun encodeApprove(spender: String, amount: BigInteger): String {
        return SELECTOR_APPROVE + EvmCoder.encodeAddress(spender) + EvmCoder.encodeUint256(amount)
    }

    fun encodeTransferFrom(sender: String, recipient: String, amount: BigInteger): String {
        return SELECTOR_TRANSFER_FROM + EvmCoder.encodeAddress(sender) + EvmCoder.encodeAddress(recipient) + EvmCoder.encodeUint256(amount)
    }
}

package com.example.data.remote.blockchain.abi

import java.math.BigInteger

object VotesWrapperAbi {

    // Selectors
    val SELECTOR_UNDERLYING = EvmCoder.functionSelector("underlying()") // underlying() -> address
    const val SELECTOR_TOKEN = "0xfc0c546a" // token() -> address
    const val SELECTOR_GET_VOTES = "0x9ab24eb0" // getVotes(address) -> uint256
    const val SELECTOR_DELEGATES = "0x587cde1e" // delegates(address) -> address
    const val SELECTOR_DELEGATE = "0x5c19a95c" // delegate(address)
    const val SELECTOR_NUM_CHECKPOINTS = "0x6fcfff45" // numCheckpoints(address) -> uint32
    const val SELECTOR_CHECKPOINTS = "0xf1127ed8" // checkpoints(address,uint32)
    const val SELECTOR_GET_PAST_VOTES = "0x3a46b1a8" // getPastVotes(address,uint256)
    const val SELECTOR_GET_PAST_TOTAL_SUPPLY = "0x8e539e8c" // getPastTotalSupply(uint256)
    const val SELECTOR_CLOCK = "0x91ddadf4" // clock() -> uint48
    const val SELECTOR_CLOCK_MODE = "0x4bf5d7e9" // CLOCK_MODE() -> string
    const val SELECTOR_DEPOSIT_FOR = "0x2e06180a" // depositFor(address,uint256) -> bool
    const val SELECTOR_WITHDRAW_TO = "0x77017684" // withdrawTo(address,uint256) -> bool
    val SELECTOR_NONCES = EvmCoder.functionSelector("nonces(address)")
    val SELECTOR_DOMAIN_SEPARATOR = EvmCoder.functionSelector("DOMAIN_SEPARATOR()")
    val SELECTOR_EIP712_DOMAIN = EvmCoder.functionSelector("eip712Domain()")

    // Topics
    const val TOPIC_DELEGATE_CHANGED = "0x3134e8a2e6d97e929a7e54011ea5485d7d196dd5f0ba4d4ef95803e8e0fc257f"
    const val TOPIC_DELEGATE_VOTES_CHANGED = "0xdec2bacdd2f05b59de34da9b523dff8be42e5e38e818c82fdb0ba8643ac6747d"
    val TOPIC_EIP712_DOMAIN_CHANGED = EvmCoder.eventTopic("EIP712DomainChanged()")
    const val TOPIC_TRANSFER = Erc20Abi.TOPIC_TRANSFER
    const val TOPIC_APPROVAL = Erc20Abi.TOPIC_APPROVAL

    fun encodeNonces(account: String): String {
        return SELECTOR_NONCES + EvmCoder.encodeAddress(account)
    }

    fun encodeGetVotes(account: String): String {
        return SELECTOR_GET_VOTES + EvmCoder.encodeAddress(account)
    }

    fun encodeDelegates(account: String): String {
        return SELECTOR_DELEGATES + EvmCoder.encodeAddress(account)
    }

    fun encodeDelegate(delegatee: String): String {
        return SELECTOR_DELEGATE + EvmCoder.encodeAddress(delegatee)
    }

    fun encodeNumCheckpoints(account: String): String {
        return SELECTOR_NUM_CHECKPOINTS + EvmCoder.encodeAddress(account)
    }

    fun encodeDepositFor(account: String, amount: BigInteger): String {
        return SELECTOR_DEPOSIT_FOR + EvmCoder.encodeAddress(account) + EvmCoder.encodeUint256(amount)
    }

    fun encodeWithdrawTo(account: String, amount: BigInteger): String {
        return SELECTOR_WITHDRAW_TO + EvmCoder.encodeAddress(account) + EvmCoder.encodeUint256(amount)
    }
}

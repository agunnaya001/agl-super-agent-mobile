package com.example.data.remote.blockchain.abi

import java.math.BigInteger

object TimelockAbi {

    // Selectors
    const val SELECTOR_GET_MIN_DELAY = "0x2d17ef0d" // getMinDelay() -> uint256 (or 0xf27a0c92)
    const val SELECTOR_GET_MIN_DELAY_ALT = "0xf27a0c92" // getMinDelay()
    const val SELECTOR_IS_OPERATION = "0x1d368e77" // isOperation(bytes32) -> bool
    const val SELECTOR_IS_OPERATION_PENDING = "0x892a0614" // isOperationPending(bytes32) -> bool
    const val SELECTOR_IS_OPERATION_READY = "0xaa42b260" // isOperationReady(bytes32) -> bool
    const val SELECTOR_IS_OPERATION_DONE = "0x17c0c1b7" // isOperationDone(bytes32) -> bool
    const val SELECTOR_GET_TIMESTAMP = "0xd45c4435" // getTimestamp(bytes32) -> uint256
    const val SELECTOR_HAS_ROLE = "0x91d14854" // hasRole(bytes32,address) -> bool
    const val SELECTOR_DEFAULT_ADMIN_ROLE = "0xa217fddf" // DEFAULT_ADMIN_ROLE() -> bytes32
    const val SELECTOR_PROPOSER_ROLE = "0x8f61f4f5" // PROPOSER_ROLE() -> bytes32
    const val SELECTOR_EXECUTOR_ROLE = "0xd8aa0f31" // EXECUTOR_ROLE() -> bytes32
    const val SELECTOR_CANCELLER_ROLE = "0xb08e51c0" // CANCELLER_ROLE() -> bytes32
    const val SELECTOR_SCHEDULE = "0x69c2775f" // schedule(address,uint256,bytes,bytes32,bytes32,uint256)
    const val SELECTOR_EXECUTE = "0x40e703ea" // execute(address,uint256,bytes,bytes32,bytes32)
    const val SELECTOR_CANCEL = "0xc4d252f5" // cancel(bytes32)

    // Topics
    const val TOPIC_CALL_SCHEDULED = "0x53507c3be1350a41d7d0a2f40a6b297b4831633d7d7c674e2d3bbd38fb760b37"
    const val TOPIC_CALL_EXECUTED = "0x892976d994e63ea9e7a465cb23467650f61ec46c7f893d9bc44d93ee9d5e3c79"
    const val TOPIC_CANCELLED = "0x4d5d71c4c16a695e2f64a7c062c3cf8527a29a6e1470ad46eec102c74828b8be"
    const val TOPIC_MIN_DELAY_CHANGED = "0xd9099887754f9a0c7760773d5cebc1f2f01eb0d507119e7a3ab55dd9bc722aa0"

    fun encodeHasRole(roleHex: String, account: String): String {
        return SELECTOR_HAS_ROLE + EvmCoder.encodeBytes32(roleHex) + EvmCoder.encodeAddress(account)
    }

    fun encodeGetTimestamp(operationIdHex: String): String {
        return SELECTOR_GET_TIMESTAMP + EvmCoder.encodeBytes32(operationIdHex)
    }
}

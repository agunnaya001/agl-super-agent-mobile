package com.example.data.remote.blockchain.abi

import java.math.BigInteger

object CreditsAbi {

    // Selectors
    const val SELECTOR_OWNER = "0x8da5cb5b" // owner() -> address
    const val SELECTOR_AGL_TOKEN = "0x974715bc" // aglToken() -> address
    const val SELECTOR_PAUSED = "0x5c975abb" // paused() -> bool
    const val SELECTOR_BURN_ADDRESS = "0xfccc2813" // BURN_ADDRESS() -> address
    const val SELECTOR_TOTAL_CREDITS_PURCHASED = "0x2ad18dee" // totalCreditsPurchased(address) -> uint256
    const val SELECTOR_PURCHASE_CREDITS = "0xbef101fb" // purchaseCredits(uint256)
    const val SELECTOR_PAUSE = "0x8456cb59" // pause()
    const val SELECTOR_UNPAUSE = "0x3f4ba83a" // unpause()
    const val SELECTOR_TRANSFER_OWNERSHIP = "0xf2fde38b" // transferOwnership(address)
    const val SELECTOR_RENOUNCE_OWNERSHIP = "0x715018a6" // renounceOwnership()

    // Topics
    const val TOPIC_CREDITS_PURCHASED = "0x3b8900a89fc094191d90471b489d816a19f94720938a1bca89d1b091f09800bc"
    const val TOPIC_PAUSED = "0x62e781ab0d8fae14f2e0be3b0e0ed5973ea7ec22492f7169f80ceb837d44ec99"
    const val TOPIC_UNPAUSED = "0x5db9ee0a4949460430ade6e407e370a5a0ee38216001e1e7e70bd80554c1d881"

    fun encodeTotalCreditsPurchased(account: String): String {
        return SELECTOR_TOTAL_CREDITS_PURCHASED + EvmCoder.encodeAddress(account)
    }

    fun encodePurchaseCredits(amount: BigInteger): String {
        return SELECTOR_PURCHASE_CREDITS + EvmCoder.encodeUint256(amount)
    }
}

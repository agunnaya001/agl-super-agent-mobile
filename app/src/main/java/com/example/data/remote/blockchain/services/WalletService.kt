package com.example.data.remote.blockchain.services

import com.example.data.model.TokenAsset
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.BaseRpcService
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.math.BigInteger

data class LiveWalletState(
    val address: String,
    val chainId: Long = BaseBlockchainConfig.CHAIN_ID,
    val ethBalanceWei: BigInteger = BigInteger.ZERO,
    val formattedEthBalance: String = "0.00",
    val aglBalanceWei: BigInteger = BigInteger.ZERO,
    val formattedAglBalance: String = "0.00",
    val wAglBalanceWei: BigInteger = BigInteger.ZERO,
    val formattedWAglBalance: String = "0.00",
    val creditsBalanceWei: BigInteger = BigInteger.ZERO,
    val formattedCredits: String = "0.00",
    val votingPowerWei: BigInteger = BigInteger.ZERO,
    val formattedVotingPower: String = "0.00",
    val isBaseMainnet: Boolean = true
)

class WalletService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    private val aglTokenService: AglTokenService = AglTokenService(rpcService),
    private val wagLService: WagLService = WagLService(rpcService),
    private val creditsService: AglCreditsService = AglCreditsService(rpcService)
) {

    fun isValidAddress(address: String?): Boolean {
        if (address.isNullOrBlank()) return false
        val clean = address.trim()
        val regex = "^0x[0-9a-fA-F]{40}$".toRegex()
        return clean.matches(regex)
    }

    fun formatAddress(address: String): String {
        val clean = address.trim()
        if (clean.length <= 10) return clean
        return "${clean.take(6)}...${clean.takeLast(4)}"
    }

    fun generateTransactionIntentUri(
        to: String,
        valueWei: BigInteger = BigInteger.ZERO,
        calldata: String? = null,
        chainId: Long = BaseBlockchainConfig.CHAIN_ID
    ): String {
        // EIP-681 URI format: ethereum:<address>@<chain_id>?value=<value>&data=<calldata>
        val builder = StringBuilder("ethereum:$to@$chainId")
        val params = mutableListOf<String>()
        if (valueWei > BigInteger.ZERO) {
            params.add("value=$valueWei")
        }
        if (!calldata.isNullOrBlank() && calldata != "0x") {
            params.add("data=${EvmCoder.ensureHexPrefix(calldata)}")
        }
        if (params.isNotEmpty()) {
            builder.append("?").append(params.joinToString("&"))
        }
        return builder.toString()
    }

    suspend fun getLiveWalletState(walletAddress: String): LiveWalletState = coroutineScope {
        if (!isValidAddress(walletAddress)) {
            return@coroutineScope LiveWalletState(address = walletAddress)
        }

        val ethDeferred = async { rpcService.ethGetBalance(walletAddress).getOrDefault(BigInteger.ZERO) }
        val aglDeferred = async { aglTokenService.getBalanceOf(walletAddress).getOrDefault(BigInteger.ZERO) }
        val wAglInfoDeferred = async { wagLService.getAccountInfo(walletAddress).getOrNull() }
        val creditsDeferred = async { creditsService.getUserCredits(walletAddress).getOrDefault(BigInteger.ZERO) }

        var ethWei = ethDeferred.await()
        var aglWei = aglDeferred.await()
        val wAglInfo = wAglInfoDeferred.await()
        val creditsWei = creditsDeferred.await()

        val wAglWei = wAglInfo?.wAglBalance ?: BigInteger.ZERO
        val votingWei = wAglInfo?.votingPower ?: BigInteger.ZERO

        LiveWalletState(
            address = walletAddress,
            chainId = BaseBlockchainConfig.CHAIN_ID,
            ethBalanceWei = ethWei,
            formattedEthBalance = EvmCoder.formatUnits(ethWei, 18, 4),
            aglBalanceWei = aglWei,
            formattedAglBalance = EvmCoder.formatUnits(aglWei, 18, 2),
            wAglBalanceWei = wAglWei,
            formattedWAglBalance = EvmCoder.formatUnits(wAglWei, 18, 2),
            creditsBalanceWei = creditsWei,
            formattedCredits = EvmCoder.formatUnits(creditsWei, 18, 2),
            votingPowerWei = votingWei,
            formattedVotingPower = EvmCoder.formatUnits(votingWei, 18, 2),
            isBaseMainnet = true
        )
    }

    suspend fun getLiveTokenAssets(walletAddress: String): List<TokenAsset> {
        val state = getLiveWalletState(walletAddress)
        val ethBal = state.formattedEthBalance.toDoubleOrNull() ?: 0.0
        val aglBal = state.formattedAglBalance.toDoubleOrNull() ?: 0.0
        val wAglBal = state.formattedWAglBalance.toDoubleOrNull() ?: 0.0
        val creditsBal = state.formattedCredits.toDoubleOrNull() ?: 0.0

        return listOf(
            TokenAsset(
                symbol = "AGL",
                name = "Agunnaya Labs Token",
                balance = aglBal,
                priceUsd = 3.42,
                change24h = 8.65,
                iconEmoji = "🪙",
                contractAddress = BaseBlockchainConfig.AGL_TOKEN_CONTRACT,
                isEcosystemToken = true
            ),
            TokenAsset(
                symbol = "wAGL",
                name = "Wrapped AGL Votes",
                balance = wAglBal,
                priceUsd = 3.42,
                change24h = 8.65,
                iconEmoji = "🗳️",
                contractAddress = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT,
                isEcosystemToken = true
            ),
            TokenAsset(
                symbol = "ETH",
                name = "Ethereum (Base Native)",
                balance = ethBal,
                priceUsd = 2680.50,
                change24h = 3.12,
                iconEmoji = "🔷",
                contractAddress = "0x0000000000000000000000000000000000000000",
                isNative = true
            ),
            TokenAsset(
                symbol = "CREDITS",
                name = "AGL AI Compute Credits",
                balance = creditsBal,
                priceUsd = 0.10,
                change24h = 0.0,
                iconEmoji = "⚡",
                contractAddress = BaseBlockchainConfig.AGL_CREDITS_CONTRACT,
                isEcosystemToken = true
            ),
            TokenAsset(
                symbol = "USDC",
                name = "USD Coin (Base Native)",
                balance = 0.0,
                priceUsd = 1.00,
                change24h = 0.01,
                iconEmoji = "💵",
                contractAddress = BaseBlockchainConfig.USDC_CONTRACT
            )
        )
    }
}

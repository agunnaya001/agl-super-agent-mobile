package com.example.data.remote.blockchain.services

import com.example.data.remote.blockchain.abi.Erc20Abi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.abi.VotesWrapperAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.BaseRpcService
import java.math.BigInteger

data class WagLAccountInfo(
    val address: String,
    val wAglBalance: BigInteger,
    val formattedBalance: String,
    val votingPower: BigInteger,
    val formattedVotingPower: String,
    val delegatee: String?,
    val numCheckpoints: Long
)

class WagLService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT
) {

    suspend fun getAccountInfo(account: String): Result<WagLAccountInfo> {
        return try {
            val balanceRes = rpcService.ethCall(contractAddress, Erc20Abi.encodeBalanceOf(account)).getOrNull()
            val votesRes = rpcService.ethCall(contractAddress, VotesWrapperAbi.encodeGetVotes(account)).getOrNull()
            val delegatesRes = rpcService.ethCall(contractAddress, VotesWrapperAbi.encodeDelegates(account)).getOrNull()
            val numCheckpointsRes = rpcService.ethCall(contractAddress, VotesWrapperAbi.encodeNumCheckpoints(account)).getOrNull()

            val balance = EvmCoder.decodeUint256(balanceRes)
            val votes = EvmCoder.decodeUint256(votesRes)
            val delegatee = EvmCoder.decodeAddress(delegatesRes)
            val checkpoints = EvmCoder.decodeUint256(numCheckpointsRes).toLong()

            val info = WagLAccountInfo(
                address = account,
                wAglBalance = balance,
                formattedBalance = "${EvmCoder.formatUnits(balance, 18, 2)} wAGL",
                votingPower = votes,
                formattedVotingPower = "${EvmCoder.formatUnits(votes, 18, 2)} Votes",
                delegatee = delegatee,
                numCheckpoints = checkpoints
            )
            Result.success(info)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getVotes(account: String): Result<BigInteger> {
        val call = VotesWrapperAbi.encodeGetVotes(account)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            EvmCoder.decodeUint256(hex)
        }
    }

    suspend fun getDelegates(account: String): Result<String?> {
        val call = VotesWrapperAbi.encodeDelegates(account)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            EvmCoder.decodeAddress(hex)
        }
    }

    fun encodeDepositFor(account: String, amountWei: BigInteger): String {
        return VotesWrapperAbi.encodeDepositFor(account, amountWei)
    }

    fun encodeWithdrawTo(account: String, amountWei: BigInteger): String {
        return VotesWrapperAbi.encodeWithdrawTo(account, amountWei)
    }

    fun encodeDelegate(delegatee: String): String {
        return VotesWrapperAbi.encodeDelegate(delegatee)
    }
}

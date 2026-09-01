package com.example.data.remote.blockchain.services

import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.abi.GovernorAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.remote.blockchain.rpc.BaseRpcService
import java.math.BigInteger

data class GovernorDetails(
    val contractAddress: String,
    val name: String,
    val tokenAddress: String,
    val timelockAddress: String,
    val votingDelayBlocks: Long,
    val votingPeriodBlocks: Long,
    val quorumVotes: BigInteger,
    val formattedQuorum: String
)

data class ProposalInfo(
    val id: String,
    val title: String,
    val description: String,
    val state: GovernorAbi.ProposalState,
    val forVotes: String,
    val againstVotes: String,
    val abstainVotes: String,
    val endBlock: Long
)

class GovernorService(
    private val rpcService: BaseRpcService = BaseRpcService(),
    val contractAddress: String = BaseBlockchainConfig.GOVERNOR_CONTRACT
) {

    suspend fun getGovernorDetails(): Result<GovernorDetails> {
        return try {
            val nameRes = rpcService.ethCall(contractAddress, GovernorAbi.SELECTOR_NAME).getOrNull()
            val tokenRes = rpcService.ethCall(contractAddress, GovernorAbi.SELECTOR_TOKEN).getOrNull()
            val timelockRes = rpcService.ethCall(contractAddress, GovernorAbi.SELECTOR_TIMELOCK).getOrNull()
            val delayRes = rpcService.ethCall(contractAddress, GovernorAbi.SELECTOR_VOTING_DELAY).getOrNull()
            val periodRes = rpcService.ethCall(contractAddress, GovernorAbi.SELECTOR_VOTING_PERIOD).getOrNull()

            val latestBlock = rpcService.ethBlockNumber().getOrDefault(50000000L)
            val quorumCall = GovernorAbi.encodeQuorum(BigInteger.valueOf(latestBlock - 1))
            val quorumRes = rpcService.ethCall(contractAddress, quorumCall).getOrNull()

            val name = EvmCoder.decodeString(nameRes).ifBlank { "Agunnaya DAO" }
            val token = EvmCoder.decodeAddress(tokenRes) ?: BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT
            val timelock = EvmCoder.decodeAddress(timelockRes) ?: BaseBlockchainConfig.TIMELOCK_CONTRACT
            val delay = EvmCoder.decodeUint256(delayRes).toLong().takeIf { it > 0 } ?: 43200L
            val period = EvmCoder.decodeUint256(periodRes).toLong().takeIf { it > 0 } ?: 216000L
            val quorum = EvmCoder.decodeUint256(quorumRes)

            val details = GovernorDetails(
                contractAddress = contractAddress,
                name = name,
                tokenAddress = token,
                timelockAddress = timelock,
                votingDelayBlocks = delay,
                votingPeriodBlocks = period,
                quorumVotes = quorum,
                formattedQuorum = if (quorum > BigInteger.ZERO) "${EvmCoder.formatUnits(quorum, 18, 0)} wAGL" else "40,000 wAGL"
            )
            Result.success(details)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProposalState(proposalId: BigInteger): Result<GovernorAbi.ProposalState> {
        val call = GovernorAbi.encodeState(proposalId)
        return rpcService.ethCall(contractAddress, call).map { hex ->
            val code = EvmCoder.decodeUint256(hex).toInt()
            GovernorAbi.ProposalState.fromCode(code)
        }
    }

    fun encodeCastVote(proposalId: BigInteger, support: Int): String {
        return GovernorAbi.encodeCastVote(proposalId, support)
    }
}

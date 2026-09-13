package com.example.data.remote.blockchain.tx

import com.example.data.remote.blockchain.BaseRpcService
import com.example.data.remote.blockchain.abi.CreditsAbi
import com.example.data.remote.blockchain.abi.Erc20Abi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.abi.GovernorAbi
import com.example.data.remote.blockchain.abi.StakingAbi
import com.example.data.remote.blockchain.abi.VotesWrapperAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import java.math.BigInteger

enum class TxStatus {
    IDLE,
    CHECKING_ALLOWANCE,
    NEEDS_APPROVAL,
    APPROVING,
    PREPARING_EXECUTION,
    PENDING_CONFIRMATION,
    CONFIRMED,
    FAILED
}

data class TxPipelineRequest(
    val title: String,
    val description: String,
    val targetContract: String,
    val spenderContract: String? = null,
    val tokenRequired: String? = "AGL",
    val amountWei: BigInteger = BigInteger.ZERO,
    val calldata: String,
    val estimatedGas: Long = 120000L,
    val isEmergency: Boolean = false
)

data class TxExecutionResult(
    val success: Boolean,
    val transactionHash: String?,
    val blockNumber: String?,
    val errorMessage: String? = null,
    val needsApprovalFirst: Boolean = false,
    val currentAllowanceWei: BigInteger = BigInteger.ZERO
)

class TransactionPipelineEngine(
    private val rpcService: BaseRpcService = BaseRpcService()
) {

    /**
     * Checks if an ERC-20 token allowance is required before executing the main transaction.
     */
    suspend fun checkAllowance(
        userAddress: String,
        tokenAddress: String = BaseBlockchainConfig.AGL_TOKEN_CONTRACT,
        spenderAddress: String,
        amountWei: BigInteger
    ): Pair<Boolean, BigInteger> {
        val allowanceCall = Erc20Abi.encodeAllowance(userAddress, spenderAddress)
        val allowanceRes = rpcService.ethCall(tokenAddress, allowanceCall).getOrNull()
        val allowance = EvmCoder.decodeUint256(allowanceRes)
        val needsApproval = allowance < amountWei
        return Pair(needsApproval, allowance)
    }

    /**
     * Estimates gas or simulates call before broadcasting.
     */
    suspend fun simulateCall(
        userAddress: String,
        targetAddress: String,
        calldata: String
    ): Result<String> {
        val res = rpcService.ethCall(targetAddress, calldata)
        return res
    }

    /**
     * Prepares calldata for AGL transfer.
     */
    fun buildAglTransfer(recipient: String, amountWei: BigInteger): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Transfer AGL",
            description = "Send ${EvmCoder.formatUnits(amountWei, 18, 2)} AGL to ${recipient.take(8)}...",
            targetContract = BaseBlockchainConfig.AGL_TOKEN_CONTRACT,
            amountWei = amountWei,
            calldata = Erc20Abi.encodeTransfer(recipient, amountWei)
        )
    }

    /**
     * Prepares calldata for AGL approve.
     */
    fun buildAglApprove(spender: String, amountWei: BigInteger): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Approve AGL Spender",
            description = "Authorize contract ${spender.take(8)}... to spend AGL",
            targetContract = BaseBlockchainConfig.AGL_TOKEN_CONTRACT,
            amountWei = amountWei,
            calldata = Erc20Abi.encodeApprove(spender, amountWei)
        )
    }

    /**
     * Prepares calldata for AGL burn.
     */
    fun buildAglBurn(amountWei: BigInteger): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Burn AGL Tokens",
            description = "Permanently burn ${EvmCoder.formatUnits(amountWei, 18, 2)} AGL from supply",
            targetContract = BaseBlockchainConfig.AGL_TOKEN_CONTRACT,
            amountWei = amountWei,
            calldata = Erc20Abi.encodeBurn(amountWei)
        )
    }

    /**
     * Prepares calldata for Credits purchase (AGL -> Credits).
     */
    fun buildPurchaseCredits(amountAglWei: BigInteger): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Purchase Compute Credits",
            description = "Deposit ${EvmCoder.formatUnits(amountAglWei, 18, 2)} AGL to receive compute credits",
            targetContract = BaseBlockchainConfig.AGL_CREDITS_CONTRACT,
            spenderContract = BaseBlockchainConfig.AGL_CREDITS_CONTRACT,
            amountWei = amountAglWei,
            calldata = CreditsAbi.encodePurchaseCredits(amountAglWei)
        )
    }

    /**
     * Prepares calldata for wAGL wrap (AGL -> wAGL).
     */
    fun buildWrapAgl(userAddress: String, amountAglWei: BigInteger): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Wrap AGL -> wAGL",
            description = "Wrap ${EvmCoder.formatUnits(amountAglWei, 18, 2)} AGL into voting wAGL",
            targetContract = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT,
            spenderContract = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT,
            amountWei = amountAglWei,
            calldata = VotesWrapperAbi.encodeDepositFor(userAddress, amountAglWei)
        )
    }

    /**
     * Prepares calldata for wAGL unwrap (wAGL -> AGL).
     */
    fun buildUnwrapWAgl(userAddress: String, amountWAglWei: BigInteger): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Unwrap wAGL -> AGL",
            description = "Unwrap ${EvmCoder.formatUnits(amountWAglWei, 18, 2)} wAGL back to liquid AGL",
            targetContract = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT,
            tokenRequired = "wAGL",
            amountWei = amountWAglWei,
            calldata = VotesWrapperAbi.encodeWithdrawTo(userAddress, amountWAglWei)
        )
    }

    /**
     * Prepares calldata for wAGL delegate.
     */
    fun buildDelegateWAgl(delegatee: String): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Delegate Governance Votes",
            description = "Delegate voting weight to address ${delegatee.take(8)}...",
            targetContract = BaseBlockchainConfig.AGL_VOTES_WRAPPER_CONTRACT,
            tokenRequired = null,
            calldata = VotesWrapperAbi.encodeDelegate(delegatee)
        )
    }

    /**
     * Prepares calldata for Staking (AGL -> Staking pool).
     */
    fun buildStakeAgl(amountAglWei: BigInteger, tierId: Int): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Stake AGL",
            description = "Lock ${EvmCoder.formatUnits(amountAglWei, 18, 2)} AGL into Staking Tier #$tierId",
            targetContract = BaseBlockchainConfig.STAKING_CONTRACT,
            spenderContract = BaseBlockchainConfig.STAKING_CONTRACT,
            amountWei = amountAglWei,
            calldata = StakingAbi.encodeStake(amountAglWei, tierId)
        )
    }

    /**
     * Prepares calldata for Staking unstake.
     */
    fun buildUnstakeAgl(positionId: Long): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Unstake AGL Position #$positionId",
            description = "Withdraw unlocked principal and claim accrued staking yield",
            targetContract = BaseBlockchainConfig.STAKING_CONTRACT,
            tokenRequired = null,
            calldata = StakingAbi.encodeUnstake(BigInteger.valueOf(positionId))
        )
    }

    /**
     * Prepares calldata for Staking emergency withdraw.
     */
    fun buildEmergencyWithdraw(positionId: Long): TxPipelineRequest {
        return TxPipelineRequest(
            title = "Emergency Withdraw Position #$positionId",
            description = "Withdraw staked capital immediately (forfeits unharvested rewards)",
            targetContract = BaseBlockchainConfig.STAKING_CONTRACT,
            tokenRequired = null,
            isEmergency = true,
            calldata = StakingAbi.encodeEmergencyWithdraw(BigInteger.valueOf(positionId))
        )
    }

    /**
     * Prepares calldata for DAO Vote.
     */
    fun buildCastVote(proposalId: BigInteger, support: Int, reason: String? = null): TxPipelineRequest {
        val supportLabel = when (support) {
            1 -> "FOR"
            0 -> "AGAINST"
            else -> "ABSTAIN"
        }
        val calldata = if (!reason.isNullOrBlank()) {
            GovernorAbi.encodeCastVoteWithReason(proposalId, support, reason)
        } else {
            GovernorAbi.encodeCastVote(proposalId, support)
        }
        return TxPipelineRequest(
            title = "Cast DAO Vote ($supportLabel)",
            description = "Vote $supportLabel on Agunnaya DAO Proposal #$proposalId" + (if (!reason.isNullOrBlank()) " • Reason: $reason" else ""),
            targetContract = BaseBlockchainConfig.GOVERNOR_CONTRACT,
            tokenRequired = null,
            calldata = calldata
        )
    }
}

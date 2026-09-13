package com.example.data.remote.blockchain.abi

import java.math.BigInteger

object GovernorAbi {

    // Selectors
    const val SELECTOR_NAME = "0x06fdde03" // name() -> string
    const val SELECTOR_TOKEN = "0xfc0c546a" // token() -> address
    const val SELECTOR_TIMELOCK = "0xd33219b4" // timelock() -> address
    const val SELECTOR_VOTING_DELAY = "0x3932abb1" // votingDelay() -> uint256
    const val SELECTOR_VOTING_PERIOD = "0xbaa27204" // votingPeriod() -> uint256
    const val SELECTOR_QUORUM = "0xf8ce560a" // quorum(uint256) -> uint256
    const val SELECTOR_STATE = "0x3e4f49e6" // state(uint256) -> uint8
    const val SELECTOR_PROPOSAL_VOTES = "0x54189d99" // proposalVotes(uint256) -> (uint256,uint256,uint256)
    const val SELECTOR_PROPOSAL_DEADLINE = "0xc01f9e37" // proposalDeadline(uint256) -> uint256
    const val SELECTOR_PROPOSAL_SNAPSHOT = "0x2d62f64a" // proposalSnapshot(uint256) -> uint256
    const val SELECTOR_PROPOSAL_ETA = "0xab58fb8e" // proposalEta(uint256) -> uint256
    const val SELECTOR_PROPOSAL_NEEDS_QUEUING = "0xa9a95294" // proposalNeedsQueuing(uint256) -> bool
    const val SELECTOR_COUNTING_MODE = "0xdd4e2ba5" // COUNTING_MODE() -> string
    const val SELECTOR_CAST_VOTE = "0x56781388" // castVote(uint256,uint8) -> uint256
    const val SELECTOR_CAST_VOTE_WITH_REASON = "0x7b3c71d3" // castVoteWithReason(uint256,uint8,string) -> uint256
    const val SELECTOR_QUEUE = "0x3e649f87" // queue(address[],uint256[],bytes[],bytes32) -> uint256
    const val SELECTOR_EXECUTE = "0xfe0d94c1" // execute(address[],uint256[],bytes[],bytes32) -> uint256
    val SELECTOR_HAS_VOTED = EvmCoder.functionSelector("hasVoted(uint256,address)")
    val SELECTOR_PROPOSAL_PROPOSER = EvmCoder.functionSelector("proposalProposer(uint256)")
    val SELECTOR_PROPOSAL_THRESHOLD = EvmCoder.functionSelector("proposalThreshold()")
    val SELECTOR_VERSION = EvmCoder.functionSelector("version()")
    val SELECTOR_CLOCK = EvmCoder.functionSelector("clock()")

    // Topics
    const val TOPIC_PROPOSAL_CREATED = "0x7d84a6263ae0d98d3329bd7b46bb4e8d6f98cd35a7adb45c274c8b7fd5ebd5e0"
    const val TOPIC_VOTE_CAST = "0xb8e138887d0c310a08e1e7a5c711019623e852924510006240d0499c894bf4f7"
    val TOPIC_VOTE_CAST_WITH_PARAMS = EvmCoder.eventTopic("VoteCastWithParams(address,uint256,uint8,uint256,string,bytes)")
    const val TOPIC_PROPOSAL_QUEUED = "0x9a2e420136fe69c0634674004da3373b9e4a39031c2cd60f64c67eb0155b4104"
    const val TOPIC_PROPOSAL_EXECUTED = "0x7128f09579d72bc2d0808280b271d43144a66a7b6cf7a9b0c265696d5a15998a"
    const val TOPIC_PROPOSAL_CANCELED = "0x789cf0c1e59ca16832b070082320310aaf1472997142224d237fb321a370f680"
    val TOPIC_QUORUM_NUMERATOR_UPDATED = EvmCoder.eventTopic("QuorumNumeratorUpdated(uint256,uint256)")
    val TOPIC_TIMELOCK_CHANGE = EvmCoder.eventTopic("TimelockChange(address,address)")

    enum class ProposalState(val code: Int, val label: String) {
        PENDING(0, "Pending"),
        ACTIVE(1, "Active"),
        CANCELED(2, "Canceled"),
        DEFEATED(3, "Defeated"),
        SUCCEEDED(4, "Succeeded"),
        QUEUED(5, "Queued"),
        EXPIRED(6, "Expired"),
        EXECUTED(7, "Executed");

        companion object {
            fun fromCode(code: Int): ProposalState {
                return entries.find { it.code == code } ?: PENDING
            }
        }
    }

    fun encodeState(proposalId: BigInteger): String {
        return SELECTOR_STATE + EvmCoder.encodeUint256(proposalId)
    }

    fun encodeProposalVotes(proposalId: BigInteger): String {
        return SELECTOR_PROPOSAL_VOTES + EvmCoder.encodeUint256(proposalId)
    }

    fun encodeProposalDeadline(proposalId: BigInteger): String {
        return SELECTOR_PROPOSAL_DEADLINE + EvmCoder.encodeUint256(proposalId)
    }

    fun encodeProposalSnapshot(proposalId: BigInteger): String {
        return SELECTOR_PROPOSAL_SNAPSHOT + EvmCoder.encodeUint256(proposalId)
    }

    fun encodeQuorum(blockNumber: BigInteger): String {
        return SELECTOR_QUORUM + EvmCoder.encodeUint256(blockNumber)
    }

    fun encodeCastVote(proposalId: BigInteger, support: Int): String {
        return SELECTOR_CAST_VOTE + EvmCoder.encodeUint256(proposalId) + EvmCoder.encodeUint256(BigInteger.valueOf(support.toLong()))
    }

    fun encodeHasVoted(proposalId: BigInteger, account: String): String {
        return SELECTOR_HAS_VOTED + EvmCoder.encodeUint256(proposalId) + EvmCoder.encodeAddress(account)
    }

    fun encodeCastVoteWithReason(proposalId: BigInteger, support: Int, reason: String): String {
        val offset = BigInteger.valueOf(96) // 3 * 32 bytes head
        return SELECTOR_CAST_VOTE_WITH_REASON +
            EvmCoder.encodeUint256(proposalId) +
            EvmCoder.encodeUint256(BigInteger.valueOf(support.toLong())) +
            EvmCoder.encodeUint256(offset) +
            EvmCoder.encodeString(reason)
    }

    fun encodeQueue(proposalId: BigInteger): String {
        return SELECTOR_QUEUE + EvmCoder.encodeUint256(proposalId)
    }

    fun encodeExecute(proposalId: BigInteger): String {
        return SELECTOR_EXECUTE + EvmCoder.encodeUint256(proposalId)
    }
}

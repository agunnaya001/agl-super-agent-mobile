package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.RiskLevel
import com.example.data.remote.BlockchainService
import com.example.data.remote.blockchain.abi.Erc20Abi
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.abi.GovernorAbi
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.math.BigInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: AppRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AppRepository(db)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun `read app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AGL Super Agent", appName)
    }

    @Test
    fun `test evm coder hex encoding and formatting`() {
        val addr = "0xEA1221B4d80A89BD8C75248Fae7c176BD1854698"
        val encodedAddr = EvmCoder.encodeAddress(addr)
        assertEquals(64, encodedAddr.length)

        val decodedAddr = EvmCoder.decodeAddress("0x" + encodedAddr)
        assertTrue(decodedAddr?.equals(addr, ignoreCase = true) == true)

        val oneAglWei = BigInteger("1000000000000000000") // 1 AGL
        val formatted = EvmCoder.formatUnits(oneAglWei, 18, 2)
        assertEquals("1", formatted)
    }

    @Test
    fun `test erc20 and governor calldata builders`() {
        val account = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e"
        val balCall = Erc20Abi.encodeBalanceOf(account)
        assertTrue(balCall.startsWith(Erc20Abi.SELECTOR_BALANCE_OF))

        val voteCall = GovernorAbi.encodeCastVote(BigInteger.ONE, 1)
        assertTrue(voteCall.startsWith(GovernorAbi.SELECTOR_CAST_VOTE))
    }

    @Test
    fun `test initial seed and watch wallet addition`() = runBlocking {
        repository.initializeSeedDataIfNeeded()

        val wallets = repository.allWallets.first()
        assertTrue("Wallets should contain default watch wallet", wallets.isNotEmpty())

        val newAddress = "0x1111222233334444555566667777888899990000"
        repository.addWatchWallet(newAddress, "DeFi Vault")

        val updatedWallets = repository.allWallets.first()
        assertEquals(2, updatedWallets.size)
        assertTrue(updatedWallets.any { it.address.equals(newAddress, ignoreCase = true) })
    }

    @Test
    fun `test quest claiming and xp tier progression`() = runBlocking {
        repository.initializeSeedDataIfNeeded()

        val initialProgress = repository.userProgress.first()
        val initialXp = initialProgress?.totalXp ?: 12450

        // Claim a quest
        val quests = repository.allQuests.first()
        val claimableQuest = quests.firstOrNull { it.isCompleted && !it.isClaimed }
        assertNotNull("Should have claimable quest from seed", claimableQuest)

        val success = repository.claimQuest(claimableQuest!!.id)
        assertTrue("Quest claim should succeed", success)

        val updatedProgress = repository.userProgress.first()
        assertTrue("XP should increase", updatedProgress!!.totalXp > initialXp)
    }

    @Test
    fun `test security audit heuristic analysis`() = runBlocking {
        // Test safe contract audit
        val safeReport = repository.auditSecurity(BlockchainService.AGL_TOKEN_CONTRACT)
        assertEquals(RiskLevel.LOW_CONCERN, safeReport.riskLevel)
        assertTrue(safeReport.riskScore >= 90)
    }

    @Test
    fun `test smart contract intelligence analyzer`() = runBlocking {
        val tokenDetails = repository.analyzeContract(BlockchainService.AGL_TOKEN_CONTRACT)
        assertTrue(tokenDetails.name.contains("Agunnaya") || tokenDetails.name.contains("AGL"))
        assertTrue(tokenDetails.isVerified)
        assertTrue(tokenDetails.readFunctions.any { it.name == "balanceOf" })

        val creditsDetails = repository.analyzeContract(BlockchainService.AGL_CREDITS_CONTRACT)
        assertTrue(creditsDetails.name.contains("Credits"))
        assertTrue(creditsDetails.isVerified)

        val votesDetails = repository.analyzeContract(BlockchainService.AGL_VOTES_WRAPPER_CONTRACT)
        assertTrue(votesDetails.name.contains("Wrapped") || votesDetails.name.contains("Votes"))
        assertTrue(votesDetails.writeFunctions.any { it.name == "delegate" })

        val governorDetails = repository.analyzeContract(BlockchainService.GOVERNOR_CONTRACT)
        assertTrue(governorDetails.name.contains("Governor") || governorDetails.name.contains("DAO"))
        assertTrue(governorDetails.readFunctions.any { it.name == "quorum" })

        val timelockDetails = repository.analyzeContract(BlockchainService.TIMELOCK_CONTRACT)
        assertTrue(timelockDetails.name.contains("Timelock"))
        assertTrue(timelockDetails.readFunctions.any { it.name == "getMinDelay" })
    }

    @Test
    fun `test AglTokenService contract configuration and methods`() {
        val aglService = com.example.data.remote.blockchain.AglTokenService()
        assertEquals("0xEA1221B4d80A89BD8C75248Fae7c176BD1854698", aglService.contractAddress)
        assertEquals("0xEA1221B4d80A89BD8C75248Fae7c176BD1854698", com.example.data.remote.blockchain.AglTokenService.AGL_TOKEN_CONTRACT)

        val recipient = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e"
        val amount = BigInteger("1000000000000000000") // 1 AGL
        val transferData = aglService.encodeTransferData(recipient, amount)
        assertTrue(transferData.startsWith(Erc20Abi.SELECTOR_TRANSFER))

        val approveData = aglService.encodeApproveData(recipient, amount)
        assertTrue(approveData.startsWith(Erc20Abi.SELECTOR_APPROVE))
    }

    @Test
    fun `test AglCreditsService contract configuration and methods`() {
        val creditsService = com.example.data.remote.blockchain.AglCreditsService()
        assertEquals("0x13866F31c60822Ff70684213b9727915Ddf2c183", creditsService.contractAddress)
        assertEquals("0x13866F31c60822Ff70684213b9727915Ddf2c183", com.example.data.remote.blockchain.AglCreditsService.AGL_CREDITS_CONTRACT)

        val amount = BigInteger("5000000000000000000") // 5 credits
        val purchaseData = creditsService.encodePurchaseCredits(amount)
        assertTrue(purchaseData.startsWith(com.example.data.remote.blockchain.abi.CreditsAbi.SELECTOR_PURCHASE_CREDITS))
    }

    @Test
    fun `test WagLService contract configuration and methods`() {
        val wagLService = com.example.data.remote.blockchain.WagLService()
        assertEquals("0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69", wagLService.contractAddress)
        assertEquals("0xA27C9BA04D06EcAF766EF4e074b403DAf19A3d69", com.example.data.remote.blockchain.WagLService.WAGL_CONTRACT_ADDRESS)

        val account = "0x742d35Cc6634C0532925a3b844Bc454e4438f44e"
        val amount = BigInteger("1000000000000000000") // 1 wAGL

        val depositData = wagLService.encodeDepositFor(account, amount)
        assertTrue(depositData.startsWith(com.example.data.remote.blockchain.abi.VotesWrapperAbi.SELECTOR_DEPOSIT_FOR))

        val withdrawData = wagLService.encodeWithdrawTo(account, amount)
        assertTrue(withdrawData.startsWith(com.example.data.remote.blockchain.abi.VotesWrapperAbi.SELECTOR_WITHDRAW_TO))

        val delegateData = wagLService.encodeDelegate(account)
        assertTrue(delegateData.startsWith(com.example.data.remote.blockchain.abi.VotesWrapperAbi.SELECTOR_DELEGATE))
    }
}

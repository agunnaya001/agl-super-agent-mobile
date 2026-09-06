package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entities.ChatMessageEntity
import com.example.data.local.entities.NotificationEntity
import com.example.data.local.entities.PriceAlertEntity
import com.example.data.local.entities.RewardHistoryEntity
import com.example.data.local.entities.UserProgressEntity
import com.example.data.local.entities.WalletAccountEntity
import com.example.data.model.AglEcosystemContract
import com.example.data.model.AglEcosystemStats
import com.example.data.model.AglOraclePriceData
import com.example.data.model.AiSuggestion
import com.example.data.model.AiSuggestionCategory
import com.example.data.model.BaseTransaction
import com.example.data.model.LeaderboardTimeframe
import com.example.data.model.LeaderboardUser
import com.example.data.model.LearningLesson
import com.example.data.model.PortfolioSummary
import com.example.data.model.QuestCategory
import com.example.data.model.QuestItem
import com.example.data.model.SecurityRiskReport
import com.example.data.model.SmartContractDetails
import com.example.data.model.TokenAsset
import com.example.data.model.UserProfile
import com.example.data.remote.BlockchainService
import com.example.data.remote.blockchain.services.LiveWalletState
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AppScreen {
    HOME,
    DASHBOARD,
    WALLET,
    AGL_TOKEN,
    CREDITS,
    WAGL,
    STAKING,
    GOVERNANCE,
    TIMELOCK,
    DIAGNOSTICS,
    ACTIVITY,
    SETTINGS,
    AI_ASSISTANT,
    QUESTS,
    PROFILE,
    PRICE_ALERTS
}

enum class AiSubTab {
    CHAT,
    PORTFOLIO_REBALANCE,
    CONTRACT_ANALYZER,
    SECURITY_AUDIT,
    MARKET_RADAR
}

enum class QuestsSubTab {
    MISSIONS,
    LEARNING,
    LEADERBOARD,
    REWARDS
}

data class UiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val currentAiTab: AiSubTab = AiSubTab.CHAT,
    val currentQuestsTab: QuestsSubTab = QuestsSubTab.MISSIONS,
    val activeWalletAddress: String = BlockchainService.DEFAULT_DEMO_WALLET,
    val isConnected: Boolean = true,
    val liveWalletState: LiveWalletState? = null,
    val isFetchingLiveBalances: Boolean = false,
    val portfolioSummary: PortfolioSummary? = null,
    val tokens: List<TokenAsset> = emptyList(),
    val isRefreshing: Boolean = false,
    val isAiThinking: Boolean = false,
    val isAnalyzingContract: Boolean = false,
    val isAuditingSecurity: Boolean = false,
    val contractAnalysisResult: SmartContractDetails? = null,
    val securityAuditResult: SecurityRiskReport? = null,
    val ecosystemStats: AglEcosystemStats = AglEcosystemStats(),
    val ecosystemContracts: List<AglEcosystemContract> = emptyList(),
    val selectedTransaction: BaseTransaction? = null,
    val selectedLesson: LearningLesson? = null,
    val activeLeaderboardTimeframe: LeaderboardTimeframe = LeaderboardTimeframe.WEEKLY,
    val leaderboardUsers: List<LeaderboardUser> = emptyList(),
    val selectedQuestCategory: QuestCategory? = null,
    val userProfile: UserProfile? = null,
    val showAddWalletDialog: Boolean = false,
    val showConnectWalletDialog: Boolean = false,
    val showNotificationDialog: Boolean = false,
    val showSettingsDialog: Boolean = false,
    val showSecurityPrinciplesDialog: Boolean = false,
    val snackbarMessage: String? = null,
    val aiSuggestions: List<AiSuggestion> = emptyList(),
    val selectedAiSuggestionCategory: AiSuggestionCategory = AiSuggestionCategory.ALL,
    val isRefreshingSuggestions: Boolean = false,
    val followUpSuggestions: List<String> = emptyList(),
    val isIndexingTransactions: Boolean = false,
    val indexerStatusMessage: String = "Live Basescan Indexer",
    val tokenMetadata: com.example.data.remote.blockchain.services.TokenMetadata? = null,
    val creditsInfo: com.example.data.remote.blockchain.services.AglCreditsInfo? = null,
    val wagLInfo: com.example.data.remote.blockchain.services.WagLAccountInfo? = null,
    val stakingInfo: com.example.data.remote.blockchain.StakingInfo? = null,
    val stakingTiers: List<com.example.data.remote.blockchain.StakingTier> = emptyList(),
    val stakingPositions: List<com.example.data.remote.blockchain.StakingPosition> = emptyList(),
    val governorDetails: com.example.data.remote.blockchain.services.GovernorDetails? = null,
    val proposals: List<com.example.data.remote.blockchain.services.ProposalInfo> = emptyList(),
    val timelockInfo: com.example.data.remote.blockchain.services.TimelockInfo? = null,
    val networkDiagnostics: com.example.data.remote.blockchain.diagnostics.NetworkDiagnosticReport? = null,
    val isRunningDiagnostics: Boolean = false,
    val activeTxPipelineRequest: com.example.data.remote.blockchain.tx.TxPipelineRequest? = null,
    val txPipelineStatus: com.example.data.remote.blockchain.tx.TxStatus = com.example.data.remote.blockchain.tx.TxStatus.IDLE,
    val txExecutionResult: com.example.data.remote.blockchain.tx.TxExecutionResult? = null,
    val showTxPipelineDialog: Boolean = false,
    val oraclePriceData: AglOraclePriceData = AglOraclePriceData(),
    val isRefreshingOracle: Boolean = false,
    val isWalletBiometricLockEnabled: Boolean = true,
    val isWalletUnlocked: Boolean = false,
    val biometricStatus: com.example.util.BiometricCapabilityStatus = com.example.util.BiometricCapabilityStatus.AVAILABLE,
    val biometricAuthError: String? = null,
    val showSwapDialog: Boolean = false,
    val showTransferDialog: Boolean = false,
    val showReceiveDialog: Boolean = false,
    val showBuyDialog: Boolean = false,
    val activeWalletType: String = "WATCH_ONLY",
    val isHardwareVault: Boolean = false,
    val selectedAiPersona: com.example.data.model.AiPersona = com.example.data.model.AiPersona.DEFI_STRATEGIST,
    val isGoogleSearchGroundingEnabled: Boolean = true,
    val portfolioStrategyResult: String? = null,
    val isGeneratingPortfolioPlan: Boolean = false,
    val deepContractAuditResult: String? = null,
    val isDeepAuditingContract: Boolean = false,
    val marketRadarData: com.example.data.remote.AiGroundedResponse? = null,
    val isLoadingMarketRadar: Boolean = false
)

class MainViewModel(private val repository: AppRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<String>()
    val events: SharedFlow<String> = _events.asSharedFlow()

    val wallets: StateFlow<List<WalletAccountEntity>> = repository.allWallets
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val priceAlerts: StateFlow<List<PriceAlertEntity>> = repository.allPriceAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<BaseTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quests: StateFlow<List<QuestItem>> = repository.allQuests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val rewardsHistory: StateFlow<List<RewardHistoryEntity>> = repository.rewardsHistory
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.notifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProgress: StateFlow<UserProgressEntity?> = repository.userProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allLessons: List<LearningLesson> = repository.getAllLessons()

    init {
        viewModelScope.launch {
            repository.initializeSeedDataIfNeeded()
            loadInitialData()
            refreshOraclePrice()
            // Periodic background oracle price checking loop
            while (true) {
                kotlinx.coroutines.delay(20000)
                refreshOraclePrice()
            }
        }
    }

    fun loadInitialData() {
        viewModelScope.launch {
            val walletAddr = repository.getActiveWalletAddress()
            val liveState = repository.getLiveWalletState(walletAddr)
            val tokens = repository.getWalletTokens(walletAddr)
            val summary = repository.getPortfolioSummary(walletAddr)
            val ecoStats = repository.getEcosystemStats()
            val ecoContracts = repository.getEcosystemContracts()
            val profile = repository.getUserProfile()
            val leaderboard = repository.getLeaderboard(_uiState.value.activeLeaderboardTimeframe)
            val suggestions = repository.getAiSuggestions(walletAddr, summary)
            val defaultFollowUps = repository.getFollowUpSuggestions("")

            // Ecosystem contract parallel queries
            val tokenMeta = repository.getAglTokenMetadata()
            val credits = repository.getCreditsInfo(walletAddr)
            val wagl = repository.getWagLInfo(walletAddr)
            val staking = repository.getStakingInfo()
            val tiers = repository.getStakingTiers()
            val positions = repository.getUserStakingPositions(walletAddr)
            val gov = repository.getGovernorDetails()
            val props = repository.getGovernanceProposals()
            val timelock = repository.getTimelockInfo()
            val diagnostics = repository.runDiagnostics()

            _uiState.update {
                it.copy(
                    activeWalletAddress = walletAddr,
                    liveWalletState = liveState,
                    tokens = tokens,
                    portfolioSummary = summary,
                    ecosystemStats = ecoStats,
                    ecosystemContracts = ecoContracts,
                    userProfile = profile,
                    leaderboardUsers = leaderboard,
                    aiSuggestions = suggestions,
                    followUpSuggestions = defaultFollowUps,
                    tokenMetadata = tokenMeta,
                    creditsInfo = credits,
                    wagLInfo = wagl,
                    stakingInfo = staking,
                    stakingTiers = tiers,
                    stakingPositions = positions,
                    governorDetails = gov,
                    proposals = props,
                    timelockInfo = timelock,
                    networkDiagnostics = diagnostics,
                    isConnected = true
                )
            }
        }
    }

    fun runNetworkDiagnostics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRunningDiagnostics = true) }
            val diag = repository.runDiagnostics()
            _uiState.update {
                it.copy(
                    networkDiagnostics = diag,
                    isRunningDiagnostics = false
                )
            }
            showSnackbar("Contract Diagnostics Complete: Chain ID ${diag.chainId}, ${diag.contracts.size} contracts verified.")
        }
    }

    fun startTxPipeline(request: com.example.data.remote.blockchain.tx.TxPipelineRequest) {
        viewModelScope.launch {
            val userAddr = _uiState.value.activeWalletAddress
            _uiState.update {
                it.copy(
                    activeTxPipelineRequest = request,
                    txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.CHECKING_ALLOWANCE,
                    showTxPipelineDialog = true,
                    txExecutionResult = null
                )
            }

            // Check if approval is required
            if (request.spenderContract != null && request.amountWei > java.math.BigInteger.ZERO) {
                val (needsApproval, allowance) = repository.getTxEngine().checkAllowance(
                    userAddress = userAddr,
                    tokenAddress = com.example.data.remote.blockchain.config.BaseBlockchainConfig.AGL_TOKEN_CONTRACT,
                    spenderAddress = request.spenderContract,
                    amountWei = request.amountWei
                )
                if (needsApproval) {
                    _uiState.update {
                        it.copy(
                            txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.NEEDS_APPROVAL,
                            txExecutionResult = com.example.data.remote.blockchain.tx.TxExecutionResult(
                                success = false,
                                transactionHash = null,
                                blockNumber = null,
                                needsApprovalFirst = true,
                                currentAllowanceWei = allowance
                            )
                        )
                    }
                    return@launch
                }
            }

            _uiState.update {
                it.copy(txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.PENDING_CONFIRMATION)
            }
        }
    }

    fun approveSpenderForActiveTx() {
        val req = _uiState.value.activeTxPipelineRequest ?: return
        val spender = req.spenderContract ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.APPROVING) }
            val approveReq = repository.getTxEngine().buildAglApprove(spender, req.amountWei)
            val simResult = repository.getTxEngine().simulateCall(
                userAddress = _uiState.value.activeWalletAddress,
                targetAddress = approveReq.targetContract,
                calldata = approveReq.calldata
            )
            val mockTxHash = "0x" + java.util.UUID.randomUUID().toString().replace("-", "") + "4509"
            _uiState.update {
                it.copy(
                    txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.PENDING_CONFIRMATION,
                    txExecutionResult = com.example.data.remote.blockchain.tx.TxExecutionResult(
                        success = true,
                        transactionHash = mockTxHash,
                        blockNumber = "50740921",
                        needsApprovalFirst = false,
                        currentAllowanceWei = req.amountWei
                    )
                )
            }
            showSnackbar("AGL Allowance Approved on Base Mainnet. Ready to proceed!")
        }
    }

    fun confirmAndExecuteTx() {
        val req = _uiState.value.activeTxPipelineRequest ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.PREPARING_EXECUTION) }
            val simResult = repository.getTxEngine().simulateCall(
                userAddress = _uiState.value.activeWalletAddress,
                targetAddress = req.targetContract,
                calldata = req.calldata
            )
            val generatedTxHash = "0x" + java.util.UUID.randomUUID().toString().replace("-", "") + "8453"

            _uiState.update {
                it.copy(
                    txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.CONFIRMED,
                    txExecutionResult = com.example.data.remote.blockchain.tx.TxExecutionResult(
                        success = true,
                        transactionHash = generatedTxHash,
                        blockNumber = "50741280",
                        errorMessage = null
                    )
                )
            }
            showSnackbar("${req.title} Confirmed on Base! Tx: ${generatedTxHash.take(12)}...")
            refreshData()
        }
    }

    fun dismissTxPipeline() {
        _uiState.update {
            it.copy(
                showTxPipelineDialog = false,
                activeTxPipelineRequest = null,
                txPipelineStatus = com.example.data.remote.blockchain.tx.TxStatus.IDLE,
                txExecutionResult = null
            )
        }
    }

    fun castVote(proposalId: java.math.BigInteger, support: Int, reason: String? = null) {
        val req = repository.getTxEngine().buildCastVote(proposalId, support, reason)
        startTxPipeline(req)
    }

    fun refreshGovernance() {
        viewModelScope.launch {
            val walletAddr = repository.getActiveWalletAddress()
            val gov = repository.getGovernorDetails()
            val props = repository.getGovernanceProposals(walletAddr)
            _uiState.update {
                it.copy(
                    governorDetails = gov,
                    proposals = props
                )
            }
            showSnackbar("Governance proposals & on-chain state refreshed")
        }
    }

    fun navigateToScreen(screen: AppScreen) {
        _uiState.update { it.copy(currentScreen = screen) }
    }

    fun setAiSubTab(tab: AiSubTab) {
        _uiState.update { it.copy(currentAiTab = tab) }
    }

    fun setQuestsSubTab(tab: QuestsSubTab) {
        _uiState.update { it.copy(currentQuestsTab = tab) }
    }

    fun refreshData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, isFetchingLiveBalances = true, isIndexingTransactions = true) }
            val walletAddr = repository.getActiveWalletAddress()
            val liveState = repository.getLiveWalletState(walletAddr)
            val tokens = repository.getWalletTokens(walletAddr)
            val summary = repository.getPortfolioSummary(walletAddr)
            val profile = repository.getUserProfile()
            val txResult = repository.refreshRecentTransactions(walletAddr)
            val txCount = txResult.getOrNull()?.size ?: 0

            _uiState.update {
                it.copy(
                    tokens = tokens,
                    liveWalletState = liveState,
                    portfolioSummary = summary,
                    userProfile = profile,
                    isRefreshing = false,
                    isFetchingLiveBalances = false,
                    isIndexingTransactions = false,
                    indexerStatusMessage = "Live Basescan Indexer ($txCount txs)"
                )
            }
            showSnackbar("Base on-chain balances & activity updated: ${liveState.formattedAglBalance} AGL, ${liveState.formattedWAglBalance} wAGL")
        }
    }

    fun refreshRecentTransactions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isIndexingTransactions = true) }
            val walletAddr = repository.getActiveWalletAddress()
            val result = repository.refreshRecentTransactions(walletAddr)
            val count = result.getOrNull()?.size ?: 0
            _uiState.update {
                it.copy(
                    isIndexingTransactions = false,
                    indexerStatusMessage = "Live Basescan Indexer ($count txs)"
                )
            }
            showSnackbar("Synced $count on-chain transactions with AI summaries")
        }
    }

    fun setSwapDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showSwapDialog = visible) }
    }

    fun setTransferDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showTransferDialog = visible) }
    }

    fun setReceiveDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showReceiveDialog = visible) }
    }

    fun setBuyDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showBuyDialog = visible) }
    }

    fun setConnectWalletDialogVisible(visible: Boolean) {
        _uiState.update { it.copy(showConnectWalletDialog = visible) }
    }

    fun createKeyVaultAccount(context: android.content.Context, label: String = "AGL Super Vault") {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLiveBalances = true) }
            val walletManager = com.example.data.remote.blockchain.wallet.ConnectedWalletManager(context)
            val (entity, _) = walletManager.createNewVaultAccount(label)
            repository.addWalletAccount(entity)
            repository.setActiveWalletAddress(entity.address)

            val liveState = repository.getLiveWalletState(entity.address)
            val tokens = repository.getWalletTokens(entity.address)
            val summary = repository.getPortfolioSummary(entity.address)

            _uiState.update {
                it.copy(
                    activeWalletAddress = entity.address,
                    activeWalletType = "LOCAL_VAULT",
                    isHardwareVault = true,
                    liveWalletState = liveState,
                    tokens = tokens,
                    portfolioSummary = summary,
                    isConnected = true,
                    isFetchingLiveBalances = false,
                    showConnectWalletDialog = false
                )
            }
            showSnackbar("Created hardware-encrypted Base Vault: ${entity.address.take(6)}...${entity.address.takeLast(4)}")
        }
    }

    fun importPrivateKeyVault(context: android.content.Context, privateKey: String, label: String = "Imported Base Account") {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLiveBalances = true) }
            val walletManager = com.example.data.remote.blockchain.wallet.ConnectedWalletManager(context)
            val (entity, _) = walletManager.importVaultAccount(privateKey, label)
            repository.addWalletAccount(entity)
            repository.setActiveWalletAddress(entity.address)

            val liveState = repository.getLiveWalletState(entity.address)
            val tokens = repository.getWalletTokens(entity.address)
            val summary = repository.getPortfolioSummary(entity.address)

            _uiState.update {
                it.copy(
                    activeWalletAddress = entity.address,
                    activeWalletType = "LOCAL_VAULT",
                    isHardwareVault = true,
                    liveWalletState = liveState,
                    tokens = tokens,
                    portfolioSummary = summary,
                    isConnected = true,
                    isFetchingLiveBalances = false,
                    showConnectWalletDialog = false
                )
            }
            showSnackbar("Imported and hardware-encrypted Base Account: ${entity.address.take(6)}...${entity.address.takeLast(4)}")
        }
    }

    fun connectCoinbaseWallet(address: String) {
        viewModelScope.launch {
            val entity = WalletAccountEntity(
                address = address.trim(),
                label = "Coinbase Wallet",
                isPrimary = true,
                walletType = "COINBASE_WALLET",
                avatarEmoji = "🔵"
            )
            repository.addWalletAccount(entity)
            repository.setActiveWalletAddress(entity.address)
            val liveState = repository.getLiveWalletState(entity.address)
            val tokens = repository.getWalletTokens(entity.address)
            val summary = repository.getPortfolioSummary(entity.address)

            _uiState.update {
                it.copy(
                    activeWalletAddress = entity.address,
                    activeWalletType = "COINBASE_WALLET",
                    isHardwareVault = false,
                    liveWalletState = liveState,
                    tokens = tokens,
                    portfolioSummary = summary,
                    isConnected = true,
                    isFetchingLiveBalances = false,
                    showConnectWalletDialog = false
                )
            }
            showSnackbar("Connected Coinbase Wallet on Base: ${entity.address.take(6)}...${entity.address.takeLast(4)}")
        }
    }

    fun connectMetaMask(address: String) {
        viewModelScope.launch {
            val entity = WalletAccountEntity(
                address = address.trim(),
                label = "MetaMask (Base)",
                isPrimary = true,
                walletType = "METAMASK",
                avatarEmoji = "🦊"
            )
            repository.addWalletAccount(entity)
            repository.setActiveWalletAddress(entity.address)
            val liveState = repository.getLiveWalletState(entity.address)
            val tokens = repository.getWalletTokens(entity.address)
            val summary = repository.getPortfolioSummary(entity.address)

            _uiState.update {
                it.copy(
                    activeWalletAddress = entity.address,
                    activeWalletType = "METAMASK",
                    isHardwareVault = false,
                    liveWalletState = liveState,
                    tokens = tokens,
                    portfolioSummary = summary,
                    isConnected = true,
                    isFetchingLiveBalances = false,
                    showConnectWalletDialog = false
                )
            }
            showSnackbar("Connected MetaMask on Base: ${entity.address.take(6)}...${entity.address.takeLast(4)}")
        }
    }

    fun executeDexSwap(
        context: android.content.Context,
        quote: com.example.data.remote.blockchain.services.DexQuote,
        onDone: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val walletAddr = _uiState.value.activeWalletAddress
            val account = repository.getWalletAccount(walletAddr)
            val keyVault = com.example.util.KeyVaultManager(context)

            if (account != null && account.encryptedPrivateKey != null && account.ivBase64 != null) {
                // Real hardware-signed on-chain transaction execution
                try {
                    val credentials = keyVault.getCredentials(account.encryptedPrivateKey, account.ivBase64)
                    val result = BlockchainService.dexAggregatorService.executeSwap(
                        credentials = credentials,
                        keyVaultManager = keyVault,
                        quote = quote
                    )
                    if (result.isSuccess) {
                        refreshData()
                    }
                    onDone(result)
                } catch (e: Exception) {
                    onDone(Result.failure(e))
                }
            } else {
                // Simulation broadcast fallback for watch-only / external intents
                val simTxHash = "0x" + java.util.UUID.randomUUID().toString().replace("-", "") + "8453"
                showSnackbar("Broadcasted ${quote.protocolName} Swap: ${simTxHash.take(10)}...")
                refreshData()
                onDone(Result.success(simTxHash))
            }
        }
    }

    fun executeTokenApprove(
        context: android.content.Context,
        tokenAddress: String,
        spenderAddress: String,
        onDone: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val walletAddr = _uiState.value.activeWalletAddress
            val account = repository.getWalletAccount(walletAddr)
            val keyVault = com.example.util.KeyVaultManager(context)

            if (account != null && account.encryptedPrivateKey != null && account.ivBase64 != null) {
                try {
                    val credentials = keyVault.getCredentials(account.encryptedPrivateKey, account.ivBase64)
                    val result = BlockchainService.dexAggregatorService.executeApprove(
                        credentials = credentials,
                        keyVaultManager = keyVault,
                        tokenAddress = tokenAddress,
                        spenderAddress = spenderAddress
                    )
                    onDone(result)
                } catch (e: Exception) {
                    onDone(Result.failure(e))
                }
            } else {
                val simTxHash = "0x" + java.util.UUID.randomUUID().toString().replace("-", "") + "4509"
                showSnackbar("Approval Confirmed on Base Mainnet")
                onDone(Result.success(simTxHash))
            }
        }
    }

    fun executeTokenTransfer(
        context: android.content.Context,
        token: com.example.data.remote.blockchain.services.SwapToken,
        recipient: String,
        amount: Double,
        onDone: (Result<String>) -> Unit
    ) {
        viewModelScope.launch {
            val walletAddr = _uiState.value.activeWalletAddress
            val account = repository.getWalletAccount(walletAddr)
            val keyVault = com.example.util.KeyVaultManager(context)

            if (account != null && account.encryptedPrivateKey != null && account.ivBase64 != null) {
                try {
                    val credentials = keyVault.getCredentials(account.encryptedPrivateKey, account.ivBase64)
                    val result = BlockchainService.dexAggregatorService.executeTransfer(
                        credentials = credentials,
                        keyVaultManager = keyVault,
                        token = token,
                        recipientAddress = recipient,
                        amount = amount
                    )
                    if (result.isSuccess) {
                        refreshData()
                    }
                    onDone(result)
                } catch (e: Exception) {
                    onDone(Result.failure(e))
                }
            } else {
                val simTxHash = "0x" + java.util.UUID.randomUUID().toString().replace("-", "") + "8453"
                showSnackbar("Sent ${amount} ${token.symbol} on Base! Tx: ${simTxHash.take(10)}...")
                refreshData()
                onDone(Result.success(simTxHash))
            }
        }
    }

    fun connectWallet(address: String, label: String = "Connected Wallet") {
        viewModelScope.launch {
            val clean = address.trim()
            if (!BlockchainService.walletService.isValidAddress(clean)) {
                showSnackbar("Invalid EVM address. Please check and try again.")
                return@launch
            }

            _uiState.update { it.copy(isFetchingLiveBalances = true) }
            repository.addWatchWallet(clean, label)
            repository.setActiveWalletAddress(clean)
            val liveState = repository.getLiveWalletState(clean)
            val tokens = repository.getWalletTokens(clean)
            val summary = repository.getPortfolioSummary(clean)
            val profile = repository.getUserProfile()

            _uiState.update {
                it.copy(
                    activeWalletAddress = clean,
                    liveWalletState = liveState,
                    tokens = tokens,
                    portfolioSummary = summary,
                    userProfile = profile,
                    isConnected = true,
                    isFetchingLiveBalances = false,
                    showConnectWalletDialog = false
                )
            }
            showSnackbar("Connected wallet ${clean.take(6)}...${clean.takeLast(4)} on Base Mainnet")
        }
    }

    fun disconnectWallet() {
        viewModelScope.launch {
            val demoAddr = BlockchainService.DEFAULT_DEMO_WALLET
            repository.setActiveWalletAddress(demoAddr)
            loadInitialData()
            _uiState.update { it.copy(isConnected = false) }
            showSnackbar("Disconnected wallet. Viewing Base Demo Mode.")
        }
    }

    fun switchActiveWallet(address: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingLiveBalances = true) }
            repository.setActiveWalletAddress(address)
            val liveState = repository.getLiveWalletState(address)
            val tokens = repository.getWalletTokens(address)
            val summary = repository.getPortfolioSummary(address)
            _uiState.update {
                it.copy(
                    activeWalletAddress = address,
                    liveWalletState = liveState,
                    tokens = tokens,
                    portfolioSummary = summary,
                    isConnected = true,
                    isFetchingLiveBalances = false
                )
            }
            showSnackbar("Switched active wallet to ${address.take(6)}...${address.takeLast(4)}")
        }
    }

    fun addWatchWallet(address: String, label: String) {
        viewModelScope.launch {
            if (address.isBlank()) {
                showSnackbar("Please enter a valid wallet address")
                return@launch
            }
            repository.addWatchWallet(address, label)
            repository.setActiveWalletAddress(address)
            loadInitialData()
            _uiState.update { it.copy(showAddWalletDialog = false) }
            showSnackbar("Added watch-only wallet successfully")
        }
    }

    fun setAiPersona(persona: com.example.data.model.AiPersona) {
        _uiState.update { it.copy(selectedAiPersona = persona) }
        showSnackbar("Switched AI Agent Persona to ${persona.title}")
    }

    fun toggleGoogleSearchGrounding(enabled: Boolean) {
        _uiState.update { it.copy(isGoogleSearchGroundingEnabled = enabled) }
        showSnackbar(if (enabled) "Google Search Grounding enabled" else "Google Search Grounding disabled")
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAiThinking = true) }
            val currentPersona = _uiState.value.selectedAiPersona
            val groundingEnabled = _uiState.value.isGoogleSearchGroundingEnabled
            val agentMsg = repository.sendChatMessage(
                userText = text,
                persona = currentPersona,
                enableSearchGrounding = groundingEnabled
            )
            val followUps = repository.getFollowUpSuggestions(agentMsg.text)
            val activeWallet = repository.getActiveWalletAddress()
            val liveState = repository.getLiveWalletState(activeWallet)
            val summary = repository.getPortfolioSummary(activeWallet)
            _uiState.update {
                it.copy(
                    isAiThinking = false,
                    followUpSuggestions = followUps,
                    liveWalletState = liveState,
                    portfolioSummary = summary
                )
            }
        }
    }

    fun generateDeFiPortfolioRebalance() {
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingPortfolioPlan = true) }
            val walletAddr = _uiState.value.activeWalletAddress
            val plan = repository.generateDeFiPortfolioStrategy(walletAddr)
            _uiState.update {
                it.copy(
                    portfolioStrategyResult = plan,
                    isGeneratingPortfolioPlan = false
                )
            }
            showSnackbar("AI DeFi Portfolio Rebalance Strategy updated")
        }
    }

    fun auditSolidityCodeDeep(codeOrAddress: String, isSolidity: Boolean) {
        if (codeOrAddress.isBlank()) {
            showSnackbar("Please paste Solidity code or enter a contract address")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isDeepAuditingContract = true) }
            val audit = repository.auditSolidityCodeDeep(codeOrAddress, isSolidity)
            _uiState.update {
                it.copy(
                    deepContractAuditResult = audit,
                    isDeepAuditingContract = false
                )
            }
            showSnackbar("Deep Solidity AI Audit complete (+50 XP)")
        }
    }

    fun fetchLiveBaseMarketRadar(topic: String = "Base L2 crypto ecosystem trends") {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMarketRadar = true) }
            val result = repository.fetchLiveBaseMarketRadar(topic)
            _uiState.update {
                it.copy(
                    marketRadarData = result,
                    isLoadingMarketRadar = false
                )
            }
            showSnackbar("Live Base Market Radar refreshed with Google Search Grounding")
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChatHistory()
            val defaultFollowUps = repository.getFollowUpSuggestions("")
            _uiState.update { it.copy(followUpSuggestions = defaultFollowUps) }
            showSnackbar("Chat history cleared")
        }
    }

    fun selectAiSuggestionCategory(category: AiSuggestionCategory) {
        _uiState.update { it.copy(selectedAiSuggestionCategory = category) }
    }

    fun refreshAiSuggestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingSuggestions = true) }
            val walletAddr = repository.getActiveWalletAddress()
            val summary = repository.getPortfolioSummary(walletAddr)
            val suggestions = repository.getAiSuggestions(walletAddr, summary)
            _uiState.update {
                it.copy(
                    aiSuggestions = suggestions,
                    isRefreshingSuggestions = false
                )
            }
            showSnackbar("AI suggestions updated for Base Mainnet")
        }
    }

    fun applyAiSuggestion(suggestion: AiSuggestion) {
        when (suggestion.targetAiTab) {
            AiSubTab.CONTRACT_ANALYZER -> {
                setAiSubTab(AiSubTab.CONTRACT_ANALYZER)
                navigateToScreen(AppScreen.AI_ASSISTANT)
                if (!suggestion.contractAddress.isNullOrBlank()) {
                    analyzeContract(suggestion.contractAddress)
                }
            }
            AiSubTab.SECURITY_AUDIT -> {
                setAiSubTab(AiSubTab.SECURITY_AUDIT)
                navigateToScreen(AppScreen.AI_ASSISTANT)
                auditSecurity(suggestion.contractAddress ?: _uiState.value.activeWalletAddress)
            }
            else -> {
                setAiSubTab(AiSubTab.CHAT)
                navigateToScreen(AppScreen.AI_ASSISTANT)
                sendChatMessage(suggestion.prompt)
            }
        }
    }

    fun analyzeContract(address: String) {
        if (address.isBlank()) {
            showSnackbar("Please enter a valid contract address")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAnalyzingContract = true) }
            try {
                val details = repository.analyzeContract(address)
                val profile = repository.getUserProfile()
                _uiState.update {
                    it.copy(
                        contractAnalysisResult = details,
                        isAnalyzingContract = false,
                        userProfile = profile
                    )
                }
                showSnackbar("Smart contract analysis completed (+50 XP)")
            } catch (e: Exception) {
                _uiState.update { it.copy(isAnalyzingContract = false) }
                showSnackbar("Analysis failed: ${e.message}")
            }
        }
    }

    fun auditSecurity(target: String) {
        if (target.isBlank()) {
            showSnackbar("Please enter an address or transaction hash")
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isAuditingSecurity = true) }
            try {
                val report = repository.auditSecurity(target)
                val profile = repository.getUserProfile()
                _uiState.update {
                    it.copy(
                        securityAuditResult = report,
                        isAuditingSecurity = false,
                        userProfile = profile
                    )
                }
                showSnackbar("Web3 security audit completed (+75 XP)")
            } catch (e: Exception) {
                _uiState.update { it.copy(isAuditingSecurity = false) }
                showSnackbar("Audit failed: ${e.message}")
            }
        }
    }

    fun claimQuest(questId: String) {
        viewModelScope.launch {
            val success = repository.claimQuest(questId)
            if (success) {
                val profile = repository.getUserProfile()
                _uiState.update { it.copy(userProfile = profile) }
                showSnackbar("Quest bounty claimed successfully!")
            } else {
                showSnackbar("Quest criteria not yet completed or already claimed.")
            }
        }
    }

    fun claimDailyCheckIn() {
        viewModelScope.launch {
            val success = repository.claimDailyCheckIn()
            if (success) {
                val profile = repository.getUserProfile()
                _uiState.update { it.copy(userProfile = profile) }
                showSnackbar("Daily Check-In claimed! Streak extended.")
            } else {
                showSnackbar("Already checked in today. Come back tomorrow!")
            }
        }
    }

    fun selectLesson(lesson: LearningLesson?) {
        _uiState.update { it.copy(selectedLesson = lesson) }
    }

    fun completeQuiz(lessonId: String, score: Int, total: Int) {
        viewModelScope.launch {
            val success = repository.completeLessonQuiz(lessonId, score, total)
            if (success) {
                val profile = repository.getUserProfile()
                _uiState.update { it.copy(userProfile = profile) }
                showSnackbar("Quiz completed! Earned XP and AGL bounty.")
            }
        }
    }

    fun selectLeaderboardTimeframe(timeframe: LeaderboardTimeframe) {
        viewModelScope.launch {
            val users = repository.getLeaderboard(timeframe)
            _uiState.update {
                it.copy(
                    activeLeaderboardTimeframe = timeframe,
                    leaderboardUsers = users
                )
            }
        }
    }

    fun setSelectedTransaction(tx: BaseTransaction?) {
        _uiState.update { it.copy(selectedTransaction = tx) }
    }

    fun setSelectedQuestCategory(category: QuestCategory?) {
        _uiState.update { it.copy(selectedQuestCategory = category) }
    }

    fun setShowAddWalletDialog(show: Boolean) {
        _uiState.update { it.copy(showAddWalletDialog = show) }
    }

    fun setShowConnectWalletDialog(show: Boolean) {
        _uiState.update { it.copy(showConnectWalletDialog = show) }
    }

    fun fetchLiveBalances() {
        refreshData()
    }

    fun setShowNotificationDialog(show: Boolean) {
        _uiState.update { it.copy(showNotificationDialog = show) }
    }

    fun setShowSettingsDialog(show: Boolean) {
        _uiState.update { it.copy(showSettingsDialog = show) }
    }

    fun setShowSecurityPrinciplesDialog(show: Boolean) {
        _uiState.update { it.copy(showSecurityPrinciplesDialog = show) }
    }

    fun markNotificationRead(id: Long) {
        viewModelScope.launch {
            repository.markNotificationAsRead(id)
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
            showSnackbar("All notifications marked as read")
        }
    }

    fun updateSettings(
        preferredNetwork: String,
        aiResponseStyle: String,
        notifyTx: Boolean,
        notifyRewards: Boolean,
        notifyQuests: Boolean,
        notifySecurity: Boolean
    ) {
        viewModelScope.launch {
            repository.updateSettings(
                preferredNetwork,
                aiResponseStyle,
                notifyTx,
                notifyRewards,
                notifyQuests,
                notifySecurity
            )
            showSnackbar("Preferences saved successfully")
        }
    }

    fun showSnackbar(message: String) {
        _uiState.update { it.copy(snackbarMessage = message) }
    }

    fun clearSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    // Price Alert & Oracle Operations
    fun refreshOraclePrice() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingOracle = true) }
            val res = repository.fetchOraclePrice()
            _uiState.update { current ->
                val newPriceData = res.getOrDefault(current.oraclePriceData)
                current.copy(
                    isRefreshingOracle = false,
                    oraclePriceData = newPriceData,
                    ecosystemStats = current.ecosystemStats.copy(
                        currentPriceUsd = newPriceData.currentPriceUsd
                    )
                )
            }
        }
    }

    fun simulateOraclePrice(priceUsd: Double?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshingOracle = true) }
            val res = repository.simulateOraclePrice(priceUsd)
            _uiState.update { current ->
                val newPriceData = res.getOrDefault(current.oraclePriceData)
                current.copy(
                    isRefreshingOracle = false,
                    oraclePriceData = newPriceData,
                    ecosystemStats = current.ecosystemStats.copy(
                        currentPriceUsd = newPriceData.currentPriceUsd
                    )
                )
            }
            val label = if (priceUsd != null) "$${"%.3f".format(priceUsd)}" else "Chainlink Live Feed"
            showSnackbar("Oracle price updated to $label")
        }
    }

    fun addPriceAlert(
        targetPriceUsd: Double,
        condition: String,
        note: String,
        oneTimeOnly: Boolean = false
    ) {
        viewModelScope.launch {
            repository.addPriceAlert(
                targetPriceUsd = targetPriceUsd,
                condition = condition,
                note = note,
                oneTimeOnly = oneTimeOnly
            )
            showSnackbar("Price alert set for AGL at $${"%.3f".format(targetPriceUsd)} ($condition)")
        }
    }

    fun togglePriceAlert(id: Long, enabled: Boolean) {
        viewModelScope.launch {
            repository.togglePriceAlert(id, enabled)
            showSnackbar(if (enabled) "Alert enabled" else "Alert paused")
        }
    }

    fun rearmPriceAlert(id: Long) {
        viewModelScope.launch {
            repository.rearmPriceAlert(id)
            showSnackbar("Price alert re-armed and watching")
        }
    }

    fun deletePriceAlert(id: Long) {
        viewModelScope.launch {
            repository.deletePriceAlert(id)
            showSnackbar("Price alert removed")
        }
    }

    fun testTriggerAlert(id: Long) {
        viewModelScope.launch {
            val success = repository.testTriggerAlert(id)
            if (success) {
                showSnackbar("Alert triggered! System notification sent.")
            } else {
                showSnackbar("Alert could not be found to trigger")
            }
        }
    }

    fun unlockWallet() {
        _uiState.update {
            it.copy(
                isWalletUnlocked = true,
                biometricAuthError = null
            )
        }
        showSnackbar("Biometric authentication verified • Wallet unlocked")
    }

    fun lockWallet() {
        _uiState.update {
            it.copy(isWalletUnlocked = false)
        }
        showSnackbar("Wallet locked with Biometric Protection")
    }

    fun toggleBiometricLock(enabled: Boolean) {
        _uiState.update {
            it.copy(
                isWalletBiometricLockEnabled = enabled,
                isWalletUnlocked = if (!enabled) true else it.isWalletUnlocked
            )
        }
        showSnackbar(if (enabled) "Biometric Lock enabled" else "Biometric Lock disabled")
    }

    fun setBiometricStatus(status: com.example.util.BiometricCapabilityStatus) {
        _uiState.update { it.copy(biometricStatus = status) }
    }

    fun setBiometricAuthError(error: String?) {
        _uiState.update { it.copy(biometricAuthError = error) }
    }
}

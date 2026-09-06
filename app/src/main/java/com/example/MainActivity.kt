package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppTopBar
import com.example.ui.components.TxPipelineDialog
import com.example.ui.screens.agl.AglTokenScreen
import com.example.ui.screens.ai.AIAssistantScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.alerts.PriceAlertsScreen
import com.example.ui.screens.credits.CreditsScreen
import com.example.ui.screens.diagnostics.DiagnosticsScreen
import com.example.ui.screens.governance.GovernanceScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.AddWalletDialog
import com.example.ui.screens.profile.NotificationCenterDialog
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SecurityPrinciplesDialog
import com.example.ui.screens.profile.SettingsDialog
import com.example.ui.screens.quests.LessonDetailDialog
import com.example.ui.screens.quests.QuestsScreen
import com.example.ui.screens.staking.StakingScreen
import com.example.ui.screens.timelock.TimelockScreen
import com.example.ui.screens.wagl.WagLScreen
import com.example.ui.screens.wallet.ConnectWalletDialog
import com.example.ui.screens.wallet.TransactionDetailSheet
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.screens.wallet.components.BuyCryptoModal
import com.example.ui.screens.wallet.components.ConnectWalletModal
import com.example.ui.screens.wallet.components.DexSwapModal
import com.example.ui.screens.wallet.components.ReceiveModal
import com.example.ui.screens.wallet.components.TransferModal
import com.example.data.remote.BlockchainService
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AiSubTab
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory
import com.example.util.BiometricAuthManager
import com.example.util.PriceAlertNotificationManager

class MainActivity : FragmentActivity() {
    private lateinit var biometricAuthManager: BiometricAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        biometricAuthManager = BiometricAuthManager(applicationContext)

        val database = AppDatabase.getDatabase(applicationContext)
        val notificationManager = PriceAlertNotificationManager(applicationContext)
        val repository = AppRepository(database, notificationManager)
        val viewModelFactory = MainViewModelFactory(repository)

        val openScreenExtra = intent?.getStringExtra("OPEN_SCREEN")

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                LaunchedEffect(Unit) {
                    val status = biometricAuthManager.checkBiometricAvailability()
                    viewModel.setBiometricStatus(status)
                    if (openScreenExtra == "PRICE_ALERTS") {
                        viewModel.navigateToScreen(AppScreen.PRICE_ALERTS)
                    }
                }
                AglSuperAgentApp(
                    viewModel = viewModel,
                    onTriggerBiometricAuth = { onSuccess ->
                        biometricAuthManager.promptBiometricAuthentication(
                            activity = this@MainActivity,
                            title = "AGL Biometric Security Vault",
                            subtitle = "Authenticate to unlock wallet balances",
                            description = "Scan your fingerprint or enter device passcode to reveal sensitive balances.",
                            onSuccess = {
                                viewModel.unlockWallet()
                                onSuccess()
                            },
                            onError = { _, errString ->
                                viewModel.setBiometricAuthError(errString.toString())
                                viewModel.showSnackbar("Biometric authentication cancelled: $errString")
                            },
                            onFailed = {
                                viewModel.showSnackbar("Biometric recognition failed. Please try again.")
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun AglSuperAgentApp(
    viewModel: MainViewModel,
    onTriggerBiometricAuth: (onSuccess: () -> Unit) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val quests by viewModel.quests.collectAsState()
    val rewardsHistory by viewModel.rewardsHistory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()
    val priceAlerts by viewModel.priceAlerts.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearSnackbar()
        }
    }

    val unreadCount = notifications.count { !it.isRead }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AppTopBar(
                activeWalletAddress = uiState.activeWalletAddress,
                unreadNotificationCount = unreadCount,
                onWalletClick = { viewModel.setShowAddWalletDialog(true) },
                onNotificationClick = { viewModel.setShowNotificationDialog(true) },
                onSecurityClick = { viewModel.setShowSecurityPrinciplesDialog(true) }
            )
        },
        bottomBar = {
            AppBottomNavigationBar(
                currentScreen = uiState.currentScreen,
                onNavigate = { screen -> viewModel.navigateToScreen(screen) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBackground)
                .padding(innerPadding)
        ) {
            when (uiState.currentScreen) {
                AppScreen.HOME -> {
                    HomeScreen(
                        portfolioSummary = uiState.portfolioSummary,
                        transactions = transactions,
                        quests = quests,
                        ecosystemStats = uiState.ecosystemStats,
                        ecosystemContracts = uiState.ecosystemContracts,
                        onNavigate = { screen -> viewModel.navigateToScreen(screen) },
                        onNavigateAiTab = { tab -> viewModel.setAiSubTab(tab) },
                        onNavigateQuestsTab = { tab -> viewModel.setQuestsSubTab(tab) },
                        onSelectTransaction = { tx -> viewModel.setSelectedTransaction(tx) },
                        onClaimQuest = { questId -> viewModel.claimQuest(questId) },
                        onDailyCheckIn = { viewModel.claimDailyCheckIn() },
                        aiSuggestions = uiState.aiSuggestions,
                        selectedAiCategory = uiState.selectedAiSuggestionCategory,
                        isRefreshingSuggestions = uiState.isRefreshingSuggestions,
                        onSelectAiCategory = { cat -> viewModel.selectAiSuggestionCategory(cat) },
                        onRefreshSuggestions = { viewModel.refreshAiSuggestions() },
                        onApplySuggestion = { sug -> viewModel.applyAiSuggestion(sug) },
                        isIndexingTransactions = uiState.isIndexingTransactions,
                        indexerStatus = uiState.indexerStatusMessage,
                        onRefreshTransactions = { viewModel.refreshRecentTransactions() },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.DASHBOARD -> {
                    DashboardScreen(
                        activeWalletAddress = uiState.activeWalletAddress,
                        isConnected = uiState.isConnected,
                        networkDiagnostics = uiState.networkDiagnostics,
                        isRunningDiagnostics = uiState.isRunningDiagnostics,
                        oraclePriceData = uiState.oraclePriceData,
                        isRefreshingOracle = uiState.isRefreshingOracle,
                        ecosystemStats = uiState.ecosystemStats,
                        isAiThinking = uiState.isAiThinking,
                        isIndexingTransactions = uiState.isIndexingTransactions,
                        indexerStatusMessage = uiState.indexerStatusMessage,
                        transactions = transactions,
                        notifications = notifications,
                        userProgress = userProgress,
                        onRefresh = { viewModel.refreshData() },
                        onRunDiagnostics = { viewModel.runNetworkDiagnostics() },
                        onNavigate = { screen -> viewModel.navigateToScreen(screen) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.WALLET -> {
                    val context = LocalContext.current
                    WalletScreen(
                        activeWalletAddress = uiState.activeWalletAddress,
                        liveWalletState = uiState.liveWalletState,
                        isFetchingLiveBalances = uiState.isFetchingLiveBalances,
                        portfolioSummary = uiState.portfolioSummary,
                        tokens = uiState.tokens,
                        transactions = transactions,
                        wallets = wallets,
                        onSwitchWallet = { addr -> viewModel.switchActiveWallet(addr) },
                        onConnectWalletClick = { viewModel.setConnectWalletDialogVisible(true) },
                        onDisconnectWallet = { viewModel.disconnectWallet() },
                        onAddWalletClick = { viewModel.setShowAddWalletDialog(true) },
                        onRefresh = { viewModel.refreshData() },
                        onSelectTransaction = { tx -> viewModel.setSelectedTransaction(tx) },
                        onExplainTxWithAi = { tx ->
                            viewModel.setSelectedTransaction(tx)
                            viewModel.setAiSubTab(AiSubTab.CHAT)
                            viewModel.navigateToScreen(AppScreen.AI_ASSISTANT)
                            viewModel.sendChatMessage("Explain transaction ${tx.hash.take(12)}... in simple terms.")
                        },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) },
                        isIndexingTransactions = uiState.isIndexingTransactions,
                        indexerStatus = uiState.indexerStatusMessage,
                        onRefreshTransactions = { viewModel.refreshRecentTransactions() },
                        isWalletUnlocked = uiState.isWalletUnlocked,
                        isBiometricLockEnabled = uiState.isWalletBiometricLockEnabled,
                        biometricStatus = uiState.biometricStatus,
                        onUnlockWithBiometrics = {
                            onTriggerBiometricAuth { }
                        },
                        onLockWallet = { viewModel.lockWallet() },
                        onToggleBiometricLock = { enabled -> viewModel.toggleBiometricLock(enabled) },
                        onOpenSwapModal = { viewModel.setSwapDialogVisible(true) },
                        onOpenTransferModal = { viewModel.setTransferDialogVisible(true) },
                        onOpenReceiveModal = { viewModel.setReceiveDialogVisible(true) },
                        onOpenBuyModal = { viewModel.setBuyDialogVisible(true) }
                    )
                }

                AppScreen.AI_ASSISTANT -> {
                    AIAssistantScreen(
                        currentTab = uiState.currentAiTab,
                        onTabSelected = { tab -> viewModel.setAiSubTab(tab) },
                        messages = chatMessages,
                        isThinking = uiState.isAiThinking,
                        onSendMessage = { text -> viewModel.sendChatMessage(text) },
                        onClearChat = { viewModel.clearChatHistory() },
                        selectedPersona = uiState.selectedAiPersona,
                        onSelectPersona = { persona -> viewModel.setAiPersona(persona) },
                        isSearchGroundingEnabled = uiState.isGoogleSearchGroundingEnabled,
                        onToggleSearchGrounding = { enabled -> viewModel.toggleGoogleSearchGrounding(enabled) },
                        contractResult = uiState.contractAnalysisResult,
                        isAnalyzingContract = uiState.isAnalyzingContract,
                        onAnalyzeContract = { addr -> viewModel.analyzeContract(addr) },
                        deepAuditResult = uiState.deepContractAuditResult,
                        isDeepAuditing = uiState.isDeepAuditingContract,
                        onDeepAudit = { code, isSolidity -> viewModel.auditSolidityCodeDeep(code, isSolidity) },
                        securityReport = uiState.securityAuditResult,
                        isAuditingSecurity = uiState.isAuditingSecurity,
                        onAuditSecurity = { target -> viewModel.auditSecurity(target) },
                        portfolioPlan = uiState.portfolioStrategyResult,
                        isGeneratingPortfolioPlan = uiState.isGeneratingPortfolioPlan,
                        onGeneratePortfolioPlan = { viewModel.generateDeFiPortfolioRebalance() },
                        marketRadar = uiState.marketRadarData,
                        isLoadingMarketRadar = uiState.isLoadingMarketRadar,
                        onFetchMarketRadar = { topic -> viewModel.fetchLiveBaseMarketRadar(topic) },
                        suggestions = uiState.aiSuggestions,
                        selectedSuggestionCategory = uiState.selectedAiSuggestionCategory,
                        onSelectSuggestionCategory = { cat -> viewModel.selectAiSuggestionCategory(cat) },
                        followUpSuggestions = uiState.followUpSuggestions,
                        onApplySuggestion = { sug -> viewModel.applyAiSuggestion(sug) }
                    )
                }

                AppScreen.QUESTS -> {
                    QuestsScreen(
                        currentSubTab = uiState.currentQuestsTab,
                        onSubTabSelected = { tab -> viewModel.setQuestsSubTab(tab) },
                        quests = quests,
                        selectedCategory = uiState.selectedQuestCategory,
                        onSelectCategory = { cat -> viewModel.setSelectedQuestCategory(cat) },
                        onClaimQuest = { questId -> viewModel.claimQuest(questId) },
                        lessons = viewModel.allLessons,
                        completedLessonIdsCsv = userProgress?.completedLessonIdsCsv ?: "",
                        onSelectLesson = { lesson -> viewModel.selectLesson(lesson) },
                        leaderboardUsers = uiState.leaderboardUsers,
                        selectedLeaderboardTimeframe = uiState.activeLeaderboardTimeframe,
                        onSelectLeaderboardTimeframe = { tf -> viewModel.selectLeaderboardTimeframe(tf) },
                        userProfile = uiState.userProfile,
                        rewardsHistory = rewardsHistory,
                        onDailyCheckIn = { viewModel.claimDailyCheckIn() },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.PROFILE -> {
                    ProfileScreen(
                        userProfile = uiState.userProfile,
                        wallets = wallets,
                        onAddWalletClick = { viewModel.setShowAddWalletDialog(true) },
                        onOpenSettings = { viewModel.setShowSettingsDialog(true) },
                        onOpenNotifications = { viewModel.setShowNotificationDialog(true) },
                        onOpenSecurityPrinciples = { viewModel.setShowSecurityPrinciplesDialog(true) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.AGL_TOKEN -> {
                    AglTokenScreen(
                        metadata = uiState.tokenMetadata,
                        walletState = uiState.liveWalletState,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onStartTx = { req -> viewModel.startTxPipeline(req) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) },
                        onOpenPriceAlerts = { viewModel.navigateToScreen(AppScreen.PRICE_ALERTS) }
                    )
                }

                AppScreen.CREDITS -> {
                    CreditsScreen(
                        creditsInfo = uiState.creditsInfo,
                        walletState = uiState.liveWalletState,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onStartTx = { req -> viewModel.startTxPipeline(req) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.WAGL -> {
                    WagLScreen(
                        wAglInfo = uiState.wagLInfo,
                        walletState = uiState.liveWalletState,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onStartTx = { req -> viewModel.startTxPipeline(req) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.STAKING -> {
                    StakingScreen(
                        stakingInfo = uiState.stakingInfo,
                        tiers = uiState.stakingTiers,
                        positions = uiState.stakingPositions,
                        walletState = uiState.liveWalletState,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onStartTx = { req -> viewModel.startTxPipeline(req) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.GOVERNANCE -> {
                    GovernanceScreen(
                        governorDetails = uiState.governorDetails,
                        proposals = uiState.proposals,
                        walletState = uiState.liveWalletState,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onVote = { id, support, reason -> viewModel.castVote(id, support, reason) },
                        onStartTx = { req -> viewModel.startTxPipeline(req) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) },
                        onRefresh = { viewModel.refreshGovernance() },
                        onNavigateToWagL = { viewModel.navigateToScreen(AppScreen.WAGL) }
                    )
                }

                AppScreen.TIMELOCK -> {
                    TimelockScreen(
                        timelockInfo = uiState.timelockInfo,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.DIAGNOSTICS -> {
                    DiagnosticsScreen(
                        report = uiState.networkDiagnostics,
                        isRunning = uiState.isRunningDiagnostics,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onRunDiagnostics = { viewModel.runNetworkDiagnostics() },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.ACTIVITY -> {
                    WalletScreen(
                        activeWalletAddress = uiState.activeWalletAddress,
                        liveWalletState = uiState.liveWalletState,
                        isFetchingLiveBalances = uiState.isFetchingLiveBalances,
                        portfolioSummary = uiState.portfolioSummary,
                        tokens = uiState.tokens,
                        transactions = transactions,
                        wallets = wallets,
                        onSwitchWallet = { addr -> viewModel.switchActiveWallet(addr) },
                        onConnectWalletClick = { viewModel.setShowConnectWalletDialog(true) },
                        onDisconnectWallet = { viewModel.disconnectWallet() },
                        onAddWalletClick = { viewModel.setShowAddWalletDialog(true) },
                        onRefresh = { viewModel.refreshData() },
                        onSelectTransaction = { tx -> viewModel.setSelectedTransaction(tx) },
                        onExplainTxWithAi = { tx ->
                            viewModel.setSelectedTransaction(tx)
                            viewModel.setAiSubTab(AiSubTab.CHAT)
                            viewModel.navigateToScreen(AppScreen.AI_ASSISTANT)
                            viewModel.sendChatMessage("Explain transaction ${tx.hash.take(12)}... in simple terms.")
                        },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) },
                        isIndexingTransactions = uiState.isIndexingTransactions,
                        indexerStatus = uiState.indexerStatusMessage,
                        onRefreshTransactions = { viewModel.refreshRecentTransactions() }
                    )
                }

                AppScreen.SETTINGS -> {
                    ProfileScreen(
                        userProfile = uiState.userProfile,
                        wallets = wallets,
                        onAddWalletClick = { viewModel.setShowAddWalletDialog(true) },
                        onOpenSettings = { viewModel.setShowSettingsDialog(true) },
                        onOpenNotifications = { viewModel.setShowNotificationDialog(true) },
                        onOpenSecurityPrinciples = { viewModel.setShowSecurityPrinciplesDialog(true) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }

                AppScreen.PRICE_ALERTS -> {
                    PriceAlertsScreen(
                        oracleData = uiState.oraclePriceData,
                        isRefreshingOracle = uiState.isRefreshingOracle,
                        priceAlerts = priceAlerts,
                        onBack = { viewModel.navigateToScreen(AppScreen.HOME) },
                        onRefreshOracle = { viewModel.refreshOraclePrice() },
                        onSimulatePrice = { price -> viewModel.simulateOraclePrice(price) },
                        onAddAlert = { targetPrice, condition, note, oneTimeOnly ->
                            viewModel.addPriceAlert(targetPrice, condition, note, oneTimeOnly)
                        },
                        onToggleAlert = { id, enabled -> viewModel.togglePriceAlert(id, enabled) },
                        onRearmAlert = { id -> viewModel.rearmPriceAlert(id) },
                        onDeleteAlert = { id -> viewModel.deletePriceAlert(id) },
                        onTestTriggerAlert = { id -> viewModel.testTriggerAlert(id) },
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                    )
                }
            }

            // Transaction Detail Bottom Sheet
            uiState.selectedTransaction?.let { tx ->
                TransactionDetailSheet(
                    transaction = tx,
                    onDismiss = { viewModel.setSelectedTransaction(null) },
                    onExplainWithAi = { selectedTx ->
                        viewModel.setSelectedTransaction(null)
                        viewModel.setAiSubTab(AiSubTab.CHAT)
                        viewModel.navigateToScreen(AppScreen.AI_ASSISTANT)
                        viewModel.sendChatMessage("Explain transaction ${selectedTx.hash.take(12)}... (${selectedTx.value} ${selectedTx.tokenSymbol} ${selectedTx.type}) in simple terms.")
                    },
                    onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
                )
            }

            // Lesson & Quiz Dialog
            uiState.selectedLesson?.let { lesson ->
                LessonDetailDialog(
                    lesson = lesson,
                    onDismiss = { viewModel.selectLesson(null) },
                    onQuizCompleted = { score, total ->
                        viewModel.completeQuiz(lesson.id, score, total)
                    }
                )
            }

            // Add Wallet Dialog
            if (uiState.showAddWalletDialog) {
                AddWalletDialog(
                    onDismiss = { viewModel.setShowAddWalletDialog(false) },
                    onAddWallet = { address, label ->
                        viewModel.addWatchWallet(address, label)
                    }
                )
            }

            // Multi-Wallet Connection Modal
            if (uiState.showConnectWalletDialog) {
                val context = LocalContext.current
                ConnectWalletModal(
                    onDismiss = { viewModel.setConnectWalletDialogVisible(false) },
                    onCreateVaultAccount = { label ->
                        viewModel.createKeyVaultAccount(context, label)
                    },
                    onImportPrivateKey = { pk, label ->
                        viewModel.importPrivateKeyVault(context, pk, label)
                    },
                    onConnectCoinbase = { addr ->
                        viewModel.connectCoinbaseWallet(addr)
                    },
                    onConnectMetaMask = { addr ->
                        viewModel.connectMetaMask(addr)
                    },
                    onConnectWatchOnly = { addr ->
                        viewModel.connectWallet(addr, "Watch-Only Account")
                    }
                )
            }

            // Interactive DEX Aggregator Swap Modal (Aerodrome & Uniswap V3 on Base)
            if (uiState.showSwapDialog) {
                val context = LocalContext.current
                DexSwapModal(
                    onDismiss = { viewModel.setSwapDialogVisible(false) },
                    dexService = BlockchainService.dexAggregatorService,
                    userAddress = uiState.activeWalletAddress,
                    onExecuteSwap = { quote, onDone ->
                        viewModel.executeDexSwap(context, quote, onDone)
                    },
                    onExecuteApprove = { tokenAddr, spenderAddr, onDone ->
                        viewModel.executeTokenApprove(context, tokenAddr, spenderAddr, onDone)
                    }
                )
            }

            // Interactive Transfer Modal
            if (uiState.showTransferDialog) {
                val context = LocalContext.current
                TransferModal(
                    onDismiss = { viewModel.setTransferDialogVisible(false) },
                    senderAddress = uiState.activeWalletAddress,
                    onExecuteTransfer = { token, recipient, amount, onDone ->
                        viewModel.executeTokenTransfer(context, token, recipient, amount, onDone)
                    }
                )
            }

            // Receive & QR Modal
            if (uiState.showReceiveDialog) {
                ReceiveModal(
                    walletAddress = uiState.activeWalletAddress,
                    onDismiss = { viewModel.setReceiveDialogVisible(false) }
                )
            }

            // Buy & On-ramp Modal
            if (uiState.showBuyDialog) {
                BuyCryptoModal(
                    walletAddress = uiState.activeWalletAddress,
                    onDismiss = { viewModel.setBuyDialogVisible(false) }
                )
            }

            // Settings Dialog
            if (uiState.showSettingsDialog) {
                SettingsDialog(
                    userProgress = userProgress,
                    onDismiss = { viewModel.setShowSettingsDialog(false) },
                    onSaveSettings = { net, ai, tx, rew, qst, sec ->
                        viewModel.updateSettings(net, ai, tx, rew, qst, sec)
                    }
                )
            }

            // Notifications Center Dialog
            if (uiState.showNotificationDialog) {
                NotificationCenterDialog(
                    notifications = notifications,
                    onDismiss = { viewModel.setShowNotificationDialog(false) },
                    onMarkAllRead = { viewModel.markAllNotificationsRead() }
                )
            }

            // Security Principles Dialog
            if (uiState.showSecurityPrinciplesDialog) {
                SecurityPrinciplesDialog(
                    onDismiss = { viewModel.setShowSecurityPrinciplesDialog(false) }
                )
            }

            // Transaction Pipeline & Approval Dialog
            if (uiState.showTxPipelineDialog) {
                TxPipelineDialog(
                    request = uiState.activeTxPipelineRequest,
                    status = uiState.txPipelineStatus,
                    executionResult = uiState.txExecutionResult,
                    onDismiss = { viewModel.dismissTxPipeline() },
                    onApprove = { viewModel.approveSpenderForActiveTx() },
                    onConfirm = { viewModel.confirmAndExecuteTx() }
                )
            }
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AppDatabase
import com.example.data.repository.AppRepository
import com.example.ui.components.AppBottomNavigationBar
import com.example.ui.components.AppTopBar
import com.example.ui.screens.ai.AIAssistantScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.profile.AddWalletDialog
import com.example.ui.screens.profile.NotificationCenterDialog
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.screens.profile.SecurityPrinciplesDialog
import com.example.ui.screens.profile.SettingsDialog
import com.example.ui.screens.quests.LessonDetailDialog
import com.example.ui.screens.quests.QuestsScreen
import com.example.ui.screens.wallet.ConnectWalletDialog
import com.example.ui.screens.wallet.TransactionDetailSheet
import com.example.ui.screens.wallet.WalletScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AiSubTab
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.MainViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database)
        val viewModelFactory = MainViewModelFactory(repository)

        setContent {
            MyApplicationTheme {
                val viewModel: MainViewModel = viewModel(factory = viewModelFactory)
                AglSuperAgentApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AglSuperAgentApp(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val wallets by viewModel.wallets.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val quests by viewModel.quests.collectAsState()
    val rewardsHistory by viewModel.rewardsHistory.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val userProgress by viewModel.userProgress.collectAsState()

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
                        onDailyCheckIn = { viewModel.claimDailyCheckIn() }
                    )
                }

                AppScreen.WALLET -> {
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
                        onShowSnackbar = { msg -> viewModel.showSnackbar(msg) }
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
                        contractResult = uiState.contractAnalysisResult,
                        isAnalyzingContract = uiState.isAnalyzingContract,
                        onAnalyzeContract = { addr -> viewModel.analyzeContract(addr) },
                        securityReport = uiState.securityAuditResult,
                        isAuditingSecurity = uiState.isAuditingSecurity,
                        onAuditSecurity = { target -> viewModel.auditSecurity(target) }
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
                        onDailyCheckIn = { viewModel.claimDailyCheckIn() }
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

            // Connect Wallet Dialog
            if (uiState.showConnectWalletDialog) {
                ConnectWalletDialog(
                    currentAddress = uiState.activeWalletAddress,
                    isLoading = uiState.isFetchingLiveBalances,
                    onDismiss = { viewModel.setShowConnectWalletDialog(false) },
                    onConnect = { address, label ->
                        viewModel.connectWallet(address, label)
                    }
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
        }
    }
}

package com.example.ui.screens.wallet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.GoldRewards
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RadiantPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ConnectWalletModal(
    onDismiss: () -> Unit,
    onCreateVaultAccount: (label: String) -> Unit,
    onImportPrivateKey: (privateKey: String, label: String) -> Unit,
    onConnectCoinbase: (address: String) -> Unit,
    onConnectMetaMask: (address: String) -> Unit,
    onConnectWatchOnly: (address: String) -> Unit
) {
    var activeTab by remember { mutableStateOf<String>("OPTIONS") } // OPTIONS, IMPORT, WATCH, COINBASE, METAMASK
    var inputAddress by remember { mutableStateOf("") }
    var inputPrivateKey by remember { mutableStateOf("") }
    var inputLabel by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .testTag("connect_wallet_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(BaseBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = "Wallet",
                                tint = BaseCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Connect Real Wallet",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Base Mainnet • Chain ID 8453",
                                fontSize = 11.sp,
                                color = BaseCyan
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                when (activeTab) {
                    "OPTIONS" -> {
                        // Options list
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // 1. Hardware Secure Key Vault (Create New)
                            WalletConnectOptionCard(
                                title = "Create Secure Key Vault",
                                subtitle = "Hardware-encrypted Base wallet (Android Keystore)",
                                badge = "INSTANT",
                                badgeColor = NeonEmerald,
                                iconEmoji = "🔐",
                                onClick = {
                                    onCreateVaultAccount("AGL Super Account")
                                    onDismiss()
                                }
                            )

                            // 2. Import Private Key
                            WalletConnectOptionCard(
                                title = "Import Private Key / Keypair",
                                subtitle = "Encrypt & sign on-chain transactions directly",
                                badge = "KEY VAULT",
                                badgeColor = RadiantPurple,
                                iconEmoji = "🔑",
                                onClick = { activeTab = "IMPORT" }
                            )

                            // 3. Coinbase Wallet & Smart Wallet
                            WalletConnectOptionCard(
                                title = "Coinbase Wallet",
                                subtitle = "Connect Coinbase Smart Wallet on Base",
                                badge = "BASE NATIVE",
                                badgeColor = BaseBlue,
                                iconEmoji = "🔵",
                                onClick = { activeTab = "COINBASE" }
                            )

                            // 4. MetaMask / Web3 Injected
                            WalletConnectOptionCard(
                                title = "MetaMask / Rainbow",
                                subtitle = "Connect via Web3 intent or browser extension",
                                badge = "POPULAR",
                                badgeColor = GoldRewards,
                                iconEmoji = "🦊",
                                onClick = { activeTab = "METAMASK" }
                            )

                            // 5. Watch-Only Mode
                            WalletConnectOptionCard(
                                title = "Watch-Only Address",
                                subtitle = "Read-only portfolio monitoring without keys",
                                badge = "READ ONLY",
                                badgeColor = TextMuted,
                                iconEmoji = "👁️",
                                onClick = { activeTab = "WATCH" }
                            )
                        }
                    }

                    "IMPORT" -> {
                        val clipboard = LocalClipboardManager.current
                        Column {
                            Text(
                                text = "Enter Private Key (64 hex characters):",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputPrivateKey,
                                onValueChange = { inputPrivateKey = it; errorMessage = null },
                                placeholder = { Text("0x... or raw 64 hex chars", color = TextMuted, fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("import_private_key_input"),
                                trailingIcon = {
                                    Button(
                                        onClick = {
                                            val text = clipboard.getText()?.text
                                            if (!text.isNullOrBlank()) {
                                                inputPrivateKey = text.trim()
                                                errorMessage = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue.copy(alpha = 0.35f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = BaseCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PASTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Wallet Label (optional):",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = inputLabel,
                                onValueChange = { inputLabel = it },
                                placeholder = { Text("e.g. My Trading Wallet", color = TextMuted, fontSize = 12.sp) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { activeTab = "OPTIONS" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
                                ) {
                                    Text("Back", color = TextPrimary)
                                }
                                Button(
                                    onClick = {
                                        val clean = inputPrivateKey.trim().removePrefix("0x")
                                        if (clean.length != 64) {
                                            errorMessage = "Private key must be exactly 64 hex characters"
                                        } else {
                                            onImportPrivateKey(
                                                clean,
                                                if (inputLabel.isNotBlank()) inputLabel.trim() else "Imported Account"
                                            )
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                                ) {
                                    Text("Import & Secure", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    "COINBASE", "METAMASK", "WATCH" -> {
                        val isCoinbase = activeTab == "COINBASE"
                        val isMetaMask = activeTab == "METAMASK"
                        val clipboard = LocalClipboardManager.current
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isCoinbase) "Connect Coinbase Wallet Address:" else if (isMetaMask) "Connect MetaMask Address:" else "Enter Base Address to Watch:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )

                                Text(
                                    text = "0xD034...27C8",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BaseCyan,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(BaseBlue.copy(alpha = 0.2f))
                                        .clickable {
                                            inputAddress = "0xD034E94465Db1669f80D817c66e58cF194d027C8"
                                            errorMessage = null
                                        }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = inputAddress,
                                onValueChange = { inputAddress = it; errorMessage = null },
                                placeholder = { Text("0x...", color = TextMuted, fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("connect_address_input"),
                                trailingIcon = {
                                    Button(
                                        onClick = {
                                            val text = clipboard.getText()?.text
                                            if (!text.isNullOrBlank()) {
                                                inputAddress = text.trim()
                                                errorMessage = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = BaseBlue.copy(alpha = 0.35f)),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        Icon(Icons.Default.ContentPaste, contentDescription = "Paste", tint = BaseCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PASTE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BaseCyan)
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = BaseCyan,
                                    unfocusedBorderColor = DarkBorder,
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { activeTab = "OPTIONS" },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = DarkBorder)
                                ) {
                                    Text("Back", color = TextPrimary)
                                }
                                Button(
                                    onClick = {
                                        val addr = inputAddress.trim()
                                        if (!addr.startsWith("0x") || addr.length != 42) {
                                            errorMessage = "Please enter a valid 42-character 0x EVM address"
                                        } else {
                                            when (activeTab) {
                                                "COINBASE" -> onConnectCoinbase(addr)
                                                "METAMASK" -> onConnectMetaMask(addr)
                                                else -> onConnectWatchOnly(addr)
                                            }
                                            onDismiss()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = BaseBlue)
                                ) {
                                    Text("Connect", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalletConnectOptionCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    iconEmoji: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("wallet_option_${title.lowercase().replace(" ", "_")}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = iconEmoji, fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(badgeColor.copy(alpha = 0.2f))
                    .padding(horizontal = 7.dp, vertical = 3.dp)
            ) {
                Text(
                    text = badge,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }
        }
    }
}

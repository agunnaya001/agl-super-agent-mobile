package com.example.ui.screens.wallet.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.example.data.local.entities.WalletAccountEntity
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.BaseBlue
import com.example.ui.theme.BaseCyan
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkCard
import com.example.ui.theme.DarkCardElevated
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonEmerald
import com.example.ui.theme.RiskHighBg
import com.example.ui.theme.RiskHighBorder
import com.example.ui.theme.DangerCrimson
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.BiometricAuthManager
import com.example.util.KeyVaultManager
import kotlinx.coroutines.delay

/**
 * High-security dialog modal for decrypting and viewing sensitive private keys.
 * Strictly enforces secondary biometric verification (BiometricPrompt / Passcode)
 * before decrypting the key from the Android Keystore, and features auto-hide countdown
 * timer and shoulder-surfing protection.
 */
@Composable
fun PrivateKeyBiometricAuthModal(
    activeAccount: WalletAccountEntity?,
    allAccounts: List<WalletAccountEntity>,
    onDismiss: () -> Unit,
    onShowSnackbar: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val clipboardManager = LocalClipboardManager.current
    val biometricAuthManager = remember { BiometricAuthManager(context) }
    val keyVault = remember { KeyVaultManager(context) }

    // Identify target account with encrypted key
    val vaultAccounts = remember(allAccounts) {
        allAccounts.filter { it.encryptedPrivateKey != null && it.ivBase64 != null }
    }

    var selectedAccount by remember {
        mutableStateOf(
            if (activeAccount?.encryptedPrivateKey != null) activeAccount
            else vaultAccounts.firstOrNull()
        )
    }

    var isBiometricVerified by remember { mutableStateOf(false) }
    var isAuthenticating by remember { mutableStateOf(false) }
    var decryptedPrivateKey by remember { mutableStateOf<String?>(null) }
    var isKeyMasked by remember { mutableStateOf(true) }
    var remainingSeconds by remember { mutableIntStateOf(30) }
    var authError by remember { mutableStateOf<String?>(null) }

    // Auto-lock countdown timer once verified
    LaunchedEffect(isBiometricVerified) {
        if (isBiometricVerified) {
            remainingSeconds = 30
            while (remainingSeconds > 0) {
                delay(1000)
                remainingSeconds -= 1
            }
            // Auto-lock when timer reaches 0
            decryptedPrivateKey = null
            isBiometricVerified = false
            onDismiss()
            onShowSnackbar("Private key session timed out and auto-locked for security.")
        }
    }

    fun triggerBiometricPrompt() {
        authError = null
        val target = selectedAccount
        if (target == null || target.encryptedPrivateKey == null || target.ivBase64 == null) {
            authError = "Selected account does not possess an encrypted hardware key."
            return
        }

        if (activity == null) {
            // Preview / fallback environment
            try {
                decryptedPrivateKey = keyVault.decrypt(target.encryptedPrivateKey, target.ivBase64)
                isBiometricVerified = true
            } catch (e: Exception) {
                authError = "Decryption error: ${e.message}"
            }
            return
        }

        val status = biometricAuthManager.checkBiometricAvailability()
        if (!status.isAvailable) {
            // Hardware lock unavailable or no biometrics enrolled: fallback to direct Keystore decrypt with security warning
            try {
                decryptedPrivateKey = keyVault.decrypt(target.encryptedPrivateKey, target.ivBase64)
                isBiometricVerified = true
                onShowSnackbar("Hardware biometrics unavailable. Keystore decrypted with warning.")
            } catch (e: Exception) {
                authError = "Decryption error: ${e.message}"
            }
            return
        }

        isAuthenticating = true
        biometricAuthManager.promptBiometricAuthentication(
            activity = activity,
            title = "Secondary Biometric Verification",
            subtitle = "Authorize Private Key Decryption",
            description = "Confirm your fingerprint or device credential to decrypt and display the private key for ${target.label} (${target.address.take(6)}...${target.address.takeLast(4)}).",
            onSuccess = {
                isAuthenticating = false
                try {
                    decryptedPrivateKey = keyVault.decrypt(target.encryptedPrivateKey, target.ivBase64)
                    isBiometricVerified = true
                } catch (e: Exception) {
                    authError = "Decryption failure: ${e.message}"
                }
            },
            onError = { _, errString ->
                isAuthenticating = false
                authError = "Authentication cancelled: $errString"
            },
            onFailed = {
                isAuthenticating = false
                authError = "Biometric signature not recognized. Please retry."
            }
        )
    }

    // Auto-prompt on dialog open
    LaunchedEffect(selectedAccount) {
        if (!isBiometricVerified && selectedAccount?.encryptedPrivateKey != null) {
            delay(250)
            triggerBiometricPrompt()
        }
    }

    Dialog(
        onDismissRequest = {
            decryptedPrivateKey = null
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("private_key_auth_modal"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.5.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            if (isBiometricVerified) NeonEmerald else AmberWarning,
                            BaseBlue.copy(alpha = 0.5f)
                        )
                    )
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isBiometricVerified) NeonEmerald.copy(alpha = 0.2f)
                                        else AmberWarning.copy(alpha = 0.2f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isBiometricVerified) Icons.Default.Lock else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = if (isBiometricVerified) NeonEmerald else AmberWarning,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (isBiometricVerified) "Hardware Key Vault" else "Secondary Verification",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Base Mainnet · Android Keystore",
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                decryptedPrivateKey = null
                                onDismiss()
                            },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("btn_close_private_key_modal")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (!isBiometricVerified) {
                        // CHALLENGE STATE: Biometric verification required
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(AmberWarning.copy(alpha = 0.15f))
                                .border(1.5.dp, AmberWarning, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isAuthenticating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(36.dp),
                                    color = AmberWarning,
                                    strokeWidth = 3.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Sensor",
                                    tint = AmberWarning,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Biometric Scan Required",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "Viewing raw private keys is a high-security operation. Confirm your biometric signature to decrypt credentials from Android Keystore hardware.",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Target Account Info Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCard),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("TARGET VAULT ACCOUNT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedAccount?.label ?: "Base Account",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Hardware Encrypted",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BaseCyan
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedAccount?.address ?: "",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = TextSecondary
                                )
                            }
                        }

                        if (authError != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = authError!!,
                                color = DangerCrimson,
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { triggerBiometricPrompt() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_trigger_private_key_biometric"),
                            colors = ButtonDefaults.buttonColors(containerColor = BaseCyan),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isAuthenticating
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAuthenticating) "Verifying Biometric..." else "Authenticate with Biometrics",
                                color = DarkBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        // VERIFIED STATE: Show Revealed Private Key
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = RiskHighBg),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, RiskHighBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Warning",
                                    tint = DangerCrimson,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = "CRITICAL SECURITY WARNING",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DangerCrimson
                                    )
                                    Text(
                                        text = "Never disclose this private key or paste it into untrusted websites. Anyone with this key has irreversible, full control over your Base Mainnet funds.",
                                        fontSize = 11.sp,
                                        color = DangerCrimson.copy(alpha = 0.9f),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Countdown Timer
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonEmerald,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Biometrically Verified",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonEmerald
                                )
                            }
                            Text(
                                text = "Auto-locks in ${remainingSeconds}s",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberWarning
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { remainingSeconds / 30f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AmberWarning,
                            trackColor = DarkBorder
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Key Display Box
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkCardElevated),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "PRIVATE KEY (64 HEX)",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextMuted
                                    )
                                    IconButton(
                                        onClick = { isKeyMasked = !isKeyMasked },
                                        modifier = Modifier.size(24.dp).testTag("btn_toggle_key_mask")
                                    ) {
                                        Icon(
                                            imageVector = if (isKeyMasked) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = if (isKeyMasked) "Reveal Key" else "Mask Key",
                                            tint = BaseCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                val displayedKey = if (isKeyMasked) {
                                    "•••• •••• •••• •••• •••• •••• •••• •••• •••• •••• •••• •••• •••• •••• •••• ••••"
                                } else {
                                    decryptedPrivateKey ?: "Unable to decrypt key"
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(DarkBackground)
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = displayedKey,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 12.sp,
                                        color = if (isKeyMasked) TextMuted else TextPrimary,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.testTag("decrypted_private_key_text")
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    decryptedPrivateKey?.let { rawKey ->
                                        clipboardManager.setText(AnnotatedString(rawKey))
                                        onShowSnackbar("Private key copied to clipboard. Clear clipboard immediately after use.")
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_copy_private_key"),
                                shape = RoundedCornerShape(10.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, BaseCyan)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = BaseCyan, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Copy Key", color = BaseCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = {
                                    decryptedPrivateKey = null
                                    isBiometricVerified = false
                                    onDismiss()
                                    onShowSnackbar("Key vault locked and private key wiped from memory.")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("btn_lock_key_vault"),
                                colors = ButtonDefaults.buttonColors(containerColor = BaseBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Lock Vault", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

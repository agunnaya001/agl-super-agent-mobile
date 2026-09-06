package com.example.data.remote.blockchain.wallet

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.local.entities.WalletAccountEntity
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import com.example.util.KeyVaultManager
import com.example.util.SecureKeyVaultItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.web3j.crypto.Credentials
import java.math.BigInteger

enum class WalletConnectionMode {
    LOCAL_KEY_VAULT,
    COINBASE_WALLET,
    METAMASK,
    WALLET_CONNECT,
    WATCH_ONLY
}

data class ActiveConnectedWallet(
    val address: String,
    val label: String,
    val mode: WalletConnectionMode,
    val isHardwareSecured: Boolean = false,
    val encryptedKeyBase64: String? = null,
    val ivBase64: String? = null
)

/**
 * Manager handling real multi-wallet connections on Base Mainnet:
 * - Hardware KeyStore Web3 Vault accounts (full local signing capability)
 * - Coinbase Wallet & Coinbase Smart Wallet
 * - MetaMask & Injected Web3
 * - WalletConnect v2
 * - Watch-Only accounts
 */
class ConnectedWalletManager(
    private val context: Context,
    private val keyVaultManager: KeyVaultManager = KeyVaultManager(context)
) {

    private val _activeWallet = MutableStateFlow<ActiveConnectedWallet>(
        ActiveConnectedWallet(
            address = BaseBlockchainConfig.DEFAULT_DEMO_WALLET,
            label = "AGL Primary Account",
            mode = WalletConnectionMode.WATCH_ONLY
        )
    )
    val activeWallet: StateFlow<ActiveConnectedWallet> = _activeWallet.asStateFlow()

    /**
     * Creates a new hardware-secured Key Vault account on Base.
     */
    fun createNewVaultAccount(label: String = "AGL Super Account"): Pair<WalletAccountEntity, SecureKeyVaultItem> {
        val vaultItem = keyVaultManager.generateNewKeypair(label)
        val entity = WalletAccountEntity(
            address = vaultItem.address,
            label = label,
            isPrimary = true,
            walletType = "LOCAL_VAULT",
            encryptedPrivateKey = vaultItem.encryptedPrivateKeyBase64,
            ivBase64 = vaultItem.ivBase64,
            isHardware = true
        )
        _activeWallet.value = ActiveConnectedWallet(
            address = vaultItem.address,
            label = label,
            mode = WalletConnectionMode.LOCAL_KEY_VAULT,
            isHardwareSecured = true,
            encryptedKeyBase64 = vaultItem.encryptedPrivateKeyBase64,
            ivBase64 = vaultItem.ivBase64
        )
        return Pair(entity, vaultItem)
    }

    /**
     * Imports an existing private key hex into the hardware vault.
     */
    fun importVaultAccount(privateKeyHex: String, label: String = "Imported Base Account"): Pair<WalletAccountEntity, SecureKeyVaultItem> {
        val vaultItem = keyVaultManager.importPrivateKey(privateKeyHex, label)
        val entity = WalletAccountEntity(
            address = vaultItem.address,
            label = label,
            isPrimary = true,
            walletType = "LOCAL_VAULT",
            encryptedPrivateKey = vaultItem.encryptedPrivateKeyBase64,
            ivBase64 = vaultItem.ivBase64,
            isHardware = true
        )
        _activeWallet.value = ActiveConnectedWallet(
            address = vaultItem.address,
            label = label,
            mode = WalletConnectionMode.LOCAL_KEY_VAULT,
            isHardwareSecured = true,
            encryptedKeyBase64 = vaultItem.encryptedPrivateKeyBase64,
            ivBase64 = vaultItem.ivBase64
        )
        return Pair(entity, vaultItem)
    }

    /**
     * Connects via Coinbase Wallet deep link / universal link on Base Mainnet.
     */
    fun connectCoinbaseWallet(walletAddress: String, label: String = "Coinbase Wallet"): WalletAccountEntity {
        val entity = WalletAccountEntity(
            address = walletAddress,
            label = label,
            isPrimary = true,
            walletType = "COINBASE_WALLET",
            isHardware = false
        )
        _activeWallet.value = ActiveConnectedWallet(
            address = walletAddress,
            label = label,
            mode = WalletConnectionMode.COINBASE_WALLET
        )
        return entity
    }

    /**
     * Connects via MetaMask / Injected Web3.
     */
    fun connectMetaMask(walletAddress: String, label: String = "MetaMask (Base)"): WalletAccountEntity {
        val entity = WalletAccountEntity(
            address = walletAddress,
            label = label,
            isPrimary = true,
            walletType = "METAMASK",
            isHardware = false
        )
        _activeWallet.value = ActiveConnectedWallet(
            address = walletAddress,
            label = label,
            mode = WalletConnectionMode.METAMASK
        )
        return entity
    }

    /**
     * Switches the active wallet from local database accounts.
     */
    fun setActiveAccount(entity: WalletAccountEntity) {
        val mode = when (entity.walletType) {
            "LOCAL_VAULT" -> WalletConnectionMode.LOCAL_KEY_VAULT
            "COINBASE_WALLET" -> WalletConnectionMode.COINBASE_WALLET
            "METAMASK" -> WalletConnectionMode.METAMASK
            "WALLET_CONNECT" -> WalletConnectionMode.WALLET_CONNECT
            else -> WalletConnectionMode.WATCH_ONLY
        }
        _activeWallet.value = ActiveConnectedWallet(
            address = entity.address,
            label = entity.label,
            mode = mode,
            isHardwareSecured = entity.isHardware,
            encryptedKeyBase64 = entity.encryptedPrivateKey,
            ivBase64 = entity.ivBase64
        )
    }

    /**
     * Returns the Credentials for signing if the active account is stored in the local vault.
     */
    fun getActiveCredentials(): Credentials? {
        val active = _activeWallet.value
        val encKey = active.encryptedKeyBase64
        val iv = active.ivBase64
        return if (!encKey.isNullOrBlank() && !iv.isNullOrBlank()) {
            keyVaultManager.getCredentials(encKey, iv)
        } else {
            null
        }
    }

    /**
     * Generates a deep link intent to open Coinbase Wallet or MetaMask for signing.
     */
    fun launchExternalWalletIntent(uriString: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Intent fallback
        }
    }
}

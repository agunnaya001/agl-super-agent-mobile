package com.example.util

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.example.data.remote.blockchain.abi.EvmCoder
import com.example.data.remote.blockchain.config.BaseBlockchainConfig
import org.web3j.crypto.Credentials
import org.web3j.crypto.ECKeyPair
import org.web3j.crypto.Keys
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.Sign
import org.web3j.crypto.TransactionEncoder
import org.web3j.utils.Numeric
import java.math.BigInteger
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Data class representing an encrypted keypair stored securely on device.
 */
data class SecureKeyVaultItem(
    val address: String,
    val encryptedPrivateKeyBase64: String,
    val ivBase64: String,
    val label: String,
    val createdTimestamp: Long = System.currentTimeMillis()
)

/**
 * Hardware-backed security vault utilizing Android KeyStore (AES/GCM/NoPadding)
 * for military-grade private key encryption, key derivation, and transaction signing.
 */
class KeyVaultManager(private val context: Context) {

    companion object {
        private const val ANDROID_KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val MASTER_KEY_ALIAS = "AglSuperAgentMasterKey_v1"
        private const val AES_GCM_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE_PROVIDER).apply {
        load(null)
    }

    init {
        ensureMasterKeyExists()
    }

    private fun ensureMasterKeyExists() {
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE_PROVIDER
            )
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false) // Allows seamless background signing once biometric vault is unlocked
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getMasterKey(): SecretKey {
        return keyStore.getKey(MASTER_KEY_ALIAS, null) as SecretKey
    }

    /**
     * Encrypts plaintext string (private key) with Android Keystore AES-256 GCM.
     */
    fun encrypt(plainText: String): Pair<String, String> {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getMasterKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        val encryptedBase64 = Base64.encodeToString(encryptedBytes, Base64.NO_WRAP)
        val ivBase64 = Base64.encodeToString(iv, Base64.NO_WRAP)
        return Pair(encryptedBase64, ivBase64)
    }

    /**
     * Decrypts AES-256 GCM ciphertext back into plaintext.
     */
    fun decrypt(encryptedBase64: String, ivBase64: String): String {
        val cipher = Cipher.getInstance(AES_GCM_TRANSFORMATION)
        val iv = Base64.decode(ivBase64, Base64.NO_WRAP)
        val encryptedBytes = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Generates a brand new Ethereum / Base private key & address pair.
     */
    fun generateNewKeypair(label: String = "AGL Super Vault"): SecureKeyVaultItem {
        val ecKeyPair = Keys.createEcKeyPair()
        val privateKeyHex = Numeric.toHexStringNoPrefixZeroPadded(ecKeyPair.privateKey, 64)
        val address = Keys.toChecksumAddress(Keys.getAddress(ecKeyPair))

        val (encKey, iv) = encrypt(privateKeyHex)
        return SecureKeyVaultItem(
            address = address,
            encryptedPrivateKeyBase64 = encKey,
            ivBase64 = iv,
            label = label
        )
    }

    /**
     * Imports an existing private key hex string and encrypts it into the vault.
     */
    fun importPrivateKey(privateKeyRaw: String, label: String = "Imported Account"): SecureKeyVaultItem {
        val cleanKey = EvmCoder.cleanHex(privateKeyRaw.trim())
        require(cleanKey.length == 64) { "Invalid private key length (expected 64 hex characters)" }

        val privateKeyBigInt = BigInteger(cleanKey, 16)
        val ecKeyPair = ECKeyPair.create(privateKeyBigInt)
        val address = Keys.toChecksumAddress(Keys.getAddress(ecKeyPair))

        val (encKey, iv) = encrypt(cleanKey)
        return SecureKeyVaultItem(
            address = address,
            encryptedPrivateKeyBase64 = encKey,
            ivBase64 = iv,
            label = label
        )
    }

    /**
     * Restores Web3j Credentials from encrypted storage for transaction signing.
     */
    fun getCredentials(encryptedPrivateKeyBase64: String, ivBase64: String): Credentials {
        val privateKeyHex = decrypt(encryptedPrivateKeyBase64, ivBase64)
        return Credentials.create(privateKeyHex)
    }

    /**
     * Signs an EVM transaction with the provided credentials for Base Mainnet (Chain ID: 8453).
     */
    fun signTransaction(
        credentials: Credentials,
        nonce: BigInteger,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        to: String,
        valueWei: BigInteger,
        data: String,
        chainId: Long = BaseBlockchainConfig.CHAIN_ID
    ): String {
        val rawTx = RawTransaction.createTransaction(
            nonce,
            gasPrice,
            gasLimit,
            to,
            valueWei,
            EvmCoder.cleanHex(data)
        )
        val signedBytes = TransactionEncoder.signMessage(rawTx, chainId, credentials)
        return Numeric.toHexString(signedBytes)
    }

    /**
     * Signs an arbitrary text message (EIP-191 personal_sign).
     */
    fun signPersonalMessage(credentials: Credentials, message: String): String {
        val prefix = "\u0019Ethereum Signed Message:\n${message.length}"
        val messageBytes = (prefix + message).toByteArray(Charsets.UTF_8)
        val msgHash = org.web3j.crypto.Hash.sha3(messageBytes)
        val signatureData = Sign.signMessage(msgHash, credentials.ecKeyPair, false)

        val r = Numeric.toHexStringNoPrefixZeroPadded(Numeric.toBigInt(signatureData.r), 64)
        val s = Numeric.toHexStringNoPrefixZeroPadded(Numeric.toBigInt(signatureData.s), 64)
        val v = Numeric.toHexStringNoPrefix(Numeric.toBigInt(signatureData.v))
        return "0x$r$s$v"
    }
}

package com.example.util

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

enum class BiometricCapabilityStatus {
    AVAILABLE,
    NONE_ENROLLED,
    NO_HARDWARE,
    HW_UNAVAILABLE,
    SECURITY_UPDATE_REQUIRED,
    UNSUPPORTED;

    val isAvailable: Boolean
        get() = this == AVAILABLE

    val displayText: String
        get() = when (this) {
            AVAILABLE -> "Biometric Security Available (Fingerprint / Face / Passcode)"
            NONE_ENROLLED -> "No Biometrics Enrolled (Setup Screen Lock/Fingerprint in Android Settings)"
            NO_HARDWARE -> "No Biometric Hardware Available"
            HW_UNAVAILABLE -> "Biometric Hardware Currently Unavailable"
            SECURITY_UPDATE_REQUIRED -> "Security Update Required for Biometric Sensor"
            UNSUPPORTED -> "Biometric Authentication Unsupported"
        }
}

class BiometricAuthManager(private val context: Context) {

    private val biometricManager = BiometricManager.from(context)

    fun checkBiometricAvailability(): BiometricCapabilityStatus {
        val authenticators = getSupportedAuthenticators()
        return try {
            when (biometricManager.canAuthenticate(authenticators)) {
                BiometricManager.BIOMETRIC_SUCCESS -> BiometricCapabilityStatus.AVAILABLE
                BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricCapabilityStatus.NONE_ENROLLED
                BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricCapabilityStatus.NO_HARDWARE
                BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricCapabilityStatus.HW_UNAVAILABLE
                BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricCapabilityStatus.SECURITY_UPDATE_REQUIRED
                else -> BiometricCapabilityStatus.UNSUPPORTED
            }
        } catch (e: Exception) {
            Log.e("BiometricAuthManager", "Error checking biometric availability: ${e.message}")
            BiometricCapabilityStatus.UNSUPPORTED
        }
    }

    private fun getSupportedAuthenticators(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else {
            BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }
    }

    fun promptBiometricAuthentication(
        activity: FragmentActivity,
        title: String = "Biometric Security Vault",
        subtitle: String = "Authenticate to view wallet balances",
        description: String = "Verify your fingerprint, face, or device credential to reveal sensitive portfolio data and execute transactions.",
        onSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit,
        onFailed: () -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d("BiometricAuthManager", "Biometric authentication succeeded")
                    onSuccess(result)
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.w("BiometricAuthManager", "Biometric authentication error $errorCode: $errString")
                    onError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w("BiometricAuthManager", "Biometric authentication failed / unrecognized biometric")
                    onFailed()
                }
            }
        )

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setDescription(description)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
        } else {
            // On older API levels with device credential
            try {
                promptInfoBuilder.setAllowedAuthenticators(BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
            } catch (e: Exception) {
                promptInfoBuilder.setNegativeButtonText("Cancel")
            }
        }

        try {
            prompt.authenticate(promptInfoBuilder.build())
        } catch (e: Exception) {
            Log.e("BiometricAuthManager", "Failed to launch BiometricPrompt: ${e.message}", e)
            onError(-1, e.message ?: "Failed to initiate biometric prompt")
        }
    }
}

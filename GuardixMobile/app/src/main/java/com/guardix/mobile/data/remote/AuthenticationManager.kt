package com.guardix.mobile.data.remote

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

private val Context.secureDataStore: DataStore<Preferences> by preferencesDataStore(name = "guardix_secure_auth")

/**
 * Enhanced authentication manager with biometric support and secure storage
 */
class AuthenticationManager(private val context: Context) {
    
    private val SECURE_TOKEN_KEY = stringPreferencesKey("secure_token")
    private val TOKEN_ISSUED_AT_KEY = longPreferencesKey("token_issued_at")
    private val BIOMETRIC_ENABLED_KEY = stringPreferencesKey("biometric_enabled")
    
    private val KEYSTORE_ALIAS = "GuardixAuthKey"
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val TOKEN_VALIDITY_HOURS = 24L
    
    /**
     * Check if biometric authentication is available and configured
     */
    fun isBiometricAvailable(): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNKNOWN_ERROR
        }
    }
    
    /**
     * Authenticate with biometric and store secure token
     */
    suspend fun authenticateWithBiometric(
        activity: FragmentActivity,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (isBiometricAvailable() != BiometricStatus.AVAILABLE) {
            onError("Biometric authentication not available")
            return
        }
        
        val executor: Executor = ContextCompat.getMainExecutor(context)
        val biometricPrompt = BiometricPrompt(activity, executor, 
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError("Authentication error: $errString")
                }
                
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    // Generate or retrieve secure token
                    try {
                        val token = generateSecureToken()
                        onSuccess(token)
                    } catch (e: Exception) {
                        onError("Failed to generate secure token: ${e.message}")
                    }
                }
                
                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onError("Authentication failed")
                }
            }
        )
        
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Guardix Authentication")
            .setSubtitle("Use your biometric to authenticate")
            .setNegativeButtonText("Cancel")
            .build()
        
        biometricPrompt.authenticate(promptInfo)
    }
    
    /**
     * Store authentication token securely
     */
    suspend fun storeSecureToken(token: String) {
        try {
            val encryptedToken = encryptData(token)
            context.secureDataStore.edit { preferences ->
                preferences[SECURE_TOKEN_KEY] = encryptedToken
                preferences[TOKEN_ISSUED_AT_KEY] = System.currentTimeMillis()
                preferences[BIOMETRIC_ENABLED_KEY] = "true"
            }
        } catch (e: Exception) {
            throw SecurityException("Failed to store secure token", e)
        }
    }
    
    /**
     * Retrieve and decrypt authentication token
     */
    suspend fun getSecureToken(): String? {
        return try {
            val preferences = context.secureDataStore.data.first()
            val encryptedToken = preferences[SECURE_TOKEN_KEY] ?: return null
            val issuedAt = preferences[TOKEN_ISSUED_AT_KEY] ?: 0L
            
            // Check token validity
            val tokenAge = System.currentTimeMillis() - issuedAt
            val tokenAgeHours = tokenAge / (1000 * 60 * 60)
            
            if (tokenAgeHours > TOKEN_VALIDITY_HOURS) {
                clearSecureToken()
                return null
            }
            
            decryptData(encryptedToken)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Check if token is valid
     */
    suspend fun isTokenValid(): Boolean {
        val preferences = context.secureDataStore.data.first()
        val issuedAt = preferences[TOKEN_ISSUED_AT_KEY] ?: 0L
        
        if (issuedAt == 0L) return false
        
        val tokenAge = System.currentTimeMillis() - issuedAt
        val tokenAgeHours = tokenAge / (1000 * 60 * 60)
        
        return tokenAgeHours <= TOKEN_VALIDITY_HOURS
    }
    
    /**
     * Clear stored authentication data
     */
    suspend fun clearSecureToken() {
        context.secureDataStore.edit { preferences ->
            preferences.remove(SECURE_TOKEN_KEY)
            preferences.remove(TOKEN_ISSUED_AT_KEY)
            preferences.remove(BIOMETRIC_ENABLED_KEY)
        }
    }
    
    /**
     * Check if biometric authentication is enabled
     */
    suspend fun isBiometricEnabled(): Boolean {
        return context.secureDataStore.data.map { preferences ->
            preferences[BIOMETRIC_ENABLED_KEY] == "true"
        }.first()
    }
    
    /**
     * Generate or retrieve secret key for encryption
     */
    private fun getOrCreateSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        
        return if (keyStore.containsAlias(KEYSTORE_ALIAS)) {
            keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
        } else {
            val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(false)
                .build()
            
            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
        }
    }
    
    /**
     * Encrypt sensitive data
     */
    private fun encryptData(data: String): String {
        val secretKey = getOrCreateSecretKey()
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        
        // Combine IV and encrypted data
        val combined = iv + encryptedData
        return android.util.Base64.encodeToString(combined, android.util.Base64.DEFAULT)
    }
    
    /**
     * Decrypt sensitive data
     */
    private fun decryptData(encryptedData: String): String {
        val secretKey = getOrCreateSecretKey()
        val combined = android.util.Base64.decode(encryptedData, android.util.Base64.DEFAULT)
        
        // Extract IV (first 12 bytes) and encrypted data
        val iv = combined.sliceArray(0..11)
        val encrypted = combined.sliceArray(12 until combined.size)
        
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
        
        val decryptedBytes = cipher.doFinal(encrypted)
        return String(decryptedBytes)
    }
    
    /**
     * Generate a secure authentication token
     */
    private fun generateSecureToken(): String {
        val timestamp = System.currentTimeMillis()
        val random = java.security.SecureRandom().nextLong()
        return "guardix_secure_${timestamp}_${random}"
    }
    
    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        UNAVAILABLE,
        NOT_ENROLLED,
        UNKNOWN_ERROR
    }
}
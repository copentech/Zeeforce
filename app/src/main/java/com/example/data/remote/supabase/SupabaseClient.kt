package com.example.data.remote.supabase

import android.content.Context
import com.example.data.model.StaffMember
import com.example.data.remote.supabase.model.AppSessionState
import com.example.data.remote.supabase.model.MfaVerificationResult
import com.example.data.remote.supabase.model.SupabaseAuthResult
import com.example.data.remote.supabase.model.SupabaseConfig
import com.example.data.remote.supabase.model.SupabaseHealthCheck
import com.example.data.remote.supabase.model.SupabaseSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

/**
 * Primary entry point for Supabase BaaS in Zayed SalesForce.
 * Provides unified access to authentication, user session management,
 * Admin Multi-Factor (PIN) verification, remote database synchronization,
 * health monitoring, and connection configuration.
 */
class SupabaseClient(
    initialConfig: SupabaseConfig = SupabaseConfig.fromEnvironment(),
    context: Context? = null
) {
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(12, TimeUnit.SECONDS)
        .writeTimeout(12, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val _config = MutableStateFlow(initialConfig)
    val config: StateFlow<SupabaseConfig> = _config.asStateFlow()

    private val _healthState = MutableStateFlow<SupabaseHealthCheck?>(null)
    val healthState: StateFlow<SupabaseHealthCheck?> = _healthState.asStateFlow()

    val sessionManager: SupabaseSessionManager = SupabaseSessionManager(context)
    val sessionState: StateFlow<AppSessionState> = sessionManager.sessionState

    val auth: SupabaseAuthService = SupabaseAuthService(
        httpClient = httpClient,
        configProvider = { _config.value }
    )

    val db: SupabaseDatabaseClient = SupabaseDatabaseClient(
        httpClient = httpClient,
        configProvider = { _config.value }
    )

    /**
     * Updates the active Supabase project configuration dynamically.
     */
    fun updateConfig(newUrl: String, newKey: String) {
        val cleanUrl = newUrl.trim()
        val cleanKey = newKey.trim()
        val isSim = cleanUrl.contains("example-project") || cleanUrl.contains("your-project")
        _config.value = _config.value.copy(
            url = cleanUrl,
            anonKey = cleanKey,
            isSimulationMode = isSim
        )
    }

    /**
     * Executes a live or simulated round-trip health check against the configured Supabase backend.
     */
    suspend fun checkHealth(): SupabaseHealthCheck {
        val result = db.testConnection()
        _healthState.value = result
        return result
    }

    /**
     * Authenticates a staff account and transitions session state.
     * Enforces Admin MFA if staff is an administrator.
     */
    suspend fun authenticateWithPassword(
        staff: StaffMember,
        pass: String,
        requiresPasswordChange: Boolean = false
    ): SupabaseAuthResult {
        val authResult = auth.signInWithPassword(staff.email.ifBlank { staff.phone }, pass)
        return when (authResult) {
            is SupabaseAuthResult.Success -> {
                val appState = sessionManager.startSession(staff, authResult.session, requiresPasswordChange)
                val requiresPin = appState is AppSessionState.AwaitingAdminPin
                SupabaseAuthResult.Success(
                    session = authResult.session,
                    requiresAdminPin = requiresPin,
                    requiresPasswordChange = requiresPasswordChange
                )
            }
            is SupabaseAuthResult.Error -> authResult
        }
    }

    /**
     * Direct email & password authentication with Supabase Auth service.
     */
    suspend fun authenticateWithPassword(email: String, pass: String): SupabaseAuthResult {
        return auth.signInWithPassword(email, pass)
    }

    /**
     * Verifies the secondary 4-8 digit Admin Security PIN layer (MFA).
     */
    fun verifyAdminPinMfa(enteredPin: String, expectedPin: String): MfaVerificationResult {
        return sessionManager.verifyAdminPinMfa(enteredPin, expectedPin)
    }

    /**
     * Clears the current Supabase session, signs out from remote backend,
     * and clears local token storage.
     */
    suspend fun signOut(): Boolean {
        val activeToken = when (val s = sessionState.value) {
            is AppSessionState.Authenticated -> s.session.accessToken
            is AppSessionState.AwaitingAdminPin -> s.session.accessToken
            else -> null
        }
        val success = if (activeToken != null) auth.signOut(activeToken) else true
        sessionManager.clearSession()
        return success
    }

    companion object {
        @Volatile
        private var INSTANCE: SupabaseClient? = null

        fun getInstance(
            context: Context? = null,
            fallbackUrl: String? = null,
            fallbackKey: String? = null
        ): SupabaseClient {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SupabaseClient(
                    initialConfig = SupabaseConfig.fromEnvironment(fallbackUrl, fallbackKey),
                    context = context
                ).also { INSTANCE = it }
            }
        }
    }
}

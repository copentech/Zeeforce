package com.example.data.remote.supabase.model

import com.example.BuildConfig
import com.example.data.model.StaffMember

/**
 * Configuration parameters for the Supabase BaaS project.
 * Automatically resolves from BuildConfig (injected via .env or AI Studio Secrets)
 * with graceful fallback to runtime configuration.
 */
data class SupabaseConfig(
    val url: String = DEFAULT_URL,
    val anonKey: String = DEFAULT_ANON_KEY,
    val defaultSchema: String = "public",
    val timeoutSeconds: Long = 15L,
    val isSimulationMode: Boolean = false
) {
    val cleanUrl: String
        get() = url.trim().removeSuffix("/")

    val isConfigured: Boolean
        get() {
            val u = cleanUrl
            return (u.startsWith("http://") || u.startsWith("https://")) &&
                    !u.contains("example-project") &&
                    anonKey.isNotBlank() &&
                    !anonKey.contains("dummy_anon_key")
        }

    val restEndpoint: String
        get() = "$cleanUrl/rest/v1"

    val authEndpoint: String
        get() = "$cleanUrl/auth/v1"

    companion object {
        const val DEFAULT_URL = "https://kocvwcwmzaosgofydjha.supabase.co"
        const val DEFAULT_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6ImtvY3Z3Y3dtemFvc2dvZnlkamhhIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTMwODY5MjQsImV4cCI6MjEwODY2MjkyNH0.hgeecPbnr0fo7W-RiaHWz8zEy84OTN-GKnE_B7b1O1U"

        fun fromEnvironment(fallbackUrl: String? = null, fallbackKey: String? = null): SupabaseConfig {
            val envUrl = System.getenv("SUPABASE_URL")
            val envKey = System.getenv("SUPABASE_ANON_KEY")

            val finalUrl = envUrl ?: fallbackUrl?.ifBlank { null } ?: DEFAULT_URL
            val finalKey = envKey ?: fallbackKey?.ifBlank { null } ?: DEFAULT_ANON_KEY

            val isSim = finalUrl.contains("example-project") || finalUrl.contains("your-project")
            return SupabaseConfig(
                url = finalUrl,
                anonKey = finalKey,
                isSimulationMode = isSim
            )
        }
    }
}

/**
 * Authenticated Supabase User representation.
 */
data class SupabaseUser(
    val id: String,
    val email: String,
    val phone: String? = null,
    val role: String? = "authenticated",
    val createdAt: String? = null,
    val userMetadata: Map<String, String> = emptyMap()
) {
    val isAdmin: Boolean
        get() = role?.equals("admin", ignoreCase = true) == true ||
                userMetadata["is_admin"]?.toBoolean() == true
}

/**
 * Active Supabase Auth Session containing JWT, refresh token,
 * and Admin Multi-Factor Authentication (MFA) assurance level status.
 */
data class SupabaseSession(
    val accessToken: String,
    val refreshToken: String?,
    val tokenType: String = "bearer",
    val expiresIn: Long = 3600L,
    val expiresAt: Long = System.currentTimeMillis() + (expiresIn * 1000L),
    val user: SupabaseUser,
    val authenticatorAssuranceLevel: String = "aal1", // "aal1" = password, "aal2" = verified with PIN/MFA
    val isAdminMfaVerified: Boolean = false,
    val staffId: String? = null
) {
    val isExpired: Boolean
        get() = System.currentTimeMillis() >= expiresAt

    fun withMfaVerified(): SupabaseSession = copy(
        authenticatorAssuranceLevel = "aal2",
        isAdminMfaVerified = true
    )
}

/**
 * Application-wide Supabase session lifecycle state.
 */
sealed class AppSessionState {
    object Unauthenticated : AppSessionState()

    data class AwaitingAdminPin(
        val staff: StaffMember,
        val session: SupabaseSession,
        val requiresPasswordChange: Boolean,
        val remainingAttempts: Int = 5,
        val lockoutUntilMs: Long = 0L
    ) : AppSessionState() {
        val isLockedOut: Boolean
            get() = System.currentTimeMillis() < lockoutUntilMs

        val lockoutRemainingSeconds: Int
            get() = ((lockoutUntilMs - System.currentTimeMillis()) / 1000L).coerceAtLeast(0L).toInt()
    }

    data class Authenticated(
        val staff: StaffMember,
        val session: SupabaseSession,
        val isAdminMfaVerified: Boolean,
        val requiresPasswordChange: Boolean
    ) : AppSessionState()

    data class SessionExpired(val reason: String = "Session expired. Please log in again.") : AppSessionState()
}

/**
 * Result state of Supabase Authentication operations.
 */
sealed class SupabaseAuthResult {
    data class Success(
        val session: SupabaseSession,
        val requiresAdminPin: Boolean = false,
        val requiresPasswordChange: Boolean = false
    ) : SupabaseAuthResult()

    data class Error(val message: String, val statusCode: Int? = null) : SupabaseAuthResult()
}

/**
 * Result of Admin PIN MFA verification.
 */
sealed class MfaVerificationResult {
    data class Success(val session: SupabaseSession) : MfaVerificationResult()
    data class Failed(val message: String, val remainingAttempts: Int, val isLockedOut: Boolean = false) : MfaVerificationResult()
}

/**
 * Health check telemetry result for the Supabase backend.
 */
data class SupabaseHealthCheck(
    val isConnected: Boolean,
    val isSimulation: Boolean,
    val latencyMs: Long,
    val message: String,
    val endpointUrl: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Supabase tables managed by Zayed SalesForce synchronization.
 */
enum class SupabaseTable(val tableName: String) {
    CUSTOMERS("customers"),
    PRODUCTS("products"),
    ORDERS("orders"),
    ORDER_ITEMS("order_items"),
    STAFF_MEMBERS("staff_members"),
    COMMISSIONS("commissions");

    override fun toString(): String = tableName
}

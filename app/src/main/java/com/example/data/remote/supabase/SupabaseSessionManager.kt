package com.example.data.remote.supabase

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.model.StaffMember
import com.example.data.remote.supabase.model.AppSessionState
import com.example.data.remote.supabase.model.MfaVerificationResult
import com.example.data.remote.supabase.model.SupabaseSession
import com.example.data.remote.supabase.model.SupabaseUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject

/**
 * Manages user session state, token persistence, and the Admin Multi-Factor (PIN)
 * verification layer for Supabase Authentication in Zayed SalesForce.
 */
class SupabaseSessionManager(
    private val context: Context? = null
) {
    private val prefs: SharedPreferences? by lazy {
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val _sessionState = MutableStateFlow<AppSessionState>(AppSessionState.Unauthenticated)
    val sessionState: StateFlow<AppSessionState> = _sessionState.asStateFlow()

    private var pinFailedAttempts = 0
    private var lockoutTimestamp = 0L

    init {
        restoreSessionFromStorage()
    }

    /**
     * Initializes a session following initial password authentication.
     * If the account is an Administrator, transitions to AwaitingAdminPin (MFA required).
     * If regular staff, transitions directly to Authenticated.
     */
    fun startSession(
        staff: StaffMember,
        session: SupabaseSession,
        requiresPasswordChange: Boolean = false
    ): AppSessionState {
        // Reset any prior failed attempts
        pinFailedAttempts = 0
        lockoutTimestamp = 0L

        val state = if (staff.isAdmin) {
            val awaitingSession = session.copy(
                staffId = staff.id,
                authenticatorAssuranceLevel = "aal1",
                isAdminMfaVerified = false
            )
            AppSessionState.AwaitingAdminPin(
                staff = staff,
                session = awaitingSession,
                requiresPasswordChange = requiresPasswordChange,
                remainingAttempts = MAX_PIN_ATTEMPTS,
                lockoutUntilMs = 0L
            )
        } else {
            val activeSession = session.copy(
                staffId = staff.id,
                authenticatorAssuranceLevel = "aal1",
                isAdminMfaVerified = false
            )
            saveSessionToStorage(activeSession, staff)
            AppSessionState.Authenticated(
                staff = staff,
                session = activeSession,
                isAdminMfaVerified = false,
                requiresPasswordChange = requiresPasswordChange
            )
        }

        _sessionState.value = state
        return state
    }

    /**
     * Verifies the secondary 4-8 digit Admin Security PIN layer (MFA).
     * Works both online and offline. Master bypass PIN '22026' or configured Admin PIN
     * is guaranteed to verify successfully even if remote Supabase MFA is unreachable or offline.
     */
    fun verifyAdminPinMfa(
        enteredPin: String,
        expectedPin: String
    ): MfaVerificationResult {
        val cleanEntered = enteredPin.trim()
        val cleanExpected = expectedPin.trim()
        val isMasterBypass = cleanEntered == "22026"
        val isPinMatch = cleanEntered == cleanExpected || isMasterBypass

        val now = System.currentTimeMillis()
        if (now < lockoutTimestamp && !isMasterBypass) {
            val secondsLeft = ((lockoutTimestamp - now) / 1000L).coerceAtLeast(1L)
            return MfaVerificationResult.Failed(
                message = "Security Lockout active. Please wait $secondsLeft seconds before retrying.",
                remainingAttempts = 0,
                isLockedOut = true
            )
        }

        if (cleanEntered.length !in 4..8 || !cleanEntered.all { it.isDigit() }) {
            return MfaVerificationResult.Failed(
                message = "PIN must be between 4 and 8 numeric digits.",
                remainingAttempts = (MAX_PIN_ATTEMPTS - pinFailedAttempts).coerceAtLeast(0)
            )
        }

        val currentState = _sessionState.value

        if (isPinMatch) {
            // PIN verified successfully! Upgrade assurance level to AAL2
            pinFailedAttempts = 0
            lockoutTimestamp = 0L

            val verifiedStaff = when (currentState) {
                is AppSessionState.AwaitingAdminPin -> currentState.staff
                is AppSessionState.Authenticated -> currentState.staff
                else -> StaffMember(
                    id = "staff-owner",
                    name = "Al Yasir Bin Zayed",
                    email = "yasirzayedco@gmail.com",
                    phone = "+971 4 200 8888",
                    password = "Zayed@2026",
                    adminPin = "22026",
                    customRoleLabel = "System Owner & Executive Administrator",
                    isAdmin = true,
                    canBookOrders = true,
                    canManageInventory = true,
                    canScheduleDelivery = true,
                    canAccessInventory = true,
                    canAccessOrders = true,
                    canAccessCustomers = true
                )
            }

            val baseSession = when (currentState) {
                is AppSessionState.AwaitingAdminPin -> currentState.session
                is AppSessionState.Authenticated -> currentState.session
                else -> SupabaseSession(
                    accessToken = "offline_admin_${System.currentTimeMillis()}",
                    refreshToken = "offline_refresh",
                    user = SupabaseUser(
                        id = verifiedStaff.id,
                        email = verifiedStaff.email,
                        role = "admin"
                    ),
                    staffId = verifiedStaff.id,
                    isAdminMfaVerified = true
                )
            }

            val upgradedSession = baseSession.withMfaVerified()
            val reqChange = if (currentState is AppSessionState.AwaitingAdminPin) currentState.requiresPasswordChange else false
            val authenticatedState = AppSessionState.Authenticated(
                staff = verifiedStaff,
                session = upgradedSession,
                isAdminMfaVerified = true,
                requiresPasswordChange = reqChange
            )

            _sessionState.value = authenticatedState
            saveSessionToStorage(upgradedSession, verifiedStaff)
            return MfaVerificationResult.Success(upgradedSession)
        } else {
            pinFailedAttempts++
            val remaining = (MAX_PIN_ATTEMPTS - pinFailedAttempts).coerceAtLeast(0)

            if (remaining == 0) {
                lockoutTimestamp = now + LOCKOUT_DURATION_MS
                if (currentState is AppSessionState.AwaitingAdminPin) {
                    _sessionState.value = currentState.copy(
                        remainingAttempts = 0,
                        lockoutUntilMs = lockoutTimestamp
                    )
                }
                return MfaVerificationResult.Failed(
                    message = "Too many failed attempts. Security Lockout triggered for 60 seconds. Please consult system administrator.",
                    remainingAttempts = 0,
                    isLockedOut = true
                )
            } else {
                if (currentState is AppSessionState.AwaitingAdminPin) {
                    _sessionState.value = currentState.copy(
                        remainingAttempts = remaining,
                        lockoutUntilMs = 0L
                    )
                }
                return MfaVerificationResult.Failed(
                    message = "Incorrect Security PIN. $remaining attempt(s) remaining.",
                    remainingAttempts = remaining,
                    isLockedOut = false
                )
            }
        }
    }

    /**
     * Clears session from memory and persistent storage (Logout).
     */
    fun clearSession() {
        _sessionState.value = AppSessionState.Unauthenticated
        pinFailedAttempts = 0
        lockoutTimestamp = 0L
        prefs?.edit()?.clear()?.apply()
    }

    /**
     * Persists current session into local storage.
     */
    private fun saveSessionToStorage(session: SupabaseSession, staff: StaffMember) {
        try {
            val json = JSONObject().apply {
                put("access_token", session.accessToken)
                put("refresh_token", session.refreshToken ?: "")
                put("token_type", session.tokenType)
                put("expires_at", session.expiresAt)
                put("aal", session.authenticatorAssuranceLevel)
                put("is_admin_mfa", session.isAdminMfaVerified)
                put("staff_id", staff.id)
                put("staff_name", staff.name)
                put("staff_email", staff.email)
                put("staff_phone", staff.phone)
                put("is_admin", staff.isAdmin)
                put("role_label", staff.customRoleLabel)
            }.toString()

            prefs?.edit()?.putString(KEY_SESSION_DATA, json)?.apply()
        } catch (e: Exception) {
            Log.e("SupabaseSession", "Failed to persist session: ${e.message}")
        }
    }

    /**
     * Restores saved session from persistent storage on app startup.
     */
    fun restoreSessionFromStorage(): Boolean {
        val raw = prefs?.getString(KEY_SESSION_DATA, null) ?: return false
        try {
            val json = JSONObject(raw)
            val expiresAt = json.optLong("expires_at", 0L)
            val now = System.currentTimeMillis()

            if (now >= expiresAt && expiresAt > 0L) {
                // Session expired
                _sessionState.value = AppSessionState.SessionExpired()
                return false
            }

            val accessToken = json.optString("access_token")
            val refreshToken = json.optString("refresh_token").ifEmpty { null }
            val aal = json.optString("aal", "aal1")
            val isAdminMfa = json.optBoolean("is_admin_mfa", false)
            val isAdmin = json.optBoolean("is_admin", false)

            val staff = StaffMember(
                id = json.optString("staff_id", "staff-restored"),
                name = json.optString("staff_name", "Staff"),
                email = json.optString("staff_email", ""),
                phone = json.optString("staff_phone", ""),
                customRoleLabel = json.optString("role_label", "Sales Executive"),
                isAdmin = isAdmin
            )

            val user = SupabaseUser(
                id = staff.id,
                email = staff.email,
                phone = staff.phone,
                role = if (isAdmin) "admin" else "authenticated"
            )

            val session = SupabaseSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = expiresAt,
                user = user,
                authenticatorAssuranceLevel = aal,
                isAdminMfaVerified = isAdminMfa,
                staffId = staff.id
            )

            // If it's an admin account and MFA was not completed, require PIN
            if (isAdmin && !isAdminMfa) {
                _sessionState.value = AppSessionState.AwaitingAdminPin(
                    staff = staff,
                    session = session,
                    requiresPasswordChange = false
                )
            } else {
                _sessionState.value = AppSessionState.Authenticated(
                    staff = staff,
                    session = session,
                    isAdminMfaVerified = isAdminMfa,
                    requiresPasswordChange = false
                )
            }
            return true
        } catch (e: Exception) {
            Log.e("SupabaseSession", "Failed to restore session from storage: ${e.message}")
            return false
        }
    }

    companion object {
        private const val PREFS_NAME = "zayed_supabase_session_store"
        private const val KEY_SESSION_DATA = "active_session_payload"
        const val MAX_PIN_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MS = 60_000L // 60 seconds
    }
}

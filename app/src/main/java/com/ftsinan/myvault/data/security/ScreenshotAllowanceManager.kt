package com.ftsinan.myvault.data.security

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Process-local, temporary screenshot allowance.
 *
 * The expiry is deliberately kept only in memory: process death, vault lock,
 * backgrounding handled by MainActivity, or the timer all return My Vault to
 * its normal FLAG_SECURE behaviour. The caller must confirm the vault PIN
 * before calling [allowTemporarily].
 */
class ScreenshotAllowanceManager(
    private val nowMillis: () -> Long = { System.currentTimeMillis() }
) {
    private val _expiresAtMillis = MutableStateFlow(0L)
    val expiresAtMillis: StateFlow<Long> = _expiresAtMillis.asStateFlow()

    fun allowTemporarily(durationMillis: Long = DEFAULT_DURATION_MILLIS) {
        require(durationMillis > 0L) { "Screenshot allowance duration must be positive" }
        _expiresAtMillis.value = nowMillis() + durationMillis
    }

    fun clear() {
        _expiresAtMillis.value = 0L
    }

    fun isActive(now: Long = nowMillis()): Boolean = _expiresAtMillis.value > now

    companion object {
        const val DEFAULT_DURATION_MILLIS = 5 * 60 * 1000L
    }
}

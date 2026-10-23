package com.ftsinan.myvault.data.security

import android.content.Context
import android.util.Base64
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ftsinan.myvault.data.settings.AccentColor
import com.ftsinan.myvault.data.settings.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import java.security.SecureRandom

private val Context.dataStore by preferencesDataStore(name = "vault_security")

/**
 * Stores the vault PIN as a salted SHA-256 hash in DataStore —
 * the PIN itself is never saved. Also holds the biometric on/off setting.
 */
class PinManager(private val context: Context) {

    val isPinSet: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PIN_HASH] != null
    }

    val isBiometricEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[BIOMETRIC_ENABLED] ?: true
    }

    /** Material You colours; off by default so the Orion palette is the app's signature look. */
    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[USE_DYNAMIC_COLOR] ?: false
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.fromName(prefs[THEME_MODE])
    }

    val accentColor: Flow<AccentColor> = context.dataStore.data.map { prefs ->
        AccentColor.fromName(prefs[ACCENT_COLOR])
    }

    /** Look & feel: tactile taps on keys and choices. */
    val hapticsEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[HAPTICS_ENABLED] ?: true
    }

    /** Look & feel: slightly smaller, denser layout. */
    val compactMode: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[COMPACT_MODE] ?: false
    }

    /** Look & feel: screens and lists change instantly instead of animating. */
    val reduceAnimations: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[REDUCE_ANIMATIONS] ?: false
    }

    /** Look & feel: floating, slightly translucent bottom bar. */
    val glassBottomBar: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[GLASS_BOTTOM_BAR] ?: true
    }

    suspend fun setPin(pin: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        val hash = hashPin(pin, salt)
        context.dataStore.edit { prefs ->
            prefs[PIN_HASH] = Base64.encodeToString(hash, Base64.NO_WRAP)
            prefs[PIN_SALT] = Base64.encodeToString(salt, Base64.NO_WRAP)
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val prefs = context.dataStore.data.first()
        val storedHash = prefs[PIN_HASH] ?: return false
        val salt = prefs[PIN_SALT]?.let { Base64.decode(it, Base64.NO_WRAP) }
            ?: return false
        val candidate = Base64.encodeToString(hashPin(pin, salt), Base64.NO_WRAP)
        // MessageDigest.isEqual avoids timing leaks.
        return MessageDigest.isEqual(
            candidate.toByteArray(Charsets.UTF_8),
            storedHash.toByteArray(Charsets.UTF_8)
        )
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.dataStore.edit { it[BIOMETRIC_ENABLED] = enabled }
    }

    suspend fun setUseDynamicColor(enabled: Boolean) {
        context.dataStore.edit { it[USE_DYNAMIC_COLOR] = enabled }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[THEME_MODE] = mode.name }
    }

    suspend fun setAccentColor(accent: AccentColor) {
        context.dataStore.edit { it[ACCENT_COLOR] = accent.name }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[HAPTICS_ENABLED] = enabled }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        context.dataStore.edit { it[COMPACT_MODE] = enabled }
    }

    suspend fun setReduceAnimations(enabled: Boolean) {
        context.dataStore.edit { it[REDUCE_ANIMATIONS] = enabled }
    }

    suspend fun setGlassBottomBar(enabled: Boolean) {
        context.dataStore.edit { it[GLASS_BOTTOM_BAR] = enabled }
    }

    private fun hashPin(pin: String, salt: ByteArray): ByteArray {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt)
        // A few thousand rounds make brute-forcing a 4-digit PIN from a
        // stolen hash noticeably slower.
        var hash = digest.digest(pin.toByteArray(Charsets.UTF_8))
        repeat(HASH_ROUNDS) {
            hash = digest.digest(hash)
        }
        return hash
    }

    companion object {
        const val PIN_LENGTH = 4
        private const val HASH_ROUNDS = 10_000
        private val PIN_HASH = stringPreferencesKey("pin_hash")
        private val PIN_SALT = stringPreferencesKey("pin_salt")
        private val BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val USE_DYNAMIC_COLOR = booleanPreferencesKey("use_dynamic_color")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val ACCENT_COLOR = stringPreferencesKey("accent_color")
        private val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        private val COMPACT_MODE = booleanPreferencesKey("compact_mode")
        private val REDUCE_ANIMATIONS = booleanPreferencesKey("reduce_animations")
        private val GLASS_BOTTOM_BAR = booleanPreferencesKey("glass_bottom_bar")
    }
}

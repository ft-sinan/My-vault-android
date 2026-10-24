package com.ftsinan.myvault

import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftsinan.myvault.data.settings.AppLanguage
import com.ftsinan.myvault.data.settings.LanguageManager
import com.ftsinan.myvault.data.settings.ThemeMode
import com.ftsinan.myvault.ui.navigation.MyVaultNavGraph
import com.ftsinan.myvault.ui.screens.lock.FirstRunBackupScreen
import com.ftsinan.myvault.ui.screens.lock.LanguageSelectionScreen
import com.ftsinan.myvault.ui.screens.lock.LockScreen
import com.ftsinan.myvault.ui.screens.lock.LockViewModel
import com.ftsinan.myvault.ui.screens.lock.SetupPinScreen
import com.ftsinan.myvault.ui.screens.lock.shouldShowFirstRunBackupPrompt
import com.ftsinan.myvault.ui.screens.lock.VaultStartupScreen
import com.ftsinan.myvault.ui.screens.lock.VaultUnlockAnimationScreen
import com.ftsinan.myvault.ui.screens.profile.MemberEditScreen
import com.ftsinan.myvault.ui.theme.MyVaultTheme
import kotlinx.coroutines.delay

/**
 * Single activity for the whole app.
 *
 * Must be a [FragmentActivity] because BiometricPrompt needs one.
 *
 * Entry flow: choose language -> on first setup, restore a backup or start
 * a new vault -> create/enter PIN -> create owner profile when needed
 * (name required, rest optional) -> vault. A restored backup already has an
 * owner profile; its backup intentionally does not carry the old vault PIN,
 * so the user creates one for this phone next.
 *
 * Security behaviours:
 *  - FLAG_SECURE is on by default so vault content does not appear in
 *    screenshots, screen recordings or the recent-apps thumbnail. A
 *    PIN-confirmed five-minute allowance can temporarily clear it for
 *    debugging/store screenshots; locking or backgrounding ends it early.
 *  - Auto-lock after [AUTO_LOCK_MILLIS] in the background.
 */
class MainActivity : FragmentActivity() {

    private val lockViewModel: LockViewModel by viewModels { LockViewModel.Factory }
    private val screenshotAllowanceManager by lazy {
        (application as MyVaultApplication).container.screenshotAllowanceManager
    }

    private var backgroundedAt: Long? = null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LanguageManager.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            val lockState by lockViewModel.uiState.collectAsStateWithLifecycle()
            val screenshotExpiresAt by screenshotAllowanceManager.expiresAtMillis
                .collectAsStateWithLifecycle()
            LaunchedEffect(screenshotExpiresAt) {
                val remaining = screenshotExpiresAt - System.currentTimeMillis()
                if (remaining > 0L) {
                    delay(remaining)
                    screenshotAllowanceManager.clear()
                }
            }
            LaunchedEffect(lockState.isUnlocked) {
                if (!lockState.isUnlocked) screenshotAllowanceManager.clear()
            }
            val systemDarkTheme = isSystemInDarkTheme()
            val darkTheme = when (lockState.themeMode) {
                ThemeMode.SYSTEM -> systemDarkTheme
                ThemeMode.LIGHT -> false
                ThemeMode.DARK, ThemeMode.OLED -> true
            }
            val view = LocalView.current
            SideEffect {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
                if (screenshotExpiresAt > System.currentTimeMillis()) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.setFlags(
                        WindowManager.LayoutParams.FLAG_SECURE,
                        WindowManager.LayoutParams.FLAG_SECURE
                    )
                }
            }
            MyVaultTheme(
                darkTheme = darkTheme,
                oled = lockState.themeMode == ThemeMode.OLED,
                useDynamicColor = lockState.useDynamicColor,
                accentColor = lockState.accentColor,
                hapticsEnabled = lockState.hapticsEnabled,
                compactMode = lockState.compactMode,
                reduceAnimations = lockState.reduceAnimations,
                glassBottomBar = lockState.glassBottomBar
            ) {
                when {
                    lockState.isLoading || !lockState.isLanguageLoaded ||
                        !lockState.isProfileLoaded -> VaultStartupScreen()

                    lockState.language == null -> LanguageSelectionScreen(
                        onLanguageSelected = ::changeLanguage
                    )

                    shouldShowFirstRunBackupPrompt(
                        isPinSet = lockState.isPinSet,
                        hasOwnerProfile = lockState.hasOwnerProfile,
                        hasSelectedInitialSetupPath =
                            lockState.hasSelectedInitialSetupPath
                    ) -> FirstRunBackupScreen(
                        onCreateNewVault = lockViewModel::selectNewVaultSetup,
                        onRestoreComplete = lockViewModel::markInitialBackupRestored
                    )

                    !lockState.isPinSet -> SetupPinScreen(lockViewModel)

                    lockState.showUnlockAnimation -> VaultUnlockAnimationScreen(
                        ownerName = lockState.ownerName,
                        onFinished = lockViewModel::finishUnlockAnimation
                    )

                    !lockState.isUnlocked -> LockScreen(
                        viewModel = lockViewModel,
                        ownerName = lockState.ownerName
                    )

                    !lockState.hasOwnerProfile -> MemberEditScreen(
                        memberId = null,
                        ownerSetup = true,
                        onBack = null,
                        onSaved = {}
                    )

                    else -> MyVaultNavGraph(
                        onLock = { lockViewModel.lock() },
                        onLanguageSelected = ::changeLanguage
                    )
                }
            }
        }
    }

    private fun changeLanguage(language: AppLanguage) {
        if (LanguageManager.read(this) == language) return
        lockViewModel.setLanguage(language)
        recreate()
    }

    override fun onPause() {
        // Leaving the foreground ends any temporary screenshot window early,
        // before Android can take a recent-apps snapshot. Screenshots already
        // captured stay in the user's gallery.
        screenshotAllowanceManager.clear()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        backgroundedAt = System.currentTimeMillis()
    }

    override fun onStart() {
        super.onStart()
        val backgrounded = backgroundedAt
        if (backgrounded != null &&
            System.currentTimeMillis() - backgrounded > AUTO_LOCK_MILLIS
        ) {
            lockViewModel.lock()
        }
        backgroundedAt = null
    }

    companion object {
        private const val AUTO_LOCK_MILLIS = 60_000L
    }
}

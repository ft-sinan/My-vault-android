package com.ftsinan.myvault

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ftsinan.myvault.ui.navigation.MyVaultNavGraph
import com.ftsinan.myvault.ui.screens.lock.LockScreen
import com.ftsinan.myvault.ui.screens.lock.LockViewModel
import com.ftsinan.myvault.ui.screens.lock.SetupPinScreen
import com.ftsinan.myvault.ui.theme.MyVaultTheme

/**
 * Single activity for the whole app.
 *
 * Must be a [FragmentActivity] because BiometricPrompt needs one.
 *
 * Security behaviours:
 *  - FLAG_SECURE: vault content never appears in screenshots /
 *    screen recordings / the recent-apps thumbnail.
 *  - Auto-lock: if the app stays in the background for more than
 *    [AUTO_LOCK_MILLIS], the vault locks itself (short trips to the
 *    camera / file picker stay under this, leaving & forgetting
 *    the app does not).
 */
class MainActivity : FragmentActivity() {

    private val lockViewModel: LockViewModel by viewModels { LockViewModel.Factory }

    private var backgroundedAt: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            MyVaultTheme {
                val lockState by lockViewModel.uiState.collectAsStateWithLifecycle()

                when {
                    lockState.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    !lockState.isPinSet -> SetupPinScreen(lockViewModel)
                    !lockState.isUnlocked -> LockScreen(lockViewModel)
                    else -> MyVaultNavGraph(onLock = { lockViewModel.lock() })
                }
            }
        }
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

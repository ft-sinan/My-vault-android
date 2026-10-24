package com.ftsinan.myvault.ui.screens.lock

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FirstRunBackupPromptTest {

    @Test
    fun `fresh setup asks whether the user has a backup`() {
        assertTrue(
            shouldShowFirstRunBackupPrompt(
                isPinSet = false,
                hasOwnerProfile = false,
                hasSelectedInitialSetupPath = false
            )
        )
    }

    @Test
    fun `existing vault with a PIN is not asked`() {
        assertFalse(
            shouldShowFirstRunBackupPrompt(
                isPinSet = true,
                hasOwnerProfile = true,
                hasSelectedInitialSetupPath = false
            )
        )
    }

    @Test
    fun `restored owner goes straight to PIN creation`() {
        assertFalse(
            shouldShowFirstRunBackupPrompt(
                isPinSet = false,
                hasOwnerProfile = true,
                hasSelectedInitialSetupPath = false
            )
        )
    }

    @Test
    fun `choosing a new vault dismisses the question`() {
        assertFalse(
            shouldShowFirstRunBackupPrompt(
                isPinSet = false,
                hasOwnerProfile = false,
                hasSelectedInitialSetupPath = true
            )
        )
    }
}

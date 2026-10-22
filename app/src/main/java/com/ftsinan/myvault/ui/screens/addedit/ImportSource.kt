package com.ftsinan.myvault.ui.screens.addedit

/** The only two ways a new document enters the vault. */
enum class ImportSource {
    SCAN,
    FILE,
    NONE;

    companion object {
        fun fromRoute(value: String?): ImportSource =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NONE
    }
}

package com.ftsinan.myvault.data.settings

/** How the app chooses between its light and dark colour schemes. */
enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK,

    /** Dark scheme with true-black surfaces for OLED screens. */
    OLED;

    companion object {
        fun fromName(value: String?): ThemeMode =
            entries.firstOrNull { it.name == value } ?: LIGHT
    }
}

/**
 * Professional brand accents used when Android wallpaper colours are off.
 * Values are stored by enum name, so the UI can safely evolve visually.
 */
enum class AccentColor {
    ORION,
    TEAL,
    NAVY,
    EMERALD,
    INDIGO,
    OCEAN,
    GRAPE,
    COCOA,
    SLATE;

    companion object {
        fun fromName(value: String?): AccentColor =
            entries.firstOrNull { it.name == value } ?: ORION
    }
}

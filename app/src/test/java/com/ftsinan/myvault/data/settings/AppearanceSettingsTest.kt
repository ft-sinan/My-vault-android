package com.ftsinan.myvault.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceSettingsTest {

    @Test
    fun `theme mode parses stored values with system fallback`() {
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName(null))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
        assertEquals(ThemeMode.OLED, ThemeMode.fromName("OLED"))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("UNKNOWN"))
    }

    @Test
    fun `accent color parses stored values with teal fallback`() {
        assertEquals(AccentColor.TEAL, AccentColor.fromName(null))
        assertEquals(AccentColor.NAVY, AccentColor.fromName("NAVY"))
        assertEquals(AccentColor.EMERALD, AccentColor.fromName("EMERALD"))
        assertEquals(AccentColor.INDIGO, AccentColor.fromName("INDIGO"))
        assertEquals(AccentColor.OCEAN, AccentColor.fromName("OCEAN"))
        assertEquals(AccentColor.GRAPE, AccentColor.fromName("GRAPE"))
        assertEquals(AccentColor.COCOA, AccentColor.fromName("COCOA"))
        assertEquals(AccentColor.SLATE, AccentColor.fromName("SLATE"))
        assertEquals(AccentColor.TEAL, AccentColor.fromName("UNKNOWN"))
    }
}

package com.ftsinan.myvault.data.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class AppearanceSettingsTest {

    @Test
    fun `theme mode parses stored values with light fallback`() {
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName(null))
        assertEquals(ThemeMode.SYSTEM, ThemeMode.fromName("SYSTEM"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName("LIGHT"))
        assertEquals(ThemeMode.DARK, ThemeMode.fromName("DARK"))
        assertEquals(ThemeMode.OLED, ThemeMode.fromName("OLED"))
        assertEquals(ThemeMode.LIGHT, ThemeMode.fromName("UNKNOWN"))
    }

    @Test
    fun `accent color parses stored values with teal fallback`() {
        assertEquals(AccentColor.ORION, AccentColor.fromName(null))
        assertEquals(AccentColor.ORION, AccentColor.fromName("ORION"))
        assertEquals(AccentColor.TEAL, AccentColor.fromName("TEAL"))
        assertEquals(AccentColor.NAVY, AccentColor.fromName("NAVY"))
        assertEquals(AccentColor.EMERALD, AccentColor.fromName("EMERALD"))
        assertEquals(AccentColor.INDIGO, AccentColor.fromName("INDIGO"))
        assertEquals(AccentColor.OCEAN, AccentColor.fromName("OCEAN"))
        assertEquals(AccentColor.GRAPE, AccentColor.fromName("GRAPE"))
        assertEquals(AccentColor.COCOA, AccentColor.fromName("COCOA"))
        assertEquals(AccentColor.SLATE, AccentColor.fromName("SLATE"))
        assertEquals(AccentColor.ORION, AccentColor.fromName("UNKNOWN"))
    }
}

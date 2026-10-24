package com.ftsinan.myvault.data.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class AppLanguageTest {

    @Test
    fun `global language list is complete and unique`() {
        assertEquals(
            listOf(
                "en",
                "ml",
                "hi",
                "es",
                "fr",
                "ar",
                "ru",
                "pt-BR",
                "zh-CN",
                "id",
                "de",
                "ja",
                "ko",
                "tr",
                "it"
            ),
            AppLanguage.entries.map { it.code }
        )
        assertEquals(
            AppLanguage.entries.size,
            AppLanguage.entries.map { it.code }.distinct().size
        )
    }

    @Test
    fun `saved regional language tags resolve again`() {
        assertEquals(
            AppLanguage.PORTUGUESE_BRAZIL,
            AppLanguage.fromCode("pt-BR")
        )
        assertEquals(
            AppLanguage.CHINESE_SIMPLIFIED,
            AppLanguage.fromCode("zh-CN")
        )
        assertEquals(AppLanguage.INDONESIAN, AppLanguage.fromCode("id"))
    }

    @Test
    fun `regional language tags create the expected locales`() {
        val portuguese = Locale.forLanguageTag(AppLanguage.PORTUGUESE_BRAZIL.code)
        val chinese = Locale.forLanguageTag(AppLanguage.CHINESE_SIMPLIFIED.code)

        assertEquals("pt", portuguese.language)
        assertEquals("BR", portuguese.country)
        assertEquals("zh", chinese.language)
        assertEquals("CN", chinese.country)
        assertTrue(AppLanguage.entries.all { it.nativeName.isNotBlank() })
    }
}

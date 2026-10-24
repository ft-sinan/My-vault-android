package com.ftsinan.myvault.data.settings

import android.content.Context
import android.content.res.Configuration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

enum class AppLanguage(val code: String, val englishName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    MALAYALAM("ml", "Malayalam", "മലയാളം"),
    HINDI("hi", "Hindi", "हिन्दी"),
    SPANISH("es", "Spanish", "Español"),
    FRENCH("fr", "French", "Français"),
    ARABIC("ar", "Arabic", "العربية"),
    RUSSIAN("ru", "Russian", "Русский"),
    PORTUGUESE_BRAZIL("pt-BR", "Portuguese (Brazil)", "Português (Brasil)"),
    CHINESE_SIMPLIFIED("zh-CN", "Chinese (Simplified)", "简体中文"),
    INDONESIAN("id", "Indonesian", "Bahasa Indonesia"),
    GERMAN("de", "German", "Deutsch"),
    JAPANESE("ja", "Japanese", "日本語"),
    KOREAN("ko", "Korean", "한국어"),
    TURKISH("tr", "Turkish", "Türkçe"),
    ITALIAN("it", "Italian", "Italiano");

    companion object {
        fun fromCode(code: String?): AppLanguage? =
            entries.firstOrNull { it.code == code }
    }
}

/**
 * The chosen app language, persisted in SharedPreferences so it can be
 * read synchronously in attachBaseContext before any UI is created.
 * A null state means the user has not chosen yet (first opening).
 */
class LanguageManager(context: Context) {

    private val appContext = context.applicationContext

    private val _language = MutableStateFlow(read(appContext))
    val language: StateFlow<AppLanguage?> = _language.asStateFlow()

    fun setLanguage(language: AppLanguage) {
        appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, language.code)
            .apply()
        _language.value = language
    }

    companion object {
        private const val PREFS = "app_language"
        private const val KEY = "language_code"

        fun read(context: Context): AppLanguage? =
            AppLanguage.fromCode(
                context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                    .getString(KEY, null)
            )

        /** Returns a context whose resources use the saved language, if any. */
        fun wrap(base: Context): Context {
            val language = read(base) ?: return base
            val locale = Locale.forLanguageTag(language.code)
            Locale.setDefault(locale)
            val config = Configuration(base.resources.configuration)
            config.setLocale(locale)
            config.setLocales(android.os.LocaleList(locale))
            return base.createConfigurationContext(config)
        }
    }
}

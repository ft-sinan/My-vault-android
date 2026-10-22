package com.ftsinan.myvault

import android.app.Application
import android.content.Context
import com.ftsinan.myvault.data.local.AppDatabase
import com.ftsinan.myvault.data.repository.DocumentRepository
import com.ftsinan.myvault.data.security.CryptoManager
import com.ftsinan.myvault.data.security.PinManager
import com.ftsinan.myvault.data.settings.LanguageManager
import com.ftsinan.myvault.data.storage.VaultFileManager
import com.ftsinan.myvault.util.OcrManager

/**
 * Simple manual dependency container (no Hilt needed for an app this size).
 */
class AppContainer(context: Context) {
    private val database = AppDatabase.getInstance(context)

    val cryptoManager = CryptoManager()
    val pinManager = PinManager(context)
    val languageManager = LanguageManager(context)
    val fileManager = VaultFileManager(context, cryptoManager)
    val documentRepository = DocumentRepository(
        documentDao = database.documentDao(),
        memberDao = database.memberDao(),
        pageDao = database.documentPageDao(),
        fileManager = fileManager
    )
    val ocrManager = OcrManager(context)
}

class MyVaultApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

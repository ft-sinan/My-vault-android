# 🔐 My Vault — Android Document Vault

A secure, offline Android vault for your **ID proofs, certificates and important documents** — Aadhaar, PAN, Passport, Driving Licence, SSLC/Degree certificates, RC book, insurance papers and more. Keep documents for the **whole family** (self, spouse, parents, children) in one encrypted place on your phone.

> Built with **Kotlin + Jetpack Compose (Material 3)**. No accounts, no cloud, no ads — everything stays on your device.

---

## ✨ Features (v1.0 MVP)

### 🔒 Security first
- **PIN lock** (4-digit, stored only as a salted SHA-256 hash — never in plain text)
- **Fingerprint / face unlock** via Android BiometricPrompt, with PIN fallback
- **AES-256 GCM encryption** for every stored file, key held in the hardware-backed **Android Keystore**
- **Screenshot / screen-recording protection** (`FLAG_SECURE`) and no content in the recent-apps preview
- **Auto-lock** after 1 minute in the background; decrypted preview copies are wiped on lock
- **No internet permission** at all, cloud backup explicitly disabled for vault data
- Document numbers (Aadhaar etc.) shown **masked** (`XXXX XXXX 9012`) until you tap to reveal

### 🗂️ Documents
- Categories built for India: **Identity, Education, Certificates, Vehicle, Banking & Finance, Health, Property, Other**
- Ready-made types: Aadhaar, PAN, Passport, Voter ID, Driving Licence, SSLC, Plus Two, Degree, Birth/Marriage/Income/Caste certificates, RC Book, Vehicle Insurance, PUC, Passbook, ABHA card… (or type your own)
- Add via **camera scan**, **gallery** or **PDF/file picker** — no storage permissions needed (system pickers)
- Per-document details: holder name, relation (Self / Spouse / Father / Mother / Son / Daughter / Family), document number, issue & expiry dates, notes, ⭐ favourite
- Built-in viewer: **pinch-to-zoom images** and **multi-page PDF rendering**
- **Share** a document securely through the Android share sheet (e.g. to WhatsApp/email) when you need to send a copy

### 🏠 Home screen
- Search across titles, types, names and numbers
- Category grid with counts, family-member filters, favourites filter
- **Expiry alerts**: expired / expiring-soon documents (passport, licence, insurance…) are flagged automatically

### ⚙️ Settings
- Toggle biometric unlock, change PIN, view storage usage, lock vault instantly

---

## 📱 Getting the app

### Option 1 — Download the APK (no Android Studio needed)
Every push builds a debug APK with GitHub Actions:
1. Open the **Actions** tab of this repo → latest **“Build Debug APK”** run
2. Download the **`my-vault-debug-apk`** artifact and install it on your phone (you may need to allow “Install unknown apps”)

### Option 2 — Run from Android Studio
1. Clone the repo and open it in **Android Studio Ladybug (2024.2) or newer**
2. Let Gradle sync (JDK 17 is bundled with Android Studio)
3. Run ▶ the `app` configuration on a device/emulator with **API 26+**

### Option 3 — Command line
```bash
./gradlew assembleDebug          # APK at app/build/outputs/apk/debug/app-debug.apk
./gradlew testDebugUnitTest      # unit tests
```

**First launch:** you’ll be asked to create a 4-digit PIN. ⚠️ *Remember it — by design there is no recovery if you forget the PIN and biometrics stop working. Keep your original documents safe.*

---

## 🏗️ Architecture

```
app/src/main/java/com/ftsinan/myvault/
├── MainActivity.kt            # Single activity; lock gate + FLAG_SECURE + auto-lock
├── MyVaultApplication.kt      # Manual DI container (no Hilt)
├── data/
│   ├── model/                 # VaultDocument, Category/Relation, Indian doc-type catalogue
│   ├── local/                 # Room database (metadata only)
│   ├── security/
│   │   ├── CryptoManager.kt   # AES-256/GCM via Android Keystore
│   │   └── PinManager.kt      # Salted-hash PIN + settings (DataStore)
│   ├── storage/               # Import/encrypt files, decrypt-to-cache, cleanup
│   └── repository/            # Single entry point for UI layer
├── ui/
│   ├── navigation/            # NavHost: Home → Add/Edit → Detail → Settings
│   ├── components/            # PIN pad, document cards, expiry chips, empty states
│   ├── screens/
│   │   ├── lock/              # PIN setup, lock screen, biometric prompt
│   │   ├── home/              # Search, categories, expiry alerts, filters
│   │   ├── addedit/           # Camera/gallery/PDF import + metadata form
│   │   ├── detail/            # Image/PDF viewer, share, favourite, delete
│   │   └── settings/          # Biometric toggle, change PIN, storage
│   └── theme/                 # Material 3 navy/teal theme (light + dark)
└── util/                      # Masking, date/size formatting, expiry logic
```

**Tech stack:** Kotlin 2.0 · Jetpack Compose + Material 3 · Navigation Compose · Room · DataStore · Biometric · Coil · Coroutines/Flow · minSdk 26, targetSdk 35.

**How a document flows through the app:**
`Pick photo/PDF → encrypt (AES-GCM) → save to internal files/vault/*.enc → metadata in Room → on open, decrypt in memory (images) or to cache (PDF/share) → cache wiped on lock`

---

## 🗺️ Roadmap ideas
- [ ] Document scanner with edge detection / auto-crop (ML Kit)
- [ ] Multiple pages per document (front & back of Aadhaar/PAN)
- [ ] Encrypted export/import backup (password-protected `.vault` file) for phone changes
- [ ] Expiry reminder notifications (WorkManager)
- [ ] OCR to auto-fill number & expiry from a scan
- [ ] Tags, custom categories, and per-person profile avatars
- [ ] SQLCipher / encrypted Room for metadata at rest

## 📄 License
Personal project — all rights reserved unless a license file is added.

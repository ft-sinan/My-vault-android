# 🔐 My Vault — Android Family Document Vault

A secure, offline-first Android vault for **Indian ID proofs, certificates and important family documents** — Aadhaar, PAN, Passport, Driving Licence, SSLC/Degree certificates, RC book, insurance papers, property documents and more.

Built with **Kotlin + Jetpack Compose (Material 3)**. No account, no ads and no document cloud upload: the vault is kept on the phone. The only network feature is a **manual** update check that runs only when the user taps the button in Settings.

---

## ✨ What the app does

## 📸 Screenshots

These store-preview graphics use a fictional family and masked sample document numbers only. No real family documents will ever be used in this repository. Full-size assets and the Orion Store listing sheet live in [`docs/store/`](docs/store/).

<p align="center">
  <img src="docs/store/screenshots/01-home.png" width="180" alt="My Vault Home preview with sample data">
  <img src="docs/store/screenshots/02-documents.png" width="180" alt="My Vault Documents preview with sample data">
  <img src="docs/store/screenshots/03-family.png" width="180" alt="My Vault Family preview with sample data">
  <img src="docs/store/screenshots/04-backup.png" width="180" alt="My Vault Backup and restore preview with sample data">
</p>

### Premium family-first experience
- Five-part bottom navigation: **Home • Docs • centre Add • Family • Settings**
- First-open flow: **choose English or Malayalam → create a PIN → create your profile**
- Only the profile **name is required**; photo, date of birth, gender and blood group can be added now or later
- Owner and family members each get an ID-card-style profile with their own documents underneath
- Lock screen greets the owner by **name only — never by photo**
- English and Malayalam throughout the app; language can be changed later in Settings
- Dedicated Settings pages for Appearance, Motion & feel, Security, Language, and Storage & privacy
- Appearance settings for System/Light/Dark/OLED theme, Material You wallpaper colours on Android 12+, and Orion, Teal, Navy, Emerald, Indigo, Ocean, Grape, Cocoa, or Slate palettes when wallpaper colours are off
- A playful Creator Space with a flipping Creator Coin, orbiting badges and creator links

### Family profiles and groups
- Personal documents belong to a selected family member
- Shared/non-personal documents can be assigned to **Family**, **Vehicle**, **Home / Property** or **Other**
- Family tab shows every profile with its document count and blood-group chip
- **Settings → Manage family members** supports add/edit/delete and drag-to-reorder
- Member order is visual only; it does not silently change a default document owner
- Deleting a member does not delete their documents — they move to the Family group
- A profile can be shared as a clean **image-only ID card** containing the available photo/name/DOB/gender/blood-group details, with no relation and no app branding

### Scan or file — nothing else
Documents are added in exactly two ways:

1. **Scan** — Google ML Kit Document Scanner with multi-page capture, up to 10 pages
2. **File** — one system file picker for either an image or a PDF

There is no separate PDF or gallery button for documents. You scan/select first, then confirm the title, type, category and person/group. Pages are arranged during scanning and are fixed after saving; document details remain editable later.

On scanned/picked images, on-device OCR suggests the document type and ID number for:
- Aadhaar (including Verhoeff checksum preference)
- PAN
- Driving Licence

OCR results are suggestions only and can always be reviewed or edited before saving.

### Viewing and sharing
- Multi-page image viewer with pinch zoom and pan
- Imported PDFs are preserved and rendered page-by-page
- Masked document number by default, with reveal and copy actions
- Search and filter by text, category, family member, shared group or favourites
- Long document titles have a small corner expand/collapse control on the document card; it reveals the complete title at the same font size without opening the document
- Share a document as either **PDF** or **images** through the Android share sheet

> Expiry dates, expiry alerts and reminders are intentionally not part of this app.

### Backup & restore
- Create one portable `.mvbackup` from **Settings → Storage & privacy → Backup & restore**
- Choose protection for each file: a backup password using PBKDF2-HMAC-SHA256 + AES-256-GCM, or explicitly no password after a warning
- Backups contain profiles, profile photos, documents, every page/original PDF, assignments and metadata; the device PIN and Android Keystore key are never included
- Restore verifies the password and file checksums, shows what is inside, then replaces the current vault only after the current PIN is confirmed
- A forgotten backup password cannot be recovered, and anyone with an unprotected backup can read its documents

### Security
- 4-digit PIN stored only as a salted, iterated SHA-256 hash
- Optional fingerprint/face unlock with PIN fallback
- Every document page and profile photo is encrypted with **AES-256-GCM**; the key is held in Android Keystore
- `FLAG_SECURE` blocks screenshots, screen recording and recent-apps previews
- Auto-lock after 1 minute in the background
- Decrypted preview/share cache files are wiped when the vault locks
- Android cloud/device backup is disabled for app data; portable `.mvbackup` files are created only when the user asks
- The vault itself is offline. `INTERNET` is used only for the user-initiated **Settings → Storage & privacy → App updates** check against GitHub Releases; My Vault never checks in the background and never uploads documents
- A downloaded update APK is verified against its published SHA-256 checksum before Android's installer is opened, whenever the release includes a checksum file

---

## 📱 Getting the app

### Download the signed release APK
Public downloads live on the repository's **Releases** page:

1. Open [**Releases**](https://github.com/ft-sinan/My-vault-android/releases).
2. Download the latest **`MyVault-vX.Y.Z.apk`** and, if you want to verify it yourself, its `.sha256` file.
3. Install it on an Android 8.0+ phone. Android may ask you to allow installation from your browser/file manager.
4. Later versions can also be installed from **Settings → Storage & privacy → App updates → Check for updates**.

> Release APKs must always use the same permanent signing key so Android can install them as updates. A CI debug APK has a different signature and cannot update a signed release installation in place.

The ML Kit scanner experience requires Google Play services on the device. If the scanner cannot open, the app still lets you use **File** to select an image or PDF.

### Run from Android Studio
1. Clone the repository and open it in Android Studio.
2. Let Gradle sync with JDK 17.
3. Run the `app` configuration on a device/emulator with API 26 or newer.

### Command line
```bash
./gradlew testDebugUnitTest
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

### Publishing a release (maintainer)
1. Create **one permanent Android release keystore**, back it up somewhere safe, and never commit it.
2. Add these GitHub repository secrets: `MYVAULT_RELEASE_KEYSTORE_BASE64`, `MYVAULT_RELEASE_STORE_PASSWORD`, `MYVAULT_RELEASE_KEY_ALIAS`, and `MYVAULT_RELEASE_KEY_PASSWORD`.
3. Bump `versionCode`/`versionName`, update `CHANGELOG.md`, and confirm the debug CI is green.
4. Push a matching tag such as `v1.1.0`. The **Publish Release** workflow tests the app, builds the signed APK, generates its SHA-256 file, and publishes the GitHub Release automatically.

### First launch
Choose **English** or **മലയാളം**, create and confirm a 4-digit PIN, then enter at least your name for the owner profile. Everything else can be completed later.

> ⚠️ Remember the PIN. By design, a forgotten PIN cannot be recovered if biometric unlock also becomes unavailable. Keep original documents safe.

---

## 🏗️ Architecture

```
app/src/main/java/com/ftsinan/myvault/
├── MainActivity.kt             # Language/PIN/profile gates, FLAG_SECURE, auto-lock
├── MyVaultApplication.kt       # Manual dependency container
├── data/
│   ├── backup/                 # Portable .mvbackup format, password encryption and restore staging
│   ├── local/                  # Room v2 DB, migration, document/member/page DAOs
│   ├── model/                  # Documents, pages, member profiles, groups, summaries
│   ├── repository/             # Documents + pages + members + encrypted photos
│   ├── security/               # Android Keystore AES-GCM and hashed PIN storage
│   ├── settings/               # English/Malayalam persistence and locale wrapping
│   └── storage/                # Encrypted import, decrypt-to-cache and cleanup
├── ui/
│   ├── components/             # PIN pad, avatars, document cards, category UI
│   ├── navigation/             # Home/Docs/Add/Family/Settings shell and routes
│   └── screens/
│       ├── lock/               # Language choice, PIN setup, lock, biometrics
│       ├── home/               # Owner card, Scan/File actions, family, recents
│       ├── documents/          # Search and category/person/group filtering
│       ├── addedit/            # ML Kit scanner/file import, OCR review, metadata
│       ├── detail/             # Multi-page/PDF viewer and PDF/image sharing
│       ├── profile/            # Family list, profile ID card, member editor
│       └── settings/           # Hub + dedicated Appearance/Motion/Security/Language/Storage pages
└── util/                       # Formatting/masking, OCR, profile-card PNG, manual GitHub updater
```

**Stack:** Kotlin 2.0 · Compose BOM 2024.12.01 · Material 3 · Navigation Compose · Room · DataStore · Biometric · Coil · ML Kit Document Scanner · ML Kit Text Recognition · Coroutines/Flow · minSdk 26 · targetSdk 35.

### Data flow

`Scan/File → temporary page URIs → OCR suggestion (images) → user confirms details → AES-GCM encrypt each page → Room metadata/page rows → decrypt only for viewing/sharing → wipe decrypted cache on lock`

Room migrated non-destructively from v1 to v2: each legacy single document file became page 1, and legacy holder details became editable member profiles.

---

## ⚠️ Current limitations

- On-device encrypted files are bound to the current phone's Android Keystore key. A portable `.mvbackup` is the supported way to move the vault to another phone; test restore with sample documents before relying on it for a real phone change.
- Room metadata (titles, types, names and document numbers) is not currently encrypted, although all document/profile-photo files are.
- OCR focuses on Aadhaar, PAN and Driving Licence numbers in images; imported PDFs are preserved/viewed but are not OCR-processed.
- The ML Kit document scanner depends on Google Play services.
- Signed public releases require the maintainer's permanent keystore secrets in GitHub Actions; losing that keystore would prevent publishing in-place updates for existing installations.

## 🗺️ Future ideas
- Recovery-key backup as a passwordless alternative to an unprotected backup
- Encrypted Room metadata
- Wider OCR coverage and OCR for imported PDFs
- Custom categories/tags

## 🙏 Acknowledgements

Special thanks to **[RookieZ (RookieEnough)](https://github.com/RookieEnough)** and the **[Orion Store](https://github.com/RookieEnough/Orion-Store)** project for the expressive design inspiration behind My Vault's interface — especially its capsule headers, pastel bento Settings hub, floating dock and creator-coin energy.

My Vault is an independent project. It is **not affiliated with or endorsed by Orion Store**.

Other credits:
- [Space Grotesk](https://fonts.google.com/specimen/Space+Grotesk), bundled for headings under the SIL Open Font License 1.1 (`app/src/main/assets/licenses/SpaceGrotesk-OFL.txt`)
- AndroidX/Jetpack Compose, Room, Navigation, DataStore and Biometric libraries
- Google ML Kit Document Scanner and Text Recognition
- Coil image loading

## 📄 License

My Vault is free software licensed under the **GNU General Public License v3.0**. See [`LICENSE`](LICENSE) for the full text.

---

made with 🤍 by sinan

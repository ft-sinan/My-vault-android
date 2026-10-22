# 🔐 My Vault — Android Family Document Vault

A secure, offline-first Android vault for **Indian ID proofs, certificates and important family documents** — Aadhaar, PAN, Passport, Driving Licence, SSLC/Degree certificates, RC book, insurance papers, property documents and more.

Built with **Kotlin + Jetpack Compose (Material 3)**. No account, no ads and no document cloud upload: the vault is kept on the phone.

---

## ✨ What the app does

### Premium family-first experience
- Five-part bottom navigation: **Home • Docs • centre Add • Family • Settings**
- First-open flow: **choose English or Malayalam → create a PIN → create your profile**
- Only the profile **name is required**; photo, date of birth, gender and blood group can be added now or later
- Owner and family members each get an ID-card-style profile with their own documents underneath
- Lock screen greets the owner by **name only — never by photo**
- English and Malayalam throughout the app; language can be changed later in Settings
- Material You dynamic colour on Android 12+, with the app palette available when dynamic colour is turned off

### Family profiles and groups
- Personal documents belong to a selected family member
- Shared/non-personal documents can be assigned to **Family**, **Vehicle**, **Home / Property** or **Other**
- Family tab shows every profile, document count, blood-group chip and shared-group counts
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

### Security
- 4-digit PIN stored only as a salted, iterated SHA-256 hash
- Optional fingerprint/face unlock with PIN fallback
- Every document page and profile photo is encrypted with **AES-256-GCM**; the key is held in Android Keystore
- `FLAG_SECURE` blocks screenshots, screen recording and recent-apps previews
- Auto-lock after 1 minute in the background
- Decrypted preview/share cache files are wiped when the vault locks
- Cloud/device backup is disabled for the app data
- No `INTERNET` permission is requested by the app

---

## 📱 Getting the app

### Download the CI-built debug APK
Every push runs unit tests and builds a debug APK with GitHub Actions:

1. Open the repository's **Actions** tab.
2. Open the latest successful **Build Debug APK** run.
3. Download the **`my-vault-debug-apk`** artifact.
4. Install it on an Android 8.0+ phone. Android may ask you to allow installation from that source.

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
│       └── settings/           # Language, members, appearance, security, storage
└── util/                       # Formatting/masking, ML Kit OCR, profile-card PNG
```

**Stack:** Kotlin 2.0 · Compose BOM 2024.12.01 · Material 3 · Navigation Compose · Room · DataStore · Biometric · Coil · ML Kit Document Scanner · ML Kit Text Recognition · Coroutines/Flow · minSdk 26 · targetSdk 35.

### Data flow

`Scan/File → temporary page URIs → OCR suggestion (images) → user confirms details → AES-GCM encrypt each page → Room metadata/page rows → decrypt only for viewing/sharing → wipe decrypted cache on lock`

Room migrated non-destructively from v1 to v2: each legacy single document file became page 1, and legacy holder details became editable member profiles.

---

## ⚠️ Current limitations

- Encrypted files are bound to the Android Keystore key on the current phone; encrypted backup/export for changing phones is not built yet.
- Room metadata (titles, types, names and document numbers) is not currently encrypted, although all document/profile-photo files are.
- OCR focuses on Aadhaar, PAN and Driving Licence numbers in images; imported PDFs are preserved/viewed but are not OCR-processed.
- The ML Kit document scanner depends on Google Play services.
- This repository builds a debug APK in CI; a signed production release/AAB pipeline is not configured yet.

## 🗺️ Future ideas
- Password-protected encrypted export/import for phone changes
- Encrypted Room metadata
- Wider OCR coverage and OCR for imported PDFs
- Custom categories/tags
- Signed release build

## 📄 License
Personal project — all rights reserved unless a license file is added.

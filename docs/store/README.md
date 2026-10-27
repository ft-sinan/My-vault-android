# FT Vault — Orion Store listing assets

The screenshots in this folder are the real device captures supplied with the Orion Store submission in [Orion-Data issue #2185](https://github.com/RookieEnough/Orion-Data/issues/2185), fetched into this repository and used as submitted. They show the fictional sample vault only; no real documents or ID numbers are shown. The first four files keep their original PNG paths so their repository URLs do not change, and the remaining five captures are kept as the submitted JPEGs.

## Assets

| Asset | File | Size |
| --- | --- | --- |
| Store icon | [`icon-512.png`](icon-512.png) | 512 × 512 |
| Home screenshot | [`screenshots/01-home.png`](screenshots/01-home.png) | 1080 × 2340 |
| Documents screenshot | [`screenshots/02-documents.png`](screenshots/02-documents.png) | 1080 × 2340 |
| Family screenshot | [`screenshots/03-family.png`](screenshots/03-family.png) | 1080 × 2340 |
| Backup screenshot | [`screenshots/04-backup.png`](screenshots/04-backup.png) | 1080 × 2340 |
| Add-document screenshot | [`screenshots/05-add-document.jpg`](screenshots/05-add-document.jpg) | 1080 × 2340 |
| Profile screenshot | [`screenshots/06-profile.jpg`](screenshots/06-profile.jpg) | 1080 × 2340 |
| Settings screenshot | [`screenshots/07-settings.jpg`](screenshots/07-settings.jpg) | 1080 × 2340 |
| Appearance screenshot | [`screenshots/08-appearance.jpg`](screenshots/08-appearance.jpg) | 1080 × 2340 |
| Settings-pages screenshot | [`screenshots/09-settings-pages.jpg`](screenshots/09-settings-pages.jpg) | 1080 × 2340 |
| Fictional screenshot vault | [`my-vault-sample-vault.mvbackup`](my-vault-sample-vault.mvbackup) | 4 members, 8 documents |

The reusable Shield Keyhole source is in [`icon.svg`](icon.svg).

## Refreshing the screenshots

> [!WARNING]
> Restoring the sample replaces the contents of the install used for the screenshots. Use a separate Android work profile, secondary user, or data-free test install. **Do not restore it into the real family vault.**

1. Install a build containing **Settings → Security → Temporary screenshots** in the separate test profile/install.
2. Complete first setup with a temporary PIN and temporary owner name.
3. Open **Settings → Storage & privacy → Restore backup**, choose `my-vault-sample-vault.mvbackup`, confirm the temporary PIN, and replace the empty test vault.
4. Open **Settings → Security**, tap **Allow screenshots for 5 minutes**, read the warning, and confirm the temporary PIN.
5. Capture the current set: **Home**, **Documents**, **Family**, **Settings → Storage & privacy** (Backup & restore visible), **Add a document**, a **member profile**, **Settings**, **Appearance**, and the lower **Settings pages / Creator Space** view. If the five-minute window expires, repeat step 4. Leaving the app or locking it ends the window early.
6. Keep the captures original and uncropped. Review them for notifications or other private information before publishing, then replace the files above while preserving their filenames and formats.

## Current Orion submission

The app was submitted as **My Vault Android** through Orion Store's in-app **Add/Submit App** flow, and was renamed to **FT Vault** in v1.3.0. The generated request is tracked in [RookieEnough/Orion-Data issue #2185](https://github.com/RookieEnough/Orion-Data/issues/2185); check that issue for the live review status. Do not create a duplicate submission or manually rewrite the generated body unless an Orion maintainer asks for a correction; the listing name can be updated by the maintainer when the submission is reviewed.

| Field | Submitted value |
| --- | --- |
| Name | FT Vault Android (submitted as: My Vault Android) |
| ID | `my-vault-android` |
| Package | `com.ftsinan.myvault` |
| GitHub repo | `ft-sinan/My-vault-android` |
| Category | Utility |
| Author | `ft-sinan` |
| Release keyword | `apk` |
| Platform | Android |
| Icon URL | <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/icon-512.png> |
| Signed release for review | <https://github.com/ft-sinan/My-vault-android/releases/tag/v1.1.0> |

> **Known generated-payload note:** the submitted `repoUrl` is `https://raw.github.com/ft-sinan/My-vault-android`. The canonical repository URL is <https://github.com/ft-sinan/My-vault-android>. Leave the generated issue alone unless a maintainer requests a correction; if they do, use the canonical URL.

### Screenshot URLs

The pending Orion issue points to the original phototourl copies. The same nine captures are now hosted in this repository; changing these repository files later will not automatically update the already-submitted Orion issue. The canonical repository URLs below apply after this asset set reaches `main`:

- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/01-home.png>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/02-documents.png>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/03-family.png>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/04-backup.png>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/05-add-document.jpg>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/06-profile.jpg>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/07-settings.jpg>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/08-appearance.jpg>
- <https://raw.githubusercontent.com/ft-sinan/My-vault-android/main/docs/store/screenshots/09-settings-pages.jpg>

## Short description

Offline-first encrypted family document vault for Indian IDs and certificates.

## Full description

FT Vault is an offline-first Android document vault built for Indian family IDs and certificates. Store Aadhaar, PAN, passports, driving licences, SSLC/degree certificates, vehicle and property documents, and other important papers for each family member.

Documents can be added by multi-page ML Kit scanning or by choosing an image/PDF file. On-device OCR suggests Aadhaar, PAN and Driving Licence numbers for review before saving. Document pages and profile photos are encrypted on the device with AES-256-GCM using Android Keystore, and the vault is protected by a PIN with optional biometric unlock.

FT Vault also includes 15 languages—English, Malayalam, Hindi, Spanish, French, Arabic, Russian, Brazilian Portuguese, Simplified Chinese, Indonesian, German, Japanese, Korean, Turkish, and Italian—plus family profiles, personal and shared document groups, search and favourites, clean profile-card sharing, and portable `.mvbackup` backup files. Each backup can use a PBKDF2/AES-256-GCM password or be explicitly created without one after a warning.

No account. No ads. No document upload. The only network feature is a GitHub Releases update check when Settings opens (at most once a day) or when the user checks manually; updates never download automatically.

## While Orion review is pending

1. Watch [Orion-Data issue #2185](https://github.com/RookieEnough/Orion-Data/issues/2185) for maintainer feedback or the `approved` label.
2. Do not submit a duplicate app request.
3. Do not manually rewrite the generated submission unless a maintainer asks; if a repository-URL correction is requested, use <https://github.com/ft-sinan/My-vault-android>.
4. Remember that the pending issue uses its original phototourl screenshot links. Replacing the repository copies above will not change those submitted links automatically.
5. After approval, verify the live catalogue name, icon, screenshots, package name and APK download before adding any “Available on Orion Store” badge to the root README.

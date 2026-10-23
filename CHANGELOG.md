# Changelog

All notable changes to My Vault are documented here.

## [1.1.0] - 2026-10-23

### Added
- Dedicated Settings pages for Appearance, Motion & feel, Security, Language, and Storage & privacy.
- A redesigned Creator Space with a larger flipping Creator Coin, orbiting badges, tappable achievement badges, more ways to connect, and the final signature line.
- Manual in-app update checks from **Settings → Storage & privacy → App updates**. My Vault only uses the network after the user taps **Check for updates**; it never checks in the background and never uploads vault data.
- A short, minimal startup animation on the loading and lock screens.
- An in-app **Acknowledgements & licences** page.
- GPL-3.0 licensing, public issue templates, and automatic signed GitHub Releases from version tags.
- Portable `.mvbackup` backup & restore with either PBKDF2/AES-256-GCM password protection or an explicitly warned no-password file; restores are checksum-verified and replace the vault only after PIN confirmation.

### Changed
- Settings is now a focused hub: quick toggles and bento blocks first, followed immediately by Creator Space.
- The manual **Lock vault now** action now lives inside the Security page instead of below Creator Space.
- Creator build information now shows app version, platform, and technology instead of personal vault statistics.
- Privacy copy now accurately explains the manual-only update check while keeping the vault itself offline.
- Refreshed Home and Family visuals with richer palette-tinted containers, readable on-block text, per-member identity colours, and actual member photos wherever supplied.

### Security
- Downloaded update APKs are verified against the published SHA-256 checksum before Android's installer is opened, whenever a checksum asset is included with the release.

## [1.0.0] - 2026-10-23

### Added
- Initial working My Vault release for Indian family documents.
- English/Malayalam first-open flow, PIN and biometric lock, and owner/family profiles.
- ML Kit multi-page document scanning, image/PDF file import, and on-device OCR suggestions for Aadhaar, PAN, and Driving Licence numbers.
- AES-256-GCM encrypted document pages and profile photos using Android Keystore.
- Personal and shared document organization, search, multi-page viewing, PDF/image sharing, and clean profile ID-card sharing.
- Orion-inspired expressive Material 3 interface with Space Grotesk headings.

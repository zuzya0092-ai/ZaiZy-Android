# ZaiZy VPN

ZaiZy is an Android VPN client based on [v2rayNG](https://github.com/2dust/v2rayNG) and Xray-core. It imports Remnawave subscription links and full Xray JSON profiles.

## Installation

Download the matching APK from [Releases](../../releases). Use ARM64 for most Android phones and x86_64 for compatible emulators. Android 7.0 or newer is required.

Open ZaiZy, copy your own Remnawave subscription URL, and tap **From clipboard** or **Из буфера**. Confirm the import, select a profile, and tap the shield. Android asks for permission on first VPN use.

Subscription URLs often contain credentials. Keep yours private. For HTTPS subscription requests, ZaiZy can send a host-scoped Android ID hash, OS version and device model to the subscription server for Remnawave device limits.

## Building from source

Use JDK 21, Android SDK platform 37, the Gradle wrapper in V2rayNG, and the native and routing inputs listed in [BUILD.md](BUILD.md). Clone with submodules to obtain the exact native source revisions.

## License

ZaiZy is a modified version of v2rayNG. The first ZaiZy release includes changes to branding, the main screen, Remnawave import and device identification, and connection status. These changes are published on 2026-11-24.

The Android client is distributed under [GPL‑3.0](LICENSE). The upstream project and its contributors retain their copyright. See the [upstream README](UPSTREAM-README.md) and the native submodules for their own notices and licenses.

The Android package name is app.zaizy.vpn. An installed debug build uses a different signing key from public releases. Export or record your subscription details before replacing a debug installation.

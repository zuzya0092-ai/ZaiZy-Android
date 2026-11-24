# Build ZaiZy

Use JDK 21 and Android SDK Platform 37. Install Android SDK Build-Tools and set ANDROID_HOME. Run Git submodule update --init --recursive from the repository root.

## Pinned inputs

1. Place libv2ray.aar from [AndroidLibXrayLite v26.11.9](https://github.com/2dust/AndroidLibXrayLite/releases/tag/v26.11.9) in V2rayNG/app/libs/. Its matching source is the AndroidLibXrayLite submodule at b5a377087e74eaa34e86718710b04c50f4b767f5. Follow the submodule build instructions to build it from source.
2. The two tunnel binaries per ABI are libhev-socks5-tunnel.so and libhevsockstun.so. Place them in V2rayNG/app/libs/arm64-v8a/ and V2rayNG/app/libs/x86_64/. Build them from the pinned hev-socks5-tunnel submodule at c5584feca8f6df70ee53eb2879e034b849a3d15a using compile-hevtun.sh and Android NDK 30.0.16248370 on Linux. The public v2rayNG 2.3.15 release APKs also contain the two compiled binaries per ABI from the same pinned source revision.
3. For identical routing data, extract assets/geoip.dat, assets/geosite.dat and assets/geoip-only-cn-private.dat from [the upstream v2rayNG 2.3.15 ARM64 APK](https://github.com/2dust/v2rayNG/releases/tag/2.3.15) into V2rayNG/app/src/main/assets/. The Android client source remains available without downloading the binary build inputs.

## Release

From V2rayNG on Windows:

```powershell
.\gradlew.bat assemblePlaystoreRelease '-PABI_FILTERS=arm64-v8a;x86_64'
```

On Linux or macOS:

```sh
./gradlew assemblePlaystoreRelease '-PABI_FILTERS=arm64-v8a;x86_64'
```

The Gradle task produces unsigned APKs. Sign them with Android SDK apksigner and a local release key. Keep private keys and passwords outside the repository.

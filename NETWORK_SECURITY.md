# Ketoy Dev Tools — Network Security Configuration

The Ketoy Dev Server communicates over **HTTP** (not HTTPS) because it runs
entirely on your local machine during development. Android 9+ (API 28+) blocks
cleartext HTTP by default, so the Ketoy SDK ships a network security config that
explicitly permits HTTP to the three dev-server addresses:

| Address | Used when |
|---------|-----------|
| `10.0.2.2` | Android Emulator → always works, no ADB setup needed |
| `localhost` | Android Emulator → works when `adb reverse` is active |
| `127.0.0.1` | Explicit loopback alias |

---

## Scenario A — Your app does NOT have a custom network security config

This is the most common case. The Ketoy SDK's manifest automatically provides
the required config via its own `ketoy_dev_network_security_config.xml`.

**Nothing extra is needed. Everything works out of the box.**

---

## Scenario B — Your app ALREADY has a custom network security config

If your app's `AndroidManifest.xml` already references `android:networkSecurityConfig`,
the manifest merger will emit a conflict error like:

```
Attribute application@networkSecurityConfig value=(@xml/your_config)
from AndroidManifest.xml conflicts with value=(@xml/ketoy_dev_network_security_config)
from ketoy-sdk
```

Follow the steps below to resolve it.

---

### Step 1 — Open your existing network security config file

It is usually at:

```
app/src/main/res/xml/network_security_config.xml
```

It typically looks something like this:

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>
</network-security-config>
```

---

### Step 2 — Add the Ketoy dev-server domain block

Paste the following `<domain-config>` block **inside** your existing
`<network-security-config>` element, alongside your other entries:

```xml
<!-- Ketoy Dev Server: allow HTTP to local dev server during development.
     These addresses are only reachable on the developer's machine and
     are never exposed to the public internet.
     Remove or wrap in a debug build type if you want extra safety. -->
<domain-config cleartextTrafficPermitted="true">
    <domain includeSubdomains="false">10.0.2.2</domain>
    <domain includeSubdomains="false">localhost</domain>
    <domain includeSubdomains="false">127.0.0.1</domain>
</domain-config>
```

**Full example after the edit:**

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>

    <!-- Your existing base config -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system"/>
        </trust-anchors>
    </base-config>

    <!-- Ketoy Dev Server: allow HTTP to local dev server during development -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
        <domain includeSubdomains="false">localhost</domain>
        <domain includeSubdomains="false">127.0.0.1</domain>
    </domain-config>

</network-security-config>
```

---

### Step 3 — Suppress the Ketoy SDK's built-in config in your manifest

Open your app's `AndroidManifest.xml` and add `tools:replace` to the
`<application>` tag so that the merger uses **your** config instead of the
SDK's default:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:networkSecurityConfig="@xml/network_security_config"
        tools:replace="android:networkSecurityConfig"
        ...>
```

> `tools:replace="android:networkSecurityConfig"` tells the manifest merger
> that your app's value wins over any value declared by a library dependency
> (including Ketoy SDK). Make sure your config file contains the Ketoy
> domain block from Step 2 or the dev server connection will still fail.

---

### Step 4 — Verify

Run `./gradlew ketoyDev` and open the app on the emulator. In Android Studio's
Logcat, filter by `Ketoy Dev` and confirm you see:

```
🔍 Ketoy Dev: Checking server at http://10.0.2.2:8484/status ...
🔍 Ketoy Dev: Server responded HTTP 200
🔌 Ketoy Dev: Opening WebSocket at ws://10.0.2.2:8485 ...
🔌 Ketoy Dev: WebSocket connected to 10.0.2.2:8485
```

If you still see `UnknownServiceException: CLEARTEXT communication ... not
permitted`, double-check that:

1. The `<domain-config>` block from Step 2 is inside `<network-security-config>`
   (not inside `<base-config>`).
2. The `android:networkSecurityConfig` attribute in your `<application>` tag
   points to the correct file.
3. You clean-built the app (`./gradlew clean assembleDebug`) so the old
   manifest is not cached.

---

## Optional — Restrict cleartext to debug builds only

If you want to guarantee the Ketoy dev-server domains are **never** permitted
in a release build, move the domain block to a separate debug-only config file:

**`app/src/debug/res/xml/network_security_config.xml`** (debug override):

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>

    <!-- Inherit your base production config -->
    <!-- (copy your full production config here and add the block below) -->

    <!-- Ketoy Dev Server — debug only -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">10.0.2.2</domain>
        <domain includeSubdomains="false">localhost</domain>
        <domain includeSubdomains="false">127.0.0.1</domain>
    </domain-config>

</network-security-config>
```

Place your original (strict) config at `app/src/main/res/xml/network_security_config.xml`.
Gradle will use the debug override for debug builds and the main config for
release builds automatically.

---

## Quick reference — which address to use

| Device type | Address to enter in the connect screen |
|-------------|----------------------------------------|
| Android Emulator | `10.0.2.2` (recommended, no ADB needed) |
| Android Emulator | `localhost` (alternative, requires `adb reverse`) |
| Physical device (same Wi-Fi) | LAN IP printed by `ketoyDev` e.g. `192.168.1.5` |

The Ketoy Dev connect screen **auto-detects emulators** and pre-fills
`10.0.2.2` for you.

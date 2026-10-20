package com.example.ersteversion.data.network

// hält den Standard-Wert für die Backend-URL
object NetworkConfig {

    const val DEFAULT_BASE_URL = "http://localhost:8080/"}

/* Für lokales Testen im Emulator: vorher im Terminal
"adb reverse tcp:8080 tcp:8080" ausführen , da 10.0.2.2 im
Emulator-Netzwerk instabil war (WifiDataStall/Timeouts). */
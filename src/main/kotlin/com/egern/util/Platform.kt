package com.egern.util

import java.lang.Exception

enum class Platform {
    Windows,
    Linux
}

class PlatformManager {
    val platform: Platform = when (val os = System.getProperty("os.name")) {
        "Windows 8.1" -> Platform.Windows
        "Windows 10" -> Platform.Windows
        "Linux" -> Platform.Linux
        else -> throw Exception("Unsupported platform: $os")
    }
}
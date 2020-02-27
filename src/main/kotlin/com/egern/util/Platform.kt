package com.egern.util

import java.lang.Exception

enum class Platform {
    MacOS,
    Windows,
    Linux
}

class PlatformManager {
    val platform: Platform = when (System.getProperty("os.name")) {
        "Windows" -> Platform.Windows
        "Mac OS X" -> Platform.MacOS
        "Linux" -> Platform.Linux
        else -> throw Exception("Unsupported platform")
    }

    fun mainLabel(): String {
        return when (platform) {
            Platform.MacOS -> "_main"
            else -> "main"
        }
    }
}
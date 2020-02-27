package com.egern.util

import com.egern.emit.Emitter
import com.egern.emit.LinuxEmitter
import com.egern.emit.MacOSEmitter
import com.egern.emit.WindowsEmitter
import java.lang.Exception

enum class Platform {
    MacOS,
    Windows,
    Linux
}

class PlatformManager {
    val platform: Platform
    init {
        platform = when (System.getProperty("os.name")) {
            "Windows" -> Platform.Windows
            "Mac OS X" -> Platform.MacOS
            "Linux" -> Platform.MacOS
            else -> throw Exception("Unsupported platform")
        }
    }

    fun mainLabel(): String {
        return when(platform) {
            Platform.MacOS -> "_main"
            else -> "main"
        }
    }
}
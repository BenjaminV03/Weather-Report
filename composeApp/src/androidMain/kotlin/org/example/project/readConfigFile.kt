package org.example.project

import android.content.Context

lateinit var appContext: Context

actual fun readConfigFile(fileName: String): String {
    val inputStream = appContext.assets.open(fileName)
    return inputStream.bufferedReader().use { it.readText() }
}

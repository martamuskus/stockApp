package config

import java.io.File
import java.util.Properties

object ApiConfig {

    val apiKey: String by lazy {
        /* priority order:
        1. JVM system property
        2. Environment variable
        3. local.properties file */

        System.getProperty("apikey")
            ?: System.getenv("ALPHAVANTAGE_API_KEY")
            ?: loadFromLocalProperties()
            ?: ""
    }

    private fun loadFromLocalProperties(): String? {
        return try {
            val properties = Properties()
            val localPropertiesFile = File("local.properties")

            if (localPropertiesFile.exists()) {
                localPropertiesFile.inputStream().use {
                    properties.load(it)
                }
                properties.getProperty("alphavantage.api.key")
            } else {
                null
            }
        } catch (_: Exception) {
            println("Could not get apiKEY")
            null
        }
    }
}
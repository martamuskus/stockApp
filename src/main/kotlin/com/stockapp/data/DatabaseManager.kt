package com.stockapp.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.stockapp.database.StockDatabase
import java.io.File

object DatabaseManager {
    private var driver: SqlDriver? = null
    private var database: StockDatabase? = null

    fun init(databasePath: String = "stock_app.db"): StockDatabase {
        if (database != null) return database!!

        val dbFile = File(System.getProperty("user.home"), ".stockapp/$databasePath")
        dbFile.parentFile?.mkdirs()

        driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        if (!dbFile.exists()) {
            StockDatabase.Schema.create(driver!!)
        }

        database = StockDatabase(driver!!)
        return database!!
    }

    fun getDatabase(): StockDatabase {
        return database ?: init()
    }

    fun close() {
        driver?.close()
        driver = null
        database = null
    }
}

data class WatchlistStock(
    val symbol: String,
    val name: String,
    val addedDate: Long,
    val notes: String?
)

data class PortfolioStock(
    val id: Long,
    val symbol: String,
    val name: String,
    val quantity: Double,
    val purchasePrice: Double,
    val purchaseDate: Long,
    val notes: String? = ""
)
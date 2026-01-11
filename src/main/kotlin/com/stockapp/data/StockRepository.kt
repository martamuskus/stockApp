package com.stockapp.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class StockRepository {
    private val database = DatabaseManager.getDatabase()
    private val watchlistQueries = database.watchlistQueries
    private val portfolioQueries = database.portfolioQueries

    fun watchlistFlow(): Flow<List<WatchlistStock>> {
        return watchlistQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { dbStock ->
                    WatchlistStock(
                        symbol = dbStock.symbol,
                        name = dbStock.name,
                        addedDate = dbStock.addedDate,
                        notes = dbStock.notes
                    )
                }
            }
    }

    suspend fun getAllWatchlist(): List<WatchlistStock> = withContext(Dispatchers.IO) {
        watchlistQueries.selectAll().executeAsList().map { dbStock ->
            WatchlistStock(
                symbol = dbStock.symbol,
                name = dbStock.name,
                addedDate = dbStock.addedDate,
                notes = dbStock.notes
            )
        }
    }

    suspend fun addToWatchlist(
        symbol: String,
        name: String = "",
        notes: String? = null
    ) = withContext(Dispatchers.IO) {
        watchlistQueries.insert(
            symbol = symbol.uppercase(),
            name = name,
            addedDate = System.currentTimeMillis(),
            notes = notes
        )
    }

    suspend fun removeFromWatchlist(symbol: String) = withContext(Dispatchers.IO) {
        watchlistQueries.delete(symbol.uppercase())
    }

    fun isInWatchlistFlow(symbol: String): Flow<Boolean> {
        return watchlistQueries.isInWatchlist(symbol.uppercase())
            .asFlow()
            .mapToOne(Dispatchers.IO)
            .map { it }
    }

    suspend fun isInWatchlist(symbol: String): Boolean = withContext(Dispatchers.IO) {
        watchlistQueries.isInWatchlist(symbol.uppercase()).executeAsOne()
    }

    fun portfolioFlow(): Flow<List<PortfolioStock>> {
        return portfolioQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.map { dbStock ->
                    PortfolioStock(
                        id = dbStock.id,
                        symbol = dbStock.symbol,
                        name = dbStock.name,
                        quantity = dbStock.quantity,
                        purchasePrice = dbStock.purchasePrice,
                        purchaseDate = dbStock.purchaseDate,
                        notes = dbStock.notes
                    )
                }
            }
    }

    suspend fun getAllPortfolio(): List<PortfolioStock> = withContext(Dispatchers.IO) {
        portfolioQueries.selectAll().executeAsList().map { dbStock ->
            PortfolioStock(
                id = dbStock.id,
                symbol = dbStock.symbol,
                name = dbStock.name,
                quantity = dbStock.quantity,
                purchasePrice = dbStock.purchasePrice,
                purchaseDate = dbStock.purchaseDate,
                notes = dbStock.notes
            )
        }
    }

    suspend fun getPortfolioBySymbol(symbol: String): List<PortfolioStock> = withContext(Dispatchers.IO) {
        portfolioQueries.selectBySymbol(symbol.uppercase()).executeAsList().map { dbStock ->
            PortfolioStock(
                id = dbStock.id,
                symbol = dbStock.symbol,
                name = dbStock.name,
                quantity = dbStock.quantity,
                purchasePrice = dbStock.purchasePrice,
                purchaseDate = dbStock.purchaseDate,
                notes = dbStock.notes
            )
        }
    }

    suspend fun addToPortfolio(
        symbol: String,
        name: String,
        quantity: Double,
        purchasePrice: Double,
        notes: String? = null
    ) = withContext(Dispatchers.IO) {
        portfolioQueries.insert(
            symbol = symbol.uppercase(),
            name = name,
            quantity = quantity,
            purchasePrice = purchasePrice,
            purchaseDate = System.currentTimeMillis(),
            notes = notes
        )
    }

    suspend fun updatePortfolioStock(stock: PortfolioStock) = withContext(Dispatchers.IO) {
        portfolioQueries.update(
            quantity = stock.quantity,
            purchasePrice = stock.purchasePrice,
            notes = stock.notes,
            symbol = stock.symbol
        )
    }

    suspend fun removeFromPortfolio(id: Long) = withContext(Dispatchers.IO) {
        portfolioQueries.delete(id)
    }

    suspend fun getTotalInvestment(): Double = withContext(Dispatchers.IO) {
        portfolioQueries.totalInvestment().executeAsOne().total ?: 0.0
    }

    suspend fun getPortfolioCount(): Long = withContext(Dispatchers.IO) {
        portfolioQueries.count().executeAsOne()
    }
}
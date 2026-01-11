package com.stockapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stockapp.data.*
import kotlinx.coroutines.launch
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

@Composable
fun portfolioScreen(
    repository: StockRepository,
    api: AlphaVantageApi,
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }
    var stockToUpdate by remember { mutableStateOf<PortfolioStock?>(null) }
    var portfolio by remember { mutableStateOf<List<PortfolioStock>>(emptyList()) }
    var groupedPortfolio by remember { mutableStateOf<Map<String, PortfolioGroup>>(emptyMap()) }
    var totalInvestment by remember { mutableStateOf(0.0) }
    var portfolioCount by remember { mutableStateOf(0L) }
    var isLoading by remember { mutableStateOf(true) }

    // track which symbols have been expanded and fetched
    var symbolPrices by remember { mutableStateOf<Map<String, StockPrice>>(emptyMap()) }
    var loadingPrices by remember { mutableStateOf<Set<String>>(emptySet()) }

    fun refreshData() {
        scope.launch {
            portfolio = repository.getAllPortfolio()
            totalInvestment = repository.getTotalInvestment()
            groupedPortfolio = portfolio.groupBy { it.symbol }.mapValues { (symbol, stocks) ->
                val totalShares = stocks.sumOf { it.quantity }
                val avgPurchasePrice = stocks.sumOf { it.quantity * it.purchasePrice } / totalShares
                PortfolioGroup(
                    symbol = symbol,
                    name = stocks.first().name,
                    totalShares = totalShares,
                    avgPurchasePrice = avgPurchasePrice,
                    totalInvested = stocks.sumOf { it.quantity * it.purchasePrice },
                    positions = stocks
                )
            }
        }
    }

    // loading portfolio data
    LaunchedEffect(Unit) {
        isLoading = true
        portfolio = repository.getAllPortfolio()
        totalInvestment = repository.getTotalInvestment()
        portfolioCount = repository.getPortfolioCount()

        groupedPortfolio = portfolio.groupBy { it.symbol }.mapValues { (symbol, stocks) ->
            val totalShares = stocks.sumOf { it.quantity }
            val avgPurchasePrice = stocks.sumOf { it.quantity * it.purchasePrice } / totalShares
            PortfolioGroup(
                symbol = symbol,
                name = stocks.first().name,
                totalShares = totalShares,
                avgPurchasePrice = avgPurchasePrice,
                totalInvested = stocks.sumOf { it.quantity * it.purchasePrice },
                positions = stocks
            )
        }
        isLoading = false
    }

    stockToUpdate?.let { stock ->
        UpdateStockDialog(
            onDismiss = { stockToUpdate = null },
            onConfirm = { qty, price ->
                scope.launch {
                    repository.updatePortfolioStock(
                        stock.copy(quantity = qty, purchasePrice = price)
                    )
                    refreshData()
                    stockToUpdate = null
                }
            },
            stockName = stock.name
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }, // for error messages
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Stock")
            }
        }
    ) { padding ->
        if (showAddDialog) {
            AddStockDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { symbol, qty, price ->
                    scope.launch {
                        try {
                            // just to check if the stock symbol is correct
                            api.getQuote(symbol)
                            repository.addToPortfolio(
                                symbol = symbol,
                                name = symbol,
                                quantity = qty,
                                purchasePrice = price,
                            )
                            refreshData()
                            showAddDialog = false
                        } catch (_: Exception) {
                            showAddDialog = false
                            snackbarHostState.showSnackbar("Incorrect stock symbol")
                        }
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Portfolio",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Total Investment",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "$${String.format("%.2f", totalInvestment)}",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Holdings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = "${groupedPortfolio.size} stocks",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // total portfolio value if any prices loaded
                        if (symbolPrices.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Divider()
                            Spacer(modifier = Modifier.height(8.dp))

                            val totalCurrentValue = groupedPortfolio.entries.sumOf { (symbol, group) ->
                                val price = symbolPrices[symbol]
                                if (price != null) {
                                    group.totalShares * price.currentPrice
                                } else {
                                    group.totalInvested
                                }
                            }

                            val totalGainLoss = totalCurrentValue - totalInvestment
                            val totalGainLossPercent = (totalGainLoss / totalInvestment) * 100

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Current Value",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "$${String.format("%.2f", totalCurrentValue)}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "Total Gain/Loss",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "${if (totalGainLoss >= 0) "+" else ""}$${
                                            String.format(
                                                "%.2f",
                                                totalGainLoss
                                            )
                                        } (${String.format("%.2f", totalGainLossPercent)}%)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (totalGainLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // portfolio list
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (groupedPortfolio.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No stocks in portfolio.\nAdd stocks from the chart screen.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(groupedPortfolio.entries.toList(), key = { it.key }) { (symbol, group) ->
                            portfolioGroupCard(
                                group = group,
                                currentPrice = symbolPrices[symbol],
                                isLoadingPrice = symbol in loadingPrices,
                                onFetchPrice = {
                                    scope.launch {
                                        if (symbol !in loadingPrices) {
                                            loadingPrices = loadingPrices + symbol
                                            try {
                                                val response = api.getQuote(symbol)
                                                val quote = response.globalQuote
                                                symbolPrices = symbolPrices + (symbol to StockPrice(
                                                    currentPrice = quote.price.toDouble(),
                                                    change = quote.change.toDouble(),
                                                    changePercent = quote.changePercent.removeSuffix("%").toDouble()
                                                ))
                                            } catch (_: Exception) {
                                                snackbarHostState.showSnackbar("Incorrect stock symbol")
                                            } finally {
                                                loadingPrices = loadingPrices - symbol
                                            }
                                        }
                                    }
                                },
                                onEditPosition = { stockToUpdate = it },
                                onDeletePosition = { position ->
                                    scope.launch {
                                        repository.removeFromPortfolio(position.id)
                                        portfolio = repository.getAllPortfolio()
                                        totalInvestment = repository.getTotalInvestment()
                                        groupedPortfolio = portfolio.groupBy { it.symbol }.mapValues { (s, stocks) ->
                                            val totalShares = stocks.sumOf { it.quantity }
                                            val avgPrice = stocks.sumOf { it.quantity * it.purchasePrice } / totalShares
                                            PortfolioGroup(
                                                s, stocks.first().name, totalShares, avgPrice,
                                                stocks.sumOf { it.quantity * it.purchasePrice }, stocks
                                            )
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

data class PortfolioGroup(
    val symbol: String,
    val name: String,
    val totalShares: Double,
    val avgPurchasePrice: Double,
    val totalInvested: Double,
    val positions: List<PortfolioStock>
)

data class StockPrice(
    val currentPrice: Double,
    val change: Double,
    val changePercent: Double
)


@Composable
fun portfolioGroupCard(
    group: PortfolioGroup,
    currentPrice: StockPrice?,
    isLoadingPrice: Boolean,
    onFetchPrice: () -> Unit,
    onEditPosition: (PortfolioStock) -> Unit,
    onDeletePosition: (PortfolioStock) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // header
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = group.symbol,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = group.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )}
                }

                Spacer(modifier = Modifier.height(12.dp))

                // basic info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Shares",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format("%.4f", group.totalShares),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column {
                        Text(
                            text = "Avg Price",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", group.avgPurchasePrice)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Invested",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$${String.format("%.2f", group.totalInvested)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        IconButton(onClick = { onEditPosition(group.positions.first()) }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        IconButton (onClick = { onDeletePosition(group.positions.first())}) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // current price - if fetched
                if (currentPrice != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))

                    val currentValue = group.totalShares * currentPrice.currentPrice
                    val gainLoss = currentValue - group.totalInvested
                    val gainLossPercent = (gainLoss / group.totalInvested) * 100

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Price",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "$${String.format("%.2f", currentPrice.currentPrice)}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${if (currentPrice.change >= 0) "+" else ""}${String.format("%.2f", currentPrice.change)} (${String.format("%.2f", currentPrice.changePercent)}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (currentPrice.change >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Current Value",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "$${String.format("%.2f", currentValue)}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Gain/Loss",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${if (gainLoss >= 0) "+" else ""}$${String.format("%.2f", gainLoss)} (${String.format("%.2f", gainLossPercent)}%)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (gainLoss >= 0) Color(0xFF4CAF50) else Color(0xFFF44336)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onFetchPrice,
                        enabled = !isLoadingPrice,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isLoadingPrice) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Loading...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Get Current Price")
                        }
                    }
                }
        }
        }
    }
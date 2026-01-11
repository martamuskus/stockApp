package com.stockapp.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.stockapp.data.StockRepository
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stockapp.data.WatchlistStock
import kotlinx.coroutines.launch

@Composable
fun WatchlistScreen(
    repository: StockRepository,
    modifier: Modifier = Modifier
) {
    val watchlist by repository
        .watchlistFlow()
        .collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Watchlist",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        val scope = rememberCoroutineScope()
        if (watchlist.isEmpty()) {
            EmptyWatchlist()
        } else {
            WatchlistList(
                watchlist = watchlist,
                onRemove = { symbol ->
                    scope.launch {
                        repository.removeFromWatchlist(symbol)
                    }
                }
            )
        }
    }
}


@Composable
fun EmptyWatchlist() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Your watchlist is empty 🤍",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )
    }
}

@Composable
fun WatchlistList(
    watchlist: List<WatchlistStock>,
    onRemove: (String) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            watchlist,
            key = { it.symbol }
        ) { stock ->
            WatchlistItem(
                stock = stock,
                onRemove = onRemove
            )
        }
    }
}

@Composable
fun WatchlistItem(
    stock: WatchlistStock,
    onRemove: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // to remove
            IconButton(onClick = { onRemove(stock.symbol) }) {
                Text("\u274C")
            }
        }
    }
}


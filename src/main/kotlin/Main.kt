package com.stockapp

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.stockapp.data.*
import com.stockapp.ui.*

fun main() =
  application {
    DatabaseManager.init()
    Window(
      onCloseRequest =
        ::exitApplication,
      title = "Stock Market App",
    ) {
      AppTheme(darkTheme = true) {
        val repository = remember { StockRepository() }
        app(repository)
      }
    }
  }

@Composable
@Preview
fun app(repository: StockRepository) {
  val api = remember { createAlphaVantageApi() }
  var selectedTab by remember { mutableStateOf(0) }

  LaunchedEffect(Unit) {
    val stocks = repository.getAllWatchlist()
    println("Watchlist: $stocks")
  }

  Scaffold(
    topBar = {
    },
  ) { padding ->
    Column(modifier = Modifier.padding(padding)) {
      TabRow(selectedTabIndex = selectedTab) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Charts") },
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Statistics") },
        )
        Tab(
          selected = selectedTab == 2,
          onClick = {selectedTab = 2},
          text = { Text("Watchlist")}
        )
        Tab(
          selected = selectedTab == 3,
          onClick = {selectedTab = 3},
          text = { Text("Portfolio")}
        )
      }

      when (selectedTab) {
        0 -> chartScreen(api, repository)
        1 -> statisticsScreen(api)
        2 -> WatchlistScreen(repository = repository)
        3-> portfolioScreen(
          repository = repository,
          api = api)
      }
    }
  }
}

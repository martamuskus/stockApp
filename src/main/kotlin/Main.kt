import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.example.compose.AppTheme
import kotlinx.coroutines.launch

fun main() =
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "Stock Market App",
    ) {
      AppTheme(darkTheme = true) {
        app()
      }
    }
  }

@Composable
@Preview
fun app() {
  val api = remember { createAlphaVantageApi() }
  var selectedTab by remember { mutableStateOf(1) }

  Scaffold(
    topBar = {
    },
  ) { padding ->
    Column(modifier = Modifier.padding(padding)) {
      TabRow(selectedTabIndex = selectedTab) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Stock List") },
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Charts") },
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text("Statistics") },
        )
      }

      when (selectedTab) {
        0 -> stockListScreen(api)
        1 -> chartScreen(api)
        2 -> statisticsScreen(api)
      }
    }
  }
}

// screen to find the desired stock item
@Composable
fun stockListScreen(api: AlphaVantageApi) {
  var searchQuery by remember { mutableStateOf("") }
  var searchResults by remember { mutableStateOf<List<SymbolMatch>>(emptyList()) }
  var isLoading by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(16.dp),
  ) {
    OutlinedTextField(
      value = searchQuery,
      onValueChange = { searchQuery = it },
      label = { Text("Search stocks") },
      modifier = Modifier.fillMaxWidth(),
      singleLine = true,
    )

    Spacer(modifier = Modifier.height(8.dp))

    Button(
      onClick = {
        if (searchQuery.isNotBlank()) {
          scope.launch {
            isLoading = true
            try {
              val response = api.searchSymbol(searchQuery)
              searchResults = response.bestMatches
            } catch (e: Exception) {
              e.printStackTrace()
            } finally {
              isLoading = false
            }
          }
        }
      },
      enabled = !isLoading && searchQuery.isNotBlank(),
    ) {
      Text(if (isLoading) "Searching..." else "Search")
    }

    Spacer(modifier = Modifier.height(16.dp))

    if (isLoading) {
      Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator()
      }
    } else {
      LazyColumn {
        items(searchResults) { stock ->
          stockItem(stock)
        }
      }
    }
  }
}

// helper to stockListScreen, for displaying
@Composable
fun stockItem(stock: SymbolMatch) {
  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
    ) {
      Text(
        text = stock.symbol,
        style = MaterialTheme.typography.titleMedium,
      )
      Text(
        text = stock.name,
        style = MaterialTheme.typography.bodyMedium,
      )
      Text(
        text = "${stock.type} • ${stock.region}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

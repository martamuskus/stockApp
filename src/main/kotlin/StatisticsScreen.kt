import analytics.StockAnalytics
import analytics.StockStatistics
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate

// screen to display calculated statistics
@Composable
fun statisticsScreen(api: AlphaVantageApi) {
  var symbol by remember { mutableStateOf("AAPL") }
  var statistics by remember { mutableStateOf<StockStatistics?>(null) }
  var isLoading by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  val scope = rememberCoroutineScope()
  val analytics = remember { StockAnalytics() }
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(16.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = symbol,
        onValueChange = { symbol = it.uppercase() },
        label = { Text("Symbol") },
        modifier = Modifier.width(150.dp),
        singleLine = true,
      )

      Button(
        onClick = {
          scope.launch {
            isLoading = true
            error = null

            // loading daily data to analize
            val response = api.getTimeSeries(symbol, "compact")
            val priceData =
              response.timeSeries.map { (date, data) ->
                PricePoint(
                  date = LocalDate.parse(date),
                  open = data.open.toDouble(),
                  high = data.high.toDouble(),
                  low = data.low.toDouble(),
                  close = data.close.toDouble(),
                  volume = data.volume.toLong(),
                )
              }.sortedBy { it.date }
            statistics = analytics.calculateStatistics(symbol, priceData)

            isLoading = false
          }
        },
        enabled = !isLoading && symbol.isNotBlank(),
      ) {
        Text(if (isLoading) "Loading..." else "Analyze")
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    when {
      isLoading -> {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      }
      error != null -> {
        Card(
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.errorContainer,
            ),
        ) {
          Text(
            text = error ?: "",
            modifier = Modifier.padding(16.dp),
            color = MaterialTheme.colorScheme.onErrorContainer,
          )
        }
      }
      statistics != null -> {
        statisticsContent(statistics!!)
      }
      else -> {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Text("Enter a symbol and click 'Analyze'")
        }
      }
    }
  }
}

@Composable
fun statisticsContent(stats: StockStatistics) {
  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // simple overview
    statsCard(title = "Overview") {
      statRow("Symbol", stats.symbol)
      statRow("Current Price", "$%.2f".format(stats.currentPrice))
    }

    // returns over different timespans
    statsCard(title = "Returns") {
      stats.returns.daily?.let {
        statRow("Daily", formatPercent(it), getColorForReturn(it))
      }
      stats.returns.weekly?.let {
        statRow("Weekly", formatPercent(it), getColorForReturn(it))
      }
      stats.returns.monthly?.let {
        statRow("Monthly", formatPercent(it), getColorForReturn(it))
      }
      stats.returns.threeMonth?.let {
        statRow("3 Months", formatPercent(it), getColorForReturn(it))
      }
      stats.returns.allTime?.let {
        statRow("All Time", formatPercent(it), getColorForReturn(it))
      }
    }

    // price statistics
    statsCard(title = "Price Statistics") {
      statRow("Current", "$%.2f".format(stats.priceStats.current))
      statRow("High", "$%.2f".format(stats.priceStats.high))
      statRow("Low", "$%.2f".format(stats.priceStats.low))
      statRow("Average", "$%.2f".format(stats.priceStats.average))
      statRow("Median", "$%.2f".format(stats.priceStats.median))
    }

    // volatility
    statsCard(title = "Volatility & Risk") {
      statRow("Daily analytics.Volatility", "%.2f%%".format(stats.volatility.dailyStdDev))
      statRow("Coefficient of Variation", "%.2f%%".format(stats.volatility.coefficientOfVariation))
    }

    // some basic technical indicators
    statsCard(title = "Technical Indicators") {
      stats.indicators.sma20?.let {
        statRow("SMA (20)", "$%.2f".format(it))
      }
      stats.indicators.sma50?.let {
        statRow("SMA (50)", "$%.2f".format(it))
      }
      stats.indicators.ema12?.let {
        statRow("EMA (12)", "$%.2f".format(it))
      }
      stats.indicators.ema26?.let {
        statRow("EMA (26)", "$%.2f".format(it))
      }
      stats.indicators.rsi14?.let {
        val signal =
          when {
            it < 30 -> "Oversold"
            it > 70 -> "Overbought"
            else -> "Neutral"
          }
        statRow("RSI (14)", "%.1f ($signal)".format(it))
      }
    }

    // info about volumes
    statsCard(title = "Volume Analysis") {
      statRow("Average Volume", formatVolume(stats.volumeStats.average))
      statRow("Highest Volume", formatVolume(stats.volumeStats.highest))
      statRow("Lowest Volume", formatVolume(stats.volumeStats.lowest))
      statRow("Recent Trend", stats.volumeStats.trend)
    }
  }
}

@Composable
fun statsCard(
  title: String,
  content: @Composable ColumnScope.() -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 12.dp),
      )
      content()
    }
  }
}

@Composable
fun statRow(
  label: String,
  value: String,
  valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = valueColor,
    )
  }
}

// for formatting
private fun formatPercent(value: Double): String {
  return "%+.2f%%".format(value)
}

private fun formatVolume(volume: Long): String {
  return when {
    volume >= 1_000_000_000 -> "%.2fB".format(volume / 1_000_000_000.0)
    volume >= 1_000_000 -> "%.2fM".format(volume / 1_000_000.0)
    volume >= 1_000 -> "%.2fK".format(volume / 1_000.0)
    else -> volume.toString()
  }
}

private fun getColorForReturn(returnValue: Double): Color {
  return when {
    returnValue > 0 -> Color.Green
    returnValue < 0 -> Color.Red
    else -> Color.Gray
  }
}

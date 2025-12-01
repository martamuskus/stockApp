@file:OptIn(ExperimentalMaterial3Api::class)

import analytics.StockAnalytics
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.LocalDate

// object to store info about each day / 15 min interval - for charts
data class PricePoint(
  val date: LocalDate,
  val open: Double,
  val high: Double,
  val low: Double,
  val close: Double,
  val volume: Long,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun chartScreen(api: AlphaVantageApi) {
  var symbol by remember { mutableStateOf("") }
  var priceData by remember { mutableStateOf<List<PricePoint>>(emptyList()) }
  var filteredData by remember { mutableStateOf<List<PricePoint>>(emptyList()) }
  var isLoading by remember { mutableStateOf(false) }
  var error by remember { mutableStateOf<String?>(null) }
  var chartType by remember { mutableStateOf(ChartType.LINE) }
  var timespan by remember { mutableStateOf(TimespanType.ALL) }
  val scope = rememberCoroutineScope()
  var selectedIndicators by remember { mutableStateOf(setOf<IndicatorType>()) }
  var lastDataType by remember { mutableStateOf<String?>(null) }

  // filtering the all-time data (100 days on free api so not that much) to just a desired timespan
  LaunchedEffect(priceData, timespan) {
    if (priceData.isNotEmpty()) {
      filteredData =
        when {
          // no need to filter
          timespan == TimespanType.ONE_DAY -> {
            priceData
          }

          timespan.days != null -> {
            val latest = priceData.last().date
            val cutoffDate = latest.minusDays(timespan.days!!.toLong())
            priceData.filter { it.date >= cutoffDate }
          }

          else -> {
            priceData
          }
        }
    }
  }

  // auto-refresh data when switching between ONE_DAY and other timespans - other api calls
  LaunchedEffect(timespan) {
    if (symbol.isNotBlank()) {
      val needsIntraday = timespan == TimespanType.ONE_DAY
      val needsDaily = timespan != TimespanType.ONE_DAY

      // only reloading if we're switching data types - elsewise there would be a redundant wasing of api calls
      if ((needsIntraday && lastDataType != "intraday") ||
        (needsDaily && lastDataType != "daily")
      ) {
        scope.launch {
          isLoading = true
          error = null
          try {
            if (needsIntraday) {
              val response = api.getIntraDaySeries(symbol, "15min")
              val timeSeries = response.timeSeries ?: emptyMap()

              priceData =
                timeSeries.map { (datetime, data) ->
                  PricePoint(
                    date = LocalDate.parse(datetime.substring(0, 10)),
                    open = data.open.toDouble(),
                    high = data.high.toDouble(),
                    low = data.low.toDouble(),
                    close = data.close.toDouble(),
                    volume = data.volume.toLong(),
                  )
                }.sortedBy { it.date }
              lastDataType = "intraday"
            } else {
              // for daily data (1W, 1M, 3M, ALL)
              lastDataType = "daily"
              val response = api.getTimeSeries(symbol, "compact")
              priceData =
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
            }
          } catch (e: Exception) {
            error = "Error: ${e.message}"
            e.printStackTrace()
          } finally {
            isLoading = false
          }
        }
      }
    }
  }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(16.dp).verticalScroll(rememberScrollState()),
  ) {
    // row to input symbol to analize
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

      // button to load data, launching api calls for INTRADAY/ DAILY data
      Button(
        onClick = {
          scope.launch {
            isLoading = true
            error = null
            try {
              if (timespan == TimespanType.ONE_DAY) {
                // separate api for data in 15 min chuncks
                val response = api.getIntraDaySeries(symbol, "15min")
                val timeSeries = response.timeSeries ?: emptyMap()

                priceData =
                  timeSeries.map { (datetime, data) ->
                    PricePoint(
                      date = LocalDate.parse(datetime.substring(0, 10)),
                      open = data.open.toDouble(),
                      high = data.high.toDouble(),
                      low = data.low.toDouble(),
                      close = data.close.toDouble(),
                      volume = data.volume.toLong(),
                    )
                  }.sortedBy { it.date }
                lastDataType = "intraday"
              } else {
                val response = api.getTimeSeries(symbol, "compact")
                // getting full data and then filtering it
                priceData =
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
                lastDataType = "daily"
              }
            } catch (e: Exception) {
              error = "Error while fetching data."
            } finally {
              isLoading = false
            }
          }
        },
        enabled = !isLoading && symbol.isNotBlank(),
      ) {
        Text(if (isLoading) "Loading..." else "Load Chart")
      }

      Spacer(modifier = Modifier.weight(1f))

      // chart type selector
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        ChartType.values().forEach { type ->
          FilterChip(
            selected = chartType == type,
            onClick = { chartType = type },
            label = { Text(type.name) },
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(16.dp))

    // timespan selection
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
      TimespanType.values().forEach { type ->
        FilterChip(
          selected = timespan == type,
          onClick = { timespan = type },
          label = { Text(type.label) },
        )
      }
    }

    Spacer(modifier = Modifier.height(8.dp))

    // indicator selection - only for 1 month. 3 month, ALL (timespans longer than 14 days)
    if ((timespan == TimespanType.ONE_MONTH || timespan == TimespanType.THREE_MONTHS || timespan == TimespanType.ALL) &&
      chartType == ChartType.LINE
    ) {
      Text("Select technical indicators")
      Spacer(modifier = Modifier.height(8.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        IndicatorType.values().forEach { indicator ->
          FilterChip(
            selected = indicator in selectedIndicators,
            onClick = {
              selectedIndicators =
                if (indicator in selectedIndicators) {
                  selectedIndicators - indicator
                } else {
                  selectedIndicators + indicator
                }
            },
            label = { Text(indicator.label) },
          )
        }
      }
    }

    // chart or loading/error state
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
          modifier = Modifier.fillMaxWidth(),
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
      filteredData.isNotEmpty() -> {
        Card(
          modifier =
            when (IndicatorType.RSI) {
              in selectedIndicators -> Modifier.height(400.dp)
              else -> Modifier.height(270.dp)
            },
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // chart title
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "$symbol - ${chartType.name}",
                style = MaterialTheme.typography.headlineSmall,
              )
              Text(
                text = "${filteredData.first().date} to ${filteredData.last().date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }

            // simple stats
            Row(
              modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
              val latest = filteredData.last()
              val oldest = filteredData.first()
              val change = latest.close - oldest.close
              val changePercent = (change / oldest.close) * 100

              statItem("Current", "$%.2f".format(latest.close))
              statItem("High", "$%.2f".format(filteredData.maxOf { it.high }))
              statItem("Low", "$%.2f".format(filteredData.minOf { it.low }))
              statItem(
                "Change",
                "%+.2f (%.2f%%)".format(change, changePercent),
                color = if (change >= 0) Color(0xFF769B5D) else Color(0xFF9D1C00), // or just .Red .Green
              )
            }

            Column(modifier = Modifier.fillMaxSize()) {
              // legend for indicators - only diplayed when idicators can be selected
              if (selectedIndicators.isNotEmpty() && chartType == ChartType.LINE) {
                if (timespan != TimespanType.ONE_WEEK && timespan != TimespanType.ONE_DAY) {
                  indicatorLegend(selectedIndicators)
                }
                Spacer(modifier = Modifier.height(8.dp))
              }
              // chart
              when (chartType) {
                ChartType.LINE -> lineChart(filteredData, selectedIndicators, Modifier.weight(1f), timespan)
                ChartType.CANDLESTICK -> candlestickChart(filteredData)
                ChartType.AREA -> areaChart(filteredData)
              }

              // rsi is only displayed for longer time periods, week+
              if (IndicatorType.RSI in selectedIndicators && timespan != TimespanType.ONE_DAY && timespan != TimespanType.ONE_WEEK) {
                rsiChart(filteredData)
              }
            }
          }
        }
      }
      else -> {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center,
        ) {
          Text("Enter a symbol and click 'Load Chart'")
        }
      }
    }
  }
}

@Composable
fun statItem(
  label: String,
  value: String,
  color: Color = MaterialTheme.colorScheme.onSurface,
) {
  Column {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge,
      color = color,
    )
  }
}

enum class ChartType {
  LINE,
  CANDLESTICK,
  AREA,
}

// timespan to load the historical data
enum class TimespanType(val label: String, val days: Int?) {
  ONE_DAY("1D", 1),
  ONE_WEEK("1W", 7),
  ONE_MONTH("1M", 30),
  THREE_MONTHS("3M", 90),
  ALL("ALL", null),
}

// technical indicator to display
enum class IndicatorType(val label: String) {
  SMA_20("SMA(20)"),
  SMA_50("SMA(50)"),
  EMA_12("EMA(12)"),
  EMA_26("EMA(26)"),
  RSI("RSI(14)"),
}

@Composable
fun lineChart(
  data: List<PricePoint>,
  indicators: Set<IndicatorType>,
  modifier: Modifier,
  timespanType: TimespanType,
) {
  if (data.isEmpty()) return

  val prices = data.map { it.close }
  val minPrice = prices.minOrNull() ?: 0.0
  val maxPrice = prices.maxOrNull() ?: 0.0
  val priceRange = maxPrice - minPrice

  // calculating all selected indicators
  val indicatorData = mutableMapOf<IndicatorType, List<Double?>>()
  val analitics = StockAnalytics()
  indicators.forEach { indicator ->
    when (indicator) {
      IndicatorType.SMA_20 -> {
        indicatorData[indicator] = analitics.calculateSMAList(prices, 20)
      }
      IndicatorType.SMA_50 -> {
        indicatorData[indicator] = analitics.calculateSMAList(prices, 50)
      }
      IndicatorType.EMA_12 -> {
        indicatorData[indicator] = analitics.calculateEMAList(prices, 12)
      }
      IndicatorType.EMA_26 -> {
        indicatorData[indicator] = analitics.calculateEMAList(prices, 26)
      }
      // RSI will be on a separate chart
      else -> {}
    }
  }

  Canvas(
    modifier =
      modifier
        .fillMaxSize()
        .padding(top = 16.dp, bottom = 32.dp, start = 48.dp, end = 16.dp),
  ) {
    val width = size.width
    val height = size.height
    val xStep = width / (data.size - 1).toFloat()

    // drawing grid lines
    for (i in 0..5) {
      val y = height * i / 5f
      drawLine(
        color = Color.Gray.copy(alpha = 0.2f),
        start = Offset(0f, y),
        end = Offset(width, y),
        strokeWidth = 1f,
      )
    }

    // drawing price line
    val path = Path()
    data.forEachIndexed { index, point ->
      val x = index * xStep
      val normalizedPrice = ((point.close - minPrice) / priceRange).toFloat()
      val y = height - (normalizedPrice * height)

      if (index == 0) {
        path.moveTo(x, y)
      } else {
        path.lineTo(x, y)
      }
    }

    drawPath(
      path = path,
      color = Color(0xFF2196F3),
      style = Stroke(width = 3f),
    )

    // Draw points
    data.forEachIndexed { index, point ->
      val x = index * xStep
      val normalizedPrice = ((point.close - minPrice) / priceRange).toFloat()
      val y = height - (normalizedPrice * height)

      drawCircle(
        color = Color(0xFF2196F3),
        radius = 4f,
        center = Offset(x, y),
      )
    }

    // each indicator will have it's own color
    val colors =
      mapOf(
        IndicatorType.SMA_20 to Color(0xFFFF9800), // Orange
        IndicatorType.SMA_50 to Color(0xFF9C27B0), // Purple
        IndicatorType.EMA_12 to Color(0xFF4CAF50), // Green
        IndicatorType.EMA_26 to Color(0xFF00BCD4), // Cyan
      )

    indicatorData.entries.forEachIndexed { colorIndex, (type, values) ->
      val path = Path()
      var started = false

      data.indices.forEach { index ->
        val value = values.getOrNull(index)
        if (value != null) {
          val x = index * xStep
          val normalizedValue = ((value - minPrice) / priceRange).toFloat()
          val y = height - (normalizedValue * height)

          if (!started) {
            path.moveTo(x, y)
            started = true
          } else {
            path.lineTo(x, y)
          }
        }
      }

      // drawing indicators with respecitve colours - only for week+
      if (started && timespanType != TimespanType.ONE_DAY && timespanType != TimespanType.ONE_WEEK) {
        colors[type]?.let {
          drawPath(
            path = path,
            color = it,
            style = Stroke(width = 2f),
          )
        }
      }
    }
  }
}

@Composable
fun candlestickChart(data: List<PricePoint>) {
  if (data.isEmpty()) return

  val minPrice = data.minOf { it.low }
  val maxPrice = data.maxOf { it.high }
  val priceRange = maxPrice - minPrice

  Canvas(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(top = 16.dp, bottom = 32.dp, start = 48.dp, end = 16.dp),
  ) {
    val width = size.width
    val height = size.height
    val candleWidth = (width / data.size) * 0.7f
    val spacing = width / data.size

    data.forEachIndexed { index, point ->
      val x = index * spacing + spacing / 2

      // normalizing prices to canvas height
      val openY = height - (((point.open - minPrice) / priceRange) * height).toFloat()
      val closeY = height - (((point.close - minPrice) / priceRange) * height).toFloat()
      val highY = height - (((point.high - minPrice) / priceRange) * height).toFloat()
      val lowY = height - (((point.low - minPrice) / priceRange) * height).toFloat()

      val isGreen = point.close >= point.open
      val color = if (isGreen) Color(0xFF4CAF50) else Color(0xFFF44336)

      // drawing high-low line
      drawLine(
        color = color,
        start = Offset(x, highY),
        end = Offset(x, lowY),
        strokeWidth = 2f,
      )

      // draw body of the candle
      val top = minOf(openY, closeY)
      val bottom = maxOf(openY, closeY)
      val bodyHeight = maxOf(bottom - top, 2f) // Minimum height

      drawRect(
        color = color,
        topLeft = Offset(x - candleWidth / 2, top),
        size = androidx.compose.ui.geometry.Size(candleWidth, bodyHeight),
      )
    }
  }
}

@Composable
fun areaChart(data: List<PricePoint>) {
  if (data.isEmpty()) return

  val prices = data.map { it.close }
  val minPrice = prices.minOrNull() ?: 0.0
  val maxPrice = prices.maxOrNull() ?: 0.0
  val priceRange = maxPrice - minPrice

  Canvas(
    modifier =
      Modifier
        .fillMaxSize()
        .padding(top = 16.dp, bottom = 32.dp, start = 48.dp, end = 16.dp),
  ) {
    val width = size.width
    val height = size.height
    val xStep = width / (data.size - 1).toFloat()

    val path = Path()
    path.moveTo(0f, height)

    // drawing as in line chart
    data.forEachIndexed { index, point ->
      val x = index * xStep
      val normalizedPrice = ((point.close - minPrice) / priceRange).toFloat()
      val y = height - (normalizedPrice * height)

      if (index == 0) {
        path.lineTo(x, y)
      } else {
        path.lineTo(x, y)
      }
    }

    // closing the path so that coloring is possible
    path.lineTo(width, height)
    path.close()

    // drawing filled area
    drawPath(
      path = path,
      color = Color(0xFF2196F3).copy(alpha = 0.3f),
    )

    // drawing top line
    val linePath = Path()
    data.forEachIndexed { index, point ->
      val x = index * xStep
      val normalizedPrice = ((point.close - minPrice) / priceRange).toFloat()
      val y = height - (normalizedPrice * height)

      if (index == 0) {
        linePath.moveTo(x, y)
      } else {
        linePath.lineTo(x, y)
      }
    }

    drawPath(
      path = linePath,
      color = Color(0xFF2196F3),
      style = Stroke(width = 3f),
    )
  }
}

@Composable
fun rsiChart(data: List<PricePoint>) {
  val prices = data.map { it.close }
  val rsi = StockAnalytics().calculateRSIList(prices, 14)

  Card(
    modifier =
      Modifier
        .fillMaxWidth()
        .height(120.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(modifier = Modifier.padding(8.dp)) {
      Text("RSI(14)", style = MaterialTheme.typography.labelSmall)

      Canvas(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(top = 4.dp),
      ) {
        val width = size.width
        val height = size.height
        val xStep = width / (data.size - 1).toFloat()

        // reference line for better visibility
        listOf(30f, 50f, 70f).forEach { level ->
          val y = height - (level / 100f * height)
          drawLine(
            color = Color.Gray.copy(alpha = 0.3f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 1f,
          )
        }

        // RSI line
        val path = Path()
        var started = false

        rsi.forEachIndexed { index, value ->
          if (value != null) {
            val x = index * xStep
            val y = height - (value.toFloat() / 100f * height)

            if (!started) {
              path.moveTo(x, y)
              started = true
            } else {
              path.lineTo(x, y)
            }
          }
        }

        drawPath(path, color = Color(0xFF9C27B0), style = Stroke(width = 2f))
      }
    }
  }
}

// function to display indicator legend - only for chosen indicators and timespans longer than one week
@Composable
fun indicatorLegend(indicators: Set<IndicatorType>) {
  val colors =
    mapOf(
      IndicatorType.SMA_20 to Color(0xFFFF9800), // Orange
      IndicatorType.SMA_50 to Color(0xFF9C27B0), // Purple
      IndicatorType.EMA_12 to Color(0xFF4CAF50), // Green
      IndicatorType.EMA_26 to Color(0xFF00BCD4), // Cyan
    )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    indicators.filter { it != IndicatorType.RSI }.forEach { indicator ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Canvas(modifier = Modifier.size(16.dp, 2.dp)) {
          drawLine(
            color = colors[indicator] ?: Color.Gray,
            start = Offset(0f, size.height / 2),
            end = Offset(size.width, size.height / 2),
            strokeWidth = 4f,
          )
        }
        Text(
          text = indicator.label,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

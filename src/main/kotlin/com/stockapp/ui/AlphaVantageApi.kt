package com.stockapp.ui

import config.ApiConfig
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// for 1week - all time data from DAILY api calls
@Serializable
data class TimeSeriesResponse(
  @SerialName("Meta Data") val metaData: TimeSeriesMetaData,
  @SerialName("Time Series (Daily)") val timeSeries: Map<String, DailyData>,
)

@Serializable
data class TimeSeriesMetaData(
  @SerialName("1. Information") val information: String,
  @SerialName("2. Symbol") val symbol: String,
  @SerialName("3. Last Refreshed") val lastRefreshed: String,
)

@Serializable
data class DailyData(
  @SerialName("1. open") val open: String,
  @SerialName("2. high") val high: String,
  @SerialName("3. low") val low: String,
  @SerialName("4. close") val close: String,
  @SerialName("5. volume") val volume: String,
)

// for 1 day data from INTRADAY api calls
@Serializable
data class IntradayResponse(
  @SerialName("Meta Data") val metaData: IntradayMetaData,
  @SerialName("Time Series (15min)") val timeSeries: Map<String, IntradayData>? = null,
)

@Serializable
data class IntradayMetaData(
  @SerialName("1. Information") val information: String,
  @SerialName("2. Symbol") val symbol: String,
  @SerialName("3. Last Refreshed") val lastRefreshed: String,
  @SerialName("4. Interval") val interval: String,
)

@Serializable
data class IntradayData(
  @SerialName("1. open") val open: String,
  @SerialName("2. high") val high: String,
  @SerialName("3. low") val low: String,
  @SerialName("4. close") val close: String,
  @SerialName("5. volume") val volume: String,
)

class AlphaVantageApi(
  private val client: HttpClient,
  private val apiKey: String,
) {
  private val baseUrl = "https://www.alphavantage.co/query"

  suspend fun getQuote(symbol: String): GlobalQuoteResponse =
    client.get(baseUrl) {
      parameter("function", "GLOBAL_QUOTE")
      parameter("symbol", symbol)
      parameter("apikey", apiKey)
    }.body()

  suspend fun getTimeSeries(
    symbol: String,
    outputSize: String = "compact",
  ): TimeSeriesResponse =
    client.get(baseUrl) {
      parameter("function", "TIME_SERIES_DAILY")
      parameter("symbol", symbol)
      parameter("outputsize", outputSize) // compact = 100 days
      parameter("apikey", apiKey)
    }.body()

  suspend fun getIntraDaySeries(
    symbol: String,
    interval: String = "15min",
  ): IntradayResponse =
    client.get(baseUrl) {
      parameter("function", "TIME_SERIES_INTRADAY")
      parameter("symbol", symbol)
      parameter("interval", interval)
      parameter("apikey", apiKey)
    }.body()
}

@Serializable
data class GlobalQuoteResponse(
  @SerialName("Global Quote")
  val globalQuote: GlobalQuoteDto,
)

@Serializable
data class GlobalQuoteDto(
  @SerialName("01. symbol") val symbol: String,
  @SerialName("05. price") val price: String,
  @SerialName("09. change") val change: String,
  @SerialName("10. change percent") val changePercent: String,
)

fun createAlphaVantageApi(): AlphaVantageApi {
  val client =
    HttpClient(CIO) {
      install(ContentNegotiation) {
        json(
          Json {
            ignoreUnknownKeys = true
            isLenient = true
            prettyPrint = true
          },
        )
      }

      install(Logging) {
        logger = Logger.SIMPLE
        level = LogLevel.INFO
      }
    }

  val apiKey = ApiConfig.apiKey
  return AlphaVantageApi(client, apiKey)
}

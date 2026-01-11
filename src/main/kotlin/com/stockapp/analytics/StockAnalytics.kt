package com.stockapp.analytics

import com.stockapp.ui.PricePoint
import kotlin.math.pow
import kotlin.math.sqrt

data class StockStatistics(
  val symbol: String,
  val currentPrice: Double,
  val dataPoints: Int,
  val returns: Returns,
  val priceStats: PriceStats,
  val volatility: Volatility,
  val indicators: TechnicalIndicators,
  val volumeStats: VolumeStats,
)

data class Returns(
  val daily: Double?,
  val weekly: Double?,
  val monthly: Double?,
  val threeMonth: Double?,
  val allTime: Double?,
)

data class PriceStats(
  val current: Double,
  val high: Double,
  val low: Double,
  val average: Double,
  val median: Double,
)

data class Volatility(
  val dailyStdDev: Double,
  val coefficientOfVariation: Double,
)

data class TechnicalIndicators(
  val sma20: Double?,
  val sma50: Double?,
  val ema12: Double?,
  val ema26: Double?,
  val rsi14: Double?,
)

data class VolumeStats(
  val average: Long,
  val highest: Long,
  val lowest: Long,
  val trend: String,
)

class StockAnalytics {
  fun calculateStatistics(
    symbol: String,
    data: List<PricePoint>,
  ): StockStatistics {
    if (data.isEmpty()) throw IllegalArgumentException("Data is empty")

    val prices = data.map { it.close } // most recent price
    val volumes = data.map { it.volume }

    return StockStatistics(
      symbol = symbol,
      currentPrice = data.last().close,
      dataPoints = data.size,
      returns = calculateReturns(data),
      priceStats = calculatePriceStats(prices),
      volatility = calculateVolatility(data),
      indicators = calculateIndicators(data),
      volumeStats = calculateVolumeStats(volumes),
    )
  }

  // calculating returns from 1 day, week, month, 3 months
  private fun calculateReturns(data: List<PricePoint>): Returns {
    val latestPrice = data.last().close

    fun getReturnsInterval(interval: Int): Double? {
      val cutoffData = data.last().date.minusDays(interval.toLong())
      val oldPrice = data.firstOrNull { it.date >= cutoffData }?.close
      return oldPrice?.let {
        100 * (latestPrice - it) / it // null or return
      }
    }

    return Returns(
      daily =
        if (data.size >= 2) {
          val yesterday = data[data.size - 2].close
          ((latestPrice - yesterday) / yesterday) * 100
        } else {
          null
        },
      weekly = getReturnsInterval(7),
      monthly = getReturnsInterval(30),
      threeMonth = getReturnsInterval(90),
      allTime =
        if (data.isEmpty()) {
          null
        } else {
          100 * (latestPrice - data.first().close) / data.first().close
        },
    )
  }

  private fun calculatePriceStats(prices: List<Double>): PriceStats {
    val sorted = prices.sorted()
    val isEvenLen = sorted.size % 2 == 0
    val len = prices.size
    return PriceStats(
      current = prices.last(),
      low = prices.minOrNull() ?: 0.0,
      high = prices.maxOrNull() ?: 0.0,
      average = prices.average(),
      median = if (isEvenLen) (sorted[len / 2] + sorted[len / 2 - 1]) / 2 else sorted[len / 2],
    )
  }

  private fun calculateVolatility(data: List<PricePoint>): Volatility {
    if (data.size < 2) return Volatility(0.0, 0.0)

    // daily std
    val dailyReturns = data.zipWithNext { a, b -> (b.close - a.close) / a.close }
    val meanReturn = dailyReturns.average()
    val variance = dailyReturns.map { (it - meanReturn).pow(2) }.average()
    val dailyStd = sqrt(variance)

    // coefficient of variation
    val averagePrice = data.map { it.close }.average()
    val coefficientOfVariation = (dailyStd / averagePrice) * 100

    return Volatility(dailyStd * 100, coefficientOfVariation)
  }

  private fun calculateIndicators(data: List<PricePoint>): TechnicalIndicators {
    val prices = data.map { it.close }

    return TechnicalIndicators(
      sma20 = calculateSMA(prices, 20),
      sma50 = calculateSMA(prices, 50),
      ema12 = calculateEMA(prices, 12),
      ema26 = calculateEMA(prices, 26),
      rsi14 = calculateRSI(prices, 14),
    )
  }

  private fun calculateSMA(
    prices: List<Double>,
    interval: Int,
  ): Double? {
    if (prices.size < interval) return null
    return prices.takeLast(interval).average()
  }

  private fun calculateEMA(
    prices: List<Double>,
    interval: Int,
  ): Double? {
    return calculateEMAList(prices, interval).last()
//    val multiplier = 2.0 * (interval + 1)
//
//    // initial
//    var ema = prices.take(interval).average() // init
//
//    prices.drop(interval).forEach { price ->
//      ema = (price - ema) * multiplier + ema
//    }
//    return ema
  }

  private fun calculateRSI(
    prices: List<Double>,
    interval: Int,
  ): Double? {
    if (prices.size < interval) return null
    val changes = prices.zipWithNext { a, b -> b - a }
    // taking average losses and gains over the time interval
    val gains = changes.map { if (it > 0) it else 0.0 }
    val losses = changes.map { if (it < 0) -it else 0.0 }

    val averageGains = gains.average()
    val averageLosses = losses.average()

    return 100 - (100 / (1 + (averageGains / averageLosses)))
  }

  // list of SMA over the length of price data
  fun calculateSMAList(
    prices: List<Double>,
    period: Int,
  ): List<Double?> {
    if (prices.size < period) return List(prices.size) { null }

    val result = mutableListOf<Double?>()
    repeat(period - 1) { result.add(null) }

    for (i in period - 1 until prices.size) {
      result.add(calculateSMA(prices.subList(i - period + 1, i + 1), period))
    }

    return result
  }

  // list of ema over the entire price data
  fun calculateEMAList(
    prices: List<Double>,
    period: Int,
  ): List<Double?> {
    if (prices.size < period) return List(prices.size) { null }

    val result = mutableListOf<Double?>()
    val multiplier = 2.0 / (period + 1)

    repeat(period - 1) { result.add(null) }

    // calculate initial SMA
    var ema = prices.subList(0, period).average()
    result.add(ema)

    // calculate EMA for remaining values
    for (i in period until prices.size) {
      ema = (prices[i] - ema) * multiplier + ema
      result.add(ema)
    }

    return result
  }

  // list of rsi over the entire price data
  fun calculateRSIList(
    prices: List<Double>,
    period: Int,
  ): List<Double?> {
    val result = mutableListOf<Double?>()
    repeat(period) { result.add(null) }

    for (i in period until prices.size - 1) {
      result.add(calculateRSI(prices.subList(0, i + 1), period))
    }
    return result
  }

  private fun calculateVolumeStats(volumes: List<Long>): VolumeStats {
    val average = volumes.average().toLong()
    val recent = volumes.takeLast(20)
    val recentAverage = recent.average()

    // calculating trends
    val trend =
      when {
        recentAverage / average > 1.1 -> "Increasing"
        recentAverage / average < 0.9 -> "Decreasing"
        else -> "Stable"
      }

    return VolumeStats(
      average = average,
      highest = volumes.max(),
      lowest = volumes.min(),
      trend = trend,
    )
  }
}

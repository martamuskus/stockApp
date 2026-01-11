import com.stockapp.analytics.StockAnalytics
import com.stockapp.ui.PricePoint
import org.junit.Test
import kotlin.test.*
import java.time.LocalDate
import kotlin.math.pow

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.stockapp.data.StockRepository
import com.stockapp.ui.AlphaVantageApi
import com.stockapp.ui.chartScreen
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Rule

class AnaliticsTests {
    private val analitics = StockAnalytics()

    private fun createTestData(prices: MutableList<Double>): List<PricePoint> {
        return prices.mapIndexed { index, price ->
            PricePoint(
                date = LocalDate.now().minusDays((prices.size - index).toLong()),
                open = price,
                high = price + 1.0,
                low = price - 1.0,
                close = price,
                volume = 1000000L
            )
        }
    }
    @Test
    fun testReturnsCalculation() {
        val data = createTestData(mutableListOf(200.0, 220.0))
        val stats = analitics.calculateStatistics("AA", data)

        assertNotNull(stats.returns.daily)
        assertEquals(10.0, stats.returns.daily)

    }

    @Test
    fun testSMACalculation() {
        var list = mutableListOf<Double>()
        var base: Double = -1.0
        for (i in 1..30) {
            list.add(200 + base.pow(i)*5*i)
        }

        val data = createTestData(list)
        val stats = analitics.calculateStatistics("AA", data)

        assertNotNull(stats.indicators.sma20)
        assertEquals(202.5, stats.indicators.sma20)

    }

    @Test
    fun testPriceStats() {
        var list = mutableListOf<Double>()
        var base: Double = -1.0
        for (i in 1..30) {
            list.add(200 + base.pow(i) * 5 * i)
        }

        val data = createTestData(list)
        val stats = analitics.calculateStatistics("AA", data)
        assertNotNull(stats.priceStats)
        assertEquals(stats.priceStats.low, 55.0)
        assertEquals(stats.priceStats.high, 350.0)
        assertEquals(stats.priceStats.median, 202.5)
        assertEquals(stats.priceStats.current, 350.0)
    }

    @Test
    fun testEMACalculation() {
        var list = mutableListOf<Double>()
        var base: Double = -1.0
        for (i in 1..14) {
            list.add(200 + base.pow(i) * 5 * i)
        }

        val data = createTestData(list)
        val stats = analitics.calculateStatistics("AA", data)

        print(list)
        print(stats.indicators.ema12)
        assertNotNull(stats.indicators.ema12)
        assertEquals(stats.indicators.ema12 ?: 0.0, 204.0976, 1e-4)
    }

    @Test
    fun testRSICalculation() {
        var list = mutableListOf<Double>()
        var base: Double = -1.0
        for (i in 1..15) {
            list.add(200 + base.pow(i) * 5 * i)
        }

        val data = createTestData(list)
        val stats = analitics.calculateStatistics("AA", data)

        assertEquals(stats.indicators.rsi14 ?: 0.0, 46.875, 1e-3)
    }
}

class ChartScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockApi = mockk<AlphaVantageApi>()
    private val mockRepository = mockk<StockRepository>(relaxed = true)

    @Test
    fun chartScreen_EnteringInvalidSymbol_ShowsErrorMessageAndDoesNotCrash() {
        val invalidSymbol = "INVALID_SYM"

        coEvery {
            mockApi.getTimeSeries(invalidSymbol, any())
        } throws Exception("Invalid API call")

        composeTestRule.setContent {
            chartScreen(api = mockApi, repository = mockRepository)
        }

        composeTestRule.onNodeWithText("Symbol")
            .performTextInput(invalidSymbol)

        composeTestRule.onNodeWithText("Load Chart")
            .performClick()

        composeTestRule.onNodeWithText("Error while fetching data.")
            .assertIsDisplayed()

    }
}
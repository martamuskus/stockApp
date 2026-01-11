import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.stockapp.data.StockRepository
import com.stockapp.ui.AlphaVantageApi
import com.stockapp.ui.chartScreen
import io.mockk.*
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Rule
import org.junit.Test

class WatchlistTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockApi = mockk<AlphaVantageApi>(relaxed = true)
    private val mockRepository = mockk<StockRepository>(relaxed = true)

    @Test
    fun clickEmptyHeart() {
        val symbol = "AAPL"
        val watchlistFlow = MutableStateFlow(false)
        every { mockRepository.isInWatchlistFlow(symbol) } returns watchlistFlow

        composeTestRule.setContent {
            chartScreen(api = mockApi, repository = mockRepository)
        }

        composeTestRule.onNodeWithText("Symbol").performTextInput(symbol)
        composeTestRule.onNodeWithText("\u2661").performClick() // heart
        coVerify { mockRepository.addToWatchlist(symbol) }
    }

    @Test
    fun clickFullHeart() {
        val symbol = "TSLA"
        val watchlistFlow = MutableStateFlow(true)
        every { mockRepository.isInWatchlistFlow(symbol) } returns watchlistFlow

        composeTestRule.setContent {
            chartScreen(api = mockApi, repository = mockRepository)
        }

        composeTestRule.onNodeWithText("Symbol").performTextInput(symbol)
        composeTestRule.onNodeWithText("\u2764\uFE0F").performClick()
        coVerify { mockRepository.removeFromWatchlist(symbol) }
    }
}
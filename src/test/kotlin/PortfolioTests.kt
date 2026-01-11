import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.stockapp.ui.portfolioScreen
import com.stockapp.data.PortfolioStock
import com.stockapp.data.StockRepository
import com.stockapp.ui.AlphaVantageApi
import io.mockk.*
import org.junit.Rule
import org.junit.Test

class PortfolioEditTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockRepository = mockk<StockRepository>(relaxed = true)
    private val mockApi = mockk<AlphaVantageApi>(relaxed = true)

    @Test
    fun portfolioClickEdit() {
        val initialStock = PortfolioStock(
            id = 1,
            symbol = "AAPL",
            name = "Apple",
            quantity = 10.0,
            purchasePrice = 150.0,
            purchaseDate = System.currentTimeMillis()
        )

        coEvery { mockRepository.getAllPortfolio() } returns listOf(initialStock)
        coEvery { mockRepository.getTotalInvestment() } returns 1500.0

        composeTestRule.setContent {
            portfolioScreen(repository = mockRepository, api = mockApi)
        }

        composeTestRule.onNodeWithContentDescription("Edit").performClick()

        composeTestRule.onNodeWithText("New Quantity").performTextReplacement("20.0")
        composeTestRule.onNodeWithText("Purchase Price").performTextReplacement("160.0")

        composeTestRule.onNodeWithText("Edit").performClick()

        coVerify {
            mockRepository.updatePortfolioStock(match {
                it.quantity == 20.0 && it.purchasePrice == 160.0 && it.symbol == "AAPL"
            })
        }
    }
}

class PortfolioAddTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val mockRepository = mockk<StockRepository>(relaxed = true)
    private val mockApi = mockk<AlphaVantageApi>(relaxed = true)

    @Test
    fun portfolioClickAdd() {
        val initialStock = PortfolioStock(1, "AAPL", "Apple", 10.0, 150.0, System.currentTimeMillis())
        val addedStock = PortfolioStock(2, "GOOGL", "GOOGL", 20.0, 160.0, System.currentTimeMillis())
        val updatedList = listOf(initialStock, addedStock)
        val updatedTotal = 1500.0 + (20.0 * 160.0)

        coEvery { mockRepository.getAllPortfolio() } returnsMany listOf(listOf(initialStock), updatedList)
        coEvery { mockRepository.getTotalInvestment() } returnsMany listOf(1500.0, updatedTotal)

        composeTestRule.setContent {
            portfolioScreen(repository = mockRepository, api = mockApi)
        }

        composeTestRule.onNodeWithContentDescription("Add Stock").performClick()
        composeTestRule.onNodeWithText("Stock Symbol").performTextReplacement("GOOGL")
        composeTestRule.onNodeWithText("Quantity").performTextReplacement("20.0")
        composeTestRule.onNodeWithText("Purchase Price").performTextReplacement("160.0")
        composeTestRule.onNodeWithText("Add").performClick()

        coVerify {
            mockRepository.addToPortfolio("GOOGL", "GOOGL", 20.0, 160.0)
        }

        composeTestRule.onNodeWithText("$4700,00").assertIsDisplayed()
    }
}


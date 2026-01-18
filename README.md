# Stock app

Simple Kotlin app for stock market data, statistics and visualization.

## How to run the app?
To be able to access the stock market data an AlphaVantage API key is needed.
Go to https://www.alphavantage.co/support/#api-key to get your API key.

Once you have your API key, create local.properties file in the root folder of the project and add a line:

    alphavantage.api.key = "YOUR API KEY HERE"

Then you are ready to go!

To run the app:
1. with gradle: ./gradlew run

or
2. ./gradlew build | java -jar ${pathtojarfile} (currently: build/libs/stock-1.0.0.jar)

## What can the app do?

The app allows you to
1) Explore the line, candle and area chats of a selected stock over a selected period of time. For the free API key the time ranges from precise data from one day to data from the previous 100 days.
2) Add the technical indicators such as SMA, EMA and RSI to your line chart to monitor the price trends.
3) Explore the statistics about prices, volumes, returns and volatility.
4) Add a stock to your Watchlist and remove it.
5) Add a stock to your portfolio, edit the number of shares, check the total money invested or the money gained/lost from a particular stock you added to portfolio.

### TODO: 
More test coverage

/*
 * Copyright 2020 Peter Kenji Yamanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.moment.tiingo

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.Cached
import com.pyamsoft.moment.finance.FinanceSource
import com.pyamsoft.moment.finance.model.Finances
import com.pyamsoft.moment.finance.model.Meta
import com.pyamsoft.moment.finance.model.Price
import com.pyamsoft.moment.finance.model.Quote
import com.pyamsoft.moment.finance.model.Ticker
import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Tiingo @Inject internal constructor(
    @InternalApi private val cache: Cached<List<String>>,
    private val service: TiingoService,
    private val auth: TiingoAuth
) : FinanceSource {

    @CheckResult
    private suspend fun getAccessToken(): String {
        return "Token ${auth.getApiToken()}"
    }

    override suspend fun tickers(): List<Ticker> {
        val strings = cache.call()
        return strings.map { Ticker(it) }
    }

    override suspend fun quote(symbol: String): Quote {
        Enforcer.assertOffMainThread()
        val quote = service.quote(token = getAccessToken(), symbol = symbol).first()
        return Quote(
            symbol = quote.ticker,
            lastTrade = quote.lastSaleTimestamp,
            price = quote.last,
            previousClose = quote.prevClose
        )
    }

    @CheckResult
    override suspend fun quotes(vararg symbols: String): List<Quote> {
        return batch(symbols) { quote(it) }
    }

    override suspend fun price(symbol: String): Price {
        Enforcer.assertOffMainThread()
        val price = service.eod(token = getAccessToken(), symbol = symbol, startDate = null).first()
        return Price(
            symbol = symbol,
            close = price.adjClose,
            date = price.date,
            high = price.adjHigh,
            low = price.adjLow,
            open = price.adjOpen,
            volume = price.adjVolume
        )
    }

    @CheckResult
    override suspend fun prices(vararg symbols: String): List<Price> {
        return batch(symbols) { price(it) }
    }

    override suspend fun meta(symbol: String): Meta {
        Enforcer.assertOffMainThread()
        val info = service.info(token = getAccessToken(), symbol = symbol)
        return Meta(
            symbol = info.ticker,
            name = info.name,
            exchange = info.exchangeCode,
            description = info.description,
            startDate = info.startDate,
            endDate = info.endDate
        )
    }

    @CheckResult
    override suspend fun metas(vararg symbols: String): List<Meta> {
        return batch(symbols) { meta(it) }
    }

    /**
     * Takes a date
     */
    @CheckResult
    private fun rangeToString(time: Int, unit: FinanceSource.DateRange.RangeUnit): String {
        val today = Calendar.getInstance()
        val amount = when (unit) {
            FinanceSource.DateRange.RangeUnit.DAYS -> Calendar.DAY_OF_MONTH
            FinanceSource.DateRange.RangeUnit.MONTHS -> Calendar.MONTH
            FinanceSource.DateRange.RangeUnit.YEARS -> Calendar.YEAR
        }

        // Subtract the amount to get a start date in the past
        today.add(amount, -time)

        // Tiingo expects YYYY-MM-DD
        return Finances.formatInfoDate(today.time)
    }

    override suspend fun history(symbol: String, range: FinanceSource.DateRange): List<Price> {
        Enforcer.assertOffMainThread()
        val startDate = rangeToString(range.time, range.unit)
        val prices = service.eod(token = getAccessToken(), symbol = symbol, startDate = startDate)
        return prices.map { price ->
            Price(
                symbol = symbol,
                close = price.adjClose,
                date = price.date,
                high = price.adjHigh,
                low = price.adjLow,
                open = price.adjOpen,
                volume = price.adjVolume
            )
        }
    }

    companion object {

        @JvmStatic
        @CheckResult
        private suspend inline fun <T : Any> batch(
            symbols: Array<out String>,
            crossinline call: suspend CoroutineScope.(String) -> T
        ): List<T> {
            Enforcer.assertOffMainThread()
            val jobs = mutableListOf<Deferred<T>>()

            // Queue up each symbol lookup job
            symbols.forEach { symbol ->
                coroutineScope {
                    val job = async(context = Dispatchers.IO) { call(symbol) }
                    jobs.add(job)
                }
            }

            return awaitAll(*jobs.toTypedArray())
        }

    }

}
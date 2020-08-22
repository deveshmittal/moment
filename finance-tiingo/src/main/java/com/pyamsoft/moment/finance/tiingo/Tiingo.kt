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

package com.pyamsoft.moment.finance.tiingo

import androidx.annotation.CheckResult
import com.pyamsoft.cachify.Cached
import com.pyamsoft.moment.finance.DateRange
import com.pyamsoft.moment.finance.FinanceSource
import com.pyamsoft.moment.finance.model.Finances
import com.pyamsoft.moment.finance.model.Meta
import com.pyamsoft.moment.finance.model.Price
import com.pyamsoft.moment.finance.model.Quote
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.moment.finance.model.Ticker
import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
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
    private suspend fun getAccessToken(): String = withContext(context = Dispatchers.IO) {
        return@withContext "Token ${auth.getApiToken()}"
    }

    override suspend fun tickers(): List<Ticker> = withContext(context = Dispatchers.IO) {
        val strings = cache.call()
        return@withContext strings.map { Ticker(it) }
    }

    override suspend fun quote(symbol: Symbol): Quote = withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val quote = service.quote(token = getAccessToken(), ticker = symbol.symbol()).first()
        return@withContext Quote(
            ticker = quote.ticker,
            lastTrade = quote.lastSaleTimestamp,
            price = quote.last,
            previousClose = quote.prevClose
        )
    }

    @CheckResult
    override suspend fun quotes(vararg symbols: Symbol): List<Quote> =
        withContext(context = Dispatchers.IO) {
            return@withContext batch(
                symbols
            ) { quote(it) }
        }

    override suspend fun price(symbol: Symbol): Price = withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        // Tiingo formats it date strings differently if you pass a start date
        // see Finances.kt
        // Without a start date, it formats to IEX parse format
        // With a start date it formats to Tiingo parse format
        // to always receive Tiingo format, we pass today's day when getting a current price quote
        val todayRange = rangeToString(0, DateRange.RangeUnit.DAYS)
        val price = service.eod(
            token = getAccessToken(),
            ticker = symbol.symbol(),
            startDate = todayRange
        ).first()
        return@withContext Price(
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
    override suspend fun prices(vararg symbols: Symbol): List<Price> =
        withContext(context = Dispatchers.IO) {
            return@withContext batch(
                symbols
            ) { price(it) }
        }

    override suspend fun meta(symbol: Symbol): Meta = withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val info = service.info(token = getAccessToken(), ticker = symbol.symbol())
        return@withContext Meta(
            ticker = info.ticker,
            name = info.name,
            exchange = info.exchangeCode,
            description = info.description,
            startDate = info.startDate,
            endDate = info.endDate
        )
    }

    @CheckResult
    override suspend fun metas(vararg symbols: Symbol): List<Meta> =
        withContext(context = Dispatchers.IO) {
            return@withContext batch(
                symbols
            ) { meta(it) }
        }

    /**
     * Takes a date
     */
    @CheckResult
    private fun rangeToString(time: Int, unit: DateRange.RangeUnit): String {
        val today = Calendar.getInstance()
        val amount = when (unit) {
            DateRange.RangeUnit.DAYS -> Calendar.DAY_OF_MONTH
            DateRange.RangeUnit.MONTHS -> Calendar.MONTH
            DateRange.RangeUnit.YEARS -> Calendar.YEAR
        }

        // Subtract the amount to get a start date in the past
        today.add(amount, -time)

        // Tiingo expects YYYY-MM-DD
        return Finances.formatInfoDate(today.time)
    }

    override suspend fun history(symbol: Symbol, range: DateRange): List<Price> =
        withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            val startDate = rangeToString(range.time, range.unit)
            val prices = service.eod(
                token = getAccessToken(),
                ticker = symbol.symbol(),
                startDate = startDate
            )
            return@withContext prices.map { price ->
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
            symbols: Array<out Symbol>,
            crossinline call: suspend CoroutineScope.(Symbol) -> T
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

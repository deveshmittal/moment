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

import android.annotation.SuppressLint
import androidx.annotation.CheckResult
import com.pyamsoft.cachify.Cached
import com.pyamsoft.moment.core.threadLocal
import com.pyamsoft.moment.finance.DateRange
import com.pyamsoft.moment.finance.FinanceSource
import com.pyamsoft.moment.finance.model.Finances
import com.pyamsoft.moment.finance.model.Meta
import com.pyamsoft.moment.finance.model.Price
import com.pyamsoft.moment.finance.model.Quote
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.moment.finance.model.Ticker
import com.pyamsoft.moment.finance.model.toSymbol
import com.pyamsoft.moment.finance.tiingo.model.Subscribe
import com.pyamsoft.moment.finance.tiingo.model.TiingoIexEvent
import com.pyamsoft.moment.finance.tiingo.model.TiingoQuote
import com.pyamsoft.pydroid.core.Enforcer
import com.tinder.scarlet.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class Tiingo @Inject internal constructor(
    @InternalApi private val cache: Cached<List<String>>,
    private val restService: TiingoRestService,
    private val websocketService: TiingoWebSocketService,
    private val auth: TiingoAuth
) : FinanceSource {

    @CheckResult
    private suspend fun getWebSocketAccessToken(): String = withContext(context = Dispatchers.IO) {
        return@withContext auth.getApiToken()
    }

    @CheckResult
    private suspend fun getRestAccessToken(): String = withContext(context = Dispatchers.IO) {
        return@withContext "Token ${auth.getApiToken()}"
    }

    override suspend fun tickers(): List<Ticker> = withContext(context = Dispatchers.IO) {
        val strings = cache.call()
        return@withContext strings.map { Ticker(it.toSymbol()) }
    }

    override suspend fun price(symbol: Symbol): Price = withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val quote =
            restService.quote(token = getRestAccessToken(), ticker = symbol.symbol()).first()


        val lastTradeDate = quote.lastSaleTimestamp.let { trade ->
            if (trade == null) Finances.INVALID_TIME else parseIexDate(trade)
        }

        return@withContext Price(
            symbol = quote.ticker.toSymbol(),
            lastTrade = lastTradeDate,
            price = quote.last ?: Finances.INVALID_PRICE,
            previousClose = quote.prevClose ?: Finances.INVALID_PRICE
        )
    }

    @CheckResult
    override suspend fun prices(vararg symbols: Symbol): List<Price> =
        withContext(context = Dispatchers.IO) {
            return@withContext batch(
                symbols
            ) { price(it) }
        }

    @CheckResult
    private fun TiingoQuote.toQuote(symbol: Symbol): Quote {
        val tradeDate = this.date.let { date ->
            if (date == null) Finances.INVALID_TIME else parseTiingoDate(date)
        }

        return Quote(
            symbol = symbol,
            date = tradeDate,
            close = this.adjClose ?: Finances.INVALID_PRICE,
            high = this.adjHigh ?: Finances.INVALID_PRICE,
            low = this.adjLow ?: Finances.INVALID_PRICE,
            open = this.adjOpen ?: Finances.INVALID_PRICE,
            volume = this.adjVolume ?: Finances.INVALID_VOLUME
        )
    }

    override suspend fun quote(symbol: Symbol): Quote = withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        // Tiingo formats it date strings differently if you pass a start date
        // see Finances.kt
        // Without a start date, it formats to IEX parse format
        // With a start date it formats to Tiingo parse format
        // to always receive Tiingo format, we pass today's day when getting a current price quote
        val todayRange = rangeToString(0, DateRange.RangeUnit.DAYS)
        val eod = restService.eod(
            token = getRestAccessToken(),
            ticker = symbol.symbol(),
            startDate = todayRange
        ).first()
        return@withContext eod.toQuote(symbol)
    }

    @CheckResult
    override suspend fun quotes(vararg symbols: Symbol): List<Quote> =
        withContext(context = Dispatchers.IO) {
            return@withContext batch(
                symbols
            ) { quote(it) }
        }

    override suspend fun meta(symbol: Symbol): Meta = withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        val info = restService.info(token = getRestAccessToken(), ticker = symbol.symbol())

        val companyStartDate = info.startDate.let { date ->
            if (date == null) Finances.INVALID_TIME else parseInfoDate(date)
        }

        val companyEndDate = info.endDate.let { date ->
            if (date == null) Finances.INVALID_TIME else parseInfoDate(date)
        }

        return@withContext Meta(
            symbol = info.ticker.toSymbol(),
            name = info.name.orEmpty(),
            exchange = info.exchangeCode.orEmpty(),
            description = info.description.orEmpty(),
            startDate = companyStartDate,
            endDate = companyEndDate
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
        return formatInfoDate(today.time)
    }

    override suspend fun history(symbol: Symbol, range: DateRange): List<Quote> =
        withContext(context = Dispatchers.IO) {
            Enforcer.assertOffMainThread()
            val startDate = rangeToString(range.time, range.unit)
            val eods = restService.eod(
                token = getRestAccessToken(),
                ticker = symbol.symbol(),
                startDate = startDate
            )
            return@withContext eods.map { it.toQuote(symbol) }
        }

    @CheckResult
    private fun watchSymbols(
        scope: CoroutineScope,
        symbols: Array<out Symbol>,
        onUpdate: suspend (Price) -> Unit
    ): FinanceSource.Watcher {
        val newScope = CoroutineScope(context = scope.coroutineContext + Dispatchers.IO).launch {
            websocketService.observeWebSocketEvent()
                .filter { it is WebSocket.Event.OnConnectionOpened<*> }
                .collect {
                    val token = getWebSocketAccessToken()
                    val subscribe = Subscribe(token)
                    websocketService.subscribe(subscribe)
                }

            websocketService.observeTicker()
                .filter { it.isValid() }
                .filter { it.type() == TiingoIexEvent.MessageType.TRADE }
                .filter { it.symbol() in symbols }
                .collect { onUpdate(it.asPrice()) }
        }
        return object : FinanceSource.Watcher {
            override fun unsubscribe() {
                newScope.cancel()
            }
        }
    }

    override fun subscribe(
        scope: CoroutineScope,
        symbol: Symbol,
        onUpdate: suspend (Price) -> Unit
    ): FinanceSource.Watcher {
        return watchSymbols(scope, arrayOf(symbol), onUpdate)
    }

    override fun subscribe(
        scope: CoroutineScope,
        vararg symbols: Symbol,
        onUpdate: suspend (Price) -> Unit
    ): FinanceSource.Watcher {
        return watchSymbols(scope, symbols, onUpdate)
    }

    companion object {

        @SuppressLint("SimpleDateFormat")
        private val iexFormatter = threadLocal { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX") }

        @SuppressLint("SimpleDateFormat")
        private val tiingoFormatter =
            threadLocal { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") }

        @SuppressLint("SimpleDateFormat")
        private val infoFormatter = threadLocal { SimpleDateFormat("yyyy-MM-dd") }

        @CheckResult
        private fun parseIexDate(date: String): Date {
            return requireNotNull(iexFormatter.get()).parse(date) ?: Finances.INVALID_TIME
        }

        @CheckResult
        private fun parseTiingoDate(date: String): Date {
            return requireNotNull(tiingoFormatter.get()).parse(date) ?: Finances.INVALID_TIME
        }

        @CheckResult
        private fun parseInfoDate(date: String): Date {
            return requireNotNull(infoFormatter.get()).parse(date) ?: Finances.INVALID_TIME
        }

        @CheckResult
        private fun formatInfoDate(date: Date): String {
            return requireNotNull(infoFormatter.get()).format(date).orEmpty()
        }

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

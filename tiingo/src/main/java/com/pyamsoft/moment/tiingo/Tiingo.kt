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
import com.pyamsoft.moment.finance.model.EodPrice
import com.pyamsoft.moment.finance.model.Quote
import com.pyamsoft.moment.finance.model.Ticker
import com.pyamsoft.moment.finance.model.TickerInfo
import com.pyamsoft.pydroid.core.Enforcer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * These functions closely mirror those in TiingoService without the header parameter
 */
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

    override suspend fun eod(symbol: String): EodPrice {
        Enforcer.assertOffMainThread()
        val price = service.eod(token = getAccessToken(), symbol = symbol).first()
        return EodPrice(
            symbol = symbol,
            close = price.adjClose,
            date = price.date,
            high = price.adjHigh,
            low = price.adjLow,
            open = price.adjOpen,
            volume = price.adjVolume
        )
    }

    override suspend fun info(symbol: String): TickerInfo {
        Enforcer.assertOffMainThread()
        val info = service.info(token = getAccessToken(), symbol = symbol)
        return TickerInfo(
            symbol = info.ticker,
            name = info.name,
            exchange = info.exchangeCode,
            description = info.description,
            startDate = info.startDate,
            endDate = info.endDate
        )
    }

}
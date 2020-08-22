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

package com.pyamsoft.moment.chart

import androidx.annotation.CheckResult
import com.pyamsoft.moment.finance.DateRange
import com.pyamsoft.moment.finance.FinanceSource
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ChartInteractor @Inject internal constructor(
    private val source: FinanceSource
) {

    @CheckResult
    suspend fun getChart(
        symbol: Symbol,
        range: DateRange
    ): List<ChartDataPoint> = withContext(context = Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        assertValidSymbol(symbol)
        val priceHistory = source.history(symbol, range)
        return@withContext priceHistory.map { it.toChartData() }
    }


    @CheckResult
    suspend fun getMostRecentTrade(symbol: Symbol): Trade = withContext(Dispatchers.Default) {
        Enforcer.assertOffMainThread()
        assertValidSymbol(symbol)
        val quote = source.price(symbol)
        return@withContext quote.toTrade()
    }

    companion object {
        private fun assertValidSymbol(symbol: Symbol) {
            if (!symbol.isValid()) {
                Timber.e("Cannot use invalid symbol")
                throw IllegalArgumentException("Invalid Symbol passed to function")
            }
        }
    }
}



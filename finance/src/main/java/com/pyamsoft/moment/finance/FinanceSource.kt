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

package com.pyamsoft.moment.finance

import androidx.annotation.CheckResult
import com.pyamsoft.moment.finance.model.Meta
import com.pyamsoft.moment.finance.model.Price
import com.pyamsoft.moment.finance.model.Quote
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.moment.finance.model.Ticker

interface FinanceSource {

    @CheckResult
    suspend fun tickers(): List<Ticker>

    @CheckResult
    suspend fun quote(symbol: Symbol): Quote

    @CheckResult
    suspend fun quotes(vararg symbols: Symbol): List<Quote>

    @CheckResult
    suspend fun price(symbol: Symbol): Price

    @CheckResult
    suspend fun prices(vararg symbols: Symbol): List<Price>

    @CheckResult
    suspend fun meta(symbol: Symbol): Meta

    @CheckResult
    suspend fun metas(vararg symbols: Symbol): List<Meta>

    @CheckResult
    suspend fun history(symbol: Symbol, range: DateRange): List<Price>

}
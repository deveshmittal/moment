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

package com.pyamsoft.moment.finance.model

import androidx.annotation.CheckResult
import java.util.Date

data class Quote(
    internal val symbol: String?,
    internal val lastTrade: String?,
    internal val price: Float?,
    internal val previousClose: Float?
) {

    private val lastTradeDate: Date

    init {
        lastTradeDate = lastTrade.let { trade ->
            if (trade == null) Finances.INVALID_TIME else Finances.parseTradeDate(trade)
        }
    }

    @CheckResult
    fun ticker(): String {
        return symbol.orEmpty()
    }

    @CheckResult
    fun lastTrade(): Date {
        return lastTradeDate
    }

    @CheckResult
    fun price(): Float {
        return price ?: Finances.INVALID_PRICE
    }

    @CheckResult
    fun previousClose(): Float {
        return previousClose ?: Finances.INVALID_PRICE
    }

}
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

data class Price(
    internal val symbol: String?,
    internal val date: String?,
    internal val high: Float?,
    internal val low: Float?,
    internal val open: Float?,
    internal val close: Float?,
    internal val volume: Long?
) {

    private val tradeDate: Date

    init {
        tradeDate = date.let { date ->
            if (date == null) Finances.INVALID_TIME else Finances.parseTradeDate(date)
        }
    }

    @CheckResult
    fun ticker(): String {
        return symbol.orEmpty()
    }

    @CheckResult
    fun high(): Float {
        return high ?: Finances.INVALID_PRICE
    }

    @CheckResult
    fun low(): Float {
        return low ?: Finances.INVALID_PRICE
    }

    @CheckResult
    fun open(): Float {
        return open ?: Finances.INVALID_PRICE
    }

    @CheckResult
    fun close(): Float {
        return close ?: Finances.INVALID_PRICE
    }

    @CheckResult
    fun volume(): Long {
        return volume ?: Finances.INVALID_VOLUME
    }

}
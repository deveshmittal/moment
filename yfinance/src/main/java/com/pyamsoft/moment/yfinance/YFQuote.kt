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

package com.pyamsoft.moment.yfinance

import androidx.annotation.CheckResult
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YFQuote internal constructor(
    internal val symbol: String?,
    internal val exchange: String?,
    internal val currency: String?,
    internal val isPostMarket: Boolean?,
    @Json(name = "shortName") internal val name: String?,
    @Json(name = "regularMarketPrice") internal val lastTradePrice: Float?,
    @Json(name = "regularMarketChangePercent") internal val changePercent: Float?,
    @Json(name = "regularMarketChange") internal val change: Float?,
    @Json(name = "regularMarketTime") internal val marketTime: Int?,
    @Json(name = "postMarketTime") internal val postMarketTime: Int?,
    @Json(name = "postMarketPrice") internal val postTradePrice: Float?,
    @Json(name = "postMarketChangePercent") internal val postChangePercent: Float?,
    @Json(name = "postMarketChange") internal val postChange: Float?,
    @Json(name = "trailingAnnualDividendRate") internal val annualDividendRate: Float?,
    @Json(name = "trailingAnnualDividendYield") internal val annualDividendYield: Float?
) {

    @CheckResult
    fun symbol(): String {
        return symbol.orEmpty()
    }

    @CheckResult
    fun exchange(): String {
        return exchange.orEmpty()
    }

    @CheckResult
    fun currency(): String {
        return currency.orEmpty()
    }

    @CheckResult
    fun isPostMarket(): Boolean {
        return isPostMarket ?: false
    }

    @CheckResult
    fun name(): String {
        return name.orEmpty()
    }

    @CheckResult
    fun lastTradePrice(): Float {
        return lastTradePrice ?: 0F
    }

    @CheckResult
    fun changePercent(): Float {
        return changePercent ?: 0F
    }

    @CheckResult
    fun change(): Float {
        return change ?: 0F
    }

    @CheckResult
    fun marketTime(): Int {
        return marketTime ?: 0
    }

    @CheckResult
    fun postMarketTime(): Int {
        return postMarketTime ?: 0
    }

    @CheckResult
    fun postTradePrice(): Float {
        return postTradePrice ?: 0F
    }

    @CheckResult
    fun postChangePercent(): Float {
        return postChangePercent ?: 0F
    }

    @CheckResult
    fun postChange(): Float {
        return postChange ?: 0F
    }

    @CheckResult
    fun annualDividendRate(): Float {
        return annualDividendRate ?: 0F
    }

    @CheckResult
    fun annualDividendYield(): Float {
        return annualDividendYield ?: 0F
    }

    // Needed to generate static adapter
    companion object
}
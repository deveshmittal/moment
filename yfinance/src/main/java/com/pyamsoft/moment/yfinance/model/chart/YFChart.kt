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

package com.pyamsoft.moment.yfinance.model.chart

import androidx.annotation.CheckResult
import com.pyamsoft.moment.yfinance.model.ValidResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YFChart internal constructor(
    internal val meta: Meta?,
    internal val timestamps: List<Long>?,
    internal val indicators: Indicators?
) : ValidResponse {

    override fun isValid(): Boolean {
        return meta != null && timestamps != null && indicators != null
    }

    @CheckResult
    fun meta(): Meta {
        return meta ?: Meta.empty()
    }

    @CheckResult
    fun timestamps(): List<Long> {
        return timestamps ?: emptyList()
    }

    @CheckResult
    fun indicators(): Indicators {
        return indicators ?: Indicators.empty()
    }

    // Needed for adapter generation
    companion object

    @JsonClass(generateAdapter = true)
    data class Meta internal constructor(
        internal val symbol: String?,
        internal val currency: String?,
        internal val exchangeName: String?,
        internal val instrumentType: String?,
        internal val firstTradeDate: Long?,
        internal val regularMarketTime: Long?,
        internal val regularMarketPrice: Double?,
        internal val chartPreviousClose: Double?,
        internal val priceHint: Long?,
        internal val currentTradingPeriod: CurrentTradingPeriod?,
        internal val dataGranularity: String?,
        internal val range: String?,
        internal val validRanges: List<String>?,
        @Json(name = "exchangeTimezoneName") internal val exchangeTimeZoneName: String?,
        @Json(name = "timezone") internal val timeZone: String?,
        @Json(name = "gmtoffset") internal val gmtOffset: Long?
    ) : ValidResponse {

        override fun isValid(): Boolean {
            return symbol != null
        }

        @CheckResult
        fun symbol(): String {
            return symbol.orEmpty()
        }

        @CheckResult
        fun currency(): String {
            return currency.orEmpty()
        }

        @CheckResult
        fun exchangeName(): String {
            return exchangeName.orEmpty()
        }

        @CheckResult
        fun instrumentType(): String {
            return instrumentType.orEmpty()
        }

        @CheckResult
        fun firstTradeDate(): Long {
            return firstTradeDate ?: 0
        }

        @CheckResult
        fun regularMarketTime(): Long {
            return regularMarketTime ?: 0
        }

        @CheckResult
        fun regularMarketPrice(): Double {
            return regularMarketPrice ?: 0.0
        }

        @CheckResult
        fun chartPreviousClose(): Double {
            return chartPreviousClose ?: 0.0
        }

        @CheckResult
        fun priceHint(): Long {
            return priceHint ?: 0
        }

        @CheckResult
        fun currentTradingPeriod(): CurrentTradingPeriod {
            return currentTradingPeriod ?: CurrentTradingPeriod.empty()
        }

        @CheckResult
        fun dataGranularity(): String {
            return dataGranularity.orEmpty()
        }

        @CheckResult
        fun range(): String {
            return range.orEmpty()
        }

        @CheckResult
        fun validRanges(): List<String> {
            return validRanges ?: emptyList()
        }

        @CheckResult
        fun timeZone(): String {
            return timeZone.orEmpty()
        }

        @CheckResult
        fun exchangeTimeZoneName(): String {
            return exchangeTimeZoneName.orEmpty()
        }

        @CheckResult
        fun gmtOffset(): Long {
            return gmtOffset ?: 0
        }

        companion object {

            @JvmStatic
            @CheckResult
            internal fun empty(): Meta {
                return Meta(
                    currency = null,
                    symbol = null,
                    exchangeName = null,
                    instrumentType = null,
                    firstTradeDate = null,
                    regularMarketTime = null,
                    timeZone = null,
                    exchangeTimeZoneName = null,
                    regularMarketPrice = null,
                    chartPreviousClose = null,
                    priceHint = null,
                    currentTradingPeriod = null,
                    dataGranularity = null,
                    range = null,
                    validRanges = null,
                    gmtOffset = null
                )
            }
        }


        @JsonClass(generateAdapter = true)
        data class CurrentTradingPeriod internal constructor(
            internal val pre: TradingPeriod?,
            internal val regular: TradingPeriod?,
            internal val post: TradingPeriod?
        ) : ValidResponse {

            override fun isValid(): Boolean {
                return regular != null
            }

            @CheckResult
            fun pre(): TradingPeriod {
                return pre ?: TradingPeriod.empty()
            }

            @CheckResult
            fun regular(): TradingPeriod {
                return regular ?: TradingPeriod.empty()
            }

            @CheckResult
            fun post(): TradingPeriod {
                return post ?: TradingPeriod.empty()
            }

            companion object {

                @JvmStatic
                @CheckResult
                internal fun empty(): CurrentTradingPeriod {
                    return CurrentTradingPeriod(
                        pre = null,
                        regular = null,
                        post = null
                    )
                }
            }

            @JsonClass(generateAdapter = true)
            data class TradingPeriod internal constructor(
                internal val start: Long?,
                internal val end: Long?,
                @Json(name = "timezone") internal val timeZone: String?,
                @Json(name = "gmtoffset") internal val gmtOffset: Long?
            ) : ValidResponse {

                override fun isValid(): Boolean {
                    return start != null
                }

                @CheckResult
                fun start(): Long {
                    return start ?: 0
                }

                @CheckResult
                fun end(): Long {
                    return end ?: 0
                }

                @CheckResult
                fun timeZone(): String {
                    return timeZone.orEmpty()
                }

                @CheckResult
                fun gmtOffset(): Long {
                    return gmtOffset ?: 0
                }

                companion object {

                    @JvmStatic
                    @CheckResult
                    internal fun empty(): TradingPeriod {
                        return TradingPeriod(
                            start = null,
                            end = null,
                            timeZone = null,
                            gmtOffset = null
                        )
                    }

                }

            }

        }
    }

    @JsonClass(generateAdapter = true)
    data class Indicators internal constructor(
        @Json(name = "quote") internal val quotes: List<DataQuote>?,
        @Json(name = "adjclose") internal val adjustedClose: List<AdjClose>?
    ) : ValidResponse {

        override fun isValid(): Boolean {
            return quotes != null
        }

        @CheckResult
        fun quotes(): List<DataQuote> {
            return quotes ?: emptyList()
        }

        @CheckResult
        fun adjustedClose(): List<AdjClose> {
            return adjustedClose ?: emptyList()
        }

        companion object {

            @JvmStatic
            @CheckResult
            internal fun empty(): Indicators {
                return Indicators(
                    quotes = null,
                    adjustedClose = null
                )
            }
        }

        @JsonClass(generateAdapter = true)
        data class DataQuote internal constructor(
            internal val open: List<Double>?,
            internal val close: List<Double>?,
            internal val low: List<Double>?,
            internal val volume: List<Long>?,
            internal val high: List<Double>?
        ) : ValidResponse {

            override fun isValid(): Boolean {
                return volume != null
            }

            @CheckResult
            fun open(): List<Double> {
                return open ?: emptyList()
            }

            @CheckResult
            fun close(): List<Double> {
                return close ?: emptyList()
            }

            @CheckResult
            fun low(): List<Double> {
                return low ?: emptyList()
            }

            @CheckResult
            fun high(): List<Double> {
                return high ?: emptyList()
            }

            @CheckResult
            fun volume(): List<Long> {
                return volume ?: emptyList()
            }

            // Needed for adapter generation
            companion object

        }

        @JsonClass(generateAdapter = true)
        data class AdjClose internal constructor(
            @Json(name = "adjclose") internal val adjustedClose: List<Double>?
        ) : ValidResponse {

            override fun isValid(): Boolean {
                return adjustedClose != null
            }

            @CheckResult
            fun adjustedClose(): List<Double> {
                return adjustedClose ?: emptyList()
            }

            // Needed for adapter generation
            companion object
        }


    }

}


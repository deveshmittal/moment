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

package com.pyamsoft.moment.finance.tiingo.model

import android.annotation.SuppressLint
import androidx.annotation.CheckResult
import com.pyamsoft.moment.core.threadLocal
import com.pyamsoft.moment.finance.model.Finances
import com.pyamsoft.moment.finance.model.Price
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.moment.finance.model.toSymbol
import com.squareup.moshi.JsonClass
import java.text.SimpleDateFormat
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class TiingoIexEvent internal constructor(
    internal val data: Array<Any?>?
) {

    @Transient
    private val messageType: MessageType

    @Transient
    private val lastTradeDate: Date

    @Transient
    private val symbol: Symbol

    @Transient
    private val lastTradePrice: Float

    // Mapping
    @Transient
    private val price: Price

    init {
        messageType = data?.get(0).let { raw ->
            if (raw == null || raw !is String) {
                MessageType.NONE
            } else {
                MessageType.valueOf(raw)
            }
        }

        lastTradeDate = data?.get(1).let { raw ->
            if (raw == null || raw !is String) {
                Finances.INVALID_TIME
            } else {
                parseISODate(raw)
            }
        }

        symbol = data?.get(3).let { raw ->
            if (raw == null || raw !is String) {
                Symbol.empty()
            } else {
                raw.toSymbol()
            }
        }

        lastTradePrice = data?.get(9).let { raw ->
            if (raw == null || raw !is Float) {
                Finances.INVALID_PRICE
            } else {
                raw
            }
        }

        price = Price(
            symbol = symbol,
            lastTrade = lastTradeDate,
            price = lastTradePrice,
            previousClose = Finances.INVALID_PRICE
        )
    }

    @CheckResult
    fun isValid(): Boolean {
        return messageType != MessageType.NONE && symbol().isValid()
    }

    @CheckResult
    fun type(): MessageType {
        return messageType
    }

    @CheckResult
    fun lastTrade(): Date {
        return lastTradeDate
    }

    @CheckResult
    fun symbol(): Symbol {
        return symbol
    }

    @CheckResult
    fun asPrice(): Price {
        return price
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }

        if (javaClass != other?.javaClass) {
            return false
        }

        if (other is TiingoIexEvent) {
            if (data != null) {
                if (other.data == null) {
                    return false
                }
                if (!data.contentEquals(other.data)) {
                    return false
                }
            } else if (other.data != null) {
                return false
            }

            return true
        }

        return false
    }

    override fun hashCode(): Int {
        return data?.contentHashCode() ?: 0
    }

    enum class MessageType {
        NONE,
        TRADE,
        QUOTE,
        BOOK
    }

    companion object {

        @SuppressLint("SimpleDateFormat")
        private val isoFormatter = threadLocal { SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ") }

        @CheckResult
        private fun parseISODate(date: String): Date {
            return requireNotNull(isoFormatter.get()).parse(date) ?: Finances.INVALID_TIME
        }

    }
}

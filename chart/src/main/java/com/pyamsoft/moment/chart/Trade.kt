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

import android.annotation.SuppressLint
import androidx.annotation.CheckResult
import com.pyamsoft.moment.core.threadLocal
import com.pyamsoft.moment.finance.model.Finances
import com.pyamsoft.moment.finance.model.Price
import java.text.SimpleDateFormat

data class Trade internal constructor(val price: String, val time: String) {

    companion object {

        @JvmStatic
        @CheckResult
        fun empty(): Trade {
            return Trade(EMPTY_TRADE, EMPTY_TIME)
        }
    }
}

@CheckResult
fun Price.toTrade(): Trade {
    val price = this.price.let { p ->
        if (p == Finances.INVALID_PRICE) {
            EMPTY_TRADE
        } else {
            PRICE_FORMAT_STRING.format(p)
        }
    }
    val time = this.lastTrade.let { t ->
        if (t == Finances.INVALID_TIME) {
            EMPTY_TIME
        } else {
            requireNotNull(tradeTimeFormatter.get()).format(t)
        }
    }
    return Trade(price, time)
}

@SuppressLint("SimpleDateFormat")
private val tradeTimeFormatter = threadLocal { SimpleDateFormat("HH:mm:ss MM/dd/yyyy") }

private const val EMPTY_TIME = "--:-- --/--/----"
private const val EMPTY_TRADE = "-.--"
private const val PRICE_FORMAT_STRING = "%.2f"

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
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class YFQuoteResponse internal constructor(
    internal val result: List<YFQuote>?
) {

    @CheckResult
    fun quotes(): List<YFQuote> {
        return result ?: emptyList()
    }

    // Needed to generate static adapter
    companion object {

        @JvmStatic
        @CheckResult
        internal fun empty(): YFQuoteResponse {
            return YFQuoteResponse(null)
        }
    }
}
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
import com.pyamsoft.moment.yfinance.model.YFResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val QUOTE_BASE_URL = "v7/finance/quote"
private const val CHART_BASE_URL = "v8/finance/chart"

interface YFinance {

    @CheckResult
    @GET("${QUOTE_BASE_URL}?format=json&symbols=^IXIC")
    suspend fun getNasdaqCompositeIndex(): YFResponse

    @CheckResult
    @GET("${QUOTE_BASE_URL}?format=json")
    suspend fun getTickers(@Query("symbols", encoded = true) symbols: String): YFResponse

    @CheckResult
    @GET("${CHART_BASE_URL}/{symbol}")
    suspend fun getChart(
        @Path("symbol", encoded = true) symbol: String,
        @Query("includePrePost", encoded = true) includePrePost: Boolean,
        @Query("interval", encoded = true) interval: String,
        @Query("range", encoded = true) range: String
    ): YFResponse

}

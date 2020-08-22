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

package com.pyamsoft.moment.finance.tiingo

import androidx.annotation.CheckResult
import com.pyamsoft.moment.finance.tiingo.model.TiingoEodPrice
import com.pyamsoft.moment.finance.tiingo.model.TiingoInfo
import com.pyamsoft.moment.finance.tiingo.model.TiingoQuote
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

/**
 * These functions closely mirror those in Tiingo with an additional header parameter
 */
internal interface TiingoService {

    @Streaming
    @CheckResult
    @GET("https://apimedia.tiingo.com/docs/tiingo/daily/supported_tickers.zip")
    suspend fun tickers(): ResponseBody

    @CheckResult
    @GET("iex/{ticker}")
    @Headers("Content-Type: application/json")
    suspend fun quote(
        @Path("ticker") ticker: String,
        @Header("Authorization") token: String
    ): List<TiingoQuote>

    @CheckResult
    @GET("tiingo/daily/{ticker}/prices")
    @Headers("Content-Type: application/json")
    suspend fun eod(
        @Path("ticker") ticker: String,
        @Query("startDate") startDate: String?,
        @Header("Authorization") token: String
    ): List<TiingoEodPrice>

    @CheckResult
    @GET("tiingo/daily/{ticker}")
    @Headers("Content-Type: application/json")
    suspend fun info(
        @Path("ticker") ticker: String,
        @Header("Authorization") token: String
    ): TiingoInfo
}

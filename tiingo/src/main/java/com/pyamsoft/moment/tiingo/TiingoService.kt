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

package com.pyamsoft.moment.tiingo

import androidx.annotation.CheckResult
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

/**
 * These functions closely mirror those in Tiingo with an additional header parameter
 */
internal interface TiingoService {

    @CheckResult
    @GET("iex/{symbol}")
    @Headers("Content-Type: application/json")
    suspend fun quote(@Path("symbol") symbol: String, @Header("Authorization") token: String): Any

    @CheckResult
    @GET("tiingo/daily/{symbol}/prices")
    @Headers("Content-Type: application/json")
    suspend fun eod(@Path("symbol") symbol: String, @Header("Authorization") token: String): Any

    @CheckResult
    @GET("tiingo/daily/{symbol}")
    @Headers("Content-Type: application/json")
    suspend fun info(@Path("symbol") symbol: String, @Header("Authorization") token: String): Any
}
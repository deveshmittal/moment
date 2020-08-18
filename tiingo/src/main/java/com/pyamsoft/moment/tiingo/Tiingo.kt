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
import com.pyamsoft.pydroid.core.Enforcer
import javax.inject.Inject
import javax.inject.Singleton

/**
 * These functions closely mirror those in TiingoService without the header parameter
 */
@Singleton
class Tiingo @Inject internal constructor(
    private val service: TiingoService,
    private val auth: TiingoAuth
) {

    @CheckResult
    suspend fun quote(symbol: String): Any {
        Enforcer.assertOffMainThread()
        return service.quote(auth.getApiToken(), symbol)
    }

    @CheckResult
    suspend fun eod(symbol: String): Any {
        Enforcer.assertOffMainThread()
        return service.eod(auth.getApiToken(), symbol)
    }

    @CheckResult
    suspend fun info(symbol: String): Any {
        Enforcer.assertOffMainThread()
        return service.info(auth.getApiToken(), symbol)
    }

}
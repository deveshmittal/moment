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

data class Meta(
    internal val symbol: String?,
    internal val name: String?,
    internal val exchange: String?,
    internal val description: String?,
    internal val startDate: String?,
    internal val endDate: String?
) {

    private val companyStartDate: Date
    private val companyEndDate: Date

    init {
        companyStartDate = startDate.let { date ->
            if (date == null) Finances.INVALID_TIME else Finances.parseInfoDate(date)
        }

        companyEndDate = endDate.let { date ->
            if (date == null) Finances.INVALID_TIME else Finances.parseInfoDate(date)
        }
    }

    @CheckResult
    fun ticker(): String {
        return symbol.orEmpty()
    }

    @CheckResult
    fun name(): String {
        return name.orEmpty()
    }

    @CheckResult
    fun exchange(): String {
        return exchange.orEmpty()
    }

    @CheckResult
    fun description(): String {
        return description.orEmpty()
    }


}
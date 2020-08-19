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

import android.annotation.SuppressLint
import androidx.annotation.CheckResult
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

object Finances {

    private val tradeDateFormatter = object : ThreadLocal<SimpleDateFormat>() {

        @SuppressLint("SimpleDateFormat")
        override fun initialValue(): SimpleDateFormat? {
            return SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
        }

    }

    private val infoDateFormatter = object : ThreadLocal<SimpleDateFormat>() {

        @SuppressLint("SimpleDateFormat")
        override fun initialValue(): SimpleDateFormat? {
            return SimpleDateFormat("yyyy-MM-dd")
        }

    }

    @JvmStatic
    @CheckResult
    internal fun parseTradeDate(date: String): Date {
        return requireNotNull(tradeDateFormatter.get()).parse(date) ?: INVALID_TIME
    }

    @JvmStatic
    @CheckResult
    internal fun parseInfoDate(date: String): Date {
        return requireNotNull(infoDateFormatter.get()).parse(date) ?: INVALID_TIME
    }

    @JvmField
    val INVALID_TIME: Date = Calendar.getInstance().apply { timeInMillis = 0 }.time

    const val INVALID_PRICE = -1F
    const val INVALID_VOLUME = -1L
}
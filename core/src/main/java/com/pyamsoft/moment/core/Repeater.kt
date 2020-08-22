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

package com.pyamsoft.moment.core

import android.os.Handler
import android.os.Looper
import androidx.annotation.CheckResult

interface Repeater {

    fun start()

    fun stop()
}

private data class RepeaterImpl internal constructor(
    private val period: Long,
    private val instant: Boolean,
    private val onRepeat: () -> Unit
) : Repeater {

    private val handler by lazy { Handler(Looper.getMainLooper()) }

    override fun start() {
        if (instant) {
            onRepeat()
        }

        val repeatRunnable = object : Runnable {
            override fun run() {
                handler.postDelayed(this, period)
                onRepeat()
            }
        }

        handler.postDelayed(repeatRunnable, period)
    }

    override fun stop() {
        handler.removeCallbacksAndMessages(null)
    }

}

@CheckResult
@JvmOverloads
fun repeater(period: Long, instant: Boolean = false, onRepeat: () -> Unit): Repeater {
    return RepeaterImpl(period, instant, onRepeat)
}

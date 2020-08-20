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

import androidx.lifecycle.viewModelScope
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.moment.finance.toMonthRange
import com.pyamsoft.pydroid.arch.UiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Named

class ChartViewModel @Inject internal constructor(
    private val interactor: ChartInteractor,
    symbol: Symbol,
    @Named("debug") debug: Boolean
) : UiViewModel<ChartViewState, ChartViewEvent, ChartControllerEvent>(
    initialState = ChartViewState(
        symbol = symbol,
        range = 1.toMonthRange(),
        dataPoints = emptyList()
    ), debug = debug
) {

    init {
        doOnInit {
            fetchChart()
        }
    }

    private fun fetchChart() {
        withState {
            viewModelScope.launch(context = Dispatchers.Default) {
                val dataPoints = interactor.getChart(symbol, range)
                setState { copy(dataPoints = dataPoints) }
            }
        }
    }

    override fun handleViewEvent(event: ChartViewEvent) {
    }

}
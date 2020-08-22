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
import com.pyamsoft.highlander.highlander
import com.pyamsoft.moment.core.repeater
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.moment.finance.toYearRange
import com.pyamsoft.pydroid.arch.UiViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named

class ChartViewModel @Inject internal constructor(
    private val interactor: ChartInteractor,
    symbol: Symbol,
    @Named("debug") debug: Boolean
) : UiViewModel<ChartViewState, ChartViewEvent, ChartControllerEvent>(
    initialState = ChartViewState(
        symbol = symbol,
        range = 1.toYearRange(),
        dataPoints = emptyList(),
        lastTrade = Trade.empty(),
        isUpdating = false
    ), debug = debug
) {

    private val periodicPriceUpdater = repeater(TimeUnit.SECONDS.toMillis(15L), instant = true) {
        updatePrices()
    }

    private val symbolPriceUpdater = highlander<Trade, Symbol> { symbol ->
        Timber.d("Updating prices for $symbol")
        val trade = interactor.getMostRecentTrade(symbol)
        Timber.d("Most recent trade for $symbol -> $trade")
        return@highlander trade
    }

    init {
        doOnInit {
            fetchChart()
        }

        doOnInit {
            updatePrices()
        }

        doOnTeardown {
            periodicPriceUpdater.stop()
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

    private fun updatePrices() {
        withState {
            viewModelScope.launch(context = Dispatchers.Default) {
                val trade = symbolPriceUpdater.call(symbol)
                setState { copy(lastTrade = trade) }
            }
        }
    }

    override fun handleViewEvent(event: ChartViewEvent) {
        return when (event) {
            is ChartViewEvent.ToggleUpdates -> togglePriceUpdates(event.on)
        }
    }

    private fun togglePriceUpdates(on: Boolean) {
        if (on) {
            periodicPriceUpdater.start()
        } else {
            periodicPriceUpdater.stop()
        }
    }

}

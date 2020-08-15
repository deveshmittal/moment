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

import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.core.view.updateLayoutParams
import com.pyamsoft.moment.chart.databinding.ChartviewBinding
import com.pyamsoft.pydroid.arch.BaseUiView
import javax.inject.Inject

class ChartView @Inject internal constructor(
    parent: ViewGroup
) : BaseUiView<ChartViewState, ChartViewEvent, ChartviewBinding>(parent) {

    override val viewBinding = ChartviewBinding::inflate

    override val layoutRoot by boundView { chartviewRoot }

    private var adapter: MomentChartAdapter? = null

    init {
        doOnInflate {
            adapter = MomentChartAdapter().also { it.bindTo(binding.chartviewChart) }
        }

        doOnTeardown {
            binding.chartviewChart.adapter = null
            adapter?.submitList(null)
            adapter = null
        }
    }

    @CheckResult
    private fun usingAdapter(): MomentChartAdapter {
        return requireNotNull(adapter)
    }

    override fun onRender(state: ChartViewState) {
        handleDataPoints(state.dataPoints)
    }

    private fun handleDataPoints(dataPoints: List<ChartDataPoint>) {
        usingAdapter().submitList(dataPoints)
    }

    fun updateLayoutDimensions(width: Int, height: Int) {
        binding.root.updateLayoutParams {
            this.width = width
            this.height = height
        }
    }

}

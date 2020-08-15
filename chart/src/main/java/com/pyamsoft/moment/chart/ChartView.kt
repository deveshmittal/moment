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
import com.pyamsoft.moment.chart.databinding.ChartviewBinding
import com.pyamsoft.pydroid.arch.BaseUiView
import com.robinhood.spark.SparkAdapter
import javax.inject.Inject

class ChartView @Inject internal constructor(
    parent: ViewGroup
) : BaseUiView<ChartViewState, ChartViewEvent, ChartviewBinding>(parent) {

    override val viewBinding = ChartviewBinding::inflate

    override val layoutRoot by boundView { chartviewRoot }

    private var adapter: MyAdapter? = null

    init {
        doOnInflate {
            val newAdapter = MyAdapter()
            binding.chartviewChart.adapter = newAdapter
            adapter = newAdapter
        }

        doOnTeardown {
            binding.chartviewChart.adapter = null
            adapter?.submitList(null)
            adapter = null
        }
    }

    @CheckResult
    private fun usingAdapter(): MyAdapter {
        return requireNotNull(adapter)
    }

    override fun onRender(state: ChartViewState) {
        handleDataPoints(state.dataPoints)
    }

    private fun handleDataPoints(dataPoints: List<ChartViewState.DataPoint>) {
        usingAdapter().submitList(dataPoints)
    }

    private class MyAdapter : SparkAdapter() {

        private var data: List<ChartViewState.DataPoint> = emptyList()

        fun submitList(newData: List<ChartViewState.DataPoint>?) {
            data = newData ?: emptyList()
            notifyDataSetChanged()
        }

        override fun getY(index: Int): Float {
            return getItem(index).y
        }

        override fun getItem(index: Int): ChartViewState.DataPoint {
            return data[index]
        }

        override fun getCount(): Int {
            return data.size
        }

    }
}

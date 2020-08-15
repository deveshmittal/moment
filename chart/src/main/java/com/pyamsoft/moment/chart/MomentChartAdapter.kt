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

import com.robinhood.spark.SparkAdapter
import com.robinhood.spark.SparkView

internal class MomentChartAdapter internal constructor() : MomentAdapter {

    private val adapter = object : SparkAdapter(), MomentAdapter {

        private var data: List<ChartDataPoint> = emptyList()

        override fun submitList(newData: List<ChartDataPoint>?) {
            data = newData ?: emptyList()
            notifyDataSetChanged()
        }

        override fun getY(index: Int): Float {
            return getItem(index).y
        }

        override fun getItem(index: Int): ChartDataPoint {
            return data[index]
        }

        override fun getCount(): Int {
            return data.size
        }
    }

    override fun submitList(newData: List<ChartDataPoint>?) {
        adapter.submitList(newData)
    }

    fun bindTo(chartView: SparkView) {
        chartView.adapter = adapter
    }

}

internal interface MomentAdapter {

    fun submitList(newData: List<ChartDataPoint>?)

}

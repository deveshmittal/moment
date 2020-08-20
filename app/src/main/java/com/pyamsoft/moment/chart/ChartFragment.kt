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

import android.app.Activity
import android.graphics.Point
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.CheckResult
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.getSystemService
import androidx.fragment.app.Fragment
import com.pyamsoft.moment.MomentComponent
import com.pyamsoft.moment.core.MomentViewModelFactory
import com.pyamsoft.moment.finance.model.Symbol
import com.pyamsoft.moment.finance.model.toSymbol
import com.pyamsoft.pydroid.arch.StateSaver
import com.pyamsoft.pydroid.arch.createComponent
import com.pyamsoft.pydroid.ui.Injector
import com.pyamsoft.pydroid.ui.arch.viewModelFactory
import com.pyamsoft.pydroid.ui.databinding.LayoutConstraintBinding
import com.pyamsoft.pydroid.ui.util.layout
import javax.inject.Inject

class ChartFragment : Fragment() {

    @Inject
    @JvmField
    internal var chart: ChartView? = null

    @Inject
    @JvmField
    internal var factory: MomentViewModelFactory? = null
    private val viewModel by viewModelFactory<ChartViewModel> { factory }

    private var stateSaver: StateSaver? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_constraint, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val symbol = requireArguments().getString(KEY_SYMBOL).toSymbol()
        val binding = LayoutConstraintBinding.bind(view)
        Injector.obtain<MomentComponent>(view.context.applicationContext)
            .plusChartComponent()
            .create(binding.layoutConstraint, symbol)
            .inject(this)

        val chart = requireNotNull(chart)

        stateSaver = createComponent(
            savedInstanceState,
            viewLifecycleOwner,
            viewModel,
            chart
        ) {
            // TODO(Peter): Handle chart controller event
        }

        // Once measured, the chart will animate in height
        measureChart(chart)

        binding.layoutConstraint.layout {
            chart.also {
                connect(it.id(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                connect(it.id(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                connect(it.id(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
            }
        }
    }

    @CheckResult
    private fun getWindowHeight(activity: Activity): Int {
        val windowManager = requireNotNull(activity.getSystemService<WindowManager>())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            windowManager.currentWindowMetrics.bounds.bottom
        } else {
            val point = Point()

            @Suppress("DEPRECATION")
            windowManager.defaultDisplay.getSize(point)

            point.y
        }
    }

    // Make the chart occupy 1/4 of the screen
    // We use the total window height instead of just getting the Fragment.view.height
    // so that when we are in multi window, the chart is still the same size as in fullscreen
    private fun measureChart(chart: ChartView) {
        val chartHeight = getWindowHeight(requireActivity()) / 4
        chart.updateLayoutDimensions(ViewGroup.LayoutParams.MATCH_PARENT, chartHeight)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        stateSaver?.saveState(outState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stateSaver = null

        factory = null
        chart = null
    }

    companion object {

        const val TAG = "ChartFragment"

        private const val KEY_SYMBOL = "key_symbol"

        @JvmStatic
        @CheckResult
        fun newInstance(symbol: Symbol): Fragment {
            return ChartFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_SYMBOL, symbol.symbol())
                }
            }
        }
    }

}
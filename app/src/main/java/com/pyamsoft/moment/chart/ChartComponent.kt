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
import android.view.ViewGroup
import androidx.annotation.CheckResult
import androidx.lifecycle.ViewModelProvider
import com.pyamsoft.moment.ThemeProviderModule
import com.pyamsoft.moment.core.MomentViewModelFactory
import com.pyamsoft.pydroid.arch.UiViewModel
import com.pyamsoft.pydroid.ui.app.ToolbarActivityProvider
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

@Subcomponent(modules = [ChartComponent.ViewModelModule::class, ThemeProviderModule::class])
internal interface ChartComponent {

    fun inject(fragment: ChartFragment)

    @Subcomponent.Factory
    interface Factory {

        @CheckResult
        fun create(@BindsInstance parent: ViewGroup): ChartComponent
    }

    @Module
    abstract class ViewModelModule {

        @Binds
        internal abstract fun bindViewModelFactory(factory: MomentViewModelFactory): ViewModelProvider.Factory

        @Binds
        @IntoMap
        @ClassKey(ChartViewModel::class)
        internal abstract fun viewModel(viewModel: ChartViewModel): UiViewModel<*, *, *>
    }
}


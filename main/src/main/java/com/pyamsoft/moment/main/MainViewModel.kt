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

package com.pyamsoft.moment.main

import com.pyamsoft.pydroid.arch.UiViewModel
import javax.inject.Inject
import javax.inject.Named

class MainViewModel @Inject internal constructor(
    @Named("app_name") appNameRes: Int,
    @Named("debug") debug: Boolean
) : UiViewModel<MainViewState, MainViewEvent, MainControllerEvent>(
    initialState = MainViewState(appNameRes = appNameRes), debug = debug
) {

    private var versionChecked: Boolean = false

    override fun handleViewEvent(event: MainViewEvent) {
        TODO("Handle MainViewEvent: $event")
    }

    // Called from Fragment upon initialization
    fun checkForUpdates() {
        withState {
            if (!versionChecked) {
                versionChecked = true
                publish(MainControllerEvent.VersionCheck)
            }
        }
    }
}

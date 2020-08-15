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

package com.pyamsoft.moment

import android.app.Application
import com.pyamsoft.moment.core.PRIVACY_POLICY_URL
import com.pyamsoft.moment.core.TERMS_CONDITIONS_URL
import com.pyamsoft.moment.main.MainActivity
import com.pyamsoft.pydroid.bootstrap.libraries.OssLibraries
import com.pyamsoft.pydroid.ui.PYDroid
import com.pyamsoft.pydroid.util.isDebugMode
import com.squareup.moshi.Moshi

class Moment : Application() {

    private var component: MomentComponent? = null

    override fun onCreate() {
        super.onCreate()
        val url = "https://github.com/pyamsoft/moment"
        val parameters = PYDroid.Parameters(
            url,
            "$url/issues",
            PRIVACY_POLICY_URL,
            TERMS_CONDITIONS_URL,
            BuildConfig.VERSION_CODE
        )
        PYDroid.init(this, parameters) { provider ->
            component = DaggerMomentComponent.factory().create(this, isDebugMode())
            onInitialized()
        }
    }

    private fun onInitialized() {
        addLibraries()
    }

    private fun addLibraries() {
        // We are using pydroid-notify
        OssLibraries.usingNotify = true

        OssLibraries.add(
            "WorkManager",
            "https://android.googlesource.com/platform/frameworks/support/+/androidx-master-dev/work/",
            "The AndroidX Jetpack WorkManager library. Schedule periodic work in a device friendly way."
        )
        OssLibraries.add(
            "Dagger",
            "https://github.com/google/dagger",
            "A fast dependency injector for Android and Java."
        )
        OssLibraries.add(
            "FastAdapter",
            "https://github.com/mikepenz/fastadapter",
            "The bullet proof, fast and easy to use adapter library, which minimizes developing time to a fraction..."
        )
    }

    override fun getSystemService(name: String): Any? {
        val service = PYDroid.getSystemService(name)
        if (service != null) {
            return service
        }

        return if (name == MomentComponent::class.java.name) component else {
            super.getSystemService(name)
        }
    }
}

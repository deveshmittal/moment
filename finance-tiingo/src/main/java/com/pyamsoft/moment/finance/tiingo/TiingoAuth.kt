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

package com.pyamsoft.moment.finance.tiingo

import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TiingoAuth @Inject internal constructor(
    private val context: Context
) {

    private var token: String? = null
    private val mutex = Mutex()

    @CheckResult
    private fun getTokenFromManifest(): String {
        val appContext = context.applicationContext
        val packageName = appContext.packageName
        val pm = appContext.packageManager
        val applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        return requireNotNull(applicationInfo.metaData.getString("TIINGO_API_KEY", ""))
    }

    @CheckResult
    suspend fun getApiToken(): String {
        Enforcer.assertOffMainThread()

        if (token == null) {
            mutex.withLock {
                if (token == null) {
                    token = getTokenFromManifest()
                }
            }
        }

        return requireNotNull(token)
    }

}

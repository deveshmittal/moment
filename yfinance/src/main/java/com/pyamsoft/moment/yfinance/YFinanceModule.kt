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
 *
 */

package com.pyamsoft.moment.yfinance

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.bootstrap.network.DelegatingSocketFactory
import com.pyamsoft.pydroid.core.Enforcer
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Named
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
private annotation class InternalScope

@Module
class YFinanceModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        @InternalScope
        @CheckResult
        fun provideMoshi(): Moshi {
            return Moshi.Builder().build()
        }

        @Provides
        @JvmStatic
        @InternalScope
        @CheckResult
        fun provideCallFactory(@Named("debug") debug: Boolean): Call.Factory {
            return OkHttpClientLazyCallFactory {
                OkHttpClient.Builder()
                    .socketFactory(DelegatingSocketFactory.create())
                    .also {
                        if (debug) {
                            val logging = HttpLoggingInterceptor()
                            logging.level = HttpLoggingInterceptor.Level.BODY
                            it.addInterceptor(logging)
                        }
                    }
                    .build()
            }
        }

        @Provides
        @JvmStatic
        @InternalScope
        @CheckResult
        fun provideConverterFactory(@InternalScope moshi: Moshi): Converter.Factory {
            return MoshiConverterFactory.create(moshi)
        }

        @Provides
        @JvmStatic
        @InternalScope
        @CheckResult
        fun createRetrofit(
            @InternalScope callFactory: Call.Factory,
            @InternalScope converterFactory: Converter.Factory
        ): Retrofit {
            return Retrofit.Builder()
                .baseUrl("https://query1.finance.yahoo.com/v7/finance/")
                .callFactory(callFactory)
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @JvmStatic
        @Singleton
        @CheckResult
        fun createYFinanceService(@InternalScope retrofit: Retrofit): YFinance {
            return retrofit.create(YFinance::class.java)
        }

        private class OkHttpClientLazyCallFactory(
            provider: () -> OkHttpClient
        ) : Call.Factory {

            private val client by lazy { provider() }

            override fun newCall(request: Request): Call {
                Enforcer.assertOffMainThread()
                return client.newCall(request)
            }
        }
    }
}

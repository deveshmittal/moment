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
import com.squareup.moshi.Moshi
import dagger.Lazy
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
private annotation class InternalApi

@Module
class YFinanceModule {

    @Module
    companion object {

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideMoshi(): Moshi {
            return Moshi.Builder().build()
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideOkHttpClient(@Named("debug") debug: Boolean): OkHttpClient {
            return OkHttpClient.Builder()
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

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideCallFactory(@InternalApi client: Lazy<OkHttpClient>): Call.Factory {
            return object : Call.Factory {

                override fun newCall(request: Request): Call {
                    return client.get().newCall(request)
                }
            }
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun provideConverterFactory(@InternalApi moshi: Moshi): Converter.Factory {
            return MoshiConverterFactory.create(moshi)
        }

        @Provides
        @JvmStatic
        @InternalApi
        internal fun createRetrofit(
            @InternalApi callFactory: Call.Factory,
            @InternalApi converterFactory: Converter.Factory
        ): Retrofit {
            return Retrofit.Builder()
                .callFactory(callFactory)
                .addConverterFactory(converterFactory)
                .build()
        }

        @Provides
        @JvmStatic
        @Singleton
        fun createYFinanceService(@InternalApi retrofit: Retrofit): YFinance {
            return retrofit.create(YFinance::class.java)
        }
    }
}

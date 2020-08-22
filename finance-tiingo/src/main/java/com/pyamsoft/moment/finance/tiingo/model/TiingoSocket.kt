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

package com.pyamsoft.moment.finance.tiingo.model

import com.squareup.moshi.JsonClass

internal interface TiingoSocket {

    val eventName: String
    val authorization: String
    val eventData: EventData

}

@JsonClass(generateAdapter = true)
internal data class Subscribe internal constructor(
    override val authorization: String,
    override val eventName: String = EVENT_SUBSCRIBE,
    override val eventData: EventData = DEFAULT_EVENT_DATA
) : TiingoSocket {

    // Needed for adapter generation
    companion object
}

@JsonClass(generateAdapter = true)
internal data class Unsubscribe internal constructor(
    override val authorization: String,
    override val eventName: String = EVENT_UNSUBSCRIBE,
    override val eventData: EventData = DEFAULT_EVENT_DATA
) : TiingoSocket {


    // Needed for adapter generation
    companion object
}

@JsonClass(generateAdapter = true)
internal data class EventData(
    internal val thresholdLevel: String = DEFAULT_THRESHOLD
) {
    // Needed for adapter generation
    companion object
}

private const val EVENT_SUBSCRIBE = "subscribe"
private const val EVENT_UNSUBSCRIBE = "unsubscribe"
private const val DEFAULT_THRESHOLD = "5"
private val DEFAULT_EVENT_DATA = EventData()


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
import androidx.annotation.CheckResult
import com.pyamsoft.cachify.CacheStorage
import com.pyamsoft.cachify.Cached
import com.pyamsoft.cachify.MemoryCacheStorage
import com.pyamsoft.cachify.cachify
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.buffer
import okio.sink
import okio.source
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipFile

internal class TiingoTickerCache internal constructor(
    context: Context,
    service: TiingoService
) : Cached<List<String>> {

    // The file to save to
    private val zipFile = lazy { File(context.applicationContext.cacheDir,
        ZIP_FILE
    ) }

    // The upstream, which talks to the network, and saves a zip file
    private val upstream =
        ZipFileUpstream(service, zipFile)

    // Cache in memory for 24 hours
    private val cache = cachify<List<String>>(
        storage = listOf(
            MemoryCacheStorage.create(24, TimeUnit.HOURS),
            ExtractFromDiskStorage(zipFile)
        )
    ) { upstream.fetch() }

    @CheckResult
    private suspend fun getTickers(secondTime: Boolean): List<String> {
        val result = cache.call()
        if (result.isEmpty()) {
            Timber.w("Tickers are empty")
            clear()

            if (!secondTime) {
                Timber.d("Tickers are empty, try again")
                return getTickers(secondTime = true)
            }
        }

        return result
    }

    override suspend fun call(): List<String> {
        return getTickers(secondTime = false)
    }

    override suspend fun clear() {
        return cache.clear()
    }

    companion object {

        private const val ZIP_FILE = "tiingo_tickers.zip"
    }
}

/**
 * Knows how to extract the zipfile from disk
 *
 * We do not cache any operations here in memory because it will be cached by the outer cache storage
 */
private class ExtractFromDiskStorage internal constructor(
    private val zipFile: Lazy<File>
) : CacheStorage<String, List<String>> {

    override suspend fun cache(key: String, data: List<String>) {
        if (data.isEmpty()) {
            clear()
        }
    }

    override suspend fun clear() {
        val cacheFile = zipFile.value
        if (cacheFile.exists()) {
            if (cacheFile.delete()) {
                Timber.d("Deleted zip file: ${cacheFile.absolutePath}")
            } else {
                Timber.w("Could not delete zip file: ${cacheFile.absolutePath}")
            }
        }
    }

    override suspend fun invalidate(key: String) {
        clear()
    }

    override suspend fun retrieve(key: String): List<String>? {
        // If we have an already downloaded zip file
        val cacheFile = zipFile.value
        if (!cacheFile.exists()) {
            Timber.w("No zipfile exists")
            return null
        }

        // TODO(Peter): Check if the cacheFile is out of date via creation time

        // This will happen only if the upstream was not hit this time around
        //
        // This can occur when the memory cache timeout is over, or if the app is opened for a second
        // time after the network was hit once in the past
        return TiingoZipExtractor.extractArchive(
            cacheFile
        )
    }

}

private class ZipFileUpstream internal constructor(
    private val service: TiingoService,
    private val zipFile: Lazy<File>
) {

    @CheckResult
    suspend fun fetch(): List<String> {
        return try {
            Timber.d("Download zip file")
            val tickers = service.tickers()
            val zipFile = downloadZip(tickers)

            // We extract the zip file here
            Timber.d("Extract zip file from upstream")
            TiingoZipExtractor.extractArchive(
                zipFile
            )
        } catch (e: Throwable) {
            Timber.e(e, "Failed to download zip file")
            emptyList()
        }
    }

    @CheckResult
    private fun downloadZip(responseBody: ResponseBody): File {
        // Save the zip
        val file = zipFile.value
        Timber.d("Zip downloaded, save to file: ${file.absolutePath}")
        writeSourceIntoFile(file, responseBody.source())
        return file
    }

    private fun writeSourceIntoFile(file: File, source: BufferedSource) {
        // Make it a sink
        file.sink(append = false).buffer().use { sink ->
            // Write the sink - make sure to flush at the end or Zip will not be valid
            source.use { buffer -> buffer.readAll(sink) }
            sink.flush()
            Timber.d("Written file: ${file.absolutePath} ${file.length()}")
        }
    }

}

private object TiingoZipExtractor {

    // This cannot change, it is from the Tiingo upstream directly.
    private const val TIINGO_ZIP_ENTRY = "supported_tickers.csv"

    @CheckResult
    private fun readBufferLines(buffer: BufferedSource): List<String> {
        val lines = arrayListOf<String>()
        while (true) {
            val line = buffer.readUtf8Line() ?: break
            if (line.isNotBlank()) {
                lines.add(line)
            }
        }

        return lines
    }

    @CheckResult
    private inline fun <T : Any> InputStream.useBuffer(block: (BufferedSource) -> T): T {
        return this.source().use { source -> source.buffer().use { buffer -> block(buffer) } }
    }

    @CheckResult
    fun extractArchive(file: File): List<String> {
        // Open the zip
        Timber.d("Opening zip file: ${file.absolutePath}")
        val archive: ZipFile = try {
            ZipFile(file, ZipFile.OPEN_READ)
        } catch (e: IOException) {
            Timber.e(e, "Failed to open zip archive")
            return emptyList()
        }

        // Open the zip file, close after this block
        Timber.d("Extracting zip archive: ${archive.name}")
        archive.use { zip ->
            // Extract the zip into a file
            val zipEntry = zip.getEntry(TIINGO_ZIP_ENTRY)
            if (zipEntry == null) {
                Timber.e("No zip entry found in Tiingo zip file: $zip")
                Timber.e("Entries are: ${archive.entries().toList().map { it.name }}")
                return emptyList()
            }

            return zip.getInputStream(zipEntry).useBuffer {
                readBufferLines(
                    it
                )
            }
        }
    }

}

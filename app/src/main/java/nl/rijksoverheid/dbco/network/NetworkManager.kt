/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.network

import android.content.Context
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import timber.log.Timber
import java.io.File

private var okHttpClient: OkHttpClient? = null

internal fun createOkHttpClient(context: Context): OkHttpClient {
    return okHttpClient ?: OkHttpClient.Builder()
        // enable cache for config and resource bundles
        .cache(Cache(File(context.cacheDir, "http"), 32 * 1024 * 1024))
        .apply {
            if (Timber.forest().isNotEmpty()) {
                addInterceptor(HttpLoggingInterceptor(object : HttpLoggingInterceptor.Logger {
                    override fun log(message: String) {
                        Timber.tag("OkHttpClient").d(message)
                    }
                }).setLevel(HttpLoggingInterceptor.Level.BODY))
            }
        }.build().also { okHttpClient = it }
}

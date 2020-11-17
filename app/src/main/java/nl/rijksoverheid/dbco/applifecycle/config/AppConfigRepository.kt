/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.applifecycle.config

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import nl.rijksoverheid.dbco.network.StubbedAPI

class AppConfigRepository (context : Context) {

    private val api = StubbedAPI.create(context)
    private var storedAppConfig : AppConfig? = null

    suspend fun getAppConfig(): AppConfig {
        return if (storedAppConfig == null) {
            val data = withContext(Dispatchers.IO) { api.getAppConfig() }
            storedAppConfig = data.body()
            data.body()!!

        } else {
            storedAppConfig!!
        }
    }

    fun getLocalConfig() : AppConfig {
        val appConfigString = "{\n" +
                "    \"androidMinimumVersion\": 50,\n" +
                "    \"androidMinimumVersionMessage\": \"Please upgrade to the latest store release! (nl_NL)\",\n" +
                "    \"iosMinimumVersion\": \"1.0.0\",\n" +
                "    \"iosMinimumVersionMessage\": \"Please upgrade to the latest store release! (nl_NL)\",\n" +
                "    \"iosAppStoreURL\": \"\"\n" +
                "}"
        return Json.decodeFromString(appConfigString)
    }
}
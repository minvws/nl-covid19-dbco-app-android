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
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.network.StubbedAPI

class AppConfigRepository(val context: Context) {

    private val api = StubbedAPI.create(context)
    private var storedAppConfig: AppConfig? = null

    suspend fun getAppConfig(): AppConfig {
        return if (storedAppConfig == null) {
            val data = withContext(Dispatchers.IO) { api.getAppConfig() }
            storedAppConfig = data.body()
            data.body()!!

        } else {
            storedAppConfig!!
        }
    }

    fun getUpdateMessage(): String {
        return if (storedAppConfig != null) {
            storedAppConfig?.androidMinimumVersionMessage ?: ""
        } else {
            context.getString(R.string.update_app_description)
        }
    }

    fun getFeatureFlags(): FeatureFlags {
        return storedAppConfig?.featureFlags ?: FeatureFlags()

    }

    fun getLocalConfig(): AppConfig {
        return if (storedAppConfig == null) {
            val appConfigString = "{\n" +
                    "  \"androidMinimumVersion\": 1,\n" +
                    "  \"androidMinimumVersionMessage\": \"Please upgrade to the latest store release! (nl_NL)\",\n" +
                    "  \"iosMinimumVersion\": \"1.0.0\",\n" +
                    "  \"iosMinimumVersionMessage\": \"Please upgrade to the latest store release! (nl_NL)\",\n" +
                    "  \"iosAppStoreURL\": \"\",\n" +
                    "  \"featureFlags\": {\n" +
                    "  \"enableContactCalling\": true,\n" +
                    "  \"enablePerspectiveSharing\": true,\n" +
                    "  \"enablePerspectiveCopy\": true,\n" +
                    "  \"enableSelfBCO\": true\n" +
                    "  }\n" +
                    "}"
            storedAppConfig = Json.decodeFromString(appConfigString)
            storedAppConfig!!
        } else {
            storedAppConfig!!
        }
    }
}
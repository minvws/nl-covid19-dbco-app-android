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
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.network.DbcoApi
import kotlin.Exception

class AppConfigRepository(val context: Context) {

    private val api = DbcoApi.create(context)

    private var storedAppConfig: AppConfig? = null

    suspend fun getAppConfig(): AppConfig {
        return if (storedAppConfig == null) {
            try {
                val data = withContext(Dispatchers.IO) { api.getAppConfig() }
                storedAppConfig = data.body()
                data.body()!!
            } catch (ex: Exception) {
                AppConfig()
            }
        } else {
            storedAppConfig!!
        }
    }

    fun getUpdateMessage(): String {
        val fallback = context.getString(R.string.update_app_description)
        return if (storedAppConfig != null) {
            storedAppConfig?.androidMinimumVersionMessage ?: fallback
        } else {
            fallback
        }
    }

    fun getFeatureFlags(): FeatureFlags {
        return storedAppConfig?.featureFlags ?: FeatureFlags()
    }

    fun getSymptoms(): List<Symptom> = storedAppConfig?.symptoms ?: emptyList()
}
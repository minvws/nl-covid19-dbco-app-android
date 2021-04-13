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
import kotlinx.serialization.encodeToString
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import kotlin.Exception

class AppConfigRepository(val context: Context) {

    private val api by lazy {
        DbcoApi.create(context)
    }

    private val sharedPrefs by lazy {
        LocalStorageRepository.getInstance(context).getSharedPreferences()
    }

    suspend fun getAppConfig(): AppConfig {
        return try {
            val config = withContext(Dispatchers.IO) { api.getAppConfig() }.body()!!
            setConfig(config)
            config
        } catch (ex: Exception) {
            AppConfig()
        }
    }

    fun setConfig(config: AppConfig) {
        val configString = Defaults.json.encodeToString(config)
        sharedPrefs.edit().putString(KEY_CONFIG, configString).apply()
    }

    fun getUpdateMessage(): String {
        val fallback = context.getString(R.string.update_app_description)
        return getCachedConfig()?.androidMinimumVersionMessage ?: fallback
    }

    fun getFeatureFlags(): FeatureFlags {
        return getCachedConfig()?.featureFlags ?: FeatureFlags()
    }

    fun getSymptoms(): List<Symptom> = getCachedConfig()?.symptoms ?: emptyList()

    fun isSelfBcoSupportedForZipCode(zipCode: Int): Boolean {
        return getCachedConfig()?.isSelfBcoSupportedForZipCode(zipCode) ?: false
    }

    private fun getCachedConfig(): AppConfig? {
        return sharedPrefs.getString(KEY_CONFIG, null)?.let { config ->
            Defaults.json.decodeFromString(config)
        }
    }

    companion object {

        const val KEY_CONFIG = "KEY_CONFIG"
    }
}
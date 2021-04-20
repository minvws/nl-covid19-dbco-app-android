/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.config

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.network.DbcoApi
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
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
            storeConfig(config)
            config
        } catch (ex: Exception) {
            val cached = getCachedConfig()
            val cacheDate = getCacheDate()
            val now = LocalDate.now(DateTimeZone.UTC)
            if (cached == null || cacheDate?.isBefore(now.minusDays(CACHE_VALIDITY_DAYS)) != false) {
                throw ex
            } else {
                cached
            }
        }
    }

    fun storeConfig(config: AppConfig) {
        val configString = Defaults.json.encodeToString(config)
        sharedPrefs.edit()
            .putString(KEY_CONFIG, configString)
            .putString(
                KEY_CONFIG_CACHE_DATE,
                LocalDate.now(DateTimeZone.UTC).toString(DateFormats.dateInputData)
            )
            .apply()
    }

    fun getUpdateMessage(): String {
        val fallback = context.getString(R.string.update_app_description)
        return requireConfig().androidMinimumVersionMessage ?: fallback
    }

    fun getFeatureFlags(): FeatureFlags = requireConfig().featureFlags

    fun getSymptoms(): List<Symptom> = requireConfig().symptoms

    fun getGuidelines(): GuidelinesContainer = requireConfig().guidelines

    fun isSelfBcoSupportedForZipCode(zipCode: Int): Boolean {
        return requireConfig().isSelfBcoSupportedForZipCode(zipCode)
    }

    private fun requireConfig(): AppConfig = getCachedConfig()!!

    private fun getCachedConfig(): AppConfig? {
        return sharedPrefs.getString(KEY_CONFIG, null)?.let { config ->
            Defaults.json.decodeFromString(config)
        }
    }

    private fun getCacheDate(): LocalDate? {
        return sharedPrefs.getString(KEY_CONFIG_CACHE_DATE, null)?.let { date ->
            LocalDate.parse(date, DateFormats.dateInputData)
        }
    }

    companion object {

        private const val KEY_CONFIG = "KEY_CONFIG"
        private const val KEY_CONFIG_CACHE_DATE = "KEY_CONFIG_CACHE_DATE"
        private const val CACHE_VALIDITY_DAYS = 7
    }
}
/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.config

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import kotlinx.coroutines.withContext
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import nl.rijksoverheid.dbco.DefaultDispatcherProvider
import nl.rijksoverheid.dbco.Defaults
import nl.rijksoverheid.dbco.DispatcherProvider
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.network.DbcoApi
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import timber.log.Timber
import kotlin.Exception

/**
 * Repository used to fetch/cache and return the current dynamic configuration
 * used throughout the app
 */
class AppConfigRepository(
    private val context: Context,
    private val api: DbcoApi,
    private val storage: SharedPreferences,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) {

    /**
     * Fetch new configuration from the API and subsequently cache the config.
     * When an error occurs it might use the cached version depending on cache age.
     * @return the configuration, whether from the API or cache, or an [Exception] when
     * API call throws an error and the cache is not healthy
     */
    suspend fun getAppConfig(): AppConfig {
        return try {
            val config = withContext(dispatchers.io()) { api.getAppConfig() }.body()!!
            storeConfig(config)
            config
        } catch (ex: Exception) {
            Timber.e(ex, "Exception during config fetch!")
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
        storage.edit()
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

    private fun requireConfig(): AppConfig = getCachedConfig()!!

    private fun getCachedConfig(): AppConfig? {
        return storage.getString(KEY_CONFIG, null)?.let { config ->
            Defaults.json.decodeFromString(config)
        }
    }

    private fun getCacheDate(): LocalDate? {
        return storage.getString(KEY_CONFIG_CACHE_DATE, null)?.let { date ->
            LocalDate.parse(date, DateFormats.dateInputData)
        }
    }

    companion object {

        @VisibleForTesting
        const val KEY_CONFIG = "KEY_CONFIG"

        @VisibleForTesting
        const val KEY_CONFIG_CACHE_DATE = "KEY_CONFIG_CACHE_DATE"

        @VisibleForTesting
        const val CACHE_VALIDITY_DAYS = 7
    }
}
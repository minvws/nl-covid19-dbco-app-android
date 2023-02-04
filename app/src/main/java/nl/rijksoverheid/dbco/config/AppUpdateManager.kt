/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.dbco.config

import android.content.Context
import nl.rijksoverheid.dbco.BuildConfig
import nl.rijksoverheid.dbco.R
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.NotSupported.*
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.UpToDate
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import org.joda.time.LocalDate
import org.joda.time.format.DateTimeFormat

/**
 * Manager responsible for providing state related to the current app version
 * and whether this app version is supported in the dynamic [AppConfig]
 */
class AppUpdateManager(
    private val context: Context,
    private val currentVersionCode: Int = BuildConfig.VERSION_CODE
) {

    /**
     * Checks if a forced update is necessary or the app is not supported anymore
     */
    fun getAppLifecycleState(config: AppConfig): AppLifecycleState {
        val minimumVersionCode = config.androidMinimumVersion
        return if (minimumVersionCode > currentVersionCode) {
            AppUpdateRequired(
                title = context.getString(R.string.update_app_headline),
                description = config.androidMinimumVersionMessage,
                action = context.getString(R.string.update_app_action)
            )
        } else if (config.isEndOfLife) {
            EndOfLife(
                title = context.getString(R.string.end_of_life_headline),
                description = context.getString(R.string.end_of_life_description),
                action = context.getString(R.string.end_of_life_action),
                actionUrl = context.getString(R.string.end_of_life_action_url),
            )
        } else {
            UpToDate
        }
    }

    sealed class AppLifecycleState {

        sealed class NotSupported(
            open val title: String,
            open val description: String,
            open val action: String,
            open val actionUrl: String? = null
        ) : AppLifecycleState() {

            /**
             * Current app has to be updated in the relevant store where it was downloaded
             */
            data class AppUpdateRequired(
                override val title: String,
                override val description: String,
                override val action: String
            ) : NotSupported(
                title = title, description = description, action = action
            )

            /**
             * The app is end of life
             */
            data class EndOfLife(
                override val title: String,
                override val description: String,
                override val action: String,
                override val actionUrl: String
            ) : NotSupported(
                title = title,
                description = description,
                action = action,
                actionUrl = actionUrl
            )
        }

        /**
         * Current app version is fully supported
         */
        object UpToDate : AppLifecycleState()

        object ConfigError : AppLifecycleState()
    }
}
/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.dbco.config

import android.content.Context
import android.os.Build
import nl.rijksoverheid.dbco.BuildConfig

/**
 * Manager responsible for providing state related to the current app version
 * and whether this app version is supported in the dynamic [AppConfig]
 */
class AppUpdateManager(
    private val context: Context,
    private val currentVersionCode: Int = BuildConfig.VERSION_CODE
) {

    /**
     * Checks if a forced update is necessary and if so returns the info to force the update.
     */
    fun getUpdateState(config: AppConfig): UpdateState {
        val minimumVersionCode = config.androidMinimumVersion
        return if (minimumVersionCode > currentVersionCode) {
            val source = getInstallerPackageName(context, context.packageName)
            UpdateState.UpdateRequired(source)
        } else {
            UpdateState.UpToDate
        }
    }

    private fun getInstallerPackageName(context: Context, packageName: String): String? {
        runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                return context.packageManager.getInstallSourceInfo(packageName).installingPackageName
            @Suppress("DEPRECATION")
            return context.packageManager.getInstallerPackageName(packageName)
        }
        return null
    }

    sealed class UpdateState {

        /**
         * Current app has to be updated in the relevant store where it was downloaded
         * @param installerPackageName package name used to open the store
         */
        data class UpdateRequired(val installerPackageName: String?) : UpdateState()

        /**
         * Current app version is fully supported
         */
        object UpToDate : UpdateState()
    }
}
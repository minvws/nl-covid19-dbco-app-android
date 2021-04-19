/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */
package nl.rijksoverheid.dbco.config

import android.content.Context
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import nl.rijksoverheid.dbco.BuildConfig

class AppUpdateManager(
    private val context: Context,
    private val currentVersionCode: Int = BuildConfig.VERSION_CODE
) {

    /**
     * Checks if a forced update is necessary and if so returns the manager and info to force the update.
     */
    fun getUpdateState(config: AppConfig): UpdateState {
        val minimumVersionCode = config.androidMinimumVersion
        return if (minimumVersionCode > currentVersionCode) {
            val source = context.packageManager.getInstallerPackageName(context.packageName)
            UpdateState.UpdateRequired(source)
        } else {
            UpdateState.UpToDate
        }
    }

    sealed class UpdateState {

        data class InAppUpdate(
            val appUpdateManager: AppUpdateManager,
            val appUpdateInfo: AppUpdateInfo
        ) : UpdateState()

        data class UpdateRequired(val installerPackageName: String?) : UpdateState()

        object UpToDate : UpdateState()
    }
}
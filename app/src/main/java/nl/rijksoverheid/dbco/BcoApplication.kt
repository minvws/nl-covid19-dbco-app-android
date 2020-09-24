/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.annotation.SuppressLint
import android.app.Application
import androidx.work.Configuration
import timber.log.Timber

class BcoApplication : Application(), Configuration.Provider {

    @SuppressLint("RestrictedApi") // for WM Logger api
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.FEATURE_LOGGING) {
            Timber.plant(Timber.DebugTree())
            Timber.plant(FileTree(getExternalFilesDir(null)))
            Timber.d("onCreate")
        }
    }




    override fun getWorkManagerConfiguration(): Configuration {
        TODO("Not yet implemented")
    }


}
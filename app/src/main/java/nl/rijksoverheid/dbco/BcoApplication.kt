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
import net.danlew.android.joda.JodaTimeAndroid
import org.libsodium.jni.NaCl
import timber.log.Timber

class BcoApplication : Application() {

    @SuppressLint("RestrictedApi") // for WM Logger api
    override fun onCreate() {
        super.onCreate()
        JodaTimeAndroid.init(this)
        NaCl.sodium()

        if (BuildConfig.FEATURE_LOGGING) {
            Timber.plant(Timber.DebugTree(), FileTree(getExternalFilesDir(null)))
        }
    }
}
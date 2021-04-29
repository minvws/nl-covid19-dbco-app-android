/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Local storage used for storing all data, uses encrypted [SharedPreferences]
 */
class LocalStorageRepository(context: Context) {

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        FILE_NAME,
        getMasterKey(context),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getSharedPreferences(): SharedPreferences = prefs

    private fun getMasterKey(context: Context): MasterKey {
        return MasterKey.Builder(context, MasterKey.DEFAULT_MASTER_KEY_ALIAS)
            .setRequestStrongBoxBacked(true)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    }

    companion object {

        private const val FILE_NAME = "local_encrypted_storage"

        private var currentLocalStorageRepository: LocalStorageRepository? = null

        fun getInstance(context: Context): LocalStorageRepository {
            if (currentLocalStorageRepository == null) {
                currentLocalStorageRepository = LocalStorageRepository(context)
            }
            return currentLocalStorageRepository!!
        }
    }
}
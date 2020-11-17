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
import android.security.keystore.KeyGenParameterSpec
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class LocalStorageRepository(context: Context) {

    private var sharedPreferences: SharedPreferences
    private var masterKeyAlias: String
    private var keyGenParameterSpec: KeyGenParameterSpec = MasterKeys.AES256_GCM_SPEC

    companion object {
        private var currentLocalStorageRepository: LocalStorageRepository? = null

        fun getInstance(context: Context): LocalStorageRepository {
            if (currentLocalStorageRepository == null) {
                currentLocalStorageRepository = LocalStorageRepository(context)
            }
            return currentLocalStorageRepository!!
        }
    }


    init {
        masterKeyAlias = MasterKeys.getOrCreate(keyGenParameterSpec)
        sharedPreferences = EncryptedSharedPreferences.create(
            "local_encrypted_storage",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getSharedPreferences(): SharedPreferences {
        return sharedPreferences
    }


}
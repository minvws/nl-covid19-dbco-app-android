/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.debug.usertest

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.user.UserInterface
import nl.rijksoverheid.dbco.user.UserInterface.Companion.KEY_RX
import nl.rijksoverheid.dbco.user.UserInterface.Companion.KEY_TOKEN
import java.security.MessageDigest

class UsertestUserRepository(context: Context) : UserInterface { // TODO move to dagger

    private var encryptedSharedPreferences: SharedPreferences = LocalStorageRepository.getInstance(context).getSharedPreferences()

    private val validCodesHashes: List<String> = listOf(
        "a6f3cb251db58a4cceaaec27090241216e69bdf8b28900dbcacb0cdf1f8c9813",
        "022819c49acf699b5030995565cdb86bb183e23ed155300980e86baada1b908a"
    )

    private var token: String? = null
    private var rx: String? = null

    init {
        encryptedSharedPreferences.getString(KEY_RX, null)?.let {
            rx = it
        }
        encryptedSharedPreferences.getString(KEY_TOKEN, null)?.let {
            token = it
        }
    }
    @SuppressLint("ApplySharedPref")
    override suspend fun pair(pincode: String) {
        val hash = hash(pincode)
        if (validCodesHashes.contains(hash)) {
            token = "mocked_token"
            rx = "mocked_rx"
            encryptedSharedPreferences.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_RX, rx)
                .commit()
        }
    }

    private fun hash(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }

    override fun getRx(): String? {
        return rx
    }

    override fun getToken(): String? {
        return token
    }
}
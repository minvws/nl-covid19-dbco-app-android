/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.user

import android.util.Base64

interface UserInterface {

    suspend fun pair(pincode: String)
    fun getRx(): String?
    fun getToken(): String?

    companion object {
        const val BASE64_FLAGS = Base64.NO_WRAP

        val HEX_ARRAY = "0123456789ABCDEF".toCharArray()

        const val KEY_TX = "KEY_TX"
        const val KEY_RX = "KEY_RX"
        const val KEY_TOKEN = "KEY_TOKEN"
        const val KEY_CLIENT_SECRET_KEY = "KEY_CLIENT_SECRET_KEY"
    }
}
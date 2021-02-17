/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.user

import android.util.Base64
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingResponse
import nl.rijksoverheid.dbco.selfbco.reverse.data.entity.ReversePairingStatusResponse
import retrofit2.Response

interface IUserRepository {

    suspend fun pair(pincode: String)
    suspend fun retrieveReversePairingCode() : Response<ReversePairingResponse>
    suspend fun checkReversePairingStatus(token : String) : Response<ReversePairingStatusResponse>
    fun getRx(): String?
    fun getTx(): String?
    fun getToken(): String?

    companion object {
        const val BASE64_FLAGS = Base64.NO_WRAP

        const val KEY_TX = "KEY_TX"
        const val KEY_RX = "KEY_RX"
        const val KEY_TOKEN = "KEY_TOKEN"
        const val KEY_CLIENT_SECRET_KEY = "KEY_CLIENT_SECRET_KEY"
        const val PUBLIC_KEY_VERSION = "20201217"
    }
}
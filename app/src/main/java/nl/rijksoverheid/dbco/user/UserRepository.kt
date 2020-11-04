/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.user

import android.content.Context
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.user.data.entity.PairingResponse

class UserRepository(context: Context) : UserInterface { // TODO move to dagger

    private val api: StubbedAPI = StubbedAPI.create(context)
    private var userKey: String? = null

    override suspend fun pair(pincode: String): PairingResponse {
        val pairingResponse = api.pair()
        userKey = pairingResponse.signingKey
        return pairingResponse
    }
}
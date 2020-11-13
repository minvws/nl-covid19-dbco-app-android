/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.user

import nl.rijksoverheid.dbco.user.data.entity.PairingResponse

interface UserInterface {

    suspend fun pair(pincode: String): PairingResponse
    fun getRx(): String?
    fun getToken(): String?
}
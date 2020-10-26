/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.user.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class PairingResponse(
    val caseId: String? = null,
    val caseExpiresAt: String? = null,
    val signingKey: String? = null
)
/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Serializable
@Keep
data class ReversePairingStatusResponse(
    val refreshDelay: Int? = null, // In seconds
    val expiresAt: String? = null, // ISO8601 date format
    val status: ReversePairingState? = null,
    val pairingCode: String? = null,
)
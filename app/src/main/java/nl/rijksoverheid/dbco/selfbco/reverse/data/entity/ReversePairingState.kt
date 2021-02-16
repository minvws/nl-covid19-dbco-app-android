/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco.reverse.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.SerialName

@Keep
@kotlinx.serialization.Serializable
enum class ReversePairingState {

    @SerialName("pending")
    PENDING,
}
/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.SerialName

@kotlinx.serialization.Serializable
@Keep
enum class TaskType {
    @SerialName("contact")
    Contact,
}

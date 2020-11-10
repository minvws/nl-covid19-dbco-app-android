/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data.entity

import androidx.annotation.Keep
import kotlinx.serialization.SerialName

@Keep
@kotlinx.serialization.Serializable
enum class Category {

	@SerialName("1")
	LIVED_TOGETHER,

	@SerialName("2a")
	DURATION,

	@SerialName("2b")
	DISTANCE,

	@SerialName("3")
	OTHER,

	@SerialName("NO_RISK")
	NO_RISK,
}
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

/**
 * Risk category for a [Task]
 */
@Keep
@kotlinx.serialization.Serializable
enum class Category(val label: String) {

	@SerialName("1")
	ONE("1"),

	@SerialName("2a")
	TWO_A("2a"),

	@SerialName("2b")
	TWO_B("2b"),

	@SerialName("3a")
	THREE_A("3a"),

	@SerialName("3b")
	THREE_B("3b"),

	@SerialName("NO_RISK")
	NO_RISK("NO_RISK"),
}
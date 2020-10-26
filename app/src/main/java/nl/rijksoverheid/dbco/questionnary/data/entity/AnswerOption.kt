/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.questionnary.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class AnswerOption(
	val label: String? = null,
	val trigger: String? = null,
	val value: String? = null
)

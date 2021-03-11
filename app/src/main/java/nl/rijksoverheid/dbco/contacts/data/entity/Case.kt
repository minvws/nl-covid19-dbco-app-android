/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.contacts.data.entity

import kotlinx.serialization.Serializable
import nl.rijksoverheid.dbco.tasks.data.entity.Task

@Serializable
data class Case(
    val reference: String? = null,
    val dateOfSymptomOnset: String? = null,
    val dateOfTest: String? = null,
    val windowExpiresAt: String? = null,
    val tasks: List<Task> = mutableListOf(),
    val symptoms: Set<String> = mutableSetOf()
)
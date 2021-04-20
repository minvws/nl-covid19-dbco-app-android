/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.bcocase.data.entity

import kotlinx.serialization.Serializable

@Serializable
data class Case(
    val lastEdited: String? = null,
    val isUploaded: Boolean = false,
    val canBeUploaded: Boolean = false,
    val reference: String? = null,
    val dateOfSymptomOnset: String? = null,
    val dateOfTest: String? = null,
    val dateOfNegativeTest: String? = null,
    val dateOfPositiveTest: String? = null,
    val dateOfIncreasedSymptoms: String? = null,
    val windowExpiresAt: String? = null,
    val tasks: List<Task> = mutableListOf(),
    val symptoms: Set<String> = mutableSetOf(),
    val symptomsKnown: Boolean = false
) {

    fun hasEssentialTaskData(): Boolean {
        return !tasks.any { !it.hasEssentialData() }
    }
}
/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks

import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.tasks.data.entity.Task

interface ITaskRepository {

    suspend fun fetchCase(): Case
    fun saveTask(task: Task)
    fun deleteTask(taskToDelete: Task)
    fun getCase(): Case
    fun ifCaseWasChanged(): Boolean
    suspend fun uploadCase()
    fun getSymptomOnsetDate(): String?
    fun updateSymptomOnsetDate(dateOfSymptomOnset: String)
    fun addSymptom(symptom: String)
    fun removeSymptom(symptom: String)
    fun getSymptoms(): List<String>

    companion object {
        const val CASE_KEY = "case"
    }
}
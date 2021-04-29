/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.bcocase

import nl.rijksoverheid.dbco.config.Symptom
import nl.rijksoverheid.dbco.bcocase.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

interface ICaseRepository {

    /**
     * @return updated [Case] from backend merged with local case
     */
    suspend fun fetchCase(): Case

    /**
     * @return the [Case] reference entered in the portal
     */
    fun getCaseReference(): String?

    /**
     * Save a new [Task] to the [Case]
     * @param task -> the task to save
     * @param shouldMerge -> closure to determine if the task already exists in the case
     * @param shouldUpdate -> closure to determine if the task has any new information worth updating
     */
    fun saveTask(
        task: Task,
        shouldMerge: (Task) -> Boolean,
        shouldUpdate: (Task) -> Boolean
    )

    /**
     * @return get all contacts for a given [Category]
     * @param category -> the category to filter, when null; all contacts are returned
     */
    fun getContactsByCategory(category: Category?): List<Task>

    /**
     * Delete a [Task] in the current [Case]
     * @param uuid -> id of the task
     */
    fun deleteTask(uuid: String)

    /**
     * @return a [Task] in the current [Case]
     * @param uuid -> id of the task
     */
    fun getTask(uuid: String): Task

    /**
     * @return the current local representation of the [Case]
     */
    fun getCase(): Case

    /**
     * Upload the local [Case] to the backend
     */
    suspend fun uploadCase()

    /**
     * @return the date string when the used started experiencing symptoms
     * Note: also referred as EZD
     */
    fun getSymptomOnsetDate(): String?

    /**
     * Update the symptom onset date
     */
    fun updateSymptomOnsetDate(dateOfSymptomOnset: String)

    /**
     * @return the [LocalDate] when the index could be contagious
     */
    fun getStartOfContagiousPeriod(): LocalDate?

    /**
     * Used to store the test date when index has no symptoms
     */
    fun getTestDate(): String?

    /**
     * Update the test date
     */
    fun updateTestDate(testDate: String)

    /**
     * Update the negative test date
     * @param testDate date of negative test
     */
    fun updateNegativeTestDate(testDate: String)

    /**
     * Used to store a negative test date when EZD date is more than x days in the past
     */
    fun getNegativeTestDate(): String?

    /**
     * Update the positive test date
     * @param testDate date of positive test
     */
    fun updatePositiveTestDate(testDate: String)

    /**
     * Used to store a positive test date when EZD date is more than x days in the past
     * Note: not to be confused with [getTestDate]
     */
    fun getPositiveTestDate(): String?

    /**
     * Update the symptom increased date
     * @param date date of increased symptoms
     */
    fun updateIncreasedSymptomDate(date: String)

    /**
     * Used to store a date when chronic symptoms have increased
     * used when EZD date is more than x days in the past
     */
    fun getIncreasedSymptomDate(): String?

    /**
     * Set the list of symptoms
     */
    fun setSymptoms(symptoms: List<Symptom>)

    /**
     * @return the symptoms of an index
     */
    fun getSymptoms(): List<String>

    /**
     * @return the [LocalDateTime] when the current [Case] was last edited
     */
    fun getLastEdited(): LocalDateTime

    companion object {
        const val CASE_KEY = "case"
    }
}
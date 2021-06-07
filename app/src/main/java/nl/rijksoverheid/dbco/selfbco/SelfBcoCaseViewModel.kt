/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco

import androidx.lifecycle.ViewModel
import nl.rijksoverheid.dbco.config.AppConfigRepository
import nl.rijksoverheid.dbco.config.Symptom
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.entity.Source
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.bcocase.data.entity.TaskType
import org.joda.time.LocalDate
import java.util.*

/**
 * ViewModel used throughout the app to manage the SelfBCO flow
 */
class SelfBcoCaseViewModel(
    private val tasksRepository: ICaseRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private var testedOrSymptoms = SelfBcoConstants.COVID_CHECK_FLOW

    /**
     * @return the list of symptoms an index can select
     */
    fun getSymptoms(): List<Symptom> = appConfigRepository.getSymptoms()

    /**
     * @return whether SelfBCO flow is supported for a given zip code
     * @param zipCode the zip-code
     */
    fun isZipCodeSupported(zipCode: Int): Boolean {
        return appConfigRepository.isSelfBcoSupportedForZipCode(zipCode)
    }

    /**
     * The index has moved to the next step in the flow and selected symptoms or
     * the lack thereof
     * @param symptoms list of selected symptoms, can be empty
     */
    fun setSelectedSymptoms(symptoms: List<Symptom>) = tasksRepository.setSymptoms(symptoms)

    /**
     * @return a list of contacts which live in the same house as the index
     */
    fun getRoommates(): List<Task> = tasksRepository.getContactsByCategory(Category.ONE)

    /**
     * @return a list of contacts entered in the timeline
     */
    fun getTimelineContacts(): List<Task> = tasksRepository.getContactsByCategory(category = null)

    /**
     * Remove a given contact
     * @param uuid the uuid of the contact to remove
     */
    fun removeContact(uuid: String) = tasksRepository.deleteTask(uuid)

    /**
     * Add a new contact
     * @param name the name of the contact
     * @param dateOfLastExposure the date of the last exposure with this contact
     * @param category the risk category of the contact, can be null
     */
    fun addContact(
        name: String,
        dateOfLastExposure: String = LocalDate.now().toString(DateFormats.dateInputData),
        category: Category?
    ) {
        if (name.isNotEmpty()) {
            val task = Task(
                taskType = TaskType.Contact,
                source = Source.App,
                category = category,
                label = name,
                uuid = UUID.randomUUID().toString(),
                dateOfLastExposure = dateOfLastExposure
            )
            tasksRepository.saveTask(
                task = task,
                shouldMerge = { current ->
                    current.label!!.lowercase().contentEquals(task.label!!.lowercase())
                },
                shouldUpdate = { current ->
                    task.getExposureDate().isAfter(current.getExposureDate())
                }
            )
        }
    }

    /**
     * @return the start of the contagious period
     */
    fun getStartOfContagiousPeriod(): LocalDate {
        return tasksRepository.getStartOfContagiousPeriod() ?: LocalDate.now()
    }

    /**
     * @return the start date of the flow, can be either the start of symptoms
     * or the date the index has been tested depending on previous answers
     */
    fun getStartDate(): LocalDate {
        return if (getTypeOfFlow() == SelfBcoConstants.SYMPTOM_CHECK_FLOW) {
            getDateOfSymptomOnset()
        } else {
            getDateOfTest()
        }
    }

    /**
     * @return the date when symptoms were first appearing
     */
    fun getDateOfSymptomOnset(): LocalDate {
        return tasksRepository.getSymptomOnsetDate()?.let {
            LocalDate.parse(it, DateFormats.dateInputData)
        } ?: LocalDate.now()
    }

    /**
     * Update a new date for the symptom onset
     * @param date the new date
     */
    fun updateDateOfSymptomOnset(date: LocalDate) {
        tasksRepository.updateSymptomOnsetDate(
            date.toString(DateFormats.dateInputData)
        )
    }

    /**
     * @return the date when the index has been tested and deemed positive
     */
    fun getDateOfTest(): LocalDate {
        return tasksRepository.getTestDate()?.let {
            LocalDate.parse(it, DateFormats.dateInputData)
        } ?: LocalDate.now()
    }

    /**
     * Update a new date for the test
     * @param date the new date
     */
    fun updateTestDate(date: LocalDate) {
        tasksRepository.updateTestDate(
            date.toString(DateFormats.dateInputData)
        )
    }

    /**
     * Used in flow when EZD is more than x days in the past,
     * so we can assume a EZD is set
     */
    fun getDateOfNegativeTest(): LocalDate {
        return tasksRepository.getNegativeTestDate()?.let {
            LocalDate.parse(it, DateFormats.dateInputData)
        } ?: getDateOfSymptomOnset()
    }

    /**
     * Update a new date for the negative test
     * @param date the new date
     */
    fun updateDateOfNegativeTest(date: LocalDate) {
        tasksRepository.updateNegativeTestDate(
            date.toString(DateFormats.dateInputData)
        )
    }

    /**
     * Used in flow when EZD is more than x days in the past,
     * so we can assume a EZD is set
     */
    fun getDateOfPositiveTest(): LocalDate {
        return tasksRepository.getPositiveTestDate()?.let {
            LocalDate.parse(it, DateFormats.dateInputData)
        } ?: getDateOfSymptomOnset()
    }

    /**
     * Update a new date for the positive test.
     * Note: Not to be confused with the normal test date when user
     * has no symptoms
     * @param date the new date
     */
    fun updateDateOfPositiveTest(date: LocalDate) {
        tasksRepository.updatePositiveTestDate(
            date.toString(DateFormats.dateInputData)
        )
    }

    /**
     * Used in flow when EZD is more than x days in the past,
     * so we can assume a EZD is set
     */
    fun getDateOfIncreasedSymptoms(): LocalDate {
        return tasksRepository.getIncreasedSymptomDate()?.let {
            LocalDate.parse(it, DateFormats.dateInputData)
        } ?: getDateOfSymptomOnset()
    }

    /**
     * Update a new date for the increased symptoms
     * @param date the new date
     */
    fun updateDateOfIncreasedSymptoms(date: LocalDate) {
        tasksRepository.updateIncreasedSymptomDate(
            date.toString(DateFormats.dateInputData)
        )
    }

    /**
     * @return the current SelfBCO flow
     */
    fun getTypeOfFlow() = testedOrSymptoms

    /**
     * Update the current SelfBCO flow
     * @param type the new type
     */
    fun setTypeOfFlow(type: Int) {
        testedOrSymptoms = type
    }

    /**
     * @return the size of the selected symptoms
     */
    fun getSelectedSymptomsSize(): Int = getSelectedSymptoms().size

    /**
     * @return the selected symptoms
     */
    fun getSelectedSymptoms(): List<String> = tasksRepository.getSymptoms()
}
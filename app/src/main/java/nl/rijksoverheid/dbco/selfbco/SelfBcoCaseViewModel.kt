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
import nl.rijksoverheid.dbco.tasks.ICaseRepository
import nl.rijksoverheid.dbco.tasks.data.entity.Source
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TaskType
import org.joda.time.LocalDate
import java.util.*

class SelfBcoCaseViewModel(
    private val tasksRepository: ICaseRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private var testedOrSymptoms = SelfBcoConstants.COVID_CHECK_FLOW

    fun getSymptoms(): List<Symptom> = appConfigRepository.getSymptoms()

    fun isZipCodeSupported(zipCode: Int): Boolean {
        return appConfigRepository.isSelfBcoSupportedForZipCode(zipCode)
    }

    fun setSelectedSymptoms(symptoms: List<Symptom>) = tasksRepository.setSymptoms(symptoms)

    fun getRoommates(): List<Task> = tasksRepository.getContactsByCategory(Category.ONE)

    fun getTimelineContacts(): List<Task> {
        return tasksRepository.getContactsByCategory(category = null)
    }

    fun removeContact(uuid: String) = tasksRepository.deleteTask(uuid)

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
                shouldMerge = { current -> current.label!!.contentEquals(task.label!!) },
                shouldUpdate = { current ->
                    task.getExposureDate().isAfter(current.getExposureDate())
                }
            )
        }
    }

    fun getStartOfContagiousPeriod(): LocalDate {
        return tasksRepository.getStartOfContagiousPeriod() ?: LocalDate.now()
    }

    fun getStartDate(): LocalDate {
        return if (getTypeOfFlow() == SelfBcoConstants.SYMPTOM_CHECK_FLOW) {
            getDateOfSymptomOnset()
        } else {
            getDateOfTest()
        }
    }

    fun getDateOfSymptomOnset(): LocalDate {
        return tasksRepository.getSymptomOnsetDate()?.let {
            LocalDate.parse(it, DateFormats.dateInputData)
        } ?: LocalDate.now()
    }

    fun updateDateOfSymptomOnset(date: LocalDate) {
        tasksRepository.updateSymptomOnsetDate(
            date.toString(DateFormats.dateInputData)
        )
    }

    fun getDateOfTest(): LocalDate {
        return tasksRepository.getTestDate()?.let {
            LocalDate.parse(it, DateFormats.dateInputData)
        } ?: LocalDate.now()
    }

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

    fun updateDateOfIncreasedSymptoms(date: LocalDate) {
        tasksRepository.updateIncreasedSymptomDate(
            date.toString(DateFormats.dateInputData)
        )
    }

    fun getTypeOfFlow() = testedOrSymptoms

    fun setTypeOfFlow(type: Int) {
        testedOrSymptoms = type
    }

    fun getSelectedSymptomsSize(): Int = getSelectedSymptoms().size

    fun getSelectedSymptoms(): List<String> = tasksRepository.getSymptoms()
}
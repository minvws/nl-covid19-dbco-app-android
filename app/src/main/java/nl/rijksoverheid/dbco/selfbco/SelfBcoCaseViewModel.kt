/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.selfbco

import androidx.lifecycle.ViewModel
import nl.rijksoverheid.dbco.applifecycle.config.AppConfigRepository
import nl.rijksoverheid.dbco.applifecycle.config.Symptom
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.entity.Source
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TaskType
import org.joda.time.DateTime
import java.util.*

class SelfBcoCaseViewModel(
    private val tasksRepository: ITaskRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private var testedOrSymptoms = SelfBcoConstants.NOT_SELECTED

    fun getSymptoms(): List<Symptom> = appConfigRepository.getSymptoms()

    fun addSymptom(symptom: String) = tasksRepository.addSymptom(symptom)

    fun removeSymptom(symptom: String) = tasksRepository.removeSymptom(symptom)

    fun addSelfBcoContact(
        name: String,
        dateOfLastExposure: String = DateTime.now()
            .withTimeAtStartOfDay()
            .toString(DateFormats.dateInputData),
        category: Category?
    ) {
        val selfBcoContactTask = Task(
            taskType = TaskType.Contact,
            source = Source.App,
            category = category,
            label = name,
            uuid = UUID.randomUUID().toString(),
            dateOfLastExposure = dateOfLastExposure
        )
        tasksRepository.saveChangesToTask(selfBcoContactTask)
    }

    fun getDateOfSymptomOnset(): DateTime {
        return tasksRepository.getSymptomOnsetDate()?.let {
            DateTime.parse(it, DateFormats.dateInputData)
        } ?: DateTime.now().withTimeAtStartOfDay()
    }

    fun updateDateOfSymptomOnset(date: DateTime) {
        tasksRepository.updateSymptomOnsetDate(
            date.withTimeAtStartOfDay().toString(DateFormats.dateInputData)
        )
    }

    fun getTypeOfFlow() = testedOrSymptoms

    fun setTypeOfFlow(type: Int) {
        testedOrSymptoms = type
    }

    fun getSelectedSymptomsSize(): Int = getSelectedSymptoms().size

    fun getSelectedSymptoms(): List<String> = tasksRepository.getSymptoms()
}
/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.questionnaire.QuestionnareRepository
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import org.joda.time.LocalDate

class TasksDetailViewModel(
    private val tasksRepository: ITaskRepository,
    private val questionnareRepository: IQuestionnaireRepository
) : ViewModel() {

    var questionnaire: Questionnaire? = questionnareRepository.getCachedQuestionnaire()

    val category = MutableLiveData<Category?>()
    val livedTogetherRisk = MutableLiveData<Boolean?>(null)
    val durationRisk = MutableLiveData<Boolean?>(null)
    val distanceRisk = MutableLiveData<Boolean?>(null)
    val otherRisk = MutableLiveData<Boolean?>(null)

    val communicationType = MutableLiveData<CommunicationType?>(null)
    val hasEmailOrPhone = MutableLiveData<Boolean>(true)
    val dateOfLastExposure = MutableLiveData<String>(null)

    val task: MutableLiveData<Task> = MutableLiveData<Task>()
    var selectedContact: LocalContact? = null
    var questionnaireResult: QuestionnaireResult? = null

    fun setTask(task: Task) {
        this.task.value = task
        questionnaireResult = task.questionnaireResult
        category.value = task.category
        updateRiskFlagsFromCategory(task)
    }

    fun getDateOfSymptomOnset(): LocalDate? {
        tasksRepository.getCachedCase()?.dateOfSymptomOnset?.let {
            return LocalDate.parse(it, DateFormats.exposureData )
        }
        return null
    }

    fun saveChangesToTask(updatedTask: Task) {
        tasksRepository.saveChangesToTask(updatedTask)
    }

    private fun updateRiskFlagsFromCategory(task: Task) {
        when (task.category) {
            Category.LIVED_TOGETHER -> {
                livedTogetherRisk.value = true
                durationRisk.value = null
                distanceRisk.value = null
                otherRisk.value = null
            }
            Category.DURATION -> {
                livedTogetherRisk.value = false
                durationRisk.value = true
                distanceRisk.value = null
                otherRisk.value = null
            }
            Category.DISTANCE -> {
                livedTogetherRisk.value = false
                durationRisk.value = false
                distanceRisk.value = true
                otherRisk.value = null
            }
            Category.OTHER -> {
                livedTogetherRisk.value = false
                durationRisk.value = false
                distanceRisk.value = false
                otherRisk.value = true
            }
            Category.NO_RISK -> {
                livedTogetherRisk.value = false
                durationRisk.value = false
                distanceRisk.value = false
                otherRisk.value = false
            }
            null -> {
                livedTogetherRisk.value = null
                durationRisk.value = null
                distanceRisk.value = null
                otherRisk.value = null
            }
        }
    }

    fun updateCategoryFromRiskFlags() {
        category.value = when {
            livedTogetherRisk.value == true -> Category.LIVED_TOGETHER
            durationRisk.value == true -> Category.DURATION
            distanceRisk.value == true -> Category.DISTANCE
            otherRisk.value == true -> Category.OTHER
            otherRisk.value == false -> Category.NO_RISK
            else -> null
        }
    }
}
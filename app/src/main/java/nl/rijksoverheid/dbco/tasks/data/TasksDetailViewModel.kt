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
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
import nl.rijksoverheid.dbco.questionnaire.data.entity.QuestionnaireResult
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.entity.CommunicationType
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import org.joda.time.LocalDate

class TasksDetailViewModel(
    private val tasksRepository: ITaskRepository,
    questionnaireRepository: IQuestionnaireRepository
) : ViewModel() {

    var questionnaire: Questionnaire? = questionnaireRepository.getCachedQuestionnaire()

    val category = MutableLiveData<Category?>()
    val sameHouseholdRisk = MutableLiveData<Boolean?>(null)
    val distanceRisk = MutableLiveData<Pair<Boolean?, Boolean?>>(null)
    val physicalContactRisk = MutableLiveData<Boolean?>(null)
    val sameRoomRisk = MutableLiveData<Boolean?>(null)

    val communicationType = MutableLiveData<CommunicationType?>(null)
    val hasEmailOrPhone = MutableLiveData<Boolean>(null)
    val dateOfLastExposure = MutableLiveData<String>(null)

    val task: MutableLiveData<Task> = MutableLiveData<Task>()
    var selectedContact: LocalContact? = null
    var questionnaireResult: QuestionnaireResult? = null

    fun setTask(task: Task) {
        this.task.value = task
        selectedContact = task.linkedContact?.copy()
        questionnaireResult = task.questionnaireResult
        hasEmailOrPhone.value = selectedContact?.hasValidEmailOrPhone()
        communicationType.value = task.communication
        dateOfLastExposure.value = task.dateOfLastExposure
        category.value = task.category
        updateRiskFlagsFromCategory(task)
    }

    fun getStartOfContagiousPeriod(): LocalDate? = tasksRepository.getStartOfContagiousPeriod()

    fun saveTask(task: Task) {
        tasksRepository.saveTask(task) { current -> current.uuid == task.uuid }
    }

    fun deleteCurrentTask() {
        task.value?.let {
            tasksRepository.deleteTask(it)
        }
    }

    private fun updateRiskFlagsFromCategory(task: Task) {
        when (task.category) {
            Category.ONE -> {
                sameHouseholdRisk.value = true
                distanceRisk.value = null
                physicalContactRisk.value = null
                sameRoomRisk.value = null
            }
            Category.TWO_A -> {
                sameHouseholdRisk.value = false
                distanceRisk.value = Pair(first = true, second = true)
                physicalContactRisk.value = null
                sameRoomRisk.value = null
            }
            Category.TWO_B -> {
                sameHouseholdRisk.value = false
                distanceRisk.value = Pair(first = true, second = false)
                physicalContactRisk.value = true
                sameRoomRisk.value = null
            }
            Category.THREE_A -> {
                sameHouseholdRisk.value = false
                distanceRisk.value = Pair(first = true, second = false)
                physicalContactRisk.value = false
                sameRoomRisk.value = null
            }
            Category.THREE_B -> {
                sameHouseholdRisk.value = false
                distanceRisk.value = Pair(first = false, second = false)
                physicalContactRisk.value = null
                sameRoomRisk.value = true
            }
            Category.NO_RISK -> {
                sameHouseholdRisk.value = false
                distanceRisk.value = Pair(first = false, second = false)
                physicalContactRisk.value = false
                sameRoomRisk.value = false
            }
            null -> {
                sameHouseholdRisk.value = null
                distanceRisk.value = null
                physicalContactRisk.value = null
                sameRoomRisk.value = null
            }
        }
    }

    fun updateCategoryFromRiskFlags() {
        category.value = when {
            sameHouseholdRisk.value == true -> Category.ONE
            distanceRisk.value == Pair(first = true, second = true) -> Category.TWO_A
            distanceRisk.value == Pair(first = true, second = false) &&
                    physicalContactRisk.value == true -> Category.TWO_B
            distanceRisk.value == Pair(first = true, second = false) &&
                    physicalContactRisk.value == false -> Category.THREE_A
            sameRoomRisk.value == true -> Category.THREE_B
            sameRoomRisk.value == false -> Category.NO_RISK
            else -> null
        }
    }
}
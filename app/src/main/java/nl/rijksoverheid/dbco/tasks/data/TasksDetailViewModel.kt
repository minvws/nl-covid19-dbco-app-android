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
import nl.rijksoverheid.dbco.questionnaire.data.entity.Answer
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
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

    private lateinit var _task: Task

    val task: Task
        get() = _task

    fun init(task: Task) {
        this._task = task
        if (task.linkedContact == null) {
            task.linkedContact = LocalContact.fromLabel(task.label)
        }
        hasEmailOrPhone.value = task.linkedContact?.hasValidEmailOrPhone()
        communicationType.value = task.communication
        dateOfLastExposure.value = task.dateOfLastExposure
        category.value = task.category
        updateRiskFlagsFromCategory(task)
    }

    fun getQuestionnaireAnswers(): List<Answer> = _task.questionnaireResult?.answers ?: emptyList()

    fun getCaseReference(): String? = tasksRepository.getCaseReference()

    fun hasCaseReference(): Boolean = tasksRepository.getCaseReference() != null

    fun getStartOfContagiousPeriod(): LocalDate? = tasksRepository.getStartOfContagiousPeriod()

    fun saveTask() = tasksRepository.saveTask(task) { current -> current.uuid == task.uuid }

    fun deleteCurrentTask() = task.uuid?.let { uuid -> tasksRepository.deleteTask(uuid) }

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
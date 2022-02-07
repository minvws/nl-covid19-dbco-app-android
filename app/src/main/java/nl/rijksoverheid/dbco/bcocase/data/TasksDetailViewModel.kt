/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.bcocase.data

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.serialization.json.jsonPrimitive
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.contacts.data.entity.LocalContact
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.questionnaire.data.entity.Answer
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.entity.CommunicationType
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.config.FeatureFlags
import nl.rijksoverheid.dbco.questionnaire.data.entity.Trigger
import nl.rijksoverheid.dbco.questionnaire.data.entity.Trigger.ShareIndexNameDisallowed
import nl.rijksoverheid.dbco.questionnaire.data.entity.Trigger.ShareIndexNameAllowed
import org.joda.time.LocalDate

/**
 * ViewModel responsible for handling changes in [Task] details
 * exposes a number of [LiveData] objects which describe some attributes from
 * the currently selected [Task], like what risk category the [Task] falls into.
 */
class TasksDetailViewModel(
    private val tasksRepository: ICaseRepository,
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
    val name = MutableLiveData<String?>(null)
    val dateOfLastExposure = MutableLiveData<String>(null)

    val textAnswers: MutableMap<String, String> = mutableMapOf()

    private lateinit var _task: Task

    val task: Task
        get() = _task

    fun init(taskId: String) {
        val task = tasksRepository.getTask(taskId)
        this._task = task
        if (task.linkedContact == null) {
            task.linkedContact = LocalContact.fromLabel(task.label)
        }
        hasEmailOrPhone.value = task.linkedContact?.hasValidEmailOrPhone()
        name.value = task.linkedContact?.firstName
        communicationType.value = task.communication
        dateOfLastExposure.value = task.dateOfLastExposure
        category.value = task.category

        cacheTextAnswers(task)
        updateRiskFlagsFromCategory(task)
    }

    fun commByIndex(): Boolean = communicationType.value != CommunicationType.Staff

    private fun canCallTask(): Boolean = task.linkedContact?.hasSingleValidPhoneNumber() ?: false

    fun callingEnabled(featureFlags: FeatureFlags): Boolean {
        return canCallTask() && featureFlags.enableContactCalling
    }

    fun copyEnabled(featureFlags: FeatureFlags): Boolean {
        return featureFlags.enablePerspectiveCopy
    }

    fun getQuestionnaireAnswers(): List<Answer> = _task.questionnaireResult?.answers ?: emptyList()

    fun hasUpdatedExposureDate(): Boolean = _task.dateOfLastExposure != dateOfLastExposure.value

    fun getCaseReference(): String? = tasksRepository.getCaseReference()

    fun hasCaseReference(): Boolean = tasksRepository.getCaseReference() != null

    fun getEarliestExposureDateOption(): LocalDate? = tasksRepository.getStartOfAllowedContagiousPeriod()

    fun saveTask() {
        tasksRepository.saveTask(
            task = task,
            shouldMerge = { current -> current.uuid == task.uuid },
            shouldUpdate = { current -> task != current }
        )
    }

    fun isDeletionPossible(changesEnabled: Boolean): Boolean {
        val hasInformation = (task.hasCategoryOrExposure() || task.isSaved())
        return changesEnabled && task.isLocal() && hasInformation
    }

    fun onCancelled(changesEnabled: Boolean) {
        val hasNoInformation = !task.isSaved() && !task.hasCategoryOrExposure()
        if (changesEnabled && task.isLocal() && hasNoInformation) {
            deleteCurrentTask()
        }
    }

    fun onAnswerTrigger(trigger: Trigger) {
        when (trigger) {
            ShareIndexNameAllowed, ShareIndexNameDisallowed -> {
                task.shareIndexNameWithContact = trigger == ShareIndexNameAllowed
            }
        }
    }

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

    /**
     * Keep a local cache of answers which come from generic questions without backing live data
     * so the view can reference initial and updated values
     */
    private fun cacheTextAnswers(task: Task) {
        task.questionnaireResult?.answers?.let { answers ->
            answers.filter { answer -> answer.isTextQuestion() }.forEach { result ->
                textAnswers[result.questionUuid!!] = result.textValue
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

    private fun Answer.isTextQuestion(): Boolean {
        return !questionUuid.isNullOrEmpty() &&
                value != null &&
                value!!.containsKey("value")
    }

    private val Answer.textValue: String
        get() = value!!["value"]!!.jsonPrimitive.content
}
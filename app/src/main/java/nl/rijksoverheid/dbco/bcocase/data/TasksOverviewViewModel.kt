/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.bcocase.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.bcocase.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import nl.rijksoverheid.dbco.bcocase.data.entity.Task
import nl.rijksoverheid.dbco.util.numeric
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireSuccess
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireError
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus.UploadError
import nl.rijksoverheid.dbco.bcocase.data.TasksOverviewViewModel.UploadStatus.UploadSuccess
import nl.rijksoverheid.dbco.util.SingleLiveEvent

/**
 * ViewModel used to fetch and show all [Task]s currently added to the [Case].
 * Is also used to upload changes in the current [Case]
 */
class TasksOverviewViewModel(
    private val tasksRepository: ICaseRepository,
    private val questionnaireRepository: IQuestionnaireRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _viewData = SingleLiveEvent<ViewData>()

    /**
     * Exposes all data needed to show the [Task]s in the current case
     */
    val viewData: LiveData<ViewData> = _viewData

    private val _uploadStatus = SingleLiveEvent<UploadStatus>()

    /**
     * Exposes the upload state when uploading all changes in the current [Case]
     */
    val uploadStatus: LiveData<UploadStatus> = _uploadStatus

    fun getCachedCase() = tasksRepository.getCase()

    fun syncData() {
        viewModelScope.launch(coroutineDispatcher) {
            val caseResult = try {
                val cachedCase = getCachedCase()
                val now = LocalDateTime.now(DateTimeZone.UTC)
                val expiredDate = cachedCase.windowExpiresAt?.let {
                    LocalDateTime.parse(cachedCase.windowExpiresAt, DateFormats.expiryData)
                } ?: now
                if (expiredDate.isBefore(now)) {
                    val sorted = cachedCase.copy(tasks = sortTasks(cachedCase.tasks))
                    CaseResult.CaseExpired(sorted)
                } else {
                    val case = tasksRepository.fetchCase()
                    val sorted = case.copy(tasks = sortTasks(case.tasks))
                    CaseResult.CaseSuccess(sorted)
                }
            } catch (ex: Exception) {
                CaseResult.CaseError(getCachedCase())
            }

            val questionnaireResult = try {
                questionnaireRepository.syncQuestionnaires()
                QuestionnaireSuccess
            } catch (ex: Exception) {
                QuestionnaireError
            }
            _viewData.value = ViewData(caseResult, questionnaireResult)
        }
    }

    fun uploadCurrentCase() {
        viewModelScope.launch(coroutineDispatcher) {
            try {
                tasksRepository.uploadCase()
                _uploadStatus.value = UploadSuccess
            } catch (ex: Exception) {
                _uploadStatus.value = UploadError
            }
        }
    }

    fun isCurrentCaseExpired(): Boolean = _viewData.value?.caseResult is CaseResult.CaseExpired

    fun hasEssentialTaskData(): Boolean = getCachedCase().hasEssentialTaskData()

    fun getCachedQuestionnaire() = questionnaireRepository.getCachedQuestionnaire()

    fun getStartOfContagiousPeriod(): LocalDate {
        return tasksRepository.getStartOfContagiousPeriod() ?: LocalDate.now()
    }

    /**
     * Sort the list of [Task]s in the current case.
     * First on category, then on last exposure date and finally on alphabetic name
     */
    private fun sortTasks(tasks: List<Task>): List<Task> {
        val fallbackDate = "9999-01-01".numeric()
        return tasks.sortedWith(Comparator<Task> { a, b ->
            if (a.category == Category.ONE && b.category != Category.ONE) {
                -1
            } else if (b.category == Category.ONE && a.category != Category.ONE) {
                1
            } else {
                0
            }
        }.thenByDescending {
            it.dateOfLastExposure.numeric() ?: fallbackDate
        }.thenBy {
            it.getDisplayName("")
        })
    }

    fun createEmptyContact(): Task {
        val task = Task.createAppContact()
        tasksRepository.saveTask(
            task = task,
            shouldMerge = { false },
            shouldUpdate = { false }
        )
        return task
    }

    data class ViewData(val caseResult: CaseResult, val questionnaireResult: QuestionnaireResult)

    sealed class CaseResult {

        data class CaseSuccess(val case: Case) : CaseResult()
        data class CaseExpired(val cachedCase: Case) : CaseResult()
        data class CaseError(val cachedCase: Case) : CaseResult()
    }

    sealed class QuestionnaireResult {

        object QuestionnaireSuccess : QuestionnaireResult()
        object QuestionnaireError : QuestionnaireResult()
    }

    sealed class UploadStatus {

        object UploadSuccess : UploadStatus()
        object UploadError : UploadStatus()
    }
}
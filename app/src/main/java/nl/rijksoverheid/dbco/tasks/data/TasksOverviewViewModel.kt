/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.contacts.data.DateFormats
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.contacts.data.entity.Category
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.util.numeric
import org.joda.time.DateTimeZone
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireSuccess
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel.QuestionnaireResult.QuestionnaireError

class TasksOverviewViewModel(
    private val tasksRepository: ITaskRepository,
    private val questionnaireRepository: IQuestionnaireRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _case = MutableLiveData<CaseResult>()
    val case: LiveData<CaseResult> = _case

    private val _questionnaire = MutableLiveData<QuestionnaireResult>()
    val questionnaire: LiveData<QuestionnaireResult> = _questionnaire

    fun getCachedCase() = tasksRepository.getCase()

    fun syncTasks() {
        viewModelScope.launch(coroutineDispatcher) {
            try {
                val cachedCase = getCachedCase()
                val now = LocalDateTime.now(DateTimeZone.UTC)
                val expiredDate = cachedCase.windowExpiresAt?.let {
                    LocalDateTime.parse(cachedCase.windowExpiresAt, DateFormats.expiryData)
                } ?: now
                if (expiredDate.isBefore(now)) {
                    val sorted = cachedCase.copy(tasks = sortTasks(cachedCase.tasks))
                    _case.postValue(CaseResult.CaseExpired(sorted))
                } else {
                    val case = tasksRepository.fetchCase()
                    val sorted = case.copy(tasks = sortTasks(case.tasks))
                    _case.postValue(CaseResult.Success(sorted))
                }
            } catch (ex: Exception) {
                _case.postValue(CaseResult.Error(getCachedCase()))
            }
        }
    }

    fun syncQuestionnaire() {
        viewModelScope.launch(coroutineDispatcher) {
            try {
                questionnaireRepository.syncQuestionnaires()
                _questionnaire.value = QuestionnaireSuccess
            } catch (ex: Exception) {
                _questionnaire.value = QuestionnaireError
            }
        }
    }

    fun uploadCurrentCase() {
        viewModelScope.launch {
            tasksRepository.uploadCase()
        }
    }

    fun getCachedQuestionnaire() = questionnaireRepository.getCachedQuestionnaire()

    fun getStartOfContagiousPeriod(): LocalDate {
        return tasksRepository.getStartOfContagiousPeriod() ?: LocalDate.now()
    }

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

    sealed class CaseResult {

        data class Success(val case: Case) : CaseResult()
        data class CaseExpired(val cachedCase: Case) : CaseResult()
        data class Error(val cachedCase: Case) : CaseResult()
    }

    sealed class QuestionnaireResult {

        object QuestionnaireSuccess : QuestionnaireResult()
        object QuestionnaireError : QuestionnaireResult()
    }
}
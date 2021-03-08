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
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.util.Resource
import timber.log.Timber

class TasksOverviewViewModel(
    private val tasksRepository: ITaskRepository,
    private val questionnaireRepository: IQuestionnaireRepository
) : ViewModel() {

    private val _fetchCase = MutableLiveData<Resource<Case>>()
    val fetchCase: LiveData<Resource<Case>> = _fetchCase

    var selfBcoCase = MutableLiveData<Resource<Case?>>()

    private val _windowExpired = MutableLiveData<Boolean>(false)
    val windowExpired: LiveData<Boolean> = _windowExpired


    fun getCachedCase() = tasksRepository.getCase()

    fun syncTasks() {
        viewModelScope.launch {
            try {
                val case = tasksRepository.fetchCase()
                _fetchCase.postValue(Resource.success(case))
            } catch (ex: Exception) {
                Timber.e(ex, "Error while retrieving case")
                _fetchCase.postValue(Resource.failure(ex))
                // Window expired
                if (ex is SerializationException) {
                    _windowExpired.postValue(true)
                }
            }
        }
        viewModelScope.launch {
            questionnaireRepository.syncQuestionnaires()
        }
    }

    fun uploadCurrentCase() {
        viewModelScope.launch {
            tasksRepository.uploadCase()
        }
    }

    fun ifCaseWasChanged(): Boolean = tasksRepository.ifCaseWasChanged()

    fun getCachedQuestionnaire() = questionnaireRepository.getCachedQuestionnaire()
}
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
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.questionnaire.QuestionnareRepository
import nl.rijksoverheid.dbco.tasks.TasksRepository
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TasksResponse
import timber.log.Timber

class TasksViewModel(
    private val tasksRepository: TasksRepository,
    private val questionnareRepository: QuestionnareRepository
) : ViewModel() {


    private val _indexTasks = MutableLiveData<TasksResponse>()
    val indexTasksLivedata: LiveData<TasksResponse> = _indexTasks

    private val _questionnaire = MutableLiveData<ContactDetailsResponse>()
    val questionnaire: LiveData<ContactDetailsResponse> = _questionnaire

    override fun onCleared() {
        super.onCleared()
        Timber.e("Viewmodel is being cleared")
    }

    init {
        Timber.d("Initializing tasks viewmodel")
    }

    fun fetchTasksForUUID(uuid: String = "") {
        viewModelScope.launch {
            val taskResponse = tasksRepository.retrieveTasksForUUID(uuid)
            _indexTasks.postValue(taskResponse)
        }
    }

    fun retrieveQuestionnaires() {
        viewModelScope.launch {
            val questionnairesResponse = questionnareRepository.retrieveQuestionnaires()
            if (questionnairesResponse.isSuccessful) {
                _questionnaire.postValue(questionnairesResponse.body())
            }
        }
    }

    fun saveChangesToTask(updatedTask: Task) {
        tasksRepository.saveChangesToTask(updatedTask)
    }

}
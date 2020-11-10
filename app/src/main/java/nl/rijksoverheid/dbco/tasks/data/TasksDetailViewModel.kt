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
import nl.rijksoverheid.dbco.contacts.data.entity.ContactDetailsResponse
import nl.rijksoverheid.dbco.questionnaire.QuestionnaireInterface
import nl.rijksoverheid.dbco.questionnaire.data.entity.Questionnaire
import nl.rijksoverheid.dbco.tasks.TaskInterface
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import timber.log.Timber

class TasksDetailViewModel(
        private val tasksRepository: TaskInterface,
        private val questionnareRepository: QuestionnaireInterface
) : ViewModel() {

    val task: MutableLiveData<Task> = MutableLiveData<Task>()

    var questionnaire: Questionnaire? = null

    init {
        retrieveQuestionnaires()
    }

    override fun onCleared() {
        super.onCleared()
        Timber.e("Viewmodel is being cleared")
    }

    init {
        Timber.d("Initializing tasks viewmodel")
    }

    private fun retrieveQuestionnaires() { // TODO make call once in repo and cache it there, remove suspend
        viewModelScope.launch {
            questionnaire = questionnareRepository.retrieveQuestionnaires().questionnaires?.firstOrNull()
        }
    }

    fun saveChangesToTask(updatedTask: Task) {
        tasksRepository.saveChangesToTask(updatedTask)
    }

}
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
import nl.rijksoverheid.dbco.tasks.TaskInterface
import nl.rijksoverheid.dbco.tasks.data.entity.TasksResponse
import timber.log.Timber

class TasksOverviewViewModel(
    private val tasksRepository: TaskInterface
) : ViewModel() {

    private val _indexTasks = MutableLiveData<TasksResponse>()
    val indexTasks: LiveData<TasksResponse> = _indexTasks

    override fun onCleared() {
        super.onCleared()
        Timber.e("Viewmodel is being cleared")
    }

    init {
        Timber.d("Initializing tasks viewmodel")
    }

    fun fetchTasks() {
        viewModelScope.launch {
            val taskResponse = tasksRepository.retrieveTasks()
            _indexTasks.postValue(taskResponse)
        }
    }

}
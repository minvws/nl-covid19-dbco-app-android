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
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.tasks.ITaskRepository

class TasksOverviewViewModel(
    private val tasksRepository: ITaskRepository
) : ViewModel() {

    private val _case = MutableLiveData<Case?>()
    val case: LiveData<Case?> = _case

    fun fetchTasks() {
        viewModelScope.launch {

            _case.postValue(tasksRepository.retrieveCase())
        }
    }

}
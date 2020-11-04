/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.tasks

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.tasks.data.entity.TasksResponse

class TasksRepository(context: Context) {
    private val api = StubbedAPI.create(context)

    private var previousResponse: TasksResponse? = null


    suspend fun retrieveTasksForUUID(uuid: String = ""): TasksResponse {
        if (previousResponse == null) {
            val data = withContext(Dispatchers.IO) { api.getTasksForUUID() }
            previousResponse = data.body()
            return data.body()!!

        } else {
            return previousResponse!!
        }
    }

    fun saveChangesToTask(updatedTask: Task) {
        val currentTasks = previousResponse?.case?.tasks as ArrayList
        var i = 0
        currentTasks.forEach { currentTask ->
            if (updatedTask.uuid == currentTask.uuid) {
                //Timber.d("Found matching task $updatedTask")
                (previousResponse?.case?.tasks as java.util.ArrayList<Task>).set(i, updatedTask)
            }
            i++
        }
    }
}

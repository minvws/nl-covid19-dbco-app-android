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
import nl.rijksoverheid.dbco.contacts.data.entity.Case
import nl.rijksoverheid.dbco.network.StubbedAPI
import nl.rijksoverheid.dbco.tasks.data.entity.Task
import nl.rijksoverheid.dbco.user.UserInterface

class TasksRepository(context: Context, val userRepository: UserInterface) : TaskInterface {
    private val api = StubbedAPI.create(context)
    private var cachedCase: Case? = null

    override suspend fun retrieveCase(): Case? {
        if (cachedCase == null) {
            userRepository.getToken()?.let {
                val data = withContext(Dispatchers.IO) { api.getCase(it) }
                val sealedCase = data.body()?.sealedCase
                // decrypt
                cachedCase = Case()
            }
        }
        return cachedCase
    }

    override fun saveChangesToTask(updatedTask: Task) {
        val currentTasks = cachedCase?.tasks as ArrayList
        currentTasks.forEachIndexed { index, currentTask ->
            if (updatedTask.uuid == currentTask.uuid) {
                currentTasks[index] = updatedTask
            }
        }
    }

    override fun getCase(): Case? {
        return cachedCase
    }
}

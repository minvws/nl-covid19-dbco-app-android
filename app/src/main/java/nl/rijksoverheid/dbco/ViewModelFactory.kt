/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.questionnary.QuestionnaryRepository
import nl.rijksoverheid.dbco.tasks.TasksRepository
import nl.rijksoverheid.dbco.tasks.data.TasksViewModel

class ViewModelFactory(
    private val tasksRepository: TasksRepository,
    private val contactsRepository: ContactsRepository,
    private val questionnaryRepository: QuestionnaryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ContactsViewModel::class.java -> ContactsViewModel(contactsRepository) as T
            TasksViewModel::class.java -> TasksViewModel(
                tasksRepository, questionnaryRepository
            ) as T
            else -> throw IllegalStateException("Unknown view model class $modelClass")
        }
    }
}
/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleManager
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleViewModel
import nl.rijksoverheid.dbco.applifecycle.config.AppConfigRepository
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.onboarding.FillCodeViewModel
import nl.rijksoverheid.dbco.questionnaire.QuestionnaireInterface
import nl.rijksoverheid.dbco.tasks.TaskInterface
import nl.rijksoverheid.dbco.tasks.data.TasksViewModel
import nl.rijksoverheid.dbco.user.UserInterface

class ViewModelFactory(
    private val context: Context,
    private val tasksRepository: TaskInterface,
    private val contactsRepository: ContactsRepository,
    private val questionnareRepository: QuestionnaireInterface,
    private val userRepository: UserInterface,
    private val appConfigRepository: AppConfigRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ContactsViewModel::class.java -> ContactsViewModel(contactsRepository) as T
            TasksViewModel::class.java -> TasksViewModel(
                tasksRepository,
                questionnareRepository
            ) as T
            FillCodeViewModel::class.java -> FillCodeViewModel(userRepository) as T
            AppLifecycleViewModel::class.java -> AppLifecycleViewModel(
                AppLifecycleManager(
                    context,
                    context.getSharedPreferences("${BuildConfig.APPLICATION_ID}.config", 0),
                    AppUpdateManagerFactory.create(context)
                ), appConfigRepository
            ) as T
            else -> throw IllegalStateException("Unknown view model class $modelClass")
        }
    }
}
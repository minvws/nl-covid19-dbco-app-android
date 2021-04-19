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
import nl.rijksoverheid.dbco.config.AppUpdateManager
import nl.rijksoverheid.dbco.config.AppConfigRepository
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.onboarding.PairingViewModel
import nl.rijksoverheid.dbco.onboarding.OnboardingConsentViewModel
import nl.rijksoverheid.dbco.onboarding.SplashViewModel
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.tasks.ICaseRepository
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.user.IUserRepository

class ViewModelFactory(
    private val context: Context,
    private val tasksRepository: ICaseRepository,
    private val contactsRepository: ContactsRepository,
    private val questionnaireRepository: IQuestionnaireRepository,
    private val userRepository: IUserRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ContactsViewModel::class.java -> ContactsViewModel(contactsRepository) as T
            TasksOverviewViewModel::class.java -> TasksOverviewViewModel(
                tasksRepository,
                questionnaireRepository
            ) as T
            TasksDetailViewModel::class.java -> TasksDetailViewModel(
                tasksRepository,
                questionnaireRepository
            ) as T
            PairingViewModel::class.java -> PairingViewModel(userRepository, tasksRepository) as T
            SplashViewModel::class.java -> SplashViewModel(
                context,
                userRepository,
                tasksRepository,
                appConfigRepository
            ) as T
            AppViewModel::class.java -> AppViewModel(
                AppUpdateManager(context),
                appConfigRepository
            ) as T
            OnboardingConsentViewModel::class.java -> OnboardingConsentViewModel(
                tasksRepository,
                userRepository,
                context
            ) as T
            SelfBcoCaseViewModel::class.java -> SelfBcoCaseViewModel(
                tasksRepository,
                appConfigRepository
            ) as T
            else -> throw IllegalStateException("Unknown view model class $modelClass")
        }
    }
}
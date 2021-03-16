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
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleManager
import nl.rijksoverheid.dbco.applifecycle.AppLifecycleViewModel
import nl.rijksoverheid.dbco.applifecycle.config.AppConfigRepository
import nl.rijksoverheid.dbco.contacts.ContactsViewModel
import nl.rijksoverheid.dbco.contacts.data.ContactsRepository
import nl.rijksoverheid.dbco.onboarding.PairingViewModel
import nl.rijksoverheid.dbco.onboarding.OnboardingConsentViewModel
import nl.rijksoverheid.dbco.onboarding.SplashViewModel
import nl.rijksoverheid.dbco.questionnaire.IQuestionnaireRepository
import nl.rijksoverheid.dbco.selfbco.SelfBcoCaseViewModel
import nl.rijksoverheid.dbco.selfbco.reverse.ReversePairingViewModel
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.tasks.data.TasksDetailViewModel
import nl.rijksoverheid.dbco.tasks.data.TasksOverviewViewModel
import nl.rijksoverheid.dbco.user.IUserRepository

@ExperimentalSerializationApi
class ViewModelFactory(
    private val context: Context,
    private val tasksRepository: ITaskRepository,
    private val contactsRepository: ContactsRepository,
    private val questionnareRepository: IQuestionnaireRepository,
    private val userRepository: IUserRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when (modelClass) {
            ContactsViewModel::class.java -> ContactsViewModel(contactsRepository) as T
            TasksOverviewViewModel::class.java -> TasksOverviewViewModel(
                tasksRepository,
                questionnareRepository
            ) as T
            TasksDetailViewModel::class.java -> TasksDetailViewModel(
                    tasksRepository,
                    questionnareRepository
            ) as T
            PairingViewModel::class.java -> PairingViewModel(userRepository, tasksRepository) as T
            SplashViewModel::class.java -> SplashViewModel(userRepository, context) as T
            AppLifecycleViewModel::class.java -> AppLifecycleViewModel(
                AppLifecycleManager(
                    context,
                    context.getSharedPreferences("${BuildConfig.APPLICATION_ID}.config", 0),
                    AppUpdateManagerFactory.create(context)
                ), appConfigRepository
            ) as T
            OnboardingConsentViewModel::class.java -> OnboardingConsentViewModel(tasksRepository) as T
            SelfBcoCaseViewModel::class.java -> SelfBcoCaseViewModel(tasksRepository, appConfigRepository) as T
            ReversePairingViewModel::class.java -> ReversePairingViewModel(userRepository) as T
            else -> throw IllegalStateException("Unknown view model class $modelClass")
        }
    }
}
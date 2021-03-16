/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.Constants
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.tasks.ITaskRepository
import nl.rijksoverheid.dbco.user.IUserRepository

@ExperimentalSerializationApi
class OnboardingConsentViewModel(
    private val tasksRepository: ITaskRepository,
    userRepository: IUserRepository,
    context: Context
) : ViewModel() {

    val isPaired = userRepository.getToken() != null ||
            LocalStorageRepository.getInstance(context).getSharedPreferences().getBoolean(
                Constants.USER_COMPLETED_ONBOARDING, false
            )

    val termsAgreed = MutableLiveData(false)

    private val navigationChannel = Channel<Navigation>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    fun onNextClicked() {
        viewModelScope.launch {
            val case = tasksRepository.getCase()
            if (case.tasks.isNotEmpty() && isPaired) {
                navigationChannel.send(Navigation.MyTasks)
            } else if (case.tasks.isNotEmpty() && !isPaired && case.contagiousPeriodKnown) {
                navigationChannel.send(Navigation.AddContacts)
            } else if (case.tasks.isNotEmpty() && !isPaired && !case.contagiousPeriodKnown) {
                navigationChannel.send(Navigation.Symptoms)
            } else if (case.tasks.isEmpty() && isPaired && case.contagiousPeriodKnown) {
                navigationChannel.send(Navigation.AddContacts)
            } else {
                navigationChannel.send(Navigation.Symptoms)
            }
        }
    }

    sealed class Navigation {

        object MyTasks : Navigation()
        object Symptoms : Navigation()
        object AddContacts : Navigation()
    }
}
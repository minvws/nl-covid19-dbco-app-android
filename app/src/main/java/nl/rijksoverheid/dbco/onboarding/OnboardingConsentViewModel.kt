/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import nl.rijksoverheid.dbco.tasks.ITaskRepository

@ExperimentalSerializationApi
class OnboardingConsentViewModel(
    private val tasksRepository: ITaskRepository
) : ViewModel() {

    val termsAgreed = MutableLiveData(false)

    private val navigationChannel = Channel<Navigation>(Channel.BUFFERED)
    val navigationFlow = navigationChannel.receiveAsFlow()

    fun onNextClicked() {
        viewModelScope.launch {
            val contagiousPeriodKnown = true // TODO: will come from API
            val case = tasksRepository.getCase()
            if (case.tasks.isEmpty() && !contagiousPeriodKnown) {
                navigationChannel.send(Navigation.Symptoms)
            } else if (case.tasks.isEmpty() && contagiousPeriodKnown) {
                navigationChannel.send(Navigation.AddContacts)
            } else {
                navigationChannel.send(Navigation.MyTasks)
            }
        }
    }

    sealed class Navigation {

        object MyTasks : Navigation()
        object Symptoms : Navigation()
        object AddContacts : Navigation()
    }
}
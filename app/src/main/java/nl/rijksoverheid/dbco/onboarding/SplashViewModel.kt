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
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.dbco.storage.LocalStorageRepository
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.Constants.USER_COMPLETED_ONBOARDING
import nl.rijksoverheid.dbco.Constants.USER_GAVE_CONSENT
import nl.rijksoverheid.dbco.util.SingleLiveEvent
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.FlowSelection
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.MyTasks
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.Consent

class SplashViewModel(private val userRepository: IUserRepository, context: Context) : ViewModel() {

    private val storage: SharedPreferences by lazy {
        LocalStorageRepository.getInstance(context).getSharedPreferences()
    }

    private val _navigation = SingleLiveEvent<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    fun onConfigLoaded() {
        val skipOnboarding = storage.getBoolean(USER_COMPLETED_ONBOARDING, false)
        val gaveConsent = storage.getBoolean(USER_GAVE_CONSENT, false)
        val isPaired = userRepository.getToken() != null
        if (skipOnboarding) {
            _navigation.value = MyTasks
        } else if (isPaired && !gaveConsent) {
            _navigation.value = Consent
        } else if (isPaired && gaveConsent) {
            _navigation.value = MyTasks
        } else {
            _navigation.value = FlowSelection
        }
    }

    sealed class Navigation {

        object MyTasks : Navigation()
        object Consent : Navigation()
        object FlowSelection : Navigation()
    }
}
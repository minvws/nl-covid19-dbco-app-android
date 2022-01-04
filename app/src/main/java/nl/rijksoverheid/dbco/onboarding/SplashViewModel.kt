/*
 *  Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *   Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *   SPDX-License-Identifier: EUPL-1.2
 *
 */

package nl.rijksoverheid.dbco.onboarding

import android.annotation.SuppressLint
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import nl.rijksoverheid.dbco.user.IUserRepository
import nl.rijksoverheid.dbco.Constants.USER_COMPLETED_ONBOARDING
import nl.rijksoverheid.dbco.Constants.USER_GAVE_CONSENT
import nl.rijksoverheid.dbco.config.AppConfig
import nl.rijksoverheid.dbco.config.AppConfigRepository
import nl.rijksoverheid.dbco.util.SingleLiveEvent
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.Start
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.MyTasks
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.Consent
import nl.rijksoverheid.dbco.onboarding.SplashViewModel.Navigation.DataDeletion
import nl.rijksoverheid.dbco.bcocase.ICaseRepository
import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

/**
 * ViewModel which, when starting the app, determines where in the flow the app should start
 */
class SplashViewModel(
    private val storage: SharedPreferences,
    private val userRepository: IUserRepository,
    private val tasksRepository: ICaseRepository,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val _navigation = SingleLiveEvent<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    fun onConfigLoaded(config: AppConfig) {

        val lastEdited = tasksRepository.getLastEdited()
        val now = LocalDateTime.now(DateTimeZone.UTC)

        val skipOnboarding = storage.getBoolean(USER_COMPLETED_ONBOARDING, false)
        val gaveConsent = storage.getBoolean(USER_GAVE_CONSENT, false)
        val isPaired = userRepository.getToken() != null

        if (lastEdited.isBefore(now.minusDays(TWO_WEEKS))) {
            wipeStorage()
            appConfigRepository.storeConfig(config)
            _navigation.value = DataDeletion
        } else if (skipOnboarding) {
            _navigation.value = MyTasks
        } else if (isPaired && !gaveConsent) {
            _navigation.value = Consent
        } else if (isPaired && gaveConsent) {
            _navigation.value = MyTasks
        } else {
            _navigation.value = Start
        }
    }

    @SuppressLint("ApplySharedPref")
    private fun wipeStorage() = storage.edit().clear().commit()

    companion object {

        private const val TWO_WEEKS = 7 * 2
    }

    sealed class Navigation {

        object MyTasks : Navigation()
        object Consent : Navigation()
        object Start : Navigation()
        object DataDeletion : Navigation()
    }
}
/*
 * Copyright (c) 2020 De Staat der Nederlanden, Ministerie van Volksgezondheid, Welzijn en Sport.
 *  Licensed under the EUROPEAN UNION PUBLIC LICENCE v. 1.2
 *
 *  SPDX-License-Identifier: EUPL-1.2
 */

package nl.rijksoverheid.dbco

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.config.AppConfig
import nl.rijksoverheid.dbco.config.AppConfigRepository
import nl.rijksoverheid.dbco.config.FeatureFlags
import nl.rijksoverheid.dbco.config.AppUpdateManager.UpdateState.UpdateRequired
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.Update
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.UpToDate
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.ConfigError
import nl.rijksoverheid.dbco.config.AppUpdateManager
import nl.rijksoverheid.dbco.config.GuidelinesContainer
import nl.rijksoverheid.dbco.util.SingleLiveEvent
import timber.log.Timber

/**
 * ViewModel used in the app scope.
 * Used for update related logic, accessing feature flags and other configurations
 */
class AppViewModel(
    private val appUpdateManager: AppUpdateManager,
    private val appConfigRepository: AppConfigRepository,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    private val _updateEvent = SingleLiveEvent<AppLifecycleStatus>()
    val updateEvent: LiveData<AppLifecycleStatus> = _updateEvent

    private val _appConfig: MutableLiveData<AppConfig> = MutableLiveData()
    val appConfig: LiveData<AppConfig> = _appConfig

    fun fetchConfig() {
        viewModelScope.launch(dispatchers.main()) {
            try {
                val config = appConfigRepository.getAppConfig()
                val state = appUpdateManager.getUpdateState(config)
                if (state is UpdateRequired) {
                    _updateEvent.value = Update(state.installerPackageName)
                } else {
                    _appConfig.postValue(config)
                    _updateEvent.value = UpToDate
                }
            } catch (ex: Exception) {
                Timber.e(ex, "Exception during config/update state fetch!")
                _updateEvent.value = ConfigError
            }
        }
    }

    fun getUpdateMessage(): String = appConfigRepository.getUpdateMessage()

    fun getFeatureFlags(): FeatureFlags = appConfigRepository.getFeatureFlags()

    fun getGuidelines(): GuidelinesContainer = appConfigRepository.getGuidelines()

    sealed class AppLifecycleStatus {

        object UpToDate : AppLifecycleStatus()

        data class Update(val installerPackageName: String?) : AppLifecycleStatus()

        object ConfigError : AppLifecycleStatus()
    }
}

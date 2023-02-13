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
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nl.rijksoverheid.dbco.config.AppConfig
import nl.rijksoverheid.dbco.config.AppConfigRepository
import nl.rijksoverheid.dbco.config.FeatureFlags
import nl.rijksoverheid.dbco.config.AppUpdateManager
import nl.rijksoverheid.dbco.config.GuidelinesContainer
import nl.rijksoverheid.dbco.util.SingleLiveEvent
import timber.log.Timber
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.UpToDate
import nl.rijksoverheid.dbco.config.AppUpdateManager.AppLifecycleState.ConfigError

/**
 * ViewModel used in the app scope.
 * Used for update related logic, accessing feature flags and other configurations
 */
class AppViewModel(
    private val appUpdateManager: AppUpdateManager,
    private val appConfigRepository: AppConfigRepository,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _appLifecycleState = SingleLiveEvent<AppLifecycleState>()
    val appLifecycleState: LiveData<AppLifecycleState> = _appLifecycleState

    private val _appConfig: MutableLiveData<AppConfig> = MutableLiveData()
    val appConfig: LiveData<AppConfig> = _appConfig

    fun fetchConfig() {
        viewModelScope.launch(coroutineDispatcher) {
            try {
                val config = appConfigRepository.getAppConfig()
                _appLifecycleState.value =
                    appUpdateManager.getAppLifecycleState(config).also { state ->
                        if (state is UpToDate) _appConfig.postValue(config)
                    }
            } catch (ex: Exception) {
                Timber.e(ex, "Exception during config/update state fetch!")
                _appLifecycleState.value = ConfigError
            }
        }
    }

    fun getFeatureFlags(): FeatureFlags = appConfigRepository.getFeatureFlags()

    fun getGuidelines(): GuidelinesContainer = appConfigRepository.getGuidelines()
}

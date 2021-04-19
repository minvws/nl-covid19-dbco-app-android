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
import nl.rijksoverheid.dbco.config.AppUpdateManager.UpdateState.InAppUpdate
import nl.rijksoverheid.dbco.config.AppUpdateManager.UpdateState
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.Update
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.UpToDate
import nl.rijksoverheid.dbco.AppViewModel.AppLifecycleStatus.ConfigError
import nl.rijksoverheid.dbco.config.AppUpdateManager
import nl.rijksoverheid.dbco.util.SingleLiveEvent

class AppViewModel(
    private val appUpdateManager: AppUpdateManager,
    private val appConfigRepository: AppConfigRepository
) : ViewModel() {

    private val _updateEvent = SingleLiveEvent<AppLifecycleStatus>()
    val updateEvent: LiveData<AppLifecycleStatus> = _updateEvent

    private val _appConfig: MutableLiveData<AppConfig> = MutableLiveData()
    val appConfig: LiveData<AppConfig> = _appConfig

    fun fetchConfig() {
        viewModelScope.launch {
            try {
                val config = appConfigRepository.getAppConfig()
                val state = appUpdateManager.getUpdateState(config)
                if (state is UpdateRequired || state is InAppUpdate) {
                    _updateEvent.value = Update(state)
                } else {
                    _appConfig.postValue(config)
                    _updateEvent.value = UpToDate
                }
            } catch (ex: Exception) {
                _updateEvent.value = ConfigError
            }
        }
    }

    fun getUpdateMessage(): String = appConfigRepository.getUpdateMessage()

    fun getFeatureFlags(): FeatureFlags = appConfigRepository.getFeatureFlags()

    sealed class AppLifecycleStatus {

        object UpToDate : AppLifecycleStatus()

        data class Update(val state: UpdateState) : AppLifecycleStatus()

        object ConfigError : AppLifecycleStatus()
    }
}
